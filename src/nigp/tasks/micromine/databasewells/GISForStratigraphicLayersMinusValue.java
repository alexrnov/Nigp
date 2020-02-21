package nigp.tasks.micromine.databasewells;

import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.PointsObservations;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteGIS;
import nigp.tasks.micromine.scaling.WriteLithoStratigraphy;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cоздает файл устьев(вертикальные скважины), файл событий(ГИС)
 * и файл интервалов (стратиграфия, литология) для Micromine. Данные считываются
 * по стратиграфическим пластам, указанным во входящих параметрах. Данные по ГИС
 * пересчитываются следующим образом: по оцениваемой толще берется коэффициент
 * (ГИС, медиана, макс, мин и т.п) затем из каждого значения вдоль толщи,
 * этот коэффициент вычитается. Такая необходимость может возникнуть при
 * выводе данных скважинной магниторазведки, поскольку при постановке этого
 * метода не учитывались вариации магнитного поля
 */
public class GISForStratigraphicLayersMinusValue extends GISForStratigraphicLayers {

    private String typeOfMinusValue = "отнять медиану"; //выставлен по умолчанию

    public GISForStratigraphicLayersMinusValue(String[] inputParameters) throws TaskException {
        super(inputParameters);
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

    @Override
    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();

            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();


            jointTable.forEach(e -> minusValue(e, nameMethodGIS, typeOfMinusValue));


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



            List<Map<String, String>> gisWellsForWrite = copyListWithSubMap(gisWells);
            gisWellsForWrite.stream().forEach(e -> e.keySet().retainAll(requiredKeysEventWell));
            List<Map<String, String>> gisWellsExistData = gisWellsForWrite.stream()
                    .filter(e -> e.size() == requiredKeysEventWell.size()) // оставить ТН с тремя полями: id, value, depth
                    .collect(Collectors.toList());

            gisWellsExistData.forEach(e -> deleteRepeatValues(e, nameMethodGIS));
            System.out.print(gisWellsExistData.size() + " ");
            allEventWells.addAll(gisWellsExistData);







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
            /*
            gisWells.stream().forEach(e -> e.keySet().retainAll(requiredKeysEventWell));
            List<Map<String, String>> gisWellsExistData = gisWells.stream()
                    .filter(e -> e.size() == requiredKeysEventWell.size()) // оставить ТН с тремя полями: id, value, depth
                    .collect(Collectors.toList());

            gisWells.forEach(e -> deleteRepeatValues(e, nameMethodGIS));

            allEventWells.addAll(gisWellsExistData);
            */

        }
    }


    /**
     * Устанавливает тип коэффициента, который вычитается из всех значений ГИС
     * @param typeOfMinusValue тип коэффициента
     */
    public void setTypeOfSubtractValue(String typeOfMinusValue) {
        this.typeOfMinusValue = typeOfMinusValue;
    }



}
