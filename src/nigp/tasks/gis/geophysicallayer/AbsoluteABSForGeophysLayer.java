package nigp.tasks.gis.geophysicallayer;

import nigp.excel.SheetsOfExcelFile;
import nigp.tables.PointsObservations;
import nigp.tasks.TaskException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AbsoluteABSForGeophysLayer extends WidthAndMeanGISForGeophysLayer {

    public AbsoluteABSForGeophysLayer(String[] inputParameters) throws TaskException {
        super(inputParameters);
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
            jointTable.forEach(this::getIntervalsForGeoplast);
            jointTable.forEach(this::getWidthGeophysicalLayer);
            jointTable.forEach((e -> getMeanGISforIntervals(e, geophysIntervals, nameMethodGIS)));


            jointTable.forEach(this::getAbsoluteABS);


            outputFile.write(jointTable);
            String report = getReportAndPrintToConsole(excelSheets, jointTable);
            reportFile.write(report);
        } else {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
        }
    }

    private void getAbsoluteABS(Map<String, String> point) {
        if (point.get("Z").equals("-999999.0") || point.get("Z").equals("")
                || point.containsKey("Интервал пласта") == false) {
            return;
        }
        String[] allContacts = point.get("Интервал пласта").split("/");
        BigDecimal upperContact;
        BigDecimal lowerContact;
        if (allContacts.length == 1) {
            String[] contacts = allContacts[0].split("-");
            upperContact = new BigDecimal(contacts[0]);
            lowerContact = new BigDecimal(contacts[1]);
        } else if (allContacts.length > 1) {
            String[] firstContacts = allContacts[0].split("-");
            String[] lastContacts = allContacts[allContacts.length - 1].split("-");
            upperContact = new BigDecimal(firstContacts[0]);
            lowerContact = new BigDecimal(lastContacts[1]);
        } else {
            return;
        }

        BigDecimal z = new BigDecimal(point.get("Z"));
        upperContact = z.subtract(upperContact);
        lowerContact = z.subtract(lowerContact);

        System.out.println(upperContact.toString() + " " + lowerContact.toString());

        point.put("abs кровли", upperContact.toString());
        point.put("abs подошвы", lowerContact.toString());
    }
}
