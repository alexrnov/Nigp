package nigp.tasks.micromine.databasewells;

import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.GeophysicalLayers;
import nigp.tables.Gis;
import nigp.tables.LithoStratigraphy;
import nigp.tables.PointsObservations;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.amendment.*;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteIntervalsCommon;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Вычисляет соотношенияе по K, Th и U для геофизического пласта, указанного
 * во входных параметрах. Также вычисляется отношение медианного значения по K
 * к медианному значению по U. Соотношение к Торию не вычисляется, поскольку на
 * многих каротажных кривых Th стремится к нулю.
 * @author NovopashinAV
 */
public class OneGeophysicalLayerRatioKThU extends OneGeophysicalLayer {

    private final List<String> newKeysForFileIntervals =
            Arrays.asList(new String[] {"ID ТН", "Кровля геофизического пласта",
                    "Подошва геофизического пласта", "Мощность пласта", "K",
                    "Th", "U", "RatioKU"});

    private Coefficient coefficientForK = new AmendmentForK();
    private Coefficient coefficientForTh = new AmendmentForTh();
    private Coefficient coefficientForU = new AmendmentForU();

    private Gis gisForK;
    private Gis gisForTh;
    private Gis gisForU;

    public OneGeophysicalLayerRatioKThU(String[] inputParameters) throws TaskException {
        super(inputParameters);
    }

    /* Чтение параметров командной строки */
    @Override
    protected void readInputParameters() throws TaskException {
        if (inputParameters.length > 2) {
            directoryForSearchExcel = inputParameters[0];
            nameGeophysLayer = inputParameters[1];
            workingCatalog = inputParameters[2];
            geophysIntervals = "Интервал пласта";
            widthGeophysLayer = "Мощность пласта";
        } else {
            throw new TaskException("Incorrect input parameters");
        }
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

    /* провести вычисления для текущего excel-файла */
    @Override
    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (! pointsObservations.isTableDefaultFormatComplete()) {
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

        pointsForK.stream()
                .forEach(e -> e.keySet().retainAll(newKeysForFileIntervals));

        allIntervalWells.addAll(pointsForK);

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

        validPoints.forEach(this::getIntervalsForGeoplast);

        validPoints.forEach(this::getWidthGeophysicalLayer);
        validPoints.forEach(e -> getMeanGISforIntervals(e, geophysIntervals, nameMethodGIS));

        validPoints.forEach(e -> getABS(e, geophysIntervals));

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
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однострочный формат.
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

        geophysicalLayers = new GeophysicalLayers(excelSheets);
        geophysicalLayers.decode();
        geophysicalLayers.checkedForFoundData();
        geophysicalLayers.sortByIdAndUpperContact();
        geophysicalLayers.pointInfoToSingleLine();
    }

    /*
     * Добавить к массиву ТН для файла устьев текущие точки наблюдений
     */
    private void addCurrentWellsToTopWells(List<Map<String, String>> validPoints) {
        List<Map<String, String>> topWells = copyListWithSubMap(validPoints);
        topWells.stream().forEach(e -> e.keySet().retainAll(allRequiredKeys));//можно ли так модифицировать коллекцию?
        //оставить только устья скважин с данными по ТН и геофизическим пластам со статистическими показателями
        List<Map<String, String>> topWellsWithInfo = topWells.stream()
                .filter(e -> e.size() == fieldsWithDepthLayerAndStatistic)
                .collect(Collectors.toList());
        //оставить только те атрибуты, которые будут нужны для файла устьев в micromine
        topWellsWithInfo.stream().forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
        topWellsWithInfo.forEach(this::amendment);
        allTopWells.addAll(topWellsWithInfo);
    }

}
