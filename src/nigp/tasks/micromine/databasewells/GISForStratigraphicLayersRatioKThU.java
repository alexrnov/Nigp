package nigp.tasks.micromine.databasewells;

import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.MeanGIS;
import nigp.tasks.Stratigraphy;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.amendment.AmendmentForK;
import nigp.tasks.micromine.amendment.AmendmentForTh;
import nigp.tasks.micromine.amendment.AmendmentForU;
import nigp.tasks.micromine.amendment.Coefficient;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteIntervalsCommon;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Вычисляет соотношенияе по K, Th и U для стратиграфического
 * подразделения(подразделений), указанного во входных параметрах.
 * Также вычисляется отношение медианного значения по K к медианному
 * значению по U. Соотношение к Торию не вычисляется, поскольку на
 * многих каротажных кривых Th стремится к нулю.
 * @author NovopashinAV
 */
public class GISForStratigraphicLayersRatioKThU extends GISForStratigraphicLayers implements Micromine, TablesAction,
        MeanGIS, Stratigraphy {

    private final String ID = "ID ТН";
    private final String NAME_OF_MEAN_VALUE = "Медиана значений ГИС";
    private final String NOT_VALUE = "notValue";
    private final List<String> newKeysForFileIntervals =
            Arrays.asList("ID ТН", "Кровля искомых отложений",
                    "Подошва искомых отложений", "K", "Th", "U", "RatioKU");

    /*
     * Текстовый файл для записи выходных данных в файл интервалов
     * (для стратиграфических подразделений)
     */
    private TextFileForMicromine outputFileIntervalsWells;

    private Coefficient coefficientForK = new AmendmentForK();
    private Coefficient coefficientForTh = new AmendmentForTh();
    private Coefficient coefficientForU = new AmendmentForU();

    private Gis gisForK;
    private Gis gisForTh;
    private Gis gisForU;

    public GISForStratigraphicLayersRatioKThU(String[] inputParameters) throws TaskException {
        super(inputParameters);
        requiredKeysTopWell = Arrays.asList("ID ТН", "UIN", "X факт.",
                "Y факт.", "Z", "Глубина ТН", "Код типа ТН");
        allRequiredKeys = Arrays.asList("ID ТН", "UIN",
                "X факт.", "Y факт.", "Z", "Глубина ТН", "Код типа ТН", "Кровля искомых отложений",
                "Подошва искомых отложений", "Медиана значений ГИС", "Среднее значений ГИС",
                "Коэффициент вариации", "Ошибка среднего", "Среднеквадратическое отклонение",
                "Максимальное значение ГИС", "Минимальное значение ГИС");
        requiredKeysIntervalWell = Arrays.asList("ID ТН", "Кровля искомых отложений",
                "Подошва искомых отложений", "Медиана значений ГИС", "Среднее значений ГИС",
                "Коэффициент вариации", "Ошибка среднего", "Среднеквадратическое отклонение",
                "Максимальное значение ГИС", "Минимальное значение ГИС");
    }

    /* Чтение параметров командной строки */
    @Override
    protected void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        countryRobbing = inputParameters[1].split(";");
        workingCatalog = inputParameters[2];
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
        outputFileIntervalsWells.writeTitle(newKeysForFileIntervals);


        allIntervalWells = deleteRepeatElementsInSubCollection(allIntervalWells, "ID ТН");

        ScalingData forGeophysicLayer =
                new WriteIntervalsCommon(outputFileIntervalsWells);

        forGeophysicLayer.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());

    }

    @Override
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

        List<Map<String, String>> pointsForK = getPointsForElement("K");
        List<Map<String, String>> pointsForTh = getPointsForElement("Th");
        List<Map<String, String>> pointsForU = getPointsForElement("U");

        pointsForK = deleteRepeatElementsInSubCollection(pointsForK, ID);
        pointsForTh = deleteRepeatElementsInSubCollection(pointsForTh, ID);
        pointsForU = deleteRepeatElementsInSubCollection(pointsForU, ID);

        arrDataOfRatioKThU(pointsForK, pointsForTh, pointsForU);

        allIntervalWells.addAll(pointsForK);
    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однсотрочный формат.
     */
    @Override
    protected void readAndTransformTables(SheetsOfExcelFile excelSheets) {

        lithoStratigraphy = new LithoStratigraphy(excelSheets);
        lithoStratigraphy.decode();
        lithoStratigraphy.checkedForFoundData();
        lithoStratigraphy.pointInfoToSingleLine();

        gisForK = new Gis(excelSheets, "K");
        gisForK.checkedForFoundData();
        gisForK.pointInfoToSingleLine();

        gisForTh = new Gis(excelSheets, "Th");
        gisForTh.checkedForFoundData();
        gisForTh.pointInfoToSingleLine();

        gisForU = new Gis(excelSheets, "U");
        gisForU.checkedForFoundData();
        gisForU.pointInfoToSingleLine();
    }

    private List<Map<String, String>> getPointsForElement(String currentMethod) {
        Coefficient coefficient;
        if (currentMethod.equals("K")) {
            gis = gisForK;
            coefficient = coefficientForK;
        } else if (currentMethod.equals("Th")) {
            gis = gisForTh;
            coefficient = coefficientForTh;
        } else {
            gis = gisForU;
            coefficient = coefficientForU;
        }
        nameMethodGIS = currentMethod;

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
         * устьев с реальной глубиной устьев создается в классе
         * TopWells
         */
        validPoints.stream().forEach(e -> {
            e.replace("Глубина ТН", "1000");
        });

        validPoints.forEach(e -> setChangeForAllGISValues(e, coefficient, nameMethodGIS));
        validPoints.forEach(e -> inputIntervalsForFindRocks(e, countryRobbing, intervalsOfFindRocks));
        validPoints.forEach(e -> getMeanGISforIntervals(e, intervalsOfFindRocks, nameMethodGIS));
        validPoints.forEach(e -> getABS(e, "Интервалы искомых отложений"));

        /*
         * на три массива (K, Th, U) будет один массив устьев
         * по K (калий выбран произвольно)
         */
        if (currentMethod.equals("K")) {
            addCurrentWellsToTopWells(validPoints);
        }

        validPoints.stream().forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));
        List<Map<String, String>> validPointsNotEmpty =
                validPoints.stream()
                        .filter(e -> e.size() == requiredKeysIntervalWell.size())
                        .collect(Collectors.toList());

        return validPointsNotEmpty;
    }

    /*
     * Добавить к массиву ТН для файла устьев текущие точки наблюдений
     */
    private void addCurrentWellsToTopWells(List<Map<String, String>> validPoints) {
        List<Map<String, String>> topWells = copyListWithSubMap(validPoints);
        topWells.stream().forEach(e -> e.keySet().retainAll(allRequiredKeys));//можно ли так модифицировать коллекцию?
        //оставить только устья скважин с данными по ТН и страт. пластам со статистическими показателями
        List<Map<String, String>> topWellsWithInfo = topWells.stream()
                .filter(e -> e.size() == allRequiredKeys.size())
                .collect(Collectors.toList());

        //оставить только те атрибуты, которые будут нужны для файла устьев в micromine
        topWellsWithInfo.stream().forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
        topWellsWithInfo.forEach(this::amendment);
        allTopWells.addAll(topWellsWithInfo);
    }

}
