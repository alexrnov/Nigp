package nigp.tasks.gis.geophysicallayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nigp.excel.FoundExcelFiles;
import nigp.file.TextFileForMapLayer;
import nigp.tables.GeophysicalLayers;
import nigp.tables.LithoStratigraphy;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.ReportFile;
import nigp.tables.PointInfoToSingleLine;
import nigp.tables.PointsObservations;
import nigp.tasks.Task;
import nigp.tasks.TaskException;

/**
 * Находит номера геофизических пластов, которые выходят на
 * поверхность карбонатного цоколя
 * @author NovopashinAV
 */
public class GeoNumberOnSurfaceOfCarbonate extends Task {

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /*
     * рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* таблица с точками наблюдений */
    private PointsObservations pointsObservations;

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    /* таблица с данными геофизических пластов */
    private GeophysicalLayers geophysicalLayers;

    /* текстовый файл для записи выходных данных */
    private TextFileForMapLayer outputFile;

    /* файл для записи отчетных данных */
    private ReportFile reportFile;

    /* название ключа с ABS поверхости кабонатного цоколя */
    private final String surfaceOfCarbonateABS = "ABS поверхности карбонатного цоколя";

    /*
     * символ, присутствующий в стратиграфическом индексе
     * кимберлитовмещающих пород
     */
    private final String[] countryRobbing = {"D", "S", "O", "G", "AR", "R", "iPz2"};

    /*
     * название ключа с номером геопласта, выходящим на поверхность
     * карбонатного цоколя
     */
    private final String numberOfCarbonateGeoplast = "Номер геопласта поверхность цоколя";

    /*
     * название ключа с интервалом геопласта, выходящим на поверхность
     * карбонатного цоколя
     */
    private final String intervalOfCarbonateGeoplast =
            "Интервал геопласта поверхность цоколя";

    /* название ключа с номером первого геопласта */
    private final String numberOfFirstGeoplast = "Номер первого геопласта";

    /* название ключа с интервалом первого геопласта */
    private final String intervalOfFirstGeoplast = "Интервал первого геопласта";

    /* количество всех точек наблюдений для записи в файл */
    private int pointsInAll = 0;

    /*
     * количество точек наблюдений с номерами геопластов, выходящих
     * на поверхность карбонатного цоколя
     */
    private int pointsWithCalcNumber = 0;

    public GeoNumberOnSurfaceOfCarbonate(String[] inputParameters)
            throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        if (inputParameters.length > 0) {
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
        long startTime = System.currentTimeMillis();
        outputFile = new TextFileForMapLayer(workingCatalog);
        outputFile.create();

        reportFile = new ReportFile(workingCatalog);
        reportFile.create();

        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        reportFile.write("Всего в файл записано " + pointsInAll
                + " точек наблюдений, " + "\n" + "из них - "
                + pointsWithCalcNumber
                + " с номером геофизических пластов, выходящих на "
                + "поверхность карбонатного цоколя");

        long spentTime = (System.currentTimeMillis() - startTime)/1000;
        reportFile.write("Время вычислений: " + spentTime + " сек.");
    }

    /*
     * провести вычисления для текущего excel-файла
     */
    private void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();
            readAndTransformTables(excelSheets);
            List<Map<String, String>> jointTable = joinTables();
            jointTable.forEach(this::getSurfaceOfCarbonateABS);
            jointTable.forEach(this::getFirstAndCarbonateGeoplast);

