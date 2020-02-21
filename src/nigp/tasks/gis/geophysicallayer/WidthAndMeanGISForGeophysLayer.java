package nigp.tasks.gis.geophysicallayer;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import nigp.excel.FoundExcelFiles;
import nigp.file.ReportFile;
import nigp.file.ShapeFileForPoints;
import nigp.file.TextFileForMapLayer;
import nigp.tasks.TaskException;
import nigp.excel.SheetsOfExcelFile;
import nigp.tables.GeophysicalLayers;
import nigp.tables.Gis;
import nigp.tables.LithoStratigraphy;
import nigp.tables.PointInfoToSingleLine;
import nigp.tables.PointsObservations;
import nigp.tasks.MeanGIS;
import nigp.tasks.Task;
import nigp.tasks.micromine.Micromine;

/**
 * Вычисляет средние значения ГИС для геофизических пластов с
 * абсолютной мощностью
 * @author NovopashinAV
 */
public class WidthAndMeanGISForGeophysLayer extends Task implements MeanGIS, Micromine {

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* название метода ГИС, для которого производятся вычисления */
    protected String nameMethodGIS;

    /* название геофизического пласта */
    private String nameGeophysLayer;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /*
     * название ключа интервалов геофизического пласта/пластов для
     * вставки в отображение объединенной таблицы
     */
    protected String geophysIntervals;

    /*
     * название ключа с мощностью геофизического пласта/пластов
     * для вставки в отображение объединенной таблицы
     */
    private String widthGeophysLayer;
    /*
     * название ключа со средним арифметическим измерений ГИС
     * по геофизическому пласту
     */
    private final String arithmeticMeanGIS = "Среднее значений ГИС";

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* путь к шейп-файлу, куда сохраняются результаты вычислений */
    private String filePath;

    /* шейп-файл для записи выходных данных*/
    private ShapeFileForPoints shapeFile;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    /* таблица с данными геофизических пластов */
    private GeophysicalLayers geophysicalLayers;

    private Gis gis; //таблица с данными измерений ГИС

    /* текстовый файл для записи выходных данных */
    protected TextFileForMapLayer outputFile;

    /* файл для записи отчетных данных */
    protected ReportFile reportFile;

    /* количество всех точек наблюдений для записи в файл */
    private int pointsInAll = 0;

    /*
     * количество точек наблюдений с вычисленным значением ГИС для
     * выбранного геопласта с абсолютной мощностью
     */
    private int pointsWithCalcMeanGIS = 0;

    /*
     * коллекция для хранения информации по: 1. Названиям атрибутов в таблицах ИСИХОГИ.
     * 2. Названия атрибутов для записи в шейп-файл (на латинице);
     * 3. Тип параметра (необходимо при записи значений в shape-файл)
     */
    private List<List<String>> nameAndTypeKeys = new ArrayList<>();

