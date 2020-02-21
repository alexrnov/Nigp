package nigp.tasks.micromine.mcaOfAllObjects;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetOfExcelFileMineralWeb;
import nigp.file.ShapeFileForPoints;
import nigp.file.TextFileForMicromine;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;
import nigp.tasks.Mineralogy;
import nigp.tasks.micromine.scaling.ScalingData;
import nigp.tasks.micromine.scaling.WriteIntervalsCommon;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static nigp.tables.MineralogyAuxiliary.indexAndNameOfColumns;

/**
 * Создает точки по шлихоминералогическим пробам, отнесенным к какому
 * либо стратиграфическому подразделению. Точки создаются таким образом, чтобы для
 * конкретно взятого интервала было несколько точек
 */
public class mineralogyPointsForStratigraphicLayer extends Task implements Micromine, Mineralogy {

    /* названия атрибутов, которые нужны для создания файла устьев скважин */
    private final List<String> requiredKeysTopWell = Arrays.asList("ID", "Линия",
            "Точка", "X", "Y", "Z", "От", "До", "Стратиграфия", "param");

    /* это значение присваивается координате Z если информация по высоте отсутствует */
    private final String defaultZ = "400.0";

    private String typeOfStratigraphic;

    /*
     * коллекция для хранения информации по: 1. Названиям атрибутов
     * в таблицах ИСИХОГИ. (либо в подготовленном массиве данных)
     * 2. Названия атрибутов для записи в шейп-файл (на латинице);
     * 3. Тип параметра (необходимо при записи значений в shape-файл)
     */
    private List<List<String>> nameAndTypeKeys = new ArrayList<>();

    /* названия атрибутов, которые нужны для создания файла интервалов скважин */
    private List<String> requiredKeysIntervalWell = new ArrayList<>
            (indexAndNameOfColumns.values());
    /*
     * идентификатор по которому в дальнейшем должны связываться данные из
     * файла устьев скважин и файла интервалов скважин. Т.е. у данных по скважинам
     * с одинаковым названием(номер/линия), и принадлежащим одному объекту ГРР,
     * должны быть одинаковые идентификационные номера
     */
    private long id = 0;

    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* Все точки, считанные из всех excel-файлов для файла устьев Micromine*/
    private List<Map<String, String>> allTopWells = new ArrayList<>();

    /*
     * Все точки, считанные из всех excel-файлов для файла интервалов
     * (минералогия) для Micromine
     */
    private List<Map<String, String>> allIntervalWells = new ArrayList<>();

    /* название файла устьев скважин */
    private final String nameFileTopWells = "top wells";

    /* текстовый файл для записи выходных данных в файл устьев*/
    private TextFileForMicromine outputFileTopWells;

    /* текстовый файл для записи выходных данных в файл интервалов (для минералогии) */
    private TextFileForMicromine outputFileIntervalsStratWells;

    /*
     * Текстовый файл для записи выходных данных в файл интервалов
     * (для минералогии)
     */
    private TextFileForMicromine outputFileIntervalsWells;

    /* название файла интервалов скважин */
    private final String nameFileIntervalsWells = "interval wells";

    /*
     * Если размер обрабатываемой коллекции больше этого значения,
     * то такую коллекцию необходимо делить пополам, и обрабатывать
     * каждую подколлекцию отдельно.
     */
    protected final int thresholdMiniList = 100;

    /* путь к шейп-файлу, куда сохраняются результаты вычислений */
    private String filePath;

    /* шейп-файл для записи выходных данных*/
    private ShapeFileForPoints shapeFile;

    public mineralogyPointsForStratigraphicLayer(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        nameAndTypeKeys.add(Arrays.asList("ID", "id tn", "String"));
        nameAndTypeKeys.add(Arrays.asList("X", "x", "String"));
        nameAndTypeKeys.add(Arrays.asList("Y", "y", "String"));
        nameAndTypeKeys.add(Arrays.asList("Z", "z", "Double"));
        nameAndTypeKeys.add(Arrays.asList("Линия", "line", "String"));
        nameAndTypeKeys.add(Arrays.asList("Точка", "tn" ,"String"));
        nameAndTypeKeys.add(Arrays.asList("От", "from", "String"));
        nameAndTypeKeys.add(Arrays.asList("До", "to", "String"));
        nameAndTypeKeys.add(Arrays.asList("Стратиграфия", "strat", "String"));
        nameAndTypeKeys.add(Arrays.asList("param", "param", "Double"));

        requiredKeysIntervalWell.removeAll(Arrays.asList("Объект", "Участок",
                "Тип ТН", "X", "Y", "Z", "Зона"));
        requiredKeysIntervalWell.add(0, "ID");
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        typeOfStratigraphic = inputParameters[1];
        workingCatalog = inputParameters[2];
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

        /*
        shapeFile = new ShapeFileForPoints(filePath);
        shapeFile.create(nameAndTypeKeys);
        shapeFile.setNameKeyForX("X");
        shapeFile.setNameKeyForY("Y");
        */
        for (File excelFile: excelFiles) {
            SheetOfExcelFileMineralWeb mineralogySheet = new SheetOfExcelFileMineralWeb(excelFile);
            processForCurrentExcel(mineralogySheet);
        }

        //shapeFile.close();


        outputFileTopWells = new TextFileForMicromine(workingCatalog, nameFileTopWells);
        outputFileTopWells.create();
        outputFileTopWells.writeTitle(requiredKeysTopWell);
        outputFileTopWells.write(allIntervalWells);


        /*
        outputFileIntervalsWells = new TextFileForMicromine(workingCatalog, nameFileIntervalsWells);
        outputFileIntervalsWells.create();
        outputFileIntervalsWells.writeTitle(requiredKeysIntervalWell);

        ScalingData forGeophysicLayer =
                new WriteIntervalsCommon(outputFileIntervalsWells);

        forGeophysicLayer.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());
        */
    }

