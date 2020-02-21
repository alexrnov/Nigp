package nigp.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Считывает данные excel-листа
 * @author NovopashinAV
 */
public class SheetWithContents {

    protected List<Map<String, String>> linesOfSheet = new ArrayList<>();

    /**
     * Инициализирует новый объект, который содержит список со строками
     * excel-листа. Каждая строка листа храниться в отдельном отображении,
     * где ключ - название столбца, значение - значение ячейки.
     *
     * @param sheet Excel-лист, из которого необходимо прочитать данные
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public SheetWithContents(Sheet sheet) throws ExcelException{

        if (sheet.getLastRowNum() == 0) {
            throw new ExcelException("Empty sheet of the excel file: " +
                    sheet.getSheetName());
        }

        Map<Integer, String> indexAndNameOfColumns =
                getIndexAndNameForColumns(sheet);

        sheet.forEach(row -> {
            if (row.getRowNum() != 0) { //пропустить заголовок
                Map<String, String> currentLineOfSheet =
                        getCurrentLineOfSheet(row, indexAndNameOfColumns);
                linesOfSheet.add(currentLineOfSheet);
            }
        });

        if (linesOfSheet.size() == 0) {
            throw new ExcelException("The sheet excel doesn't contain values, sheet: " +
                    sheet.getSheetName());
        }
    }

    private Map<Integer, String> getIndexAndNameForColumns(Sheet sheet) {
        Row title = sheet.getRow(0);
        Map<Integer, String> namesColumns = new HashMap<>();
        title.forEach(cell -> {
            namesColumns.put(cell.getColumnIndex(), cell.toString());
        });
        return namesColumns;
    }

    private Map<String, String> getCurrentLineOfSheet(Row row,
                                                      Map<Integer, String> indexAndNameOfColumns) {
        Map<String, String> currentLineOfSheet = new HashMap<>();
        indexAndNameOfColumns.forEach((indexColumn, nameColumn) -> {
            Cell cell = row.getCell(indexColumn);
            String valueCell = "Нет данных";
            if (row.getCell(indexColumn) != null
                    && cell.toString().length() > 0
                    && ! cell.toString().equals(" ")) {
                valueCell = cell.toString();
            }
            if (nameColumn.equals("ID ТН") || nameColumn.equals("№п/п")) {
                valueCell = valueCell.split("\\.")[0];
            }
            currentLineOfSheet.put(nameColumn, valueCell);
        });
        return currentLineOfSheet;
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





