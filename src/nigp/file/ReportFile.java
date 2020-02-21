package nigp.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import nigp.tasks.TaskException;

/**
 * Класс реализует запись отчетных данных по точкам наблюдений
 * в текстовый файл.
 * @author NovopashinAV
 */
public class ReportFile extends OutputFile {

    /**
     * Инициализация класса для записи отчетных данных
     * в текстовый файл.
     * @param workingCatalog рабочий каталог, куда сохраняется
     * текстовый файл с отчетными данными.
     */
    public ReportFile(String workingCatalog) {
        super(workingCatalog);
        super.nameOfFile = "ReportFile.txt";
    }

    /**
     * Записывает отчетные данные по текущему набору точек
     * наблюдений в файл. Данные добавляются в конец файла
     */
    @Override
    public <T> void write(T currentReport) throws TaskException {
        try {
            BufferedWriter bw = Files.newBufferedWriter(
                    outputFile,
                    StandardCharsets.UTF_8,//или Charset.forName("UTF-8")
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
            try (PrintWriter out = new PrintWriter(bw)) {
                String s = (String) currentReport;
                out.println(s);
                out.println();
            }
        } catch(IOException e) {
            throw new TaskException("Write file error");
        }
    }
}