            outputFile.write(jointTable);
            String report = getReportAndPrintToConsole(excelSheets, jointTable);
            reportFile.write(report);
        } else {
            logger.fine("Empty sheet of points observations. File is: "
                    + excelSheets.getNameOfFile());
        }
    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однсотрочный формат.
     */
    private void readAndTransformTables(SheetsOfExcelFile excelSheets) {

        lithoStratigraphy = new LithoStratigraphy(excelSheets);
        lithoStratigraphy.decode();
        lithoStratigraphy.checkedForFoundData();
        lithoStratigraphy.pointInfoToSingleLine();

        geophysicalLayers = new GeophysicalLayers(excelSheets);
        geophysicalLayers.decode();
        geophysicalLayers.checkedForFoundData();
        geophysicalLayers.sortByIdAndUpperContact();
        geophysicalLayers.pointInfoToSingleLine();
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (литостратиграфией, геофизическими
     * пластами). Если какя-либо таблица с геолого-геофизическими данными пуста,
     * или нет информации по конкретным точкам, то в таблицу точек наблюдений
     * эти данные быть добавлены не могут (добавляются пустые списки). Поэтому
     * отображение для каждой точки наблюдения может иметь переменный размер,
     * напр. {Точки наблюдения + стратиграфия} или {Точки наблюдения +
     * Геофизические пласты} или {Точки наблюдения} и т.п.
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
            overlap(id, geophysicalLayers).forEach(currentPoint::putIfAbsent);

            jointTable.add(currentPoint);
        });
        return jointTable;
    }

    /*
     * Если в таблице table с геолого-геофизическими данными найдена точка с
     * таким же идентификатором, как и в главной таблице с точками наблюдений,
     * вернуть геолого-геофизическую информацию по этой точке.
     */
    private Map<String, String> overlap(String id, PointInfoToSingleLine table) {
        for (Map<String, String> m : table.getTableSingleFormat()) {
            if (id.equals(m.get("ID ТН"))) {
                return m;
            }
        }
        return new HashMap<String, String>();
    }

    /*
     * Для текущей скважины метод ищет абсолютную отметку поверхности
     * карбонатного цоколя. Это значение соответствует абсолютной
     * отметке кровли первого кимберлитовмещающего пласта.
     * Если такой пласт найден, для текущей точки наблюдения
     * создается новый элемент отображения с ключем surfaceOfCarbonateABS
     */
    private void getSurfaceOfCarbonateABS(Map<String, String> point) {
        if (point.containsKey("Стратиграфия") == false
                || point.containsKey("Кровля стратопласта") == false) {
            return;
        }

        String[] stratigraphy = point.get("Стратиграфия").split("/");
        String[] upperContactStratigraphy = point.get("Кровля стратопласта").split("/");

        if (stratigraphy.length != upperContactStratigraphy.length) {
            return;
        }

        for (int i = 0; i < stratigraphy.length; i++) {
            if (isCountryRobbing(stratigraphy[i])) {
                point.put(surfaceOfCarbonateABS, upperContactStratigraphy[i]);
                return;
            }
        }
    }

    /*
     * Метод проверяет, относится ли порода с текущим стратиграфическим
     * индексом к кимберлитовмещающей толще
     */
    private boolean isCountryRobbing(String currentIndex) {
        for (String i: countryRobbing) {
            if (currentIndex.contains(i)) {
                return true;
            }
        }
        return false;
    }

    /*
     * В методе организован поиск геофизического пласта, выходящего
     * на поверхность карбонатного цоколя.
     * Если абсолютная отметка карбонатного цоколя расположена между
     * интервалами геофизического пласта, то этот пласт и будет
     * выходить на поверхность карбонатного цоколя; его номер и будет
     * записан в выходную таблицу
     */
    private void getFirstAndCarbonateGeoplast(Map<String, String> point) {
        if (point.containsKey("Геопласт") == false
                || point.containsKey("Кровля геопласта") == false
                || point.containsKey("Подошва геопласта") == false) {
            return;
        }

        String[] geoplast = point.get("Геопласт").split("/");
        String[] upperContacts = point.get("Кровля геопласта").split("/");
        String[] lowerContacts = point.get("Подошва геопласта").split("/");

        if (geoplast.length != upperContacts.length
                && upperContacts.length != lowerContacts.length) {
            return;
        }

        point.put(numberOfFirstGeoplast, geoplast[0]);
        point.put(intervalOfFirstGeoplast, upperContacts[0] + "-" + lowerContacts[0]);

        if (point.containsKey(surfaceOfCarbonateABS) == false) {
            return;
        }

        Double carbonateABS = Double.valueOf(point.get(surfaceOfCarbonateABS));
        for (int i = 0; i < upperContacts.length; i++) {
            Double upperContact = Double.valueOf(upperContacts[i]);
            Double lowerContact = Double.valueOf(lowerContacts[i]);
            if (carbonateABS >= upperContact && carbonateABS <= lowerContact) {
                point.put(numberOfCarbonateGeoplast, geoplast[i]);
                point.put(intervalOfCarbonateGeoplast, upperContact + "-" + lowerContact);
                return;
            }
        }
    }

    private String getReportAndPrintToConsole(SheetsOfExcelFile excelSheets,
                                              List<Map<String, String>> jointTable) {

        StringBuilder s = new StringBuilder();
        s.append(excelSheets.getNameOfFile());
        s.append("\n");

        int points = jointTable.size();
        s.append("Всего точек наблюдений: ");
        s.append(points);
        s.append(", из них: \n");

        s.append("с данными abs по кровле карбонатного цоколя: ");
        s.append(countPoints(jointTable, surfaceOfCarbonateABS));
        s.append("\n");

        s.append("с информацией по геофизическим пластам: ");
        s.append(countPoints(jointTable, "Геопласт"));
        s.append("\n");

        int countPointsWithNumber = countPoints(jointTable, numberOfCarbonateGeoplast);
        s.append("с номерами геопластов: ");
        s.append(countPointsWithNumber);
        System.out.println(s);
        System.out.println();

        pointsInAll += points;
        pointsWithCalcNumber += countPointsWithNumber;

        return s.toString();
    }

    private int countPoints(List<Map<String, String>> jointTable, String attr) {
        long count = jointTable.stream().
                filter(p -> p.containsKey(attr))
                .count();
        return (int) count;
    }
}
