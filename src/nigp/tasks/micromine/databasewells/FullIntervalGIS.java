package nigp.tasks.micromine.databasewells;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.MeanGIS;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.WriteGIS;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteIntervalsCommon;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cоздает файл устьев(вертикальные скважины), файл событий(ГИС)
 * и файл интервалов (статистические параметры ГИС) для Micromine.
 * Данные считываются по всему интервалу ствола скважины.
 */
public class FullIntervalGIS extends Task implements Micromine, TablesAction, MeanGIS {

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
    private String directoryForSearchExcel;

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
    protected TextFileForMicromine outputFileIntervalsStatisticWells;

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

    /* название метода ГИС, для которого производятся вычисления */
    protected String nameMethodGIS;

    private Gis gis; //таблица с данными измерений ГИС

    /* список содержит названия ключей, которые необходимы для отсева
     * скважин, не имеющих достаточной информации. (используется при
     * создании файла устьев, чтобы не записывать скважины без данных)
     */
    protected List<String> allRequiredKeys = new ArrayList<>();

    /* количество полей вместе с ГИС */
    private final int fieldsWithGIS = 17;

    public FullIntervalGIS(String[] inputParameters) throws TaskException {
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
        requiredKeysIntervalWell.add("Среднее значений ГИС");
        requiredKeysIntervalWell.add("Медиана значений ГИС");
        requiredKeysIntervalWell.add("Коэффициент вариации");
        requiredKeysIntervalWell.add("Максимальное значение ГИС");
        requiredKeysIntervalWell.add("Минимальное значение ГИС");
        requiredKeysIntervalWell.add("Среднеквадратическое отклонение");
        requiredKeysIntervalWell.add("Ошибка среднего");
        requiredKeysIntervalWell.add("Глубина ГИС");

        allRequiredKeys.addAll(requiredKeysTopWell);
        allRequiredKeys.addAll(requiredKeysEventWell);

        nonVerticaleWells.add("Скважина наклонная поисковая");
        //nonVerticaleWells.add("Поисково-Оценочные скважины");
        nonVerticaleWells.add("Штольня");
        //nonVerticaleWells.add("Разведочная");
        //nonVerticaleWells.add("Поисково-картировочные");
        //nonVerticaleWells.add("Детальная разведка");
        nonVerticaleWells.add("Скважина наклонно направленная поисковая");

    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        nameMethodGIS = inputParameters[1];
        workingCatalog = inputParameters[2];
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


        outputFileIntervalsStatisticWells = new TextFileForMicromine(workingCatalog, nameFileIntervalsWells);
        outputFileIntervalsStatisticWells.create();
        requiredKeysIntervalWell.remove("Глубина ГИС");
        requiredKeysIntervalWell.add("Начало измерений");
        requiredKeysIntervalWell.add("Конец измерений");
        outputFileIntervalsStatisticWells.writeTitle(requiredKeysIntervalWell);
        allIntervalWells = deleteRepeatElementsInSubCollection(allIntervalWells, "ID ТН");
        ScalingData forStatistic = new WriteIntervalsCommon(outputFileIntervalsStatisticWells);
        forStatistic.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());

    }

    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();

            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();

            jointTable.forEach(e -> getMeanGISforAllWell(e, nameMethodGIS));

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
             * устьев с реальной глубиной устьев создается в классе
             * TopWells
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
            gisWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysEventWell));
            List<Map<String, String>> gisWellsExistData = gisWells.stream()
                    .filter(e -> e.size() == requiredKeysEventWell.size()) // оставить ТН с тремя полями: id, value, depth
                    .collect(Collectors.toList());
            allEventWells.addAll(gisWellsExistData);

            List<Map<String, String>> statisticGISWells = copyListWithSubMap(validPoints);
            statisticGISWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));
            List<Map<String, String>> statisticGISWellsExistData = statisticGISWells.stream()
                    .filter(e -> e.size() == requiredKeysIntervalWell.size()) // оставить ТН с восмью полями:id, Среднее, медиана, коэффициент вариации, мин, макс, ско, ошибка среднего
                    .collect(Collectors.toList());

            statisticGISWellsExistData.forEach(e -> leaveMinAndMaxDepthGIS(e));

            allIntervalWells.addAll(statisticGISWellsExistData);
        }
    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однсотрочный формат.
     */
    protected void readAndTransformTables(SheetsOfExcelFile excelSheets) {
        gis = new Gis(excelSheets, nameMethodGIS);
        gis.checkedForFoundData();
        gis.pointInfoToSingleLine();
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с ГИС
     * Если какая-либо таблица с геолого-геофизическими данными пуста,
     * или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения + ГИС} или {Точки наблюдения} и т.п.
     */
    protected List<Map<String, String>> joinTables() {
        List<Map<String, String>> pointsTable =
                pointsObservations.getTableDefaultFormat();
        List<Map<String, String>> jointTable = new ArrayList<>();
        pointsTable.forEach(pointObservation -> {
            Map<String, String> currentPoint = new HashMap<>();
            String id = pointObservation.get("ID ТН");

            currentPoint.putAll(pointObservation);
            overlap(id, gis).forEach(currentPoint::putIfAbsent);

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

    /*
     * Метод позволяет отсеить скважины без данных. Используется при
     * создании файла устьев скважин (что-бы не было скважин без данных)
     */
    protected boolean wellWithData(Map<String, String> point) {
        if (point.size() == fieldsWithGIS) { //есть данные по ТН и ГИС
            return true;
        } else {
            return false;
        }
    }
}
