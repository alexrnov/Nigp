package nigp.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nigp.Constants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Осуществляет поиск листов в Excel-файле с документацией точек
 * наблюдений ИСИХОГИ
 * @author NovopashinAV
 *
 */
public class SheetsOfExcelFile {

    private static Logger logger = Logger.getLogger(SheetsOfExcelFile.class.getName());

    private Map<String, Sheet> sheets = new HashMap<>();

    private String nameOfExcelFile;
    /**
     * Инициализирует новый объект, создающий отображение, которое
     * содержит пары: (название листа - объект листа) для
     * excel-файла <b>excelFile</b>. Структура листов Excel-файла
     * должна соответствовать формату листов в файле документации
     * ИСИХОГИ по точкам наблюдений.
     * @param excelFile файл с документацией точек наблюдений ИСИХОГИ
     */
    public SheetsOfExcelFile(File excelFile) {
        try {
            nameOfExcelFile = excelFile.getName();
            Workbook workbook = getWorkBook(excelFile);
            if (isFitToFormatOfSheets(workbook)) {
                workbook.forEach(e -> sheets.put(e.getSheetName(), e));
            } else {
                logger.fine("The file doesn't correspond to a format of "
                        + "sheets ISIHOGI. File is:" + nameOfExcelFile);
            }
        } catch(IOException e) {
            logger.fine("it was not succeeded to create object sheets "
                    + "of the excel-file. File is:" + nameOfExcelFile);
        } catch(OLE2NotOfficeXmlFileException e) {
            logger.fine("Extension of the file doesn't correspond "
                    + "to a file format. File is:" + nameOfExcelFile);
        }
    }

    private Workbook getWorkBook(File excelFile) throws IOException{
        try (FileInputStream input = new FileInputStream(excelFile)) {
            String extension = getExtensionOfFile(excelFile);
            Workbook workbook = (extension.equals("xls"))
                    ? new HSSFWorkbook(input)
                    : new XSSFWorkbook(input);
            return workbook;
        }
    }

    private String getExtensionOfFile(File file) {
        String fileName = file.getName();
        String extensionOfFile = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extensionOfFile.toLowerCase();
    }

    private boolean isFitToFormatOfSheets(Workbook workbook) {
        List<String> NamesOfSheetsForCurrentExcelFile = new ArrayList<>();
        workbook.forEach(sheet -> {
            NamesOfSheetsForCurrentExcelFile.add(sheet.getSheetName());
        });
        if (NamesOfSheetsForCurrentExcelFile.containsAll(Constants.allNamesOfSheets)) {
            return true;
        }
        return false;
    }

    /**
     * @return {@code true} если excel-листы были найдены
     * {@code false} в противном случае
     */
    public boolean isValidSheetsFound() {
        return sheets.size() > 0;
    }

    /**
     * Возрващает отображение, которое содержит пары:
     * (название листа - объект листа), для excel-файла ИСИХОГИ
     * @return список лисов exel-файла
     */
    public Map<String, Sheet> getSheets() {
        return sheets;
    }

    /**
     * @return название Excel-файла
     */
    public String getNameOfFile() {
        return nameOfExcelFile;
    }
}
