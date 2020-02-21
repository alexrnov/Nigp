package nigp.tasks.micromine.databasewells;

import nigp.excel.FoundExcelFiles;
import nigp.excel.SheetsOfExcelFile;
import nigp.file.TextFileForMicromine;
import nigp.tables.*;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.micromine.Micromine;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cоздает файл устьев(вертикальные скважины)
 */
public class OnlyTopWells extends Task implements Micromine, TablesAction {

    /* название файла устьев скважин */
    private final String nameFileTopWells = "top wells";

    /* Все точки, считанные из всех excel-файлов для файла устьев скважин для Micromine*/
    private List<Map<String, String>> allTopWells = new ArrayList<>();


    /* каталог с excel-файлами */
    private String directoryForSearchExcel;

    /* рабочий каталог, куда сохраняется текстовый файл с результатами
     * вычислений
     */
    private String workingCatalog;

    /* список excel-файлов, найденных в каталоге(подкаталогах) */
    private List<File> excelFiles;

    /* текстовый файл для записи выходных данных в файл устьев*/
    protected TextFileForMicromine outputFileTopWells;

    /* таблица с точками наблюдений */
    protected PointsObservations pointsObservations;

    /* список содержит названия ключей, которые необходимы для формирования
     * полей в выходном файле устьев для micromine. Этот список также
     * используется для отсева ненужных полей в объединенной таблице
     */
    private List<String> requiredKeysTopWell = new ArrayList<>();

    /* содержит типы скважин, которые могут быть невертикальными
     * - они исключаются из расчета
     */
    private Set<String> nonVerticaleWells = new HashSet<>();

    public OnlyTopWells(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        findExcelFiles();

        requiredKeysTopWell.add("ID ТН");
        requiredKeysTopWell.add("UIN");
        requiredKeysTopWell.add("X факт.");
        requiredKeysTopWell.add("Y факт.");
        requiredKeysTopWell.add("Z");
        requiredKeysTopWell.add("Глубина ТН");
        requiredKeysTopWell.add("Код типа ТН");
        requiredKeysTopWell.add("Объект");
        requiredKeysTopWell.add("Участок");
        requiredKeysTopWell.add("Код Состояния документирования");
        requiredKeysTopWell.add("Код Состояния выработки");
        requiredKeysTopWell.add("Код Состояния ГИС");
        requiredKeysTopWell.add("Код Состояния опробования");
        requiredKeysTopWell.add("Количество проб");
        requiredKeysTopWell.add("Количество ГИС");

        nonVerticaleWells.add("Скважина наклонная поисковая");
        nonVerticaleWells.add("Поисково-Оценочные скважины");
        nonVerticaleWells.add("Штольня");
        nonVerticaleWells.add("Разведочная");
        nonVerticaleWells.add("Поисково-картировочные");
        nonVerticaleWells.add("Детальная разведка");
        nonVerticaleWells.add("Скважина наклонно направленная поисковая");
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
    }

    protected void processForCurrentExcel(SheetsOfExcelFile excelSheets)
            throws TaskException {
        pointsObservations = new PointsObservations(excelSheets);
        if (pointsObservations.isTableDefaultFormatComplete()) {
            pointsObservations.decode();
            pointsObservations.checkedForFoundData();

            List<Map<String, String>> validPoints = pointsObservations.getTableDefaultFormat().stream()
                    .filter(this::hasID)
                    .filter(this::hasDepth)
                    .filter(this::hasX)
                    .filter(this::hasY)
                    .filter(this::hasZ)
                    .filter(e -> hasVerticale(e, nonVerticaleWells))
                    .collect(Collectors.toList());

            validPoints.forEach(this::amendment);
            allTopWells.addAll(validPoints);
        }
    }

}
