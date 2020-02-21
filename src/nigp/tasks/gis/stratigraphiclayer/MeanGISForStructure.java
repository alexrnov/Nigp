package nigp.tasks.gis.stratigraphiclayer;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.ReportFile;
import nigp.file.ShapeFileForPoints;
import nigp.tables.*;
import nigp.tasks.Stratigraphy;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;

import java.io.File;
import java.util.*;

/**
 * Вычисляет средние значения ГИС для стратиграфических подразделений(свит)
 * @author NovopashinAV
 *
 */

public class MeanGISForStructure extends Task implements nigp.tasks.MeanGIS, Stratigraphy, Micromine {

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* название метода ГИС, для которого производятся вычисления */
    private String nameMethodGIS;

    /*
     * стратиграфический индекс(или индексы) для которых вычисляются
     * принадлежность к перекрывающему или вмещающему комплексу пород
     */
    private String[] findStratigraphicIndexes;

    /*
     * индекс перекрывающих или вмещающих отложений для которых  вычисляются
     * статистические показатели
     */
    private String[] findStructureIndex = new String[1];

    /* путь к шейп-файлу, куда сохраняются результаты вычислений */
    private String filePath;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* текстовый файл для записи выходных данных */
    //protected TextFileForMapLayer outputFile;

    /* шейп-файл для записи выходных данных*/
    private ShapeFileForPoints shapeFile;

    /* файл для записи отчетных данных */
    protected ReportFile reportFile;

    private Gis gis; //таблица с данными измерений ГИС

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    /* количество всех точек наблюдений для записи в файл */
    private int pointsInAll = 0;

    private final String nameKeyOfFindstratIntervals = "Интервалы искомых пластов";

    /*
     * название ключа со средним арифметическим измерений ГИС
     * по геофизическому пласту
     */
    private final String arithmeticMeanGIS = "Среднее значений ГИС";

    private final String stratigraphicField = "Стратиграфия";

    /*
     * количество точек наблюдений с вычисленным значением ГИС для
     * выбранного геопласта с абсолютной мощностью
     */
    private int pointsWithCalcMeanGIS = 0;

    /*
     * коллекция для хранения информации по: 1. Названиям атрибутов в таблицах ИСИХОГИ.
     * 2. Названия атрибутов для записи в шейп-файл (на латинице);
     * 3. Тип параметра (необходимо при записи значений в shape-файл)
     */
    private List<List<String>> nameAndTypeKeys = new ArrayList<>();

