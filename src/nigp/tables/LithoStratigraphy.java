package nigp.tables;

import static nigp.tables.TablesOfSheets.getCodeLithology;
import static nigp.tables.TablesOfSheets.getCodeStratigraphy;
import static nigp.tables.TablesOfSheets.getLithoStratigraphy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import nigp.excel.ExcelException;
import nigp.excel.SheetsOfExcelFile;

/**
 * Класс формирует таблицу excel-листа "Стратиграфия Литология", при этом коды
 * некоторых полей заменяются на расшифровки, взятые из кодовых excel-листов.
 * @author NovopashinAV
 */
public class LithoStratigraphy extends Table
        implements ReplacementCodes, PointInfoToSingleLine {

    /* таблица с расшифровкой кодов стратиграфии */
    private Map<String, String> codeStratigraphy;

    /* таблица с расшифровкой кодов литологии */
    private Map<String, String> codeLithology;

    /* таблица с геолого-геофизическими данными в однострочном формате */
    private List<Map<String, String>> tableWithSingleLines = new ArrayList<>();

    private String idPoint; //идентификатор скважины

    /* В таблице одна скважина представлена, как правило,
     * несколькими строками. Поэтому для того, чтобы хранить
     * данные по скважине в одной строке, необходимо
     * накопить данные нескольких строк в переменной типа StringBuilder.
     * Такие переменные используются для накопления данных по абсолютным
     * отметкам кровли и подошвы пластов, типа стратиграфии и литологии пород,
     * а также расширенного описания литологии пород
     */
    private StringBuilder upperContact = new StringBuilder(); //кровля пласта
    private StringBuilder lowerContact = new StringBuilder(); //подошва пласта
    private StringBuilder stratigraphy = new StringBuilder(); //возраст пород
    private StringBuilder lithology = new StringBuilder(); //литология
    private StringBuilder descriptLithology = new StringBuilder(); //описание пород

    /* Коллекция для хранения 'однострочных' данных по текущей скважине*/
    private Map<String, String> lineOfBorehole = new HashMap<>();

    /**
     * Инициализирует новый объект, который содержит список
     * со строками excel-листа <b>"Стратиграфия Литология"</b>,
     * @param sheetsOfExcelFile объект, хранящий список листов
     * excel-файла
     */
    public LithoStratigraphy(SheetsOfExcelFile sheetsOfExcelFile) {
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
        generatedTable = getLithoStratigraphy(sheets);
        codeStratigraphy = getCodeStratigraphy(sheets);
        codeLithology = getCodeLithology(sheets);
    }

    /* заменить коды текущей строки на расшифровку из excel-листов с кодами */
    @Override
    public void decode() {
        generatedTable.forEach(row -> {
            replacement(row, "L_Code возраста", codeStratigraphy);
            replacement(row, "L_Code породы", codeLithology);
        });
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
        upperContact.append(line.get("От") + "/");
        lowerContact.append(line.get("До") + "/");
        stratigraphy.append(line.get("L_Code возраста") + "/");
        lithology.append(line.get("L_Code породы") + "/");
        String currentDescription = line.get("Описание");
        descriptLithology.append(currentDescription.replace("/",".") + "/");
    }

    private void clearStringBuilder() {
        upperContact.delete(0, upperContact.length());
        lowerContact.delete(0, lowerContact.length());
        stratigraphy.delete(0, stratigraphy.length());
        lithology.delete(0, lithology.length());
        descriptLithology.delete(0, descriptLithology.length());
    }

    private void addLineOfBoreholeToTable() {
        lineOfBorehole.put("ID ТН", idPoint);
        lineOfBorehole.put("Кровля стратопласта", upperContact.toString());
        lineOfBorehole.put("Подошва стратопласта", lowerContact.toString());
        lineOfBorehole.put("Стратиграфия", stratigraphy.toString());
        lineOfBorehole.put("Литология", lithology.toString());
        lineOfBorehole.put("Описание породы", descriptLithology.toString());
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
