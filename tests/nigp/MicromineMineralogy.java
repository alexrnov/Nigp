package nigp;

import nigp.tasks.Task;
import nigp.tasks.TypeOfTask;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

public class MicromineMineralogy {
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
    public void fullIntervalMineralogy() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        Path pathIntervalWells = Paths.get("." + separator + "test output" + separator + "interval wells.txt");
        Files.deleteIfExists(pathIntervalWells);

        String[] args = {
                "Файлы Micromine (минералогия по всему стволу скважины)",
                //"." + separator + "test input" + separator + "ExcelFilesForMineralogy",
                "D:" + separator + "Локальный прогноз" + separator + "МСА по всем объектам",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
        assertTrue(Files.exists(pathIntervalWells));
    }

    @Test
    public void pointsWithMineralogyForStratigraphicLayer() throws Exception {
        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        String[] args = {
                "Точки с данными минералогии для стратиграфического подразделения",
                //"." + separator + "test input" + separator + "ExcelFilesForMineralogy",
                "D:" + separator + "Локальный прогноз" + separator + "МСА по всем объектам",
                "J1dh",
                //"J1uk",
                //"J1tn",
                //"T2-3",
                //"QIV",
                /*
                 * необходимо расскоментировать строку в файле mineralogyPointsForStratigraphicLayer
                 * if (point.get("Стратиграфия").contains(typeOfStratigraphic)) {
                 */
                //"O",
                //"J1sn",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(pathTopWells));
    }

}
