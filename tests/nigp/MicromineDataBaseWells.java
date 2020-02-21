package nigp;

import nigp.tasks.Task;
import nigp.tasks.TypeOfTask;
import nigp.tasks.micromine.databasewells.FullIntervalGISMinusValue;
import nigp.tasks.micromine.databasewells.GISForStratigraphicLayersMinusValue;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

public class MicromineDataBaseWells {

    private static Logger logger = Logger.getLogger(Main.class.getName());
    private Task task;

    static {
        new Logging(logger);
    }

    @After
    public void tearDown() throws Exception {
        task = null;
    }

    @Test
    public void topWells() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        String[] args = {
                "Файл устьев скважин для Micromine",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016" + separator + "Промышленный_2",
                "." + separator + "test output"
        };

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
    }

    @Test
    public void fullIntervalGIS() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (весь ствол скважины)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016" + separator + "Промышленный_2",
                //"Инклиномертия",
                //"Индукционный каротаж",
                //"Электро-Магнитный каротаж ",
                //"Электро-каротаж",
                "Каротаж магнитной восприимчивости",
                //"Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                //"K",
                //"Th",
                //"U",
                //"Скважинная магниторазведка",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }


    @Test
    public void fullIntervalForKThU() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine по K, Th, U (весь ствол скважины)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "K",
                //"Th",
                //"U",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void fullIntervalMinusValue() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (ГИС по всему стволу скважины с вычитанием определенного значения)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "Скважинная магниторазведка",
                //"Гамма-Каротаж интегрального канала СГК",
                //"Гамма-Каротаж",

                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        FullIntervalGISMinusValue concreteTask = (FullIntervalGISMinusValue) task;
        //concreteTask.setTypeOfSubtractValue("отнять медиану");//по умолчанию для вычитания используется медиана
        //concreteTask.setTypeOfSubtractValue("отнять среднее значение");
        concreteTask.setTypeOfSubtractValue("отнять минимальное значение");
        //concreteTask.setTypeOfSubtractValue("отнять максимальное значение");
        concreteTask.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void oneGeophysicalLayer() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (один геофизический пласт)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"Гамма-Каротаж",
                //"Электро-Магнитный каротаж ",
                //"Каротаж магнитной восприимчивости",
                //"Гамма-Каротаж интегрального канала СГК",
                "Скважинная магниторазведка",
                //"44",
                //"44А",
                "43",
                //"45",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void oneGeophysicalLayerForImplicitModelling() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Один геофизический пласт для условного моделирования",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"44",
                //"44А",
                "43",
                //"45",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void oneGeophysicalLayerRatioKThU() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (один геофизический пласт с соотношением по K, Th и U)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"44",
                "44А",
                //"47",
                //"43",
                //"45",
                //"46",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void GISForStratigraphicLayers() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (ГИС по подразделениям и статистические параметры)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"D:"+ separator +"Локальный прогноз"+separator+"old excel",
                //"D:" + separator + "Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016" + separator + "Разломный_05_02_2018",
                //"Инклиномертия",
                //"Индукционный каротаж",
                //"Электро-Магнитный каротаж ",
                "Каротаж магнитной восприимчивости",
                //"Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                //"K",
                //"Th",
                //"U",
                //"Скважинная магниторазведка",
                "D;S;O;G;AR;R;iPz2", //вмещающие отложения
                //"T2-3", //кора выветривания
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void FullIntervalStratigraphic() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (стратиграфия и литология по всему стволу скважины)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016" + separator + "Промышленный_2",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void GISForStratigraphicLayersMinusValue() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (ГИС по подразделениям с вычитанием среднего и статистические параметры)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016/Накынский/Накынский_part4.xls",
                "Скважинная магниторазведка",
                //"D;S;O;G;AR;R;iPz2", //вмещающие отложения
                //"T2-3", //кора выветривания
                "J",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        GISForStratigraphicLayersMinusValue concreteTask =
                (GISForStratigraphicLayersMinusValue) task;

        //concreteTask.setTypeOfSubtractValue("отнять медиану");//по умолчанию для вычитания используется медиана
        //concreteTask.setTypeOfSubtractValue("отнять среднее значение");
        concreteTask.setTypeOfSubtractValue("отнять минимальное значение");
        //concreteTask.setTypeOfSubtractValue("отнять максимальное значение");
        //concreteTask.setTypeOfSubtractValue("отнять медиану, затем максимальное значение");
        concreteTask.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void GISForStratigraphicLayersRatioKThU() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        //Path pathEventWells = Paths.get("." + separator + "test output" + separator + "event wells.txt");
        //Files.deleteIfExists(pathEventWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (соотношение K, Th и U по подразделениям)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "D;S;O;G;AR;R;iPz2", //вмещающие отложения
                //"dh",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        //assertTrue(Files.exists(pathEventWells));
        assertTrue(Files.exists(pathIntervalWells));
    }


    @Test
    public void absoluteGeophysicalLayers() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (геофизические пласты с абсолютной мощностью)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "Гамма-Каротаж",
                //"Электро-Магнитный каротаж ",
                //"Каротаж магнитной восприимчивости",
                //"Гамма-Каротаж интегрального канала СГК",
                "44",
                //"44А",
                //"43",
                //"45",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void allGeophysicLayers() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (все геофизические пласты)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }
}
