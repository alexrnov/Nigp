package nigp.tasks.micromine.databasewells;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.MeanGIS;
import nigp.tasks.Stratigraphy;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteGIS;
import nigp.tasks.micromine.scaling.WriteLithoStratigraphy;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cоздает файл устьев(вертикальные скважины), файл событий(ГИС)
 * и файл интервалов (стратиграфия, литология) для Micromine. Данные считываются
 * по стратиграфическим пластам, указанным во входящих параметрах
 */
public class GISForStratigraphicLayers extends Task implements Micromine, TablesAction,
                                                            MeanGIS, Stratigraphy {

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    protected final int thresholdMiniList = 100;

    /* название файла устьев скважин */
    protected final String nameFileTopWells = "top wells";

    /* название файла событий скважин */
    protected final String nameFileEventWells = "event wells";

    /* название файла интервалов скважин */
    protected final String nameFileIntervalsWells = "interval wells";

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    protected List<Map<String, String>> allTopWells = new ArrayList<>();

    /* Все точки, считанные из всех excel-файлов для файла событий (для ГИС) для Micromine*/
    protected List<Map<String, String>> allEventWells = new ArrayList<>();

    /*
     * Все точки, считанные из всех excel-файлов для файла интервалов
     * (стратиграфия, литология) для Micromine
     */
    protected List<Map<String, String>> allIntervalWells = new ArrayList<>();

    /* каталог с excel-файлами */
    protected String directoryForSearchExcel;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    protected String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    protected List<File> excelFiles;

    /* текстовый файл для записи выходных данных в файл устьев*/
    protected TextFileForMicromine outputFileTopWells;

    /* текстовый файл для записи выходных данных в файл событий (для ГИС)*/
    protected TextFileForMicromine outputFileEventGISWells;

    /* текстовый файл для записи выходных данных в файл интервалов (для стратиграфии) */
    protected TextFileForMicromine outputFileIntervalsStratWells;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    protected List<String> requiredKeysTopWell = new ArrayList<>();

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле событий(для ГИС) для micromine. Этот список
     * также используется для отсева ненужных полей в объединенной таблице
     */
    protected List<String> requiredKeysEventWell = new ArrayList<>();

    /*
     * список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле интервалов(для стратиграфии) для micromine.
     * Этот список также используется для отсева ненужных полей
     * в объединенной таблице
     */
    protected List<String> requiredKeysIntervalWell = new ArrayList<>();

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    protected Set<String> nonVerticaleWells = new HashSet<>();

    /* таблица с данными литостратиграфии */
    protected LithoStratigraphy lithoStratigraphy;

    /* название метода ГИС, для которого производятся вычисления */
    protected String nameMethodGIS;

    protected Gis gis; //таблица с данными измерений ГИС

    /* список содержит названия ключей, которые необходимы для отсева
     * скважин, не имеющих достаточной информации. (используется при
     * создании файла устьев, чтобы не записывать скважины без данных)
     */
    protected List<String> allRequiredKeys = new ArrayList<>();

    /* количество полей вместе со стратиграфией и ГИС*/
    private final int fieldsWithStratGIS = 22;

    /* количество полей вместе со стратиграфией */
    private final int fieldsWithStrat = 20;

    /* количество полей вместе с ГИС */
    private final int fieldsWithGIS = 17;

    /* символ, присутствующий в искомом стратиграфическом индексе (индексах) */
    protected String[] countryRobbing;

    protected final String intervalsOfFindRocks = "Интервалы искомых отложений";

    public GISForStratigraphicLayers(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        requiredKeysTopWell.add("ID ТН");
        requiredKeysTopWell.add("UIN");
        requiredKeysTopWell.add("X факт.");
        requiredKeysTopWell.add("Y факт.");
        requiredKeysTopWell.add("Z");
        requiredKeysTopWell.add("Глубина ТН");
        requiredKeysTopWell.add("Код типа ТН");
        requiredKeysTopWell.add("Объект");
        requiredKeysTopWell.add("Участок");
        requiredKeysTopWell.add("Код Состояния документирования");
        requiredKeysTopWell.add("Код Состояния выработки");
        requiredKeysTopWell.add("Код Состояния ГИС");
        requiredKeysTopWell.add("Код Состояния опробования");
        requiredKeysTopWell.add("Количество проб");
        requiredKeysTopWell.add("Количество ГИС");

        requiredKeysEventWell.add("ID ТН");
        requiredKeysEventWell.add(nameMethodGIS);
        requiredKeysEventWell.add("Глубина ГИС");

        requiredKeysIntervalWell.add("ID ТН");
        requiredKeysIntervalWell.add("Кровля стратопласта");
        requiredKeysIntervalWell.add("Подошва стратопласта");
        requiredKeysIntervalWell.add("Стратиграфия");
        requiredKeysIntervalWell.add("Литология");
        requiredKeysIntervalWell.add("Описание породы");

        allRequiredKeys.addAll(requiredKeysTopWell);
        allRequiredKeys.addAll(requiredKeysEventWell);
        allRequiredKeys.addAll(requiredKeysIntervalWell);

        nonVerticaleWells.add("Скважина наклонная поисковая");
        nonVerticaleWells.add("Поисково-Оценочные скважины");
        nonVerticaleWells.add("Штольня");
        nonVerticaleWells.add("Разведочная");
        nonVerticaleWells.add("Поисково-картировочные");
        nonVerticaleWells.add("Детальная разведка");
        nonVerticaleWells.add("Скважина наклонно направленная поисковая");
    }

    /* Чтение параметров командной строки */
    protected void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        nameMethodGIS = inputParameters[1];
        countryRobbing = inputParameters[2].split(";");
        workingCatalog = inputParameters[3];
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

        outputFileTopWells = new TextFileForMicromine(workingCatalog, nameFileTopWells);
        outputFileTopWells.create();
        outputFileTopWells.writeTitle(requiredKeysTopWell);

        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        allTopWells = deleteRepeatElementsInSubCollection(allTopWells,"ID ТН");
        outputFileTopWells.write(allTopWells);


        outputFileEventGISWells = new TextFileForMicromine(workingCatalog, nameFileEventWells);
        outputFileEventGISWells.create();
        outputFileEventGISWells.writeTitle(requiredKeysEventWell);
        allEventWells = deleteRepeatElementsInSubCollection(allEventWells, "ID ТН");
        ScalingData forGIS = new WriteGIS(outputFileEventGISWells, nameMethodGIS);
        forGIS.perform(allEventWells, thresholdMiniList, 0, allEventWells.size());


        outputFileIntervalsStratWells = new TextFileForMicromine(workingCatalog, nameFileIntervalsWells);
        outputFileIntervalsStratWells.create();
        outputFileIntervalsStratWells.writeTitle(requiredKeysIntervalWell);
        allIntervalWells = deleteRepeatElementsInSubCollection(allIntervalWells, "ID ТН");
        ScalingData forStratLith = new WriteLithoStratigraphy(outputFileIntervalsStratWells);
        forStratLith.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());

    }

    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();

            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();
            List<Map<String, String>> validPoints = jointTable.stream()
                    .filter(this::hasID)
                    //.filter(this::hasDepth)
                    .filter(this::hasX)
                    .filter(this::hasY)
                    .filter(this::hasZ)
                    .filter(e -> hasVerticale(e, nonVerticaleWells))
                    .collect(Collectors.toList());

            /*
             * Всем скважинам задается одинаковая глубина скважин.
             * Это делается для того, чтобы анализировать данные по
             * скважинам, у которых нет информации по глубине. Файл
             * устьев с реальной глубиной устьев создается в другом
             * классе
             */
            validPoints.stream().forEach(e -> {
                e.replace("Глубина ТН", "1000");
            });

            List<Map<String, String>> topWells = copyListWithSubMap(validPoints);

            topWells.stream().forEach(e -> e.keySet().retainAll(allRequiredKeys));//можно ли так модифицировать коллекцию?
            //оставить только устья скважин с данными по ТН, стратиграфии и/или ГИС
            List<Map<String, String>> topWellsWithInfo = topWells.stream()
                    .filter(this::wellWithData)
                    .collect(Collectors.toList());
            topWellsWithInfo.stream().forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
            topWellsWithInfo.forEach(this::amendment);
            allTopWells.addAll(topWellsWithInfo);

            List<Map<String, String>> gisWells = copyListWithSubMap(validPoints);

            gisWells.forEach(e -> inputIntervalsForFindRocks(e, countryRobbing, intervalsOfFindRocks));

            gisWells.forEach(e -> getMeanGISforIntervals(e, intervalsOfFindRocks, nameMethodGIS));




            List<Map<String, String>> stratWells = copyListWithSubMap(gisWells);
            List<Map<String, String>> stratWellsUpdate = stratWells.stream()
                    .filter(this::existDataForStratigraphyIntervalWithGIS)
                    .collect(Collectors.toList());
            stratWellsUpdate.forEach(this::selectIntervalsForStratigraphy);
            stratWellsUpdate.stream().forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));

            List<Map<String, String>> stratWellsExistData = stratWellsUpdate.stream()
                    .filter(e -> e.size() == requiredKeysIntervalWell.size()) // оставить ТН с пятью полями:id, start, lit, descript, from, to
                    .collect(Collectors.toList());

            allIntervalWells.addAll(stratWellsExistData);

            gisWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysEventWell));
            List<Map<String, String>> gisWellsExistData = gisWells.stream()
                    .filter(e -> e.size() == requiredKeysEventWell.size()) // оставить ТН с тремя полями: id, value, depth
                    .collect(Collectors.toList());

            //gisWells.forEach(e -> deleteRepeatValues(e, nameMethodGIS));//!!!!
            gisWellsExistData.forEach(e -> deleteRepeatValues(e, nameMethodGIS));//!!!!

            allEventWells.addAll(gisWellsExistData);

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
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (литостратиграфией, ГИС). Если
     * какая-либо таблица с геолого-геофизическими данными пуста,
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

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

    /*
     * Метод позволяет отсеить скважины без данных. Используется при
     * создании файла устьев скважнин (что-бы не было скважин без данных)
     */
    protected boolean wellWithData(Map<String, String> point) {
        if (point.size() == fieldsWithStratGIS || //есть данные по ТН, стратиграфии и ГИС
                point.size() == fieldsWithStrat || //есть данные по ТН и стратиграфии
                point.size() == fieldsWithGIS) { //есть данные по ТН и ГИС
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void getMeanGISforIntervals(Map<String, String> point, String stratigraphyIntervals,
                                       String nameMethodGIS) {

        StringBuilder selectDepthGIS = new StringBuilder();
        StringBuilder selectValuesGIS = new StringBuilder();

        if (point.containsKey(stratigraphyIntervals) == false) {
            point.put("Глубина ГИС", selectDepthGIS.toString());
            point.put(nameMethodGIS, selectValuesGIS.toString());
            return;
        }

        if (point.containsKey(nameMethodGIS) == false
                || point.containsKey("Глубина ГИС") == false) {
            return;
        }

        String[] intervals = point.get(stratigraphyIntervals).split("/");
        String[] valuesGIS = point.get(nameMethodGIS).split("/");
        String[] depthsGIS = point.get("Глубина ГИС").split("/");

        Double[][] jointArray = new Double[depthsGIS.length][2];

        for (int i = 0; i < depthsGIS.length; i++) {
            jointArray[i][0] = Double.valueOf(depthsGIS[i]);
            jointArray[i][1] = Double.valueOf(valuesGIS[i]);
        }

        List<List<Double>> measurementsGIS = Arrays.stream(jointArray)
                .map(Arrays::asList)
                .collect(Collectors.toList());

        List<List<Double>> selectGIS = new ArrayList<>();

        for (String interval : intervals) {
            String[] contacts = interval.split("-");

            Double upperContact = Double.valueOf(contacts[0]);
            Double lowerContact = Double.valueOf(contacts[1]);
            List<List<Double>> intervalGIS = measurementsGIS.stream()
                    .filter(survey -> isOverlap(survey, upperContact, lowerContact))
                    .filter(this::isExistValue)
                    .collect(Collectors.toList());

            selectGIS.addAll(intervalGIS);
        }

        selectGIS.forEach(e -> {
            selectDepthGIS.append(e.get(0));
            selectDepthGIS.append("/");
            selectValuesGIS.append(e.get(1));
            selectValuesGIS.append("/");
        });

        point.put("Глубина ГИС", selectDepthGIS.toString());
        point.put(nameMethodGIS, selectValuesGIS.toString());

        if (selectGIS.size() != 0) {
            calcStatistic(selectGIS, point);
        }
    }

    protected boolean existDataForStratigraphyIntervalWithGIS(Map<String, String> point) {
        if (!point.containsKey(intervalsOfFindRocks)) {
            return false;
        }
        if (!point.containsKey(nameMethodGIS)) {
            return false;
        }
        if (point.get(nameMethodGIS).equals("") || point.get(nameMethodGIS).equals(" ")) {
            return false;
        }
        return true;
    }

    protected void selectIntervalsForStratigraphy(Map<String, String> point) {
        String[] findIntervals = point.get(intervalsOfFindRocks).split("/");
        String[] lith = point.get("Литология").split("/");
        String[] strat = point.get("Стратиграфия").split("/");
        String[] top = point.get("Кровля стратопласта").split("/");
        String[] down = point.get("Подошва стратопласта").split("/");
        String[] description = point.get("Описание породы").split("/");

        if (lith.length != strat.length || lith.length != top.length
                || lith.length != down.length || lith.length != description.length) {
            logger.config("Different size stratigraphic arrays");
            return;
        }

        StringBuilder newStrat = new StringBuilder();
        StringBuilder newLith = new StringBuilder();
        StringBuilder newTop = new StringBuilder();
        StringBuilder newDown = new StringBuilder();
        StringBuilder newDesc = new StringBuilder();

        for (int i = 0; i < findIntervals.length; i++) {
            String[] findInterval = findIntervals[i].split("-");
            String fromInterval = findInterval[0];
            for (int j = 0; j < top.length; j++) {
                if (top[j].equals(fromInterval)) {
                    newStrat.append(strat[j]); newStrat.append("/");
                    newLith.append(lith[j]); newLith.append("/");
                    newTop.append(top[j]); newTop.append("/");
                    newDown.append(down[j]); newDown.append("/");
                    newDesc.append(description[j]); newDesc.append("/");
                }
            }
        }

        point.put("Стратиграфия", newStrat.toString());
        point.put("Литология" , newLith.toString());
        point.put("Кровля стратопласта", newTop.toString());
        point.put("Подошва стратопласта", newDown.toString());
        point.put("Описание породы", newDesc.toString());
    }
}
