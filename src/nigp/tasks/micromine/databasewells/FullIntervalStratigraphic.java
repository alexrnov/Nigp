package nigp.tasks.micromine.databasewells;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.Stratigraphy;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteLithoStratigraphy;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cоздает файл устьев и файл интервалов(литостратиграфия) по всему
 * стволу скважины для Micromine
 */
public class FullIntervalStratigraphic extends Task implements Micromine, TablesAction,
                                                            Stratigraphy {

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    private final int thresholdMiniList = 100;

    /* название файла устьев скважин */
    private final String nameFileTopWells = "top wells";

    /* название файла интервалов скважин */
    private final String nameFileIntervalsWells = "interval wells";

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    private final List<String> requiredKeysTopWell = Arrays.asList("ID ТН","UIN",
            "X факт.","Y факт.","Z","Глубина ТН","Код типа ТН", "Объект","Участок",
            "Код Состояния документирования","Код Состояния выработки",
            "Код Состояния ГИС","Код Состояния опробования","Количество проб",
            "Количество ГИС");

    /*
     * список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле интервалов(для стратиграфии) для micromine.
     * Этот список также используется для отсева ненужных полей
     * в объединенной таблице
     */
    private final List<String> requiredKeysIntervalWell = Arrays.asList("ID ТН",
            "Кровля стратопласта","Подошва стратопласта","Стратиграфия","Литология",
            "Описание породы");

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    private List<Map<String, String>> allTopWells = new ArrayList<>();

    /*
     * Все точки, считанные из всех excel-файлов для файла интервалов
     * (стратиграфия, литология) для Micromine
     */
    private List<Map<String, String>> allIntervalWells = new ArrayList<>();

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* текстовый файл для записи выходных данных в файл устьев*/
    private TextFileForMicromine outputFileTopWells;

    /* текстовый файл для записи выходных данных в файл интервалов (для стратиграфии) */
    private TextFileForMicromine outputFileIntervalsStratWells;

    /* таблица с точками наблюдений */
    private PointsObservations pointsObservations;

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    private Set<String> nonVerticaleWells = new HashSet<>();

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    /* список содержит названия ключей, которые необходимы для отсева
     * скважин, не имеющих достаточной информации. (используется при
     * создании файла устьев, чтобы не записывать скважины без данных)
     */
    private List<String> allRequiredKeys = new ArrayList<>();

    public FullIntervalStratigraphic(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        allRequiredKeys.addAll(requiredKeysTopWell);
        allRequiredKeys.addAll(requiredKeysIntervalWell);
        allRequiredKeys.remove("ID ТН");

        nonVerticaleWells.add("Скважина наклонная поисковая");
        //nonVerticaleWells.add("Поисково-Оценочные скважины");
        nonVerticaleWells.add("Штольня");
        //nonVerticaleWells.add("Разведочная");
        //nonVerticaleWells.add("Поисково-картировочные");
        //nonVerticaleWells.add("Детальная разведка");
        nonVerticaleWells.add("Скважина наклонно направленная поисковая");
    }

    /* Чтение параметров командной строки */
    protected void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        workingCatalog = inputParameters[1];
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

        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        outputFileTopWells = new TextFileForMicromine(workingCatalog, nameFileTopWells);
        outputFileTopWells.create();
        outputFileTopWells.writeTitle(requiredKeysTopWell);
        allTopWells = deleteRepeatElementsInSubCollection(allTopWells,"ID ТН");
        outputFileTopWells.write(allTopWells);

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

            lithoStratigraphy = new LithoStratigraphy(excelSheets);
            lithoStratigraphy.decode();
            lithoStratigraphy.checkedForFoundData();
            lithoStratigraphy.pointInfoToSingleLine();

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

            validPoints.forEach(e -> e.keySet().retainAll(allRequiredKeys));

            List<Map<String, String>> validPointsWithData = validPoints.stream()
                    .filter(e -> e.size() == allRequiredKeys.size())
                    .collect(Collectors.toList());

            List<Map<String, String>> topWells = copyListWithSubMap(validPointsWithData);
            topWells.forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
            topWells.forEach(this::amendment);
            allTopWells.addAll(topWells);

            List<Map<String, String>> intervalWells = copyListWithSubMap(validPointsWithData);
            intervalWells.forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));
            allIntervalWells.addAll(intervalWells);
        }
    }


    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * литостратиграфией. Если какая-либо таблица с геолого-геофизическими данными
     * пуста, или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения + стратиграфия} или {Точки наблюдения}.
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

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

}
