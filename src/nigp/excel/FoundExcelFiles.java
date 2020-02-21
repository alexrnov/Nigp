package nigp.excel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Осуществляет поиск Excel-файлов (*.xls, *.xlsx) в
 * указанном каталоге (подкаталогах)
 * @author NovopashinAV
 *
 */
public class FoundExcelFiles {

    private List<File> foundExcelFiles = new ArrayList<>();

    /**
     * Инициализирует новый объект, который содержит список
     * excel-файлов для каталога <b>directoryForSearchExcel</b>,
     * и его подкаталогов, если таковые имеются.
     * @param directoryForSearchExcel Дириктория каталога, в которой
     * осуществляется поиск Excel-файлов; если каталог содержит
     * подкаталоги, то поиск производится и в них.
     *
     */
    public FoundExcelFiles(String directoryForSearchExcel) {
        Path pathForSearchExcel = Paths.get(directoryForSearchExcel);
        try {
            foundExcelFiles = Files.walk(pathForSearchExcel)
                    .filter(this::isFitFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Logger logger = Logger.getLogger(FoundExcelFiles.class.getName());
            logger.log(Level.SEVERE, "it was not succeeded to receive excel-files", e);
        }
    }

    private boolean isFitFile(Path file) {
        if (Files.isRegularFile(file) && isExcelFile(file) && Files.isReadable(file)) {
            return true;
        }
        return false;
    }

    private boolean isExcelFile(Path file) {
        String fileName = file.getFileName().toString();
        if (fileName.endsWith(".xls") || fileName.endsWith(".XLS")
                || fileName.endsWith(".xlsx") || fileName.endsWith(".XLSX")) {
            return true;
        }
        return false;
    }

    /**
     * @return {@code true} если excel-файлы не были найдены;
     * {@code false} в противном случае.
     */
    public boolean isNotFoundExcelFiles() {
        return foundExcelFiles.size() == 0;
    }

    /**
     * Возвращает список, содержащий объекты excel-файлов, которые
     * были найдены в каталоге/подкаталогах <b>directoryForSearchExcel</b>
     * @return Список, содержащий excel-файлы
     */
    public List<File> getFoundExcelFiles() {
        return foundExcelFiles;
    }
}