    public MeanGISForStructure(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        nameAndTypeKeys.add(Arrays.asList("ID ТН", "id tn", "String"));
        nameAndTypeKeys.add(Arrays.asList("Линия", "line", "String"));
        nameAndTypeKeys.add(Arrays.asList("Точка наблюдений", "tn" ,"String"));
        nameAndTypeKeys.add(Arrays.asList("Объект", "object", "String"));
        nameAndTypeKeys.add(Arrays.asList("Участок", "station", "String"));
        nameAndTypeKeys.add(Arrays.asList("X факт.", "x", "String"));
        nameAndTypeKeys.add(Arrays.asList("Y факт.", "y", "String"));
        nameAndTypeKeys.add(Arrays.asList("Z", "z", "String"));
        nameAndTypeKeys.add(Arrays.asList("Глубина ТН", "depth tn", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Код типа ТН", "type", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния выработки", "state tn", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния документирования", "state doc", "String"));
        nameAndTypeKeys.add(Arrays.asList("Медиана значений ГИС", "median", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Среднее значений ГИС", "arithmetic", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Среднеквадратическое отклонение", "sko", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Ошибка среднего", "mean error", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Коэффициент вариации", "variation", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Минимальное значение ГИС", "min", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Максимальное значение ГИС", "max", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Сумма значений ГИС", "sum gis", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Количество измерений ГИС", "amount gis", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Количество пластов","amountLays","Integer"));
        /*
        nameAndTypeKeys.add(Arrays.asList("Литология", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Типа документирования", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код типа системы координат", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния ГИС", "String"));
        nameAndTypeKeys.add(Arrays.asList("Стратиграфия", "String"));
        nameAndTypeKeys.add(Arrays.asList("№п/п", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния опробования", "String"));
        nameAndTypeKeys.add(Arrays.asList("Гамма-Каротаж", "String"));
        nameAndTypeKeys.add(Arrays.asList("Интервалы искомых пластов", "String"));
        nameAndTypeKeys.add(Arrays.asList("Количество проб", "String"));
        nameAndTypeKeys.add(Arrays.asList("Литология", "String"));
        nameAndTypeKeys.add(Arrays.asList("Описание породы", "String"));
        nameAndTypeKeys.add(Arrays.asList("X проект.", "String"));
        nameAndTypeKeys.add(Arrays.asList("Y проект.", "String"));
        nameAndTypeKeys.add(Arrays.asList("X вычисляемое", "String"));
        nameAndTypeKeys.add(Arrays.asList("Проект_Факт", "String"));
        nameAndTypeKeys.add(Arrays.asList("Количество ГИС", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код Состояния ТН", "String"));
        nameAndTypeKeys.add(Arrays.asList("Y вычисляемое", "String"));
        nameAndTypeKeys.add(Arrays.asList("Код системы координат", "String"));
        nameAndTypeKeys.add(Arrays.asList("UIN", "String"));
        nameAndTypeKeys.add(Arrays.asList("Глубина ГИС", "depth gis", "String"));
        nameAndTypeKeys.add(Arrays.asList("Кровля стратопласта", "upper cont", "String"));
        nameAndTypeKeys.add(Arrays.asList("Подошва стратопласта", "lower cont", "String"));
        */
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        if (inputParameters.length > 2) {
            directoryForSearchExcel = inputParameters[0];
            nameMethodGIS = inputParameters[1];
            findStratigraphicIndexes = inputParameters[2].split(";");
            if (inputParameters[3].equals("Перекрывающие отложения")) {
                findStructureIndex[0] = "P";
            } else {
                findStructureIndex[0] = "V";
            }
            filePath = inputParameters[4];
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

        shapeFile = new ShapeFileForPoints(filePath);
        shapeFile.create(nameAndTypeKeys);
        shapeFile.setNameKeyForX("X факт.");
        shapeFile.setNameKeyForY("Y факт.");
        //outputFile = new TextFileForMapLayer(filePath);
        //outputFile.create();

        //reportFile = new ReportFile(filePath);
        //reportFile.create();

        for (File excelFile: excelFiles) {
            SheetsOfExcelFile excelSheets = new SheetsOfExcelFile(excelFile);
            if (excelSheets.isValidSheetsFound()) {
                processForCurrentExcel(excelSheets);
            } else {
                logger.fine("the required sheets of the Excel file aren't found");
            }
        }

        shapeFile.close();
        /*
        reportFile.write("Всего в файл записано " + pointsInAll
                + " точек наблюдений, " + "\n" + "из них - "
                + pointsWithCalcMeanGIS
                + " с вычисленным средним значением ГИС" + "\n"
                + "для стратиграфического пласта(пластов): " + Arrays.toString(findStratigraphicIndexes));

        long spentTime = (System.currentTimeMillis() - startTime)/1000;
        reportFile.write("Время вычислений: " + spentTime + " сек.");
        */
    }

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


        jointTable.forEach(e -> {
            if (e.containsKey("Стратиграфия") &&
                    e.containsKey("Кровля стратопласта") &&
                    e.containsKey("Подошва стратопласта")) {
                unionStratIndexesToStructure(e, findStratigraphicIndexes, "Стратиграфия");
                unionSameStratOrLithIndexes(e, "Стратиграфия");
            }
        });

        jointTable.forEach(e -> inputIntervalsForFindRocks(e, findStructureIndex,
                nameKeyOfFindstratIntervals));

        jointTable.forEach(e -> {
            int i = 0;
            if (e.get(nameKeyOfFindstratIntervals) != null) {
                String[] s = e.get(nameKeyOfFindstratIntervals).split("/");
                i = s.length;
            }
            e.put("Количество пластов", String.valueOf(i));
        });



        jointTable.forEach(e -> getMeanGISforIntervals(e, nameKeyOfFindstratIntervals, nameMethodGIS));

        jointTable.forEach(this::amendment);
        jointTable.forEach(e -> {
            String x = e.get("X факт.");
            e.put("X факт.", e.get("Y факт."));
            e.put("Y факт.", x);
        });

        shapeFile.write(jointTable);



        //outputFile.write(jointTable);
        //String report = getReportAndPrintToConsole(excelSheets, jointTable);
        //reportFile.write(report);
    }

    /*
     * Прочитать геолого-геофизические данные из таблиц
     * "Стратиграфия Литология", "Геофизический пласт", "ГИС". Расшифровать для
     * этих таблиц закодированные значения. Трансформировать таблицы в
     * однсотрочный формат.
     */
    protected void readAndTransformTables(SheetsOfExcelFile excelSheets) {

        lithoStratigraphy = new LithoStratigraphy(excelSheets);
        lithoStratigraphy.decode();
        lithoStratigraphy.checkedForFoundData();
        lithoStratigraphy.pointInfoToSingleLine();

        gis = new Gis(excelSheets, nameMethodGIS);
        gis.checkedForFoundData();
        gis.pointInfoToSingleLine();
    }

    /*
     * Объединить главную таблицу точек наблюдений и таблицы с
     * геолого-геофизическими данными (литостратиграфией, ГИС). Если
     * какая-либо таблица с геолого-геофизическими данными пуста,
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
            overlap(id, lithoStratigraphy).forEach(currentPoint::putIfAbsent);
            overlap(id, gis).forEach(currentPoint::putIfAbsent);

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

    protected String getReportAndPrintToConsole(SheetsOfExcelFile excelSheets,
                                                List<Map<String, String>> jointTable) {
        StringBuilder s = new StringBuilder();
        s.append(excelSheets.getNameOfFile());
        s.append("\n");

        int points = jointTable.size();
        s.append("Всего точек наблюдений: ");
        s.append(points);
        s.append(", из них: \n");

        s.append("с информацией по ГИС: ");
        s.append(countPoints(jointTable, nameMethodGIS));
        s.append("\n");

        s.append("С информацией по стратиграфии: ");
        s.append(countPoints(jointTable, stratigraphicField));
        s.append("\n");

        int countPointsGIS = countPoints(jointTable, arithmeticMeanGIS);
        s.append("с вычисленным средним ГИС по стратиграфическому подразделению: ");
        s.append(countPointsGIS);
        System.out.println(s);
        System.out.println();

        pointsInAll += points;
        pointsWithCalcMeanGIS += countPointsGIS;

        return s.toString();
    }

    private int countPoints(List<Map<String, String>> jointTable, String attr) {
        long count = jointTable.stream()
                .filter(p -> p.containsKey(attr))
                .count();
        return (int) count;
    }
}
