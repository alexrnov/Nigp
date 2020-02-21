package nigp.tasks.micromine.mcaOfAllObjects;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetOfExcelFileMineralWeb;
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
 * Cоздает файл устьев и файл интервалов(web-сервис "МСА по всем объектам")
 * для Micromine
 */
public class FullDataMineralogy extends Task implements Micromine, Mineralogy {

    /* названия атрибутов, которые нужны для создания файла устьев скважин */
    private final List<String> requiredKeysTopWell = Arrays.asList("ID", "Объект",
            "Участок", "Тип ТН", "Линия", "Точка", "X", "Y", "Z", "Зона", "Глубина ТН");

    /* это значение присваивается координате Z если информация по высоте отсутствует */
    private final String defaultZ = "400.0";

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

    public FullDataMineralogy(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();


        requiredKeysIntervalWell.removeAll(Arrays.asList("Объект", "Участок",
                "Тип ТН", "X", "Y", "Z", "Зона"));
        requiredKeysIntervalWell.add(0, "ID");
    }

    /* Чтение параметров командной строки */
    private void readInputParameters() throws TaskException {
        directoryForSearchExcel = inputParameters[0];
        workingCatalog = inputParameters[1];
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
        for (File excelFile: excelFiles) {
            SheetOfExcelFileMineralWeb mineralogySheet = new SheetOfExcelFileMineralWeb(excelFile);
            processForCurrentExcel(mineralogySheet);
        }

        outputFileTopWells = new TextFileForMicromine(workingCatalog, nameFileTopWells);
        outputFileTopWells.create();
        outputFileTopWells.writeTitle(requiredKeysTopWell);
        outputFileTopWells.write(allTopWells);

        requiredKeysIntervalWell.add("param");


        outputFileIntervalsWells = new TextFileForMicromine(workingCatalog, nameFileIntervalsWells);
        outputFileIntervalsWells.create();
        outputFileIntervalsWells.writeTitle(requiredKeysIntervalWell);

        ScalingData forGeophysicLayer =
                new WriteIntervalsCommon(outputFileIntervalsWells);

        forGeophysicLayer.perform(allIntervalWells, thresholdMiniList, 0, allIntervalWells.size());

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

        /*
        intervalWells.forEach(e -> {
           e.forEach((k, v) -> {
              System.out.print(k + " : " + v + "; ");
           });
           System.out.println();
           System.out.println("------------------------------");
        });
        */

        Random r = new Random();

        intervalWells.forEach(e -> {
           e.put("param", String.valueOf(r.nextInt(43)));
           double kol = Double.valueOf(e.get("Все МСА"));
           String obj = e.get("Объем");

           if (!obj.equals("Нет данных")) {
               String objS = obj.substring(0, obj.length() - 1);
               double objDouble = Double.valueOf(objS);
               double kolx = kol * 10 / objDouble;
               kolx = Math.round(kolx * 100.0) / 100.0;
               System.out.println(kol + " " + obj + " | " + kolx);
               e.put("param", String.valueOf(kolx));
           }
           else {
               e.put("param", "0");
           }
           //System.out.println(kol + " " + s + " " + k);

        });

        System.out.println("-------------");
        allIntervalWells.addAll(intervalWells);

    }


}
