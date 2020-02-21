package nigp.tasks.micromine.databasewells;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import nigp.excel.FoundExcelFiles;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.TaskException;
import nigp.excel.SheetsOfExcelFile;
import nigp.tasks.MeanGIS;
import nigp.tasks.Task;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteAllGeophysicLayers;

/**
 * Записыавает два файла - файл с устьями скважин и файл с интервалами
 * всех геофизических пластов
 * @author NovopashinAV
 */
public class AllGeophysicLayers extends Task implements MeanGIS, Micromine, TablesAction {

    /* название файла устьев скважин */
    private final String nameFileTopWells = "top wells";

    /* название файла интервалов скважин */
    private final String nameFileIntervalsWells = "interval wells";

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    private final int thresholdMiniList = 100;

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    private List<Map<String, String>> allTopWells = new ArrayList<>();

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* таблица с данными геофизических пластов */
    private GeophysicalLayers geophysicalLayers;

    /* текстовый файл для записи выходных данных в файл устьев */
    protected TextFileForMicromine outputFileTopWells;

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    private Set<String> nonVerticaleWells = new HashSet<>();

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    private List<String> requiredKeysTopWell = new ArrayList<>();

    /*
     * список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле интервалов(для геофизических пластов)
     * для micromine. Этот список также используется для отсева ненужных
     * полей в объединенной таблице
     */
    private List<String> requiredKeysIntervalWell = new ArrayList<>();

    /*
     * Все точки, считанные из всех excel-файлов для файла интервалов
     * (информация по геофизическим пластам)
     * для Micromine
     */
    private List<Map<String, String>> allIntervalWells = new ArrayList<>();

    /*
     * Текстовый файл для записи выходных данных в файл интервалов
     * (для геофизических пластов)
     */
    protected TextFileForMicromine outputFileIntervalsWells;

    /* список содержит названия ключей, которые необходимы для отсева
     * скважин, не имеющих достаточной информации. (используется при
     * создании файла устьев, чтобы не записывать скважины без данных)
     */
    private List<String> allRequiredKeys = new ArrayList<>();

    /* количество полей вместе с данными по геофизическим пластам */
    private final int fieldsWithGeophysicalLayers = 12;

    public AllGeophysicLayers(String[] inputParameters)
            throws TaskException {
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

        nonVerticaleWells.add("Скважина наклонная поисковая");
        nonVerticaleWells.add("Поисково-Оценочные скважины");
        nonVerticaleWells.add("Штольня");
        nonVerticaleWells.add("Разведочная");
        nonVerticaleWells.add("Поисково-картировочные");
        nonVerticaleWells.add("Детальная разведка");
        nonVerticaleWells.add("Скважина наклонно направленная поисковая");

        requiredKeysIntervalWell.add("ID ТН");
        requiredKeysIntervalWell.add("Кровля геопласта");
        requiredKeysIntervalWell.add("Подошва геопласта");
        requiredKeysIntervalWell.add("Геопласт");
        requiredKeysIntervalWell.add("Стратиграфия геопласта");
        requiredKeysIntervalWell.add("Описание геопласта");

        allRequiredKeys.addAll(requiredKeysTopWell);
        allRequiredKeys.addAll(requiredKeysIntervalWell);
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {

        if (inputParameters.length > 1) {
            directoryForSearchExcel = inputParameters[0];
            workingCatalog = inputParameters[1];

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

        allTopWells = deleteRepeatElementsInSubCollection(allTopWells, "ID ТН");
        outputFileTopWells.write(allTopWells);


        outputFileIntervalsWells = new TextFileForMicromine(workingCatalog, nameFileIntervalsWells);
        outputFileIntervalsWells.create();
        outputFileIntervalsWells.writeTitle(requiredKeysIntervalWell);

        allIntervalWells = deleteRepeatElementsInSubCollection(allIntervalWells, "ID ТН");

        ScalingData forGeophysicLayer =
                new WriteAllGeophysicLayers(outputFileIntervalsWells);
        forGeophysicLayer.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());
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

            List<Map<String, String>> validPoints = jointTable.stream()
                    .filter(this::hasID)
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
             * классе OnlyTopWells
             */
            validPoints.stream().forEach(e -> {
                e.replace("Глубина ТН", "1000");
            });


            List<Map<String, String>> topWells = copyListWithSubMap(validPoints);
            topWells.stream().forEach(e -> e.keySet().retainAll(allRequiredKeys));//можно ли так модифицировать коллекцию?
            //оставить только устья скважин с данными по ТН и геофизическим пластам
            List<Map<String, String>> topWellsWithInfo = topWells.stream()
                    .filter(e -> e.size() == fieldsWithGeophysicalLayers)
                    .collect(Collectors.toList());

            topWellsWithInfo.stream()
                    .forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
            topWellsWithInfo.forEach(this::amendment);
            allTopWells.addAll(topWellsWithInfo);

            List<Map<String, String>> geophysLayerIntervals = copyListWithSubMap(validPoints);
            geophysLayerIntervals.stream().forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));

            List<Map<String, String>> geophysLayerIntervalsCorrect =
                    geophysLayerIntervals.stream()
                            .filter(e -> e.size() == requiredKeysIntervalWell.size()) //оставить точки с: ID ТН, from, to, code_geoplast, strat_geoplast, descript
                            .collect(Collectors.toList());

            allIntervalWells.addAll(geophysLayerIntervalsCorrect);
        } else {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
        }
    }

    /*
     * Прочитать геолого-геофизические данные из таблицы
     * "Геофизический пласт". Расшифровать для
     * этой таблицы закодированные значения. Трансформировать таблицу в
     * однсотрочный формат.
     */
    protected void readAndTransformTables(SheetsOfExcelFile excelSheets) {

        geophysicalLayers = new GeophysicalLayers(excelSheets);
        geophysicalLayers.decode();
        geophysicalLayers.checkedForFoundData();
        geophysicalLayers.sortByIdAndUpperContact();
        geophysicalLayers.pointInfoToSingleLine();
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (геофизическими
     * пластами). Если какая-либо таблица с геолого-геофизическими данными пуста,
     * или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения +
     * Геофизические пласты} или {Точки наблюдения}.
     */
    protected List<Map<String, String>> joinTables() {
        List<Map<String, String>> pointsTable =
                pointsObservations.getTableDefaultFormat();
        List<Map<String, String>> jointTable = new ArrayList<>();
        pointsTable.forEach(pointObservation -> {
            Map<String, String> currentPoint = new HashMap<>();
            String id = pointObservation.get("ID ТН");

            currentPoint.putAll(pointObservation);
            overlap(id, geophysicalLayers).forEach(currentPoint::putIfAbsent);
            jointTable.add(currentPoint);
        });
        return jointTable;
    }

}
