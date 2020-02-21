package nigp;

import nigp.tasks.Task;
import nigp.tasks.TypeOfTask;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

/**
 * Параметризированный тест компонена. В качестве входных параметров
 * выступают наименования литологии, которые считываются из
 * специального файла. Результатом теста должен стать набор файлов,
 * с именами, соответсвтующими наименованиям литологии.
 */
@RunWith(Parameterized.class)
public class ABSForLithologyManyLayers {

    private static Logger logger = Logger.getLogger(Main.class.getName());
    private Task task;
    private String lithology;

    static {
        new Logging(logger);
    }

    public ABSForLithologyManyLayers(String lithology) {
        this.lithology = lithology;
    }

    @After
    public void tearDown() throws Exception {
        task = null;
    }

    @Parameterized.Parameters //набор входных параметров (наименования литологии)
    public static Collection<String> data() {
        String fileName = "." + separator + "test input" + separator + "indexesOfLithology.txt";
        List<String> indexesOfLithology = new ArrayList<>();
        //прочитать все литологические наименования из файла
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(e -> indexesOfLithology.add(e));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexesOfLithology;
    }

    @Test
    public void ABSForLithologyLayer() throws Exception {
        Path points = Paths.get("." + separator + "test output many layers" + separator + lithology + ".txt");
        Files.deleteIfExists(points);

        String[] args = {
                "Файл точек для Micromine (ABS кровли и подошвы по литологическим подразделениям)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                lithology, //литологическое подразделение
                "Объединять пласты",
                //"Не объединять пласты",
                "." + separator + "test output many layers"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(points));
    }
}
