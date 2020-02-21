package nigp.tables;

import static nigp.tables.TablesOfSheets.getCodeMethodGIS;
import static nigp.tables.TablesOfSheets.getGIS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nigp.excel.ExcelException;
import nigp.excel.SheetsOfExcelFile;

/**
 * Класс формирует таблицу excel-листа "ГИС" с данными того метода ГИС,
 * который указан в переменной <b>nameMethodGIS</b>. Коды
 * таблицы "Справ. МЕТОД ГИС" изменяются, чтобы формат кодов методов из
 * кодового листа совпадал с форматом имен полей из листа ГИС.
 *
 * @author NovopashinAV
 */
public class Gis extends Table implements PointInfoToSingleLine {

    /* таблица с расшифровкой кодов методов ГИС */
    private Map<String, String> codeMethodGIS;

    /* название метода ГИС, полученное из входных параметров командной строки */
    private final String nameMethodGIS;

    /*
     * список с найденными кодами для названия метода ГИС.
     * В данном случае используется список, поскольку
     * у таких методов как "Боковой Каротаж" и "Скважинная магнитометрия"
     * по два кода.
     */
    private List<String> listNeededCodeMethod = new ArrayList<>();

    /* таблица с геолого-геофизическими данными в однострочном формате */
    private List<Map<String, String>> tableWithSingleLines = new ArrayList<>();

    private String idPoint; //идентификатор скважины

    /* В таблице одна скважина представлена, как правило,
     * несколькими строками. Поэтому для того, чтобы хранить
     * данные по скважине в одной строке, необходимо
     * накопить данные нескольких строк в переменной типа StringBuilder.
     * Такие переменные используются для накопления данных по значениям
     * и глубинам ГИС-измерений.
     */
    private StringBuilder valueGis = new StringBuilder();
    private StringBuilder depthGis = new StringBuilder();

    /* Коллекция для хранения 'однострочных' данных по текущей скважине*/
    private Map<String, String> lineOfBorehole = new HashMap<>();

    /*
     * название атрибута метода ГИС (напр. 'Метод 1')
     * из таблицы с измерениями ГИС.
     */
    private String keyMethod;

    /**
     * Инициализирует новый объект, который формируетсо строками
     * excel-листа <b>"ГИС"</b>. Каждая строка листа храниться в
     * отдельном отображении, где ключ - название столбца,
     * значение - значение ячейки. Поскольку в листе "ГИС" могут быть
     * данные по нескольким методам скважинной геофизики, которые не
     * используются в вычислениях, они исключаются из результирующей таблицы.
     * @param sheetsOfExcelFile объект, хранящий список листов
     * @param nameMethodGIS название искомого метода ГИС
     */
    public Gis(SheetsOfExcelFile sheetsOfExcelFile, String nameMethodGIS) {
        super(sheetsOfExcelFile);
        this.nameMethodGIS = nameMethodGIS;
        try {
            readTables();

            fillListNeededCodeMethod();

            if (listNeededCodeMethod.size() != 0) {
                modifyGeneratedTable();
            } else {
                logger.fine("Not found code for sucn name GIS");
                generatedTable.clear();
            }
        } catch(ExcelException e) {
            generatedTable.clear();
            logger.fine(e.getMessage() + ". File is: "
                    + sheetsOfExcelFile.getNameOfFile());
        }

    }

    /* сформировать таблицы на основе excel-листов*/
    private void readTables() throws ExcelException {
        generatedTable = getGIS(sheets);
        codeMethodGIS = getCodeMethodGIS(sheets);
    }

    /* заполнить список с найденными кодами для названия метода ГИС */
    private void fillListNeededCodeMethod() {
        Map<String, String> validCodeMethodGIS = putRightKey();
        validCodeMethodGIS.forEach((codeMethod, transcript) -> {
            if (transcript.equals(nameMethodGIS)) {
                listNeededCodeMethod.add(codeMethod);
            }
        });
    }

    /*
     * подготовить кодовую таблицу, чтобы формат кодов ГИС из
     * кодового листа совпадал с форматом имен полей из листа ГИС
     */
    private Map<String, String> putRightKey() {
        Map<String, String> map = codeMethodGIS.entrySet()
                .stream()
                .collect(Collectors
                        .toMap(e -> replaceKey(e.getKey()), e -> e.getValue()));
        return map;
    }

    /* убрать точки из кодов методов, и добавить в начале слово "Метод" */
    private String replaceKey(String key) {
        String nameMethod = key;
        String[] s = key.split("\\.");
        if (s.length == 2) {
            nameMethod = "Метод " + s[0];
        }
        return nameMethod;
    }

    /* удалить лишние поля в таблице с данными ГИС,
     * или отчистить всю таблицу, если нужных полей нет */
    private void modifyGeneratedTable() {
        keyMethod = getKeyMethod();
        if (keyMethod.length() != 0) {
            generatedTable.forEach(e -> e.keySet().removeIf(this::isExcessField));
        } else {
            // метод ГИС с таким именем не найден для текущего набора точек наблюдений
            generatedTable.clear();
        }
    }

    /* вернуть название атрибута метода ГИС (напр. 'Метод 1')
     * из таблицы с измерениями ГИС. Если такого метода нет для
     * текущего набора точек наблюдений, вернуть пустую строку
     */
    private String getKeyMethod() {
        for (String e : listNeededCodeMethod) {
            if (generatedTable.get(0).containsKey(e)) {
                return e;
            }
        }
        return "";
    }

    /* проверить является ли поле данных лишним в данном контексте */
    private boolean isExcessField(String fieldName) {
        for (String e : listNeededCodeMethod) {
            if (fieldName.equals(e)) {
                return false;
            }
        }
        if (fieldName.equals("Глубина") || fieldName.equals("ID ТН")) {
            return false;
        }
        return true;
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
        valueGis.append(line.get(keyMethod) + "/");
        depthGis.append(line.get("Глубина") + "/");
    }

    private void clearStringBuilder() {
        valueGis.delete(0, valueGis.length());
        depthGis.delete(0, depthGis.length());
    }

    private void addLineOfBoreholeToTable() {
        lineOfBorehole.put("ID ТН", idPoint);
        lineOfBorehole.put(nameMethodGIS, valueGis.toString());
        lineOfBorehole.put("Глубина ГИС", depthGis.toString());
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
