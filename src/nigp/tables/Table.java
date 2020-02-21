package nigp.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;

import nigp.excel.SheetsOfExcelFile;

/**
 * Наследники этого абстрактного класса формируют таблицу
 * на основе excel-листа с геолого-геофизической документацией
 * ИСИХОГИ. При этом некоторые поля, представленные кодами,
 * расшифровываются.
 * @author NovopashinAV
 *
 */
public abstract class Table {

    protected Map<String, Sheet> sheets = new HashMap<>(); //листы excel-файла

    /* формируемая таблица excel-листа*/
    protected List<Map<String, String>> generatedTable = new ArrayList<>();

    protected static Logger logger = Logger.getLogger(Table.class.getName());

    public Table(SheetsOfExcelFile sheetsOfExcelFile) {
        sheets = sheetsOfExcelFile.getSheets();
    }

    public List<Map<String, String>> getTableDefaultFormat() {
        return generatedTable;
    }

    /**
     * Проверяет заполнена ли таблица, считанная с excel-листа
     * @return <code>true</code> если таблица, считанная из excel-листа
     * не пуста
     */
    public boolean isTableDefaultFormatComplete() {
        return generatedTable.size() > 0;
    }

    /**
     * Удаляет из считанной таблицы пустые строки (у которых поле
     * "ID ТН" содержит значение "Нет данных")
     */
    public void checkedForFoundData() {
        generatedTable = generatedTable.stream()
                .filter(e -> isFoundData(e))
                .collect(Collectors.toList());
    }

    /*
     * Проверяет строку таблицы, считанной из Excel-файла, на наличие
     * атрибута "ID ТН" со значением "Нет данных"
     */
    private boolean isFoundData(Map<String, String> line) {
        for (Map.Entry<String, String> m: line.entrySet()) {
            if (m.getKey().equals("ID ТН") && m.getValue().equals("Нет данных")) {
                return false;
            }
        }
        return true;
    }

}
