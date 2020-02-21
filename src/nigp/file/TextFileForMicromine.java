package nigp.file;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nigp.tasks.TaskException;

/**
 * Класс реализует запись итоговых данных в текстовый файл. Первой строкой
 * записываются названия атрибутов, а ниже идут значения. Столбцы
 * разделяются друг от друга разделителем separator. Такой формат может
 * быть легко импортирован в micromine, в файл DAT.
 * @author NovopashinAV
 */
public class TextFileForMicromine extends OutputFile {

    private final String separator = ";";//разделитель столбцов в файле
    private BufferedWriter writer;
    private List<String> title; //список с названиями атрибутов

    /**
     * Инициализация класса для записи итоговых данных
     * в текстовый файл.
     * @param workingCatalog рабочий каталог, куда сохраняется
     * текстовый файл.
     */
    public TextFileForMicromine(String workingCatalog, String fileName) {
        super(workingCatalog);
        super.nameOfFile = fileName + ".txt";
    }


     /** Записывает названия атрибутов */
    public void writeTitle(List<String> title) throws TaskException {
        this.title = title;
        try {

            writer = Files.newBufferedWriter(
                    outputFile,
                    Charset.forName("Windows-1251"),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            try (PrintWriter out = new PrintWriter(writer)) {
                Iterator<String> iterator = title.iterator();
                while(iterator.hasNext()) {
                    String s = iterator.next();
                    //поменять местами координаты(в ИСИХОГИ они переставлены местами)
                    if (s.equals("X факт.") || s.equals("X")) {
                        s = "north";
                    } else if (s.equals("Y факт.") || s.equals("Y")) {
                        s = "east";
                    } else if (s.equals("Глубина ТН")) {
                        s = "depth";
                    }
                    s = s.replace(" ", "_");
                    s = s.replace(separator, "_");
                    out.print(s);

                    /*
				     * для всех названий атрибутов, кроме последнего, добавлять в
				     * конец разделитель ";"
				     */
                    if (iterator.hasNext()) {
                        out.print(separator);
                    }
                }
                out.println();
            }
            writer.close();
        } catch (IOException e) {
            throw new TaskException("Write file error");
        }

    }

    /**
     * Записывает данные по текущему набору точек наблюдений в файл.
     * Данные добавляются в конец файла
     */
    @Override
    public <T> void write(T jointTable) throws TaskException {
        try {

            writer = Files.newBufferedWriter(
                    outputFile,
                    Charset.forName("Windows-1251"),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            try (PrintWriter out = new PrintWriter(writer)) {
                writeCurrentContent(out, jointTable);
            }
            writer.close();
        } catch(IOException e) {
            throw new TaskException("Write file error");
        }
    }

    @SuppressWarnings("unchecked")
    protected void writeCurrentContent(PrintWriter out, Object jointTable) {
        List<Map<String, String>> list = (List<Map<String, String>>) jointTable;

        for (Map<String, String> point: list) {
            Iterator<String> iterator = title.iterator();
            while(iterator.hasNext()) {
                String s = iterator.next();
                String value = point.get(s);
                value = value.replace(separator,", ");
                value = value.replace("\n","_");
                out.print(value);
                /*
				 * для всех названий атрибутов, кроме последнего, добавлять в
				 * конец разделитель ";"
				 */
                if (iterator.hasNext()) {
                    out.print(separator);
                }
            }
            out.println();
        }
    }

}
