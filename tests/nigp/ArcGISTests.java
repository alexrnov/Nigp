package nigp;

import nigp.tasks.TypeOfTask;
import nigp.tasks.Task;
import nigp.tasks.gis.geophysicallayer.WidthAndMeanGISForGeophysLayerMinusValue;
import nigp.tasks.gis.stratigraphiclayer.MeanGISStratigraphicLayerMinusValue;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

/**
 * Главные тесты: каждый тест проверяет решение отдельной геологической задачи,
 * предназначенной для решения в среде ArcGIS
 * 1. Мощность и среднее значение ГИС для геофизического пласта
 * 2. Номер геофизических пластов, выходящих на поверхность карбонатного цоколя
 * 3. Интерполяция по нечисловому атрибуту
 * 4. Абсолютные отметки для геофизического пласта (незакончен)
 */
public class ArcGISTests {

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
    public void widthAndMeanGisForGeophysicalLayer() throws Exception {
        //Path path = Paths.get("." + separator + "test output" + separator + "OutputFileForMapLayer.txt");
        Path path = Paths.get("." + separator + "test output" + separator + "shp" + separator + "default.shp");
        Files.deleteIfExists(path);

        String[] args = {
                "Мощность и среднее значение ГИС для геофизического пласта",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:" + separator +"Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016",
                "Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                //"Индукционный каротаж",
                //"Электро-Магнитный каротаж ",
                //"Каротаж магнитной восприимчивости",
                //"K",
                //"Th",
                //"U",
                //"Скважинная магниторазведка",
                "45",
                "D:" + separator + "shp" + separator + "default.shp"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void widthAndMeanGisForGeophysicalLayerMinusValue() throws Exception {
        //Path path = Paths.get("." + separator + "test output" + separator + "OutputFileForMapLayer.txt");
        Path path = Paths.get("." + separator + "test output" + separator + "shp" + separator + "default.shp");
        Files.deleteIfExists(path);

        String[] args = {
                "Мощность и среднее значение ГИС для геофизического пласта, с вычитанием",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:" + separator +"Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016",
                "Скважинная магниторазведка",
                "44",
                "D:" + separator + "shp" + separator + "default.shp"};
        task = TypeOfTask.getType(args);
        WidthAndMeanGISForGeophysLayerMinusValue concreteTask =
                (WidthAndMeanGISForGeophysLayerMinusValue) task;
        concreteTask.setTypeOfSubtractValue("отнять минимальное значение");//по умолчанию
        //concreteTask.setTypeOfSubtractValue("отнять максимальное значение");
        //concreteTask.setTypeOfSubtractValue("отнять среднее значение");
        //concreteTask.setTypeOfSubtractValue("отнять медиану");

        concreteTask.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void geophysicalLayerOnSurfaceOfCarbonate() throws Exception {
        Path path = Paths.get("." + separator + "test output" + separator + "OutputFileForMapLayer.txt");
        Files.deleteIfExists(path);

        String[] args = {
                "Номер геофизических пластов, выходящих на поверхность "
                        + "карбонатного цоколя",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void nonNumericOnePoint() throws Exception {
        Path path = Paths.get("." + separator + "test input" + separator + "OutputFileForXYZLayer.txt");
        Files.deleteIfExists(path);

        String[] args = {
                "Интерполяция по нечисловому атрибуту",
                "." + separator + "test input" + separator
                        + "addToolsInterpolation.txt", "200"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void absoluteABSForGeophysicalLayer() throws Exception {
        Path path = Paths.get("." + separator + "test output" + separator + "OutputFileForMapLayer.txt");
        Files.deleteIfExists(path);

        String[] args = {
                "Абсолютные отметки геофизического пласта",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "Гамма-Каротаж",
                "44", "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void meanGisForStratigraphicLayer() throws Exception {
        Path path = Paths.get("." + separator + "test output" + separator + "shp" + separator + "default.shp");
        Files.deleteIfExists(path);

        String[] args = {
                "Мощность и среднее значение ГИС для стратиграфического подразделения",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:" + separator +"Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016",
                //"Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                "Каротаж магнитной восприимчивости",
                //"Индукционный каротаж",
                //"Скважинная магниторазведка",
                //"dh;T",
                //"J1dh",
                //"T2-3",
                //"T3=J1dh;T3=J1;T2-3",
                //"J1dh;T2-3;T3=J1dh;T3=J1",
                //"D;S;O;G;AR;R;iPz2",
                "O1",
                //"J;Q;N;D;T;S;O;G;AR;R;iPz2",
                //"J;N;Q;T",
                //"." + separator + "test output" + separator + "shp" + separator + "default.shp"};
                "D:" + separator + "shp" + separator + "kmv.shp"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

    @Test
    public void meanGisForStratigraphicLayerMinusValue() throws Exception {
        Path path = Paths.get("." + separator + "test output" + separator + "shp" + separator + "default.shp");
        Files.deleteIfExists(path);

        String[] args = {
                "Мощность и среднее значение ГИС для стратиграфического подразделения, с вычитанием",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:" + separator +"Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016",
                "Скважинная магниторазведка",
                //"dh;T",
                //"J1dh",
                //"T2-3",
                //"T3=J1dh;T3=J1;T2-3",
                //"J1dh;T2-3;T3=J1dh;T3=J1",
                //"D;S;O;G;AR;R;iPz2",
                //"O",
                "J;Q;N;D;T;S;O;G;AR;R;iPz2",

                //"." + separator + "test output" + separator + "shp" + separator + "default.shp"};
                "D:" + separator + "shp" + separator + "kmv2.shp"};

        task = TypeOfTask.getType(args);

        MeanGISStratigraphicLayerMinusValue concreteTask = (MeanGISStratigraphicLayerMinusValue) task;
        concreteTask.setTypeOfSubtractValue("отнять минимальное значение");//по умолчанию
        //concreteTask.setTypeOfSubtractValue("отнять максимальное значение");
        //concreteTask.setTypeOfSubtractValue("отнять среднее значение");
        //concreteTask.setTypeOfSubtractValue("отнять медиану");
        concreteTask.toSolve();
        assertTrue(Files.exists(path));
    }

    @Test
    public void meanGisForStructure() throws Exception {
        Path path = Paths.get("." + separator + "test output" + separator + "shp" + separator + "default.shp");
        Files.deleteIfExists(path);

        String[] args = {
                "Мощность и среднее значение ГИС для перекрывающих или вмещающих отложений",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:" + separator +"Локальный прогноз" + separator + "Excel_по_последней_документации_04_07_2016",
                //"Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                "Каротаж магнитной восприимчивости",
                //"D;S;O;G;AR;R;iPz2",
                "J;N;Q;T", //стратиграфические индексы пород, отнесенные к перекрывающим отложениям
                "Перекрывающие отложения",
                //"Вмещающие отложения",
                //"." + separator + "test output" + separator + "shp" + separator + "default.shp"};
                "D:" + separator + "shp" + separator + "kmv2.shp"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(path));
    }

}