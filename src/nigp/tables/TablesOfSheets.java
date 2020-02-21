package nigp.tables;

import java.util.List;
import java.util.Map;

import nigp.Constants;
import nigp.excel.ExcelException;
import nigp.excel.SheetWithContents;
import org.apache.poi.ss.usermodel.Sheet;

import nigp.excel.SheetWithCodes;

/**
 * Содержит методы для чтения содержимого excel-листов.
 * Для excel-файлов с геолого-геофизической информацией
 * используется список со вложенными отображениями, где каждое
 * отображение описывает отдельную строку
 * Для excel-листов с расшифровкой кодов используется отображение,
 * где ключ - код, значение - расшифровка.
 * @author NovopashinAV
 */
public class TablesOfSheets {

    private static Sheet sheet; //excel-лист для чтения

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с точками наблюдений
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static List<Map<String, String>> getPointsObservations(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Точки_наблюдений.format());
        return getContentTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица данных стратиграфии и литологии
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static List<Map<String, String>> getLithoStratigraphy(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Стратиграфия_Литология.format());
        return getContentTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица данных геофизических пластов
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static List<Map<String, String>> getGeophysicalLayer(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Геофизический_пласт.format());
        return getContentTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица данных измерений ГИС
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static List<Map<String, String>> getGIS(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.ГИС.format());
        return getContentTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов тип точек наблюдения
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeTypePointsObservations(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_ТИП_ТОЧКИ_НАБЛЮДЕНИЯ.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов системы координат
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeCoordinateSystem(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_Система_координат.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов типа систем координат
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeTypeCoodrinateSystem(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_Тип_системы_координат.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов состояния точек наблюдений
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStatusPointObservations(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_СОСТОЯНИЕ_ТН.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов состояния документирования
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStatusDocumentation (
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_СОСТОЯНИЕ_ДОКУМЕНТИРОВАН.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов состояния выработки
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStatusDrillingRoom (
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_СОСТОЯНИЕ_ВЫРАБОТКИ.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов состояния ГИС
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStatusGIS (
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_СОСТОЯНИЕ_ГИС.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов состояния опробования
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStatusTesting(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_СОСТОЯНИЕ_ОПРОБОВАНИЯ.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов типа документирования
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeTypeDocumentation(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_ТИП_ДОКУМЕНТИРОВАНИЯ.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов литологии
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeLithology(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_Литология.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов стратиграфии
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeStratigraphy(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_Стратиграфия.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов геофизических пластов
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeGeophysicalLayers(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_ТН_ГЕОПЛАСТ_ИМЯ.format());
        return getCodesTable(sheet);
    }

    /**
     * @param sheets все листы excel-файла с документацией ИСИХОГИ
     * @return таблица с расшифровкой кодов методов ГИС
     * @throws ExcelException ошибка при чтении excel-листа
     */
    public static Map<String, String> getCodeMethodGIS(
            Map<String, Sheet> sheets) throws ExcelException {
        sheet = sheets.get(Constants.NameSheet.Справ_МЕТОД_ГИС.format());
        return getCodesTable(sheet);
    }

    /* возвращает список со всеми строками excel-листа */
    private static List<Map<String, String>> getContentTable(Sheet sheet)
            throws ExcelException {
        SheetWithContents sheetWithContents = new SheetWithContents(sheet);
        return sheetWithContents.getLinesOfSheet();
    }

    /* возвращает отображение с данными кодового excel-листа */
    private static Map<String, String> getCodesTable(Sheet sheet)
            throws ExcelException {
        SheetWithCodes sheetWithCodes = new SheetWithCodes(sheet);
        return sheetWithCodes.getLinesOfSheet();
    }
}
