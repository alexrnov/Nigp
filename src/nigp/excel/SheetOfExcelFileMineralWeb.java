package nigp.excel;

import nigp.excel.SheetsOfExcelFile;
import nigp.tasks.TaskException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Осуществляет поиск листа "Page 1" в Excel-файле с минералогией
 * @author NovopashinAV
 */
public class SheetOfExcelFileMineralWeb {
    private static Logger logger = Logger.getLogger(SheetsOfExcelFile.class.getName());

    private Map<String, Sheet> sheets = new HashMap<>();

    private String nameOfExcelFile;

    private Sheet sheet;
    /**
     * Инициализирует новый объект, создающий Excel-лист для
     * excel-файла <b>excelFile</b>.
     * @param excelFile файл с минералогией
     */
    public SheetOfExcelFileMineralWeb(File excelFile) throws TaskException {
        try {
            nameOfExcelFile = excelFile.getName();
            Workbook workbook = getWorkBook(excelFile);

            if (workbook.getNumberOfNames() == 1
                    && workbook.getSheetName(0).equals("Page 1")) {
                sheet = workbook.getSheetAt(0);
            } else {
                throw new TaskException("Excel sheet 'Page1' not found");
            }
        } catch(IOException e) {
            logger.fine("it was not succeeded to create object sheets "
                    + "of the excel-file. File is:" + nameOfExcelFile);
        } catch(OLE2NotOfficeXmlFileException e) {
            logger.fine("Extension of the file doesn't correspond "
                    + "to a file format. File is:" + nameOfExcelFile);
        }
    }

    private Workbook getWorkBook(File excelFile) throws IOException {
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

    public Sheet getSheet() {
        return sheet;
    }
}
