package nigp.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nigp.tasks.TaskException;

/**
 * Класс реализует запись итоговых данных по точкам наблюдений
 * в текстовый файл. Точки наблюдений разделяются друг от друга
 * разделительным словом "map_point".  Атрибуты разделяются друг от друга
 * ключевым словом ";;". Название атрибута отделяется от его значения
 * с помощью разделительного слова "::"
 * @author NovopashinAV
 */
public class TextFileForMapLayer extends OutputFile {

    /**
     * Инициализация класса для записи итоговых данных
     * в текстовый файл.
     * @param workingCatalog рабочий каталог, куда сохраняется
     * текстовый файл с результатами вычислений.
     */
    public TextFileForMapLayer(String workingCatalog) {
        super(workingCatalog);
        super.nameOfFile = "OutputFileForMapLayer.txt";
    }

    /**
     * Записывает данные по текущему набору точек наблюдений в файл.
     * Данные добавляются в конец файла
     */
    @Override
    public <T> void write(T jointTable) throws TaskException {
        try {
            BufferedWriter bw = Files.newBufferedWriter(
                    outputFile,
                    StandardCharsets.UTF_8,//или Charset.forName("UTF-8")
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
            try (PrintWriter out = new PrintWriter(bw)) {
                writeCurrentContent(out, jointTable);
            }
            bw.close();
        } catch(IOException e) {
            throw new TaskException("Write file error");
        }
    }

    @SuppressWarnings("unchecked")
    protected void writeCurrentContent(PrintWriter out, Object jointTable) {
        List<Map<String, String>> list = (List<Map<String, String>>) jointTable;
        for (Map<String, String> point: list) {
            out.println("map_point");

            Iterator<Entry<String, String>> iterator = point.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> attr = iterator.next();
                out.print(attr.getKey() + "::" + attr.getValue());
				/*
				 * для всех атрибутов, кроме последнего, добавлять в
				 * конец разделитель атрибутов ";;"
				 */
                if (iterator.hasNext()) {
                    out.print(";;");
                }
            }
            out.println();
        }
    }

}
