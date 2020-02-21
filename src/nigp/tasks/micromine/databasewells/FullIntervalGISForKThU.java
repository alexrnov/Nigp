package nigp.tasks.micromine.databasewells;

import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.PointsObservations;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.amendment.AmendmentForK;
import nigp.tasks.micromine.amendment.AmendmentForTh;
import nigp.tasks.micromine.amendment.AmendmentForU;
import nigp.tasks.micromine.amendment.Coefficient;
import nigp.tasks.micromine.scaling.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Создает файл устьев (вертикальные скважины), файл событий (ГИС)
 * и файл интервалов (стратиграфия, литология) для Micromine.
 * Данные считываются по всему интервалу ствола скважины. Этот класс
 * отличается от FullIntervalGIS тем, что вносит поправку в данные
 * значений ГИС по K, Th, U, поскольку запись данных по этим методам ГИС имеет
 * различную размерность в БД "ИСИХОГИ". Используется паттерн СТРАТЕГИЯ.
 */
public class FullIntervalGISForKThU extends FullIntervalGIS {

    private Coefficient coefficient;

    public FullIntervalGISForKThU(String[] inputParameters) throws TaskException {
        super(inputParameters);

        if (nameMethodGIS.equals("K")) {
            coefficient = new AmendmentForK();
        } else if (nameMethodGIS.equals("Th")) {
            coefficient = new AmendmentForTh();
        } else if (nameMethodGIS.equals("U")) {
            coefficient = new AmendmentForU();
        } else {
            throw new TaskException("Incorrect name method GIS");
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

    @Override
    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets) throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();

            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();

            jointTable.forEach(e -> setChangeForAllGISValues(e, coefficient, nameMethodGIS));
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
}
