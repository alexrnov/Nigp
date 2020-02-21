package nigp.file;

import static java.io.File.separator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nigp.tasks.TaskException;

/**
 * Абстрактный класс определяет некоторые базовые принципы для записи в файл
 * @author NovopashinAV
 */
public abstract class OutputFile {

    /* рабочий каталог, куда сохраняется файл */
    public String workingCatalog;

    /* имя файла с расширением */
    protected String nameOfFile;

    /* файл для записи данных */
    protected Path outputFile;

    /**
     * @param workingCatalog рабочий каталог, куда сохраняется файл
     */
    public OutputFile(String workingCatalog) {
        this.workingCatalog = workingCatalog;
    }

    /**
     * Создает текстовый файл nameOfFile
     * в каталоге {@code workingCatalog}.
     * Если файл с таким именем уже существует, он удаляется.
     * @throws TaskException
     */
    public void create() throws TaskException {
        try {
            outputFile = Paths.get(workingCatalog + separator + nameOfFile);
            Files.deleteIfExists(outputFile);
            Files.createFile(outputFile);
        } catch(IOException e) {
            throw new TaskException("Error of delete or create output file");
        }
    }

    /**
     * Записывает данные в файл.
     * @param content данные для записи
     * @throws TaskException ошибка вычислений, генерируется при ошибках,
     * связанных с записью файла
     */
    public abstract <T> void write(T content) throws TaskException;

}
