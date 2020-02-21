package nigp.tasks.micromine.points;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.Gis;
import nigp.tables.LithoStratigraphy;
import nigp.tables.PointsObservations;
import nigp.tables.TablesAction;
import nigp.tasks.MeanGIS;
import nigp.tasks.Stratigraphy;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteStructurePointsWithGIS;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Экземпляр класса реализует задачу вычисления абсолютных отметок для кровли
 * и подошвы по рассматриваемой структуре(перекрывающему комплексу, коре,
 * вмещающим отложениям). Вычисляются также статистические параметры по
 * указанному методу ГИС. Для этих статистических параметров вычисляется своя
 * абсолютная отметка, которая находится по середине между кровлей и подошвой.
 * Эта отметка является условной и нужна для построения блочной модели.
 * Переменная indexOfStratigraphicLayer содержит массив строк, каждая из которых входит в
 * тратиграфический индекс определенного типа или равна ему.
 * переменная должна иметь формат типа "Q,N,J;T2-3;O1,G", где через точку с запятой
 * перечисляются символы, которые входят в стратиграфический индекс, отнесенный
 * к определенной структуре (перекрывающие отложения, кора выветривания,
 * вмещающие отложения и т. п.). Если для определенной структуры указаны несколько
 * символов (т.е. стратиграфических подразделений), то они перечисляются через
 * запятую
 */
