package nigp.tasks.micromine.databasewells;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import nigp.excel.FoundExcelFiles;
import nigp.file.TextFileForMapLayer;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.TaskException;
import nigp.excel.SheetsOfExcelFile;
import nigp.tasks.MeanGIS;
import nigp.tasks.Task;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteImplicitGeophysicLayers;

/**
 * Вычисляет интервалы для геофизического пласта, заданного во входных
 * параметрах. Для каждого геофизического пласта создается два условных
 * пласта - один сверху, другой снизу. Данная задача необходима для
 * инструмента "условное моделирование" в micromine.
 *
 * @author NovopashinAV
 */
public class OneGeophysicalLayerImplicit extends Task
        implements MeanGIS, Micromine, TablesAction {

    /* название файла устьев скважин */
    protected final String nameFileTopWells = "top wells";

    /* название файла интервалов скважин */
    protected final String nameFileIntervalsWells = "interval wells";

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    protected final int thresholdMiniList = 100;

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    protected List<Map<String, String>> allTopWells = new ArrayList<>();

    /* каталог с excel-файлами */
    protected String directoryForSearchExcel;

    /* название геофизического пласта */
    protected String nameGeophysLayer;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    protected String workingCatalog;

    /*
     * название ключа интервалов геофизического пласта/пластов для
     * вставки в отображение объединенной таблицы
     */
    private final String geophysIntervals = "Интервал пласта";

    /*
     * название ключа с мощностью геофизического пласта/пластов
     * для вставки в отображение объединенной таблицы
     */
    private final String widthGeophysLayer = "Мощность пласта";

    /* название ключа с суммой измерений ГИС по геофизическому пласту */
    private final String sumValuesGIS = "Сумма значений ГИС";

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    protected List<File> excelFiles;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* таблица с данными геофизических пластов */
    protected GeophysicalLayers geophysicalLayers;

    /* текстовый файл для записи выходных данных в файл устьев */
    protected TextFileForMicromine outputFileTopWells;

    /* текстовый файл для записи выходных данных в файл интервалов */
    protected TextFileForMapLayer outputFileIntervalWells;

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    protected Set<String> nonVerticaleWells = new HashSet<>();

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    protected List<String> requiredKeysTopWell = new ArrayList<>();

    /*
     * список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле интервалов(для геофизических пластов)
     * для micromine. Этот список также используется для отсева ненужных
     * полей в объединенной таблице
     */
    protected List<String> requiredKeysIntervalWell = new ArrayList<>();

    /*
     * Все точки, считанные из всех excel-файлов для файла интервалов
     * (мощность геофизического пласта, статистические параметры)
     * для Micromine
     */
    protected List<Map<String, String>> allIntervalWells = new ArrayList<>();

    /*
     * Текстовый файл для записи выходных данных в файл интервалов
     * (для геофизических пластов)
     */
    protected TextFileForMicromine outputFileIntervalsWells;

    /* список содержит названия ключей, которые необходимы для отсева
     * скважин, не имеющих достаточной информации. (используется при
     * создании файла устьев, чтобы не записывать скважины без данных)
     */
    protected List<String> allRequiredKeys = new ArrayList<>();

    public OneGeophysicalLayerImplicit(String[] inputParameters)
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
        requiredKeysIntervalWell.add("Кровли пластов");
        requiredKeysIntervalWell.add("Подошвы пластов");
        requiredKeysIntervalWell.add("Геопласты");

        allRequiredKeys.addAll(requiredKeysTopWell);
        allRequiredKeys.addAll(requiredKeysIntervalWell);
        allRequiredKeys.remove("ID ТН");
    }

    /* Чтение параметров командной строки */
    protected void readInputParameters() throws TaskException {
        if (inputParameters.length > 2) {
            directoryForSearchExcel = inputParameters[0];
            nameGeophysLayer = inputParameters[1];
            workingCatalog = inputParameters[2];
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
                new WriteImplicitGeophysicLayers(outputFileIntervalsWells);

        forGeophysicLayer.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());
    }

    /*
     * провести вычисления для текущего excel-файла
     */
    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (!pointsObservations.isTableDefaultFormatComplete()) {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
            return;
        }
        pointsObservations.decode();
        pointsObservations.checkedForFoundData();
        readAndTransformTables(excelSheets);
        List<Map<String, String>> jointTable = joinTables();
        jointTable.forEach(this::getIntervalsForGeoplast);
        jointTable.forEach(this::getWidthGeophysicalLayer);
        jointTable.forEach(e -> getABS(e, ""));

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
         * классе
         */
        validPoints.stream().forEach(e -> {
            e.replace("Глубина ТН", "1000");
        });

        validPoints.forEach(this::addImplicitIntervals);

        validPoints.stream().forEach(e -> e.keySet().retainAll(allRequiredKeys));//можно ли так модифицировать коллекцию?
        List<Map<String, String>> validPointsWithData = validPoints.stream()
                .filter(e -> e.size() == allRequiredKeys.size())
                .collect(Collectors.toList());

        List<Map<String, String>> topWells = copyListWithSubMap(validPointsWithData);
        topWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
        topWells.forEach(this::amendment);
        allTopWells.addAll(topWells);


        List<Map<String, String>> intervalWells = copyListWithSubMap(validPointsWithData);
        intervalWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));
        allIntervalWells.addAll(intervalWells);

    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
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
            overlap(id, geophysicalLayers).forEach(currentPoint::putIfAbsent);

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

    /*
     * Для текущей скважины метод ищет геопласт, указанный во входных параметрах
     * Если пласт/пласты найдены, для текущей точки наблюдения
     * создается новый элемент отображения с ключем geophysIntervals и
     * значением intervals.toString()
     */
    protected void getIntervalsForGeoplast(Map<String, String> point) {
        if (point.containsKey("Геопласт") == false
                || point.containsKey("Кровля геопласта") == false) {
            return;
        }

        String[] geoLayers = point.get("Геопласт").split("/");

        String[] upperContact = point.get("Кровля геопласта").split("/");
        String[] lowerContact = point.get("Подошва геопласта").split("/");

        StringBuilder intervals = getIntervals(geoLayers, upperContact, lowerContact);

        if (intervals.length() != 0) {
            point.put(geophysIntervals, intervals.toString());
        }
    }

    /*
     * просмотреть все пласты на предмет совпадения
     * с искомым геофизическим пластом, если такой пласт/пласты найдены -
     * сформировать строку с интервалами пласта/пластов.
     */
    private StringBuilder getIntervals(String[] geoLayers, String[] upperContact,
                                       String[] lowerContact) {
        StringBuilder intervals = new StringBuilder();
        if (upperContact.length != geoLayers.length
                || lowerContact.length != geoLayers.length) {
            return intervals;
        }
        for (int i = 0; i < geoLayers.length; i++) {
            if (geoLayers[i].equals(nameGeophysLayer)) {
                intervals.append(upperContact[i]);
                intervals.append("-");
                intervals.append(lowerContact[i]);
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
     * Для текщей точки, добовляет к геофизическому пласту два условных геофизических
     * пласта - один сверху, другой снизу
     */
    private void addImplicitIntervals(Map<String, String> point) {
        if (!point.containsKey(geophysIntervals) || !point.containsKey(widthGeophysLayer)) {
            return;
        }
        String[] intervalOfLayer = point.get(geophysIntervals).split("/");
        String widthLayer = point.get(widthGeophysLayer);

        String upperContactOfLayer;
        String lowerContactOfLayer;
        if (intervalOfLayer.length == 1) {
            String[] contacts = intervalOfLayer[0].split("-");
            upperContactOfLayer = contacts[0];
            lowerContactOfLayer = contacts[1];
        } else if (intervalOfLayer.length > 1) {
            String[] firstContacts = intervalOfLayer[0].split("-");
            String[] lastContacts = intervalOfLayer[intervalOfLayer.length - 1].split("-");
            upperContactOfLayer = firstContacts[0];
            lowerContactOfLayer = lastContacts[1];
        } else {
            return;
        }

        Double upperContactFirstImplicitLayer;
        Double lowerContactSecondImplicitLayer;

        upperContactFirstImplicitLayer = Double.valueOf(upperContactOfLayer) - (Double.valueOf(widthLayer)/2);
        upperContactFirstImplicitLayer = Math.round(upperContactFirstImplicitLayer * 100.0) / 100.0;

        lowerContactSecondImplicitLayer = Double.valueOf(lowerContactOfLayer) + (Double.valueOf(widthLayer)/2);
        lowerContactSecondImplicitLayer = Math.round(lowerContactSecondImplicitLayer * 100.0) / 100.0;

        StringBuilder upperContacts = new StringBuilder();
        StringBuilder lowerContacts = new StringBuilder();

        upperContacts.append(upperContactFirstImplicitLayer);
        upperContacts.append("/");
        upperContacts.append(upperContactOfLayer);
        upperContacts.append("/");
        upperContacts.append(lowerContactOfLayer);
        upperContacts.append("/");

        lowerContacts.append(upperContactOfLayer);
        lowerContacts.append("/");
        lowerContacts.append(lowerContactOfLayer);
        lowerContacts.append("/");
        lowerContacts.append(lowerContactSecondImplicitLayer);
        lowerContacts.append("/");

        point.put("Кровли пластов", upperContacts.toString());
        point.put("Подошвы пластов", lowerContacts.toString());
        point.put("Геопласты", "b/a/b/");
    }
}
