package nigp.tables;

import nigp.excel.ExcelException;
import nigp.excel.SheetOfExcelFileMineralWeb;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nigp.tables.MineralogyAuxiliary.*;
import static nigp.tables.Table.logger;

/**
 * Считывает данные excel файла по минералогии. При этом используется
 * файл, который загружается с онлайн-ресурса "МСА по всем объектам"
 * @author NovopashinAV
 */
public class MineralogyOfWebTable {

    private Sheet sheet; //Excel-лист с данными минералогии

    /* коллекция для считываемых данных минералогии*/
    private List<Map<String, String>> linesOfSheet = new ArrayList<>();

    /**
     * Инициализирует новый объект, который содержит список со строками
     * excel-листа. Каждая строка листа храниться в отдельном отображении,
     * где ключ - название столбца, значение - значение ячейки.
     *
     * @param mineralogySheet Excel-лист, из которого необходимо
     * прочитать данные минералогии
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public MineralogyOfWebTable(SheetOfExcelFileMineralWeb mineralogySheet) {
        sheet = mineralogySheet.getSheet();

        try {
            checkTable();
            readTable();
        } catch (ExcelException e) {
            logger.fine("the sheet of the Excel file aren't found");
            linesOfSheet.clear();
        }
    }

    private void checkTable() throws ExcelException {

        if (sheet.getLastRowNum() < requiredMinimumNumOfRows) {
            throw new ExcelException("An insufficient amount rows of table: "
                    + sheet.getSheetName());
        }

        if (sheet.getRow(firstStringWithData).getLastCellNum() != requiredNumOfColumn) {
            throw new ExcelException("Amount of cell not equals required amount of cell: "
                    + sheet.getSheetName());
        }
    }

    private void readTable() {
        for (int i = firstStringWithData; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, String> currentLineOfSheet = getCurrentLineOfSheet(row);
            linesOfSheet.add(currentLineOfSheet);
        }
    }

    private Map<String, String> getCurrentLineOfSheet(Row row) {
        Map<String, String> currentLineOfSheet = new HashMap<>();
        indexAndNameOfColumns.forEach((indexColumn, nameColumn) -> {
            Cell cell = row.getCell(indexColumn);
            String valueCell = "Нет данных";
            if (cell != null && cell.toString().length() > 0
                    && ! cell.toString().equals(" ")) {
                valueCell = cell.toString();
            }
            currentLineOfSheet.put(nameColumn, valueCell);
        });
        return currentLineOfSheet;
    }

    public boolean isEmpty() {
        return (linesOfSheet.size() > 0) ? false: true;
    }
    /**
     * @return список со строками
     * excel-листа. Каждая строка листа храниться в отдельном отображении,
     * где ключ - название столбца, значение - значение ячейки
     */
    public List<Map<String, String>> getLinesOfSheet() {
        return linesOfSheet;
    }
}