public class ABSAndMeanGISForBlockModel extends Task implements TablesAction,
        Micromine, Stratigraphy, MeanGIS {

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    private final int thresholdMiniList = 500;

    /* название файла точек */
    private final String nameFile = "points";

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    private List<Map<String, String>> allTopWells = new ArrayList<>();

    /* текстовый файл для записи выходных данных в файл устьев*/
    protected TextFileForMicromine outputFileTopWells;

    /* название файла устьев скважин */
    private final String nameFileTopWells = "top wells";

    /*
    * название ключа для значений абсолютных отметок рельефа осредненных
    * значений ГИС (отметка распологается между кровлей и подошвой искомой структуры)
    */
    private final String keyNameOfABSForGISValue = "Отметки ГИС";

    /* названия ключей, которые нужны для произведения вычислений */
    private final List<String> requiredKeysForCalculate = Arrays.asList("ID ТН",
            "UIN", "X факт.", "Y факт.", "Z", "Стратиграфия", "Кровля стратопласта",
            "Подошва стратопласта", "Код Типа документирования", "Код типа ТН",
            "Участок", "Объект");

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    private final List<String> requiredKeysTopWell = Arrays.asList("ID ТН", "UIN",
            "X факт.", "Y факт.", "Z", "Глубина ТН", "Код Типа документирования",
            "Код типа ТН", "Участок", "Объект", "Метод ГИС");

    /* названия ключей, которые нужны для записи в выходной текстовый файл */
    private final List<String> requiredKeysForWrite = Arrays.asList("ID ТН",
            "UIN", "X факт.", "Y факт.", "Z", "Глубина ТН", "Код Типа документирования",
            "Код типа ТН", "Участок", "Объект", MARKS_UPPER_CONTACTS, MARKS_LOWER_CONTACTS,
            "Среднее значений ГИС", "Медиана значений ГИС", "Коэффициент вариации",
            "Максимальное значение ГИС", "Минимальное значение ГИС",
            "Среднеквадратическое отклонение", "Ошибка среднего", keyNameOfABSForGISValue,
            "Метод ГИС");

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* стратиграфический индекс для которого вычисляются абсолютные отметки*/
    private String indexOfStratigraphicLayer;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* название метода ГИС, для которого производятся вычисления */
    private String nameMethodGIS;

    /* таблица с точками наблюдений */
    private PointsObservations pointsObservations;

    /* таблица с данными литостратиграфии */
    private LithoStratigraphy lithoStratigraphy;

    private Gis gis; //таблица с данными измерений ГИС

    private final String nameKeyOfFindstratIntervals = "Интервалы искомых пластов";

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    private Set<String> nonVerticaleWells = new HashSet<>();

    /* текстовый файл для записи выходных данных в файл */
    private TextFileForMicromine outputFile;

    /* все точки, считанные из всех excel-файлов */
    private List<Map<String, String>> allPoints = new ArrayList<>();

    /* массив строк, каждая из которых входит в стратиграфический индекс
     * определенного типа или равна ему.
     */
    private String[] stratigraphicUnits;

    /*
    * переменная определяет выводить ли точки с пустыми значениями ГИС.
    * Если такие точки выводить, то общее количество точек предположительно
    * будет больше. Такой вариант лучше подходит для построения поверхностей
    * структуры в Micromine. Такие точки лучше не выводить, если нужно получить
    * только осредненные значения для блочного моделирования, чтобы потом не
    * отфильтровывать пустые(-1.0) значения в Micromine.
    */
    private boolean readPointsWithEmptyGISValue;
    //private List indexJ = new ArrayList<>();

    public ABSAndMeanGISForBlockModel(String[] inputParameters) throws TaskException {
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
        nameMethodGIS = inputParameters[1];
        indexOfStratigraphicLayer = inputParameters[2];
        stratigraphicUnits = inputParameters[3].split(";");
        readPointsWithEmptyGISValue = (inputParameters[4].equals(
                "не записывать точки с пустыми значениями ГИС")) ? false : true;
        workingCatalog = inputParameters[5];
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


        outputFile = new TextFileForMicromine(workingCatalog, nameFile);
        outputFile.create();
        outputFile.writeTitle(requiredKeysForWrite);
        allPoints = deleteRepeatElementsInSubCollection(allPoints, "ID ТН");
        ScalingData forGIS = new WriteStructurePointsWithGIS(outputFile);
        forGIS.perform(allPoints, thresholdMiniList, 0, allPoints.size());

        allTopWells = deleteRepeatElementsInSubCollection(allTopWells, "ID ТН");
        outputFileTopWells.write(allTopWells);

        /*
        indexJ.forEach(e -> {
            System.out.print("[" + e + "] ");
        });
        */
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
        readAndTransformTables(excelSheets);

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

        validPoints.forEach(e -> {
            if (e.get("Глубина ТН").equals("-999999.0") ||
                    e.get("Глубина ТН").equals("-999.75") ||
                    e.get("Глубина ТН").equals("-995.75")) {
                System.out.println(e.get("Глубина ТН"));
                e.put("Глубина ТН", "1000");
            }
        });

        /*



        validPoints.forEach(e -> {
           e.forEach((k, v) -> {
              if (e.get("Стратиграфия") != null) {
                  String[] s = e.get("Стратиграфия").split("/");
                  for (String el : s) {
                      if (!indexJ.contains(el)) {
                          indexJ.add(el);
                      }
                  }
              }
           });
        });
        */
        validPoints.forEach(e -> renameStratIndexesToUnionAndAbbreviate(e, stratigraphicUnits,
                                                            "Стратиграфия"));

        StringBuilder structure = new StringBuilder();
        for(int i = 0; i < stratigraphicUnits.length; i++) {
            structure.append(i);
            structure.append("/");
        }

        validPoints.forEach(e -> transformSomeWellsToCorrectStructure(e, structure.toString()));

        List<Map<String, String>> pointsWithTypeStructure = validPoints.stream()
                .filter(e -> {
                    if (e.get("Стратиграфия").equals(structure.toString())) {
                        return true;
                    }
                    return false;

                }).collect(Collectors.toList());

        pointsWithTypeStructure
                .forEach(e -> inputUpperAndLowerContacts(e, indexOfStratigraphicLayer, "Стратиграфия"));

        String[] findIndex = {indexOfStratigraphicLayer};
        pointsWithTypeStructure.forEach(e -> inputIntervalsForFindRocks(e, findIndex,
                nameKeyOfFindstratIntervals));

        pointsWithTypeStructure.forEach(e -> getMeanGISforIntervals(e, nameKeyOfFindstratIntervals, nameMethodGIS));
        pointsWithTypeStructure.forEach(e -> calculateAbsoluteMarksOfContacts(e));
        pointsWithTypeStructure.forEach(this::amendment);
        pointsWithTypeStructure.forEach(e -> calcABSforMeanGISValueBetweenContacts(e, keyNameOfABSForGISValue));
        pointsWithTypeStructure.forEach(e -> e.keySet().retainAll(requiredKeysForWrite));

        pointsWithTypeStructure.forEach(e -> {
            e.put("Метод ГИС", nameMethodGIS);
        });

        if (readPointsWithEmptyGISValue) {
            pointsWithTypeStructure.forEach(e -> {
                requiredKeysForWrite.forEach(keyName -> {
                    if (e.get(keyName) == null) {
                        e.put(keyName, "-1.0");
                    }
                });
            });
            allPoints.addAll(pointsWithTypeStructure);
            allTopWells.addAll(pointsWithTypeStructure);
        } else {
            List<Map<String, String>> pointsWithTypeStructureWithoutEmptyGIS =
                    pointsWithTypeStructure.stream()
                    .filter(e -> e.size() == requiredKeysForWrite.size())
                    .collect(Collectors.toList());

            allPoints.addAll(pointsWithTypeStructureWithoutEmptyGIS);
            allTopWells.addAll(pointsWithTypeStructureWithoutEmptyGIS);
            /*
            allPoints.forEach(e -> {
                e.forEach((k, v) -> {
                    System.out.print(k + ":" + v + ";");
                });
                System.out.println("------------------");
            });
            */
        }
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
            overlap(id, gis).forEach(currentPoint::putIfAbsent);

            jointTable.add(currentPoint);
        });
        return jointTable;
    }
}
