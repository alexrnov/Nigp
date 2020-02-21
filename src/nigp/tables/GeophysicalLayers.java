package nigp.tables;

import static nigp.tables.TablesOfSheets.getCodeGeophysicalLayers;
import static nigp.tables.TablesOfSheets.getCodeStratigraphy;
import static nigp.tables.TablesOfSheets.getGeophysicalLayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import nigp.excel.ExcelException;
import nigp.excel.SheetsOfExcelFile;

/**
 * Класс формирует таблицу excel-листа "Геофизический пласт", при этом коды
 * некоторых полей заменяются на расшифровки, взятые из кодовых excel-листов.
 * @author NovopashinAV
 */
public class GeophysicalLayers extends Table
        implements ReplacementCodes, PointInfoToSingleLine {

    /* таблица с расшифровкой кодов геофизических пластов */
    private Map<String, String> codeGeophysicalLayers;

    /* таблица с расшифровкой кодов стратиграфии */
    private Map<String, String> codeStratigraphy;

    private String idPoint; //идентификатор скважины

    /* В таблице одна скважина представлена, как правило,
     * несколькими строками. Поэтому для того, чтобы хранить
     * данные по скважине в одной строке, необходимо
     * накопить данные нескольких строк в переменной типа StringBuilder.
     */
	/* кровля геофизического пласта */
    private StringBuilder upperContact = new StringBuilder();
    /* подошва геофизического пласта */
    private StringBuilder lowerContact = new StringBuilder();
    /* геофизический пласт */
    private StringBuilder geophysicalLayer = new StringBuilder();
    /* атрибут 'описание' */
    private StringBuilder description = new StringBuilder();
    /* возраст геофизического пласта */
    private StringBuilder stratigraphy = new StringBuilder();
    /* таблица с геолого-геофизическими данными в однострочном формате */
    private List<Map<String, String>> tableWithSingleLines = new ArrayList<>();

    /* Коллекция для хранения 'однострочных' данных по текущей скважине*/
    private Map<String, String> lineOfBorehole = new HashMap<>();

    /**
     * Инициализирует новый объект, который содержит список
     * со строками excel-листа <b>"Геофизический пласт"</b>,
     * @param sheetsOfExcelFile объект, хранящий список листов
     * excel-файла
     */
    public GeophysicalLayers(SheetsOfExcelFile sheetsOfExcelFile) {
        super(sheetsOfExcelFile);
        try {
            readTables();
        } catch(ExcelException e) {
            generatedTable.clear();
            logger.log(Level.WARNING, "Error when reading the excel-file", e);
        }
    }

    /* сформировать таблицы на основе excel-листов*/
    private void readTables() throws ExcelException {
        generatedTable = getGeophysicalLayer(sheets);
        codeGeophysicalLayers = getCodeGeophysicalLayers(sheets);
        codeStratigraphy = getCodeStratigraphy(sheets);
    }

    @Override
    public void decode() {
        generatedTable.forEach(row -> {
            replacement(row, "Код геопласта", codeGeophysicalLayers);
            replacement(row, "L_Code возраста", codeStratigraphy);
        });
    }

    /**
     * Сортирует таблицу с гофизическими пластами сначала по id ТН,
     * затем по глубинам кровли. Это необходимо, поскольку данные
     * по геофизическим пластам перемешаны.
     */
    public void sortByIdAndUpperContact() {
        generatedTable.sort(Comparator
                .comparing(this::sortById)
                .thenComparing(this::sortByUpperContact));
    }

    private String sortById(Object e) {
        Map<?, ?> line = (Map<?, ?>) e;
        return (String) line.get("ID ТН");
    }

    private Double sortByUpperContact(Object e) {
        Map<?, ?> line = (Map<?, ?>) e;
        String s = (String) line.get("Кровля");
        return Double.valueOf(s);
    }

    @Override
    public void pointInfoToSingleLine() {
        if (generatedTable.size() != 0) {
            idPoint = generatedTable.get(0).get("ID ТН");
        }
        generatedTable.forEach(record -> {
			/*если строка таблицы содержит данные по текущей скважине*/
            if (record.get("ID ТН").equals(idPoint)) {
                addDataToStringBuilder(record);
            } else { //если начались данные по новой скважине
                addLineOfBoreholeToTable();
                clearStringBuilder();
                lineOfBorehole = new HashMap<>();
                addDataToStringBuilder(record);
                idPoint = record.get("ID ТН");
            }
        });
        //добавить строку с данными по последней скважине
        addLineOfBoreholeToTable();
    }

    private void addDataToStringBuilder(Map<String, String> line) {
        upperContact.append(line.get("Кровля") + "/");
        lowerContact.append(line.get("Подошва") + "/");
        stratigraphy.append(line.get("L_Code возраста") + "/");
        geophysicalLayer.append(line.get("Код геопласта") + "/");
        description.append(line.get("Описание") + "/");
    }

    private void clearStringBuilder() {
        upperContact.delete(0, upperContact.length());
        lowerContact.delete(0, lowerContact.length());
        stratigraphy.delete(0, stratigraphy.length());
        geophysicalLayer.delete(0, geophysicalLayer.length());
        description.delete(0, description.length());
    }

    private void addLineOfBoreholeToTable() {
        lineOfBorehole.put("ID ТН", idPoint);
        lineOfBorehole.put("Кровля геопласта", upperContact.toString());
        lineOfBorehole.put("Подошва геопласта", lowerContact.toString());
        lineOfBorehole.put("Стратиграфия геопласта", stratigraphy.toString());
        lineOfBorehole.put("Геопласт", geophysicalLayer.toString());
        lineOfBorehole.put("Описание геопласта", description.toString());
        tableWithSingleLines.add(lineOfBorehole);
    }

    @Override
    public List<Map<String, String>> getTableSingleFormat() {
        return tableWithSingleLines;
    }

    @Override
    public boolean isTableSingleFormatComplete() {
        return tableWithSingleLines.size() > 0;
    }
}
