package nigp.tables;

import static nigp.tables.TablesOfSheets.*;

import java.util.Map;

import nigp.excel.ExcelException;
import nigp.excel.SheetsOfExcelFile;

/**
 * Класс формирует таблицу excel-листа "Точки наблюдений", при этом коды
 * некоторых полей заменяются на расшифровки, взятые из кодовых excel-листов.
 * @author NovopashinAV
 */
public class PointsObservations extends Table implements ReplacementCodes {

    /* таблица с расшифровкой кодов тип точек наблюдения */
    private Map<String, String> codeTypePointsObservations;

    /* таблица с расшифровкой кодов системы координат */
    private Map<String, String> codeCoordinateSystem;

    /* таблица с расшифровкой кодов типа систем координат */
    private Map<String, String> codeTypeCoodrinateSystem;

    /* таблица с расшифровкой кодов состояния точек наблюдений */
    private Map<String, String> codeStatusPointObservations;

    /* таблица с расшифровкой кодов состояния документирования */
    private Map<String, String> codeStatusDocumentation;

    /* таблица с расшифровкой кодов состояния выработки */
    private Map<String, String> codeStatusDrillingRoom;

    /* таблица с расшифровкой кодов состояния ГИС */
    private Map<String, String> codeStatusGIS;

    /* таблица с расшифровкой кодов состояния опробования */
    private Map<String, String> codeStatusTesting;

    /* таблица с расшифровкой кодов типа документирования */
    private Map<String, String> codeTypeDocumentation;

    /**
     * Инициализирует новый объект, который содержит список
     * со строками excel-листа <b>"Точки наблюдений"</b>,
     * @param sheetsOfExcelFile объект, хранящий список листов
     * excel-файла
     */
    public PointsObservations(SheetsOfExcelFile sheetsOfExcelFile) {
        super(sheetsOfExcelFile);
        try {
            readTables();
        } catch(ExcelException e) {
            generatedTable.clear();
            logger.fine("Error when reading the excel-file " +
                    sheetsOfExcelFile.getNameOfFile());
        }
    }

    /* сформировать таблицы на основе excel-листов */
    private void readTables() throws ExcelException {
        generatedTable = getPointsObservations(sheets);
        codeTypePointsObservations = getCodeTypePointsObservations(sheets);
        codeCoordinateSystem = getCodeCoordinateSystem(sheets);
        codeTypeCoodrinateSystem = getCodeTypeCoodrinateSystem(sheets);
        codeStatusPointObservations = getCodeStatusPointObservations(sheets);
        codeStatusDocumentation = getCodeStatusDocumentation(sheets);
        codeStatusDrillingRoom = getCodeStatusDrillingRoom(sheets);
        codeStatusGIS = getCodeStatusGIS(sheets);
        codeStatusTesting = getCodeStatusTesting(sheets);
        codeTypeDocumentation = getCodeTypeDocumentation(sheets);
    }

    @Override
    public void decode() {
        generatedTable.forEach(row -> {
            replacement(row, "Код типа ТН", codeTypePointsObservations);
            replacement(row, "Код системы координат", codeCoordinateSystem);
            replacement(row, "Код типа системы координат", codeTypeCoodrinateSystem);
            replacement(row, "Код Состояния ТН", codeStatusPointObservations);
            replacement(row, "Код Состояния документирования", codeStatusDocumentation);
            replacement(row, "Код Состояния выработки", codeStatusDrillingRoom);
            replacement(row, "Код Состояния ГИС", codeStatusGIS);
            replacement(row, "Код Состояния опробования", codeStatusTesting);
            replacement(row, "Код Типа документирования", codeTypeDocumentation);
        });
    }
}
