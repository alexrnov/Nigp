package nigp.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Считывает данные excel-листа с кодами. Такой лист содержит всего
 * два столбца, поэтому его можно представить ввиде простого
 * отображения ключ-значение.
 * @author NovopashinAV
 *
 */
public class SheetWithCodes {
    private Map<String, String> linesOfSheet = new HashMap<>();

    /**
     * Инициализирует новый объект, который содержит отображение,
     * где ключ - это код, а значение - расшифровка кода
     * из справочного excel-листа к документации ИСИХОГИ.
     * @param sheet Excel-лист, из которого необходимо прочитать данные
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public SheetWithCodes(Sheet sheet) throws ExcelException {

        if (sheet.getLastRowNum() == 0) {
            throw new ExcelException("Empty sheet of the excel file, sheet: " +
                    sheet.getSheetName());
        }

        sheet.forEach(row -> {
            if (row.getRowNum() != 0 && row.getLastCellNum() > 1
                    && row.getCell(0) != null && row.getCell(1) != null) {
                String code = row.getCell(0).toString();
                String trancript = row.getCell(1).toString();
                linesOfSheet.put(code, trancript);
            }
        });

        if (linesOfSheet.size() == 0) {
            throw new ExcelException("Empty sheet with codes, sheet: " +
                    sheet.getSheetName());
        }
    }

    /**
     * @return отображение, где ключ - это код,
     * а значение - расшифровка кода из справочного excel-листа
     * к документации ИСИХОГИ.
     */
    public Map<String, String> getLinesOfSheet() {
        return linesOfSheet;
    }
}
