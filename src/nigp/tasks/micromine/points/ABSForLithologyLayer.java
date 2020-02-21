package nigp.tasks.micromine.points;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.LithoStratigraphy;
import nigp.tables.PointsObservations;
import nigp.tables.TablesAction;
import nigp.tasks.Stratigraphy;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteStratOrLithPoints;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Создает файлы точек для Micromine, которые содержат абсолютные
 * отметки по кровле и подошве для указанного литологического подразделения
 * подразделения, соответственно
 */
public class ABSForLithologyLayer extends Task implements TablesAction,
        Micromine, Stratigraphy {

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    private final int thresholdMiniList = 500;

    /* название файла точек */
    private String nameFile;

    /* названия ключей, которые нужны для произведения вычислений */
    private final List<String> requiredKeysForCalculate = Arrays.asList("ID ТН",
            "UIN", "X факт.", "Y факт.", "Z", "Литология", "Кровля стратопласта",
            "Подошва стратопласта", "Код Типа документирования", "Код типа ТН",
            "Участок", "Объект");

    /* названия ключей, которые нужны для записи в выходной текстовый файл */
    private final List<String> requiredKeysForWrite = Arrays.asList("ID ТН",
            "UIN", "X факт.", "Y факт.", "Код Типа документирования",
            "Код типа ТН", "Участок", "Объект", MARKS_UPPER_CONTACTS,
            MARKS_LOWER_CONTACTS, AMOUNT_LAYERS, "Литология");

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* литология для которой вычисляются абсолютные отметки*/
    private String indexOfLithologyLayer;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* таблица с точками наблюдений */
    private PointsObservations pointsObservations;

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    private Set<String> nonVerticaleWells = new HashSet<>();

    /* текстовый файл для записи выходных данных в файл */
    private TextFileForMicromine outputFile;

    /* все точки, считанные из всех excel-файлов */
    private List<Map<String, String>> allPoints = new ArrayList<>();

    /*
     * переменная определяет нужно ли осуществлять процедуру объединения
     * стратиграфических пластов, имеющих одинаковые стратиграфические индексы,
     * и следующие при этом друг за другом. Такая потребность может возникнуть,
     * поскольку в базе ИСИХОГИ для одной ТН может быть много пластов, следующих
     * друг за другом, и при этом имеющих одинаковый индекс но различную литологию
     */
    private boolean processUnionSameIndexes;

    public ABSForLithologyLayer(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

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
        indexOfLithologyLayer = inputParameters[1];
        nameFile = indexOfLithologyLayer;
        processUnionSameIndexes =
                (inputParameters[2].equals("Объединять пласты")) ? true : false;
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
        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        outputFile = new TextFileForMicromine(workingCatalog, nameFile);
        outputFile.create();
        outputFile.writeTitle(requiredKeysForWrite);
        allPoints = deleteRepeatElementsInSubCollection(allPoints, "ID ТН");
        ScalingData scalingData = new WriteStratOrLithPoints(outputFile);
        WriteStratOrLithPoints forPoints = (WriteStratOrLithPoints) scalingData;
        forPoints.setNameOfStratOrLithSubject("Литология");
        forPoints.perform(allPoints, thresholdMiniList, 0, allPoints.size());

    }

    private void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (! pointsObservations.isTableDefaultFormatComplete()) {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
            return;
        }

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
                .filter(e -> hasExistKeys(e, requiredKeysForCalculate))
                .collect(Collectors.toList());

        validPoints.forEach(e -> e.keySet().retainAll(requiredKeysForCalculate));

        if (processUnionSameIndexes == true) {
            validPoints.forEach(e -> unionSameStratOrLithIndexes(e, "Литология"));
        }

        validPoints
                .forEach(e -> inputUpperAndLowerContacts(e, indexOfLithologyLayer, "Литология"));

        validPoints.forEach(e -> calculateAbsoluteMarksOfContacts(e));

        validPoints.forEach(this::amendment);
        validPoints.forEach(e -> e.put("Литология", indexOfLithologyLayer));

        validPoints.forEach(e -> e.keySet().retainAll(requiredKeysForWrite));

        List<Map<String, String>> validPointsForWrite = validPoints.stream()
                .filter(e -> e.size() == requiredKeysForWrite.size())
                .collect(Collectors.toList());
        /*
        validPointsForWrite.forEach(e -> {
            e.forEach((k, v) -> {
                System.out.print(k + ": " + v + "; ");
            }) ;
            System.out.println(" ");
            System.out.println("------------------");
        });
        */
        allPoints.addAll(validPointsForWrite);

    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (литостратиграфией). Если
     * какая-либо таблица с геолого-геофизическими данными пуста,
     * или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения + стратиграфия} или {Точки наблюдения} и т.п.
     */
    private List<Map<String, String>> joinTables() {
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