    public WidthAndMeanGISForGeophysLayer(String[] inputParameters)
            throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        nameAndTypeKeys.add(Arrays.asList("ID ТН", "id tn", "String"));
        nameAndTypeKeys.add(Arrays.asList("Точка наблюдений", "tn" ,"String"));
        nameAndTypeKeys.add(Arrays.asList("Линия", "line", "String"));
        nameAndTypeKeys.add(Arrays.asList("Объект", "object", "String"));
        nameAndTypeKeys.add(Arrays.asList("Участок", "station", "String"));
        nameAndTypeKeys.add(Arrays.asList("X факт.", "x", "String"));
        nameAndTypeKeys.add(Arrays.asList("Y факт.", "y", "String"));
        nameAndTypeKeys.add(Arrays.asList("Z", "z", "String"));
        nameAndTypeKeys.add(Arrays.asList("Глубина ТН", "depth tn", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Код типа ТН", "type", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния выработки", "state tn", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния документирования", "state doc", "String"));
        nameAndTypeKeys.add(Arrays.asList("Медиана значений ГИС", "median", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Среднее значений ГИС", "arithmetic", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Среднеквадратическое отклонение", "sko", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Ошибка среднего", "mean error", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Коэффициент вариации", "variation", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Минимальное значение ГИС", "min", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Максимальное значение ГИС", "max", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Сумма значений ГИС", "sum gis", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Количество измерений ГИС", "amount gis", "Double"));

        nameAndTypeKeys.add(Arrays.asList("abs кровли", "absTop", "Double"));
        nameAndTypeKeys.add(Arrays.asList("abs подошвы", "absDown", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Абсолютная мощность пласта", "absoluteW", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Геопласт", "geoLayer", "String"));
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        if (inputParameters.length > 2) {
            directoryForSearchExcel = inputParameters[0];
            nameMethodGIS = inputParameters[1];
            nameGeophysLayer = inputParameters[2];
            geophysIntervals = "Интервал пласта";
            widthGeophysLayer = "Абсолютная мощность пласта";
            filePath = inputParameters[3];
        } else {
            throw new TaskException("Incorrect input parameters");
        }
    }

    private void findExcelFiles() throws TaskException {
        FoundExcelFiles foundExcelFiles = new FoundExcelFiles(directoryForSearchExcel);
        if (foundExcelFiles.isNotFoundExcelFiles()) {
            throw new TaskException("Excel-files not found");
        }
        excelFiles = foundExcelFiles.getFoundExcelFiles();
    }

    @Override
    public void toSolve() throws TaskException {

        long startTime = System.currentTimeMillis();

        shapeFile = new ShapeFileForPoints(filePath);
        shapeFile.create(nameAndTypeKeys);
        shapeFile.setNameKeyForX("X факт.");
        shapeFile.setNameKeyForY("Y факт.");

        //outputFile = new TextFileForMapLayer(workingCatalog);
        //outputFile.create();

        //reportFile = new ReportFile(workingCatalog);
        //reportFile.create();

        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        shapeFile.close();
        /*
        reportFile.write("Всего в файл записано " + pointsInAll
                + " точек наблюдений, " + "\n" + "из них - "
                + pointsWithCalcMeanGIS
                + " с вычисленным средним значением ГИС" + "\n"
                + "для " + nameGeophysLayer + " геофизического пласта");

        long spentTime = (System.currentTimeMillis() - startTime)/1000;
        reportFile.write("Время вычислений: " + spentTime + " сек.");
        */
    }

    /*
     * провести вычисления для текущего excel-файла
     */
    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();
            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();
            jointTable.forEach(this::getIntervalsForGeoplast);
            jointTable.forEach(this::getWidthGeophysicalLayer);
            jointTable.forEach(e -> getMeanGISforIntervals(e, geophysIntervals, nameMethodGIS));
            jointTable.forEach(this::getAbsoluteABS);

            jointTable.forEach(this::amendment);
            jointTable.forEach(e -> {
                String x = e.get("X факт.");
                e.put("X факт.", e.get("Y факт."));
                e.put("Y факт.", x);
            });

            shapeFile.write(jointTable);
            //outputFile.write(jointTable);
            //String report = getReportAndPrintToConsole(excelSheets, jointTable);
            //reportFile.write(report);
        } else {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
        }
    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однсотрочный формат.
     */
    protected void readAndTransformTables(SheetsOfExcelFile excelSheets) {

        lithoStratigraphy = new LithoStratigraphy(excelSheets);
        lithoStratigraphy.decode();
        lithoStratigraphy.checkedForFoundData();
        lithoStratigraphy.pointInfoToSingleLine();

        gis = new Gis(excelSheets, nameMethodGIS);
        gis.checkedForFoundData();
        gis.pointInfoToSingleLine();

        geophysicalLayers = new GeophysicalLayers(excelSheets);
        geophysicalLayers.decode();
        geophysicalLayers.checkedForFoundData();
        geophysicalLayers.sortByIdAndUpperContact();
        geophysicalLayers.pointInfoToSingleLine();
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (литостратиграфией, ГИС, геофизическими
     * пластами). Если какая-либо таблица с геолого-геофизическими данными пуста,
     * или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения + стратиграфия + ГИС} или {Точки наблюдения +
     * ГИС} или {Точки наблюдения} и т.п.
     */
    protected List<Map<String, String>> joinTables() {
        List<Map<String, String>> pointsTable =
                pointsObservations.getTableDefaultFormat();
        List<Map<String, String>> jointTable = new ArrayList<>();
        pointsTable.forEach(pointObservation -> {
            Map<String, String> currentPoint = new HashMap<>();
            String id = pointObservation.get("ID ТН");

            currentPoint.putAll(pointObservation);
            overlap(id, lithoStratigraphy).forEach(currentPoint::putIfAbsent);
            overlap(id, gis).forEach(currentPoint::putIfAbsent);
            overlap(id, geophysicalLayers).forEach(currentPoint::putIfAbsent);

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

    /*
     * Если в таблице table с геолого-геофизическими данными найдена точка с
     * таким же идентификатором, как и в главной таблице с точками наблюдений,
     * вернуть геолого-геофизическую информацию по этой точке.
     */
    private Map<String, String> overlap(String id, PointInfoToSingleLine table) {
        for (Map<String, String> m : table.getTableSingleFormat()) {
            if (id.equals(m.get("ID ТН"))) {
                return m;
            }
        }
        return new HashMap<String, String>();
    }

    /*
     * Для текущей скважины метод ищет геопласт/геопласты с абсолютной
     * мощностью. Если пласт/пласты найдены, для текущей точки наблюдения
     * создается новый элемент отображения с ключем geophysIntervals и
     * значением intervals.toString()
     */
    protected void getIntervalsForGeoplast(Map<String, String> point) {
        if (point.containsKey("Геопласт") == false
                || point.containsKey("Кровля геопласта") == false) {
            return;
        }

        String[] geoLayers = point.get("Геопласт").split("/");
        StringBuilder intervals = new StringBuilder();

        if (satisfiedCriteria(geoLayers)) {
            String[] upperContact = point.get("Кровля геопласта").split("/");
            intervals = getIntervals(geoLayers, upperContact);
        }

        if (intervals.length() != 0) {
            point.put(geophysIntervals, intervals.toString());
        }
    }

    /*
     * Возращает true если информация по геофизическим пластам удовлетворяет
     * следующим условиям: 1. По скважине имеется как минимум три пласта - это
     * будет означать, что имеется хотя-бы один пласт с абсолютной мощностью. 2.
     * Искомый пласт не должен являться первым и последним в списке пластов,
     * потому что это исключает возможность вычисления абсолютной мощности.
     */
    private boolean satisfiedCriteria(String[] geoLayers) {
        if (geoLayers.length > 2) {
            if (!geoLayers[0].equals(nameGeophysLayer)
                    && !geoLayers[geoLayers.length - 1].equals(nameGeophysLayer)) {
                return true;
            }
        }
        return false;
    }

    /*
     * просмотреть все пласты от второго до предпоследнего на предмет совпадения
     * с искомым геофизическим пластом, если такой пласт/пласты найдены -
     * сформировать строку с интервалами пласта/пластов.
     */
    private StringBuilder getIntervals(String[] geoLayers,
                                       String[] upperContact) {
        StringBuilder intervals = new StringBuilder();
        if (upperContact.length != geoLayers.length) {
            return intervals;
        }
        for (int i = 1; i < geoLayers.length - 1; i++) {
            if (geoLayers[i].equals(nameGeophysLayer)) {
                intervals.append(upperContact[i]);
                intervals.append("-");
                intervals.append(upperContact[i + 1]);
                intervals.append("/");
            }
        }
        return intervals;
    }

    /*
     * Получить интервалы (глубина залегания кровли и подошвы)
     * оцениваемых геофизических пластов
     */
    protected void getWidthGeophysicalLayer(Map<String, String> point) {
        if (point.containsKey(geophysIntervals) == false) {
            return;
        }

        String[] intervals = point.get(geophysIntervals).split("/");
        BigDecimal width = new BigDecimal("0.0");
        for (String interval : intervals) {
            String[] contacts = interval.split("-");
            BigDecimal upperContact = new BigDecimal(contacts[0]);
            BigDecimal lowerContact = new BigDecimal(contacts[1]);
            lowerContact = lowerContact.subtract(upperContact);
            width = width.add(lowerContact);
        }
        point.put(widthGeophysLayer, width.toString());
    }

    /*
     * Получить абсолютные отметки залегания кровли и подошвы для
     * оцениваемых геофизических пластов (высота z минус глубина
     * залегания кровли или подошвы(расстояние от устья скважины))
     */
    private void getAbsoluteABS(Map<String, String> point) {
        if (point.get("Z").equals("-999999.0") || point.get("Z").equals("")
                || point.containsKey("Интервал пласта") == false) {
            return;
        }
        String[] allContacts = point.get("Интервал пласта").split("/");
        BigDecimal upperContact;
        BigDecimal lowerContact;
        if (allContacts.length == 1) {
            String[] contacts = allContacts[0].split("-");
            upperContact = new BigDecimal(contacts[0]);
            lowerContact = new BigDecimal(contacts[1]);
        /*
        если больше одного пласта, тогда взять кровлю первого пласта
        и подошву последнего пласта
         */
        } else if (allContacts.length > 1) {
            String[] firstContacts = allContacts[0].split("-");
            String[] lastContacts = allContacts[allContacts.length - 1].split("-");
            upperContact = new BigDecimal(firstContacts[0]);
            lowerContact = new BigDecimal(lastContacts[1]);
        } else {
            return;
        }

        BigDecimal z = new BigDecimal(point.get("Z"));
        upperContact = z.subtract(upperContact);
        lowerContact = z.subtract(lowerContact);

        point.put("abs кровли", upperContact.toString());
        point.put("abs подошвы", lowerContact.toString());
    }

    protected String getReportAndPrintToConsole(SheetsOfExcelFile excelSheets,
                                              List<Map<String, String>> jointTable) {
        StringBuilder s = new StringBuilder();
        s.append(excelSheets.getNameOfFile());
        s.append("\n");

        int points = jointTable.size();
        s.append("Всего точек наблюдений: ");
        s.append(points);
        s.append(", из них: \n");

        s.append("с информацией по ГИС: ");
        s.append(countPoints(jointTable, nameMethodGIS));
        s.append("\n");

        s.append("с информацией по геофизическим пластам: ");
        s.append(countPoints(jointTable, "Геопласт"));
        s.append("\n");

        s.append("с абсолютной мощностью ");
        s.append(nameGeophysLayer);
        s.append(" геофизического пласта: " );
        s.append(countPoints(jointTable, geophysIntervals));
        s.append("\n");

        int countPointsGIS = countPoints(jointTable, arithmeticMeanGIS);
        s.append("с вычисленным средним ГИС по пласту: ");
        s.append(countPointsGIS);
        System.out.println(s);
        System.out.println();

        pointsInAll += points;
        pointsWithCalcMeanGIS += countPointsGIS;

        return s.toString();
    }

    private int countPoints(List<Map<String, String>> jointTable, String attr) {
        long count = jointTable.stream()
                .filter(p -> p.containsKey(attr))
                .count();
        return (int) count;
    }
}