    private void processForCurrentExcel(SheetOfExcelFileMineralWeb mineralogySheet)
            throws TaskException {
        nigp.tables.MineralogyOfWebTable mineralogy = new nigp.tables.MineralogyOfWebTable(mineralogySheet);
        if (mineralogy.isEmpty()) {
            return;
        }

        List<Map<String, String>> table = mineralogy.getLinesOfSheet();
        List<Map<String, String>> topWells = copyListWithSubMap(table);

        topWells.forEach(e -> e.keySet().retainAll(requiredKeysTopWell));
        List<Map<String, String>> uniqueTopWells = getWellsWithUniqueNames(topWells);

        uniqueTopWells.forEach(e -> {
            e.put("ID", String.valueOf(id));
            id++;
        });


        uniqueTopWells.forEach(e -> {
            e.put("Глубина ТН", "1000");
        });

        uniqueTopWells.forEach(e -> {
            e.replace("X", e.get("X").replace(",", "."));
            e.replace("Y", e.get("Y").replace(",", "."));
            e.replace("Z", e.get("Z").replace(",", "."));
        });

        uniqueTopWells.forEach(e -> {
            if (e.get("Z").equals("Нет данных")) {
                e.replace("Z", defaultZ);
            }
        });

        uniqueTopWells.forEach(e -> {
            String x = e.get("X");
            e.replace("X", e.get("Y"));
            e.replace("Y", x);
        });

        uniqueTopWells.forEach(this::amendmentForMCA);
        allTopWells.addAll(uniqueTopWells);

        List<Map<String, String>> intervalWells = copyListWithSubMap(table);

        assignIDToIntervals(uniqueTopWells, intervalWells);

        intervalWells.forEach(e -> e.keySet().retainAll(requiredKeysIntervalWell));

        intervalWells.forEach(e -> {
            e.replace("От", e.get("От").replace(",", "."));
            e.replace("До", e.get("До").replace(",", "."));
            e.replace("Вес шлиха", e.get("Вес шлиха").replace(",", "."));
            e.replace("Вес т.ф.", e.get("Вес т.ф.").replace(",", "."));
            e.replace("Объем", e.get("Объем").replace(",", "."));
        });

        intervalWells.sort(Comparator.comparing(this::sortById)
                .thenComparing(this::sortByUpperContact));

        intervalWells.forEach(e -> {
            double kol = Double.valueOf(e.get("Все МСА"));
            String obj = e.get("Объем");

            if (!obj.equals("Нет данных")) {
                String objS = obj.substring(0, obj.length() - 1);
                double objDouble = Double.valueOf(objS);
                double kolx = kol * 10 / objDouble;
                kolx = Math.round(kolx * 100.0) / 100.0;
                //System.out.println(kol + " " + obj + " | " + kolx);
                e.put("param", String.valueOf(kolx));
            }
            else {
                e.put("param", "0");
            }
            //System.out.println(kol + " " + s + " " + k);

        });

        List<Map<String, String>> list2 = new ArrayList<>();
        intervalWells.forEach(e -> {
            Map<String, String> m = new HashMap<>();
            m.put("ID", e.get("ID"));
            m.put("Линия", e.get("Линия"));
            m.put("Точка", e.get("Точка"));
            m.put("От", e.get("От"));
            m.put("До", e.get("До"));
            m.put("Стратиграфия", e.get("Стратиграфия"));
            m.put("param", e.get("param"));
            list2.add(m);
        });

        uniqueTopWells.forEach(e1 -> {
           list2.forEach(e2 -> {
               if (e1.get("ID").equals(e2.get("ID"))) {
                   e2.put("X", e1.get("X"));
                   e2.put("Y", e1.get("Y"));
                   e2.put("Z", e1.get("Z"));
               }
           });
        });


        System.out.println("-------------------------");

        list2.forEach(e -> {
            double from = Double.valueOf(e.get("От"));
            double to = Double.valueOf(e.get("До"));
            double z = Double.valueOf(e.get("Z"));
            from = z - from;
            to = z - to;
            double mean = (from + to) / 2;
            mean = Math.round(mean * 100.0) / 100.0;
            //System.out.println(from + " " + to + " " + mean);
            e.put("Z", String.valueOf(mean));
        });

        List<Map<String, String>> list3 = list2.stream()
                .filter(this::isStratigraphic)
                .collect(Collectors.toList());

        list3.forEach(e -> {
            e.forEach((k, v) -> {
                System.out.println(k + " : " + v + ";");
            });
            System.out.println("------------------------");
        });

        //shapeFile.write(list3);
        allIntervalWells.addAll(list3);

    }

    private boolean isStratigraphic(Map<String, String> point) {
        if(point.get("Стратиграфия").equals(typeOfStratigraphic)) {
        //if (point.get("Стратиграфия").contains(typeOfStratigraphic)) {
            return true;
        } else {
            return false;
        }
    }
}
