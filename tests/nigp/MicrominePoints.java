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

public class MicrominePoints {
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
    public void ABSForStratigraphicLayer() throws Exception {
        Path points = Paths.get("." + separator + "test output" + separator + "points.txt");
        Files.deleteIfExists(points);

        String[] args = {
                "Файл точек для Micromine (ABS кровли и подошвы по стратиграфическим подразделениям)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"J1dh", //стратиграфическое подразделение
                //"J1sn",// в методе inputUpperAndLowerContacts (Stratigraphy) нужно раскоментировать if (allIndexes[j].contains(nameOfStratOrLithIndex)) {
                //"J1tn",
                "J1uk",
                //"QIV",
                //"T2-3",
                //"G3mrh",
                //"G3mrk",
                //"T3=J1",
                //"T3=J1dh",
                //"O1",// в методе inputUpperAndLowerContacts (Stratigraphy) нужно раскоментировать if (allIndexes[j].contains(nameOfStratOrLithIndex)) {
                "Объединять пласты",
                //"Не объединять пласты",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(points));
    }

    @Test
    public void ABSForLithologyLayer() throws Exception {
        Path points = Paths.get("." + separator + "test output" + separator + "Алевролит.txt");
        Files.deleteIfExists(points);

        String[] args = {
                "Файл точек для Micromine (ABS кровли и подошвы по литологическим подразделениям)",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "Алевролит", //литологическое подразделение
                "Объединять пласты",
                //"Не объединять пласты",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(points));
    }

    @Test
    public void ABSAndMeanGISForBlockModel() throws Exception {
        Path points = Paths.get("." + separator + "test output" + separator + "points.txt");
        Files.deleteIfExists(points);

        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        String[] args = {
                "Файл точек для Micromine (ABS по основным подразделениям и средние значения ГИС)",
                "." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                //"D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                "Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                "2", //стратиграфическое подразделение /0-перекрывающие, 1-дяхтар, 2-кора, 3-ордовик
                /*
                "QIV,aQIV,QII-III,QIII,N2,N2=QI,QIIIkr,QI-IIIek,Qюhn,a5QI," +
                "a2QIII,a1QIII,QIII-IV,QIII[3]=QIV,a4QII,Qh,J1sn,J1sn!,J1tn," +
                "J1uk,J1-2sn$,J1sn#,J1sn@,J1sn@_#,J1tn!,J1-2sn,J2jak,J1-2lh," +
                "J1or,J1sn$,J1-2 sn@_$,J1-2 sn#_$,J1tn#,J1sn!_#,J1,J1tn$,J1tn@," +
                "J1 or1,J1vk!,J1vk@,J2jak2,J2jak1;J1dh,T3=J1dh,T3=J1;T2-3;" +
                "O1ol,O1bl,O2st,O2st1,O2st2,O1bl#,O1bl@,O1sh,O1sr",
                */

                "QIV,aQIV,QII-III,QIII,N2,N2=QI,QIIIkr,QI-IIIek,Qюhn,a5QI," +
                "a2QIII,a1QIII,QIII-IV,QIII[3]=QIV,a4QII,Qh,J1sn,J1sn!,J1tn," +
                "J1uk,J1-2sn$,J1sn#,J1sn@,J1sn@_#,J1tn!,J1-2sn,J2jak,J1-2lh," +
                "J1or,J1sn$,J1-2 sn@_$,J1-2 sn#_$,J1tn#,J1sn!_#,J1,J1tn$,J1tn@," +
                "J1 or1,J1vk!,J1vk@,J2jak2,J2jak1;J1dh,T3=J1dh,T3=J1,T2-3;" +
                "O1ol,O1bl,O2st,O2st1,O2st2,O1bl#,O1bl@,O1sh,O1sr",

                "не записывать точки с пустыми значениями ГИС",
                //"записывать точки с пустыми значениями ГИС",
                "." + separator + "test output"};
        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(points));
        assertTrue(Files.exists(pathTopWells));
    }


    /*
    доработан только для разделения по следующим толщам: 0-перекрывающие,
    1-дяхтар, 2-кора, 3-ордовик, поскольку следующий блок кода предусматривает
    частные случаи комбинаций пластов только для четырех пластов
    List<Map<String, String>> pointsWithTypeStructure = validPoints.stream()
                .filter(e -> {
                    if (e.get("Стратиграфия").equals("0/1/2/3/")
                            || e.get("Стратиграфия").equals("0/1/3/")
                            || e.get("Стратиграфия").equals("0/2/3/")
                            || e.get("Стратиграфия").equals("1/2/3/")
                            || e.get("Стратиграфия").equals("0/3/")
                            || e.get("Стратиграфия").equals("1/3/")
                            || e.get("Стратиграфия").equals("2/3/")) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
     */
    @Test //доработан для разделения по следующим толщам 0-перекрывающие, 1-дяхтар, 2-кора, 3-ордовик
    public void ABSAndMeanGISForBlockModelMinWidth() throws Exception {
        Path points = Paths.get("." + separator + "test output" + separator + "points.txt");
        Files.deleteIfExists(points);

        Path pathTopWells = Paths.get("." + separator + "test output" + separator + "top wells.txt");
        Files.deleteIfExists(pathTopWells);

        String[] args = {
                "Файл точек для Micromine (ABS по основным подразделениям и средние значения ГИС)." +
                        "по умолчанию используется минимальная мощность структур",
                //"." + separator + "test input" + separator + "ExcelFilesForNigpTools",
                "D:"+ separator +"Локальный прогноз"+separator+"Excel_по_последней_документации_04_07_2016",
                //"Гамма-Каротаж",
                //"Гамма-Каротаж интегрального канала СГК",
                "Каротаж магнитной восприимчивости",
                /*
                "3", //стратиграфическое подразделение /0-перекрывающие, 1-дяхтар, 2-кора, 3-ордовик

                "QIV,aQIV,QII-III,QIII,N2,N2=QI,QIIIkr,QI-IIIek,Qюhn,a5QI," +
                "a2QIII,a1QIII,QIII-IV,QIII[3]=QIV,a4QII,Qh,J1sn,J1sn!,J1tn," +
                "J1uk,J1-2sn$,J1sn#,J1sn@,J1sn@_#,J1tn!,J1-2sn,J2jak,J1-2lh," +
                "J1or,J1sn$,J1-2 sn@_$,J1-2 sn#_$,J1tn#,J1sn!_#,J1,J1tn$,J1tn@," +
                "J1 or1,J1vk!,J1vk@,J2jak2,J2jak1;J1dh,T3=J1dh,T3=J1;T2-3;" +
                "O1ol,O1bl,O2st,O2st1,O2st2,O1bl#,O1bl@,O1sh,O1sr",
                "не записывать точки с пустыми значениями ГИС",
                //"записывать точки с пустыми значениями ГИС",
                "." + separator + "test output"};
                */

                "6", //стратиграфическое подразделение /0-четвертичные, 1-J1sn, 2-J1tn, 3-J1uk, 4-дяхтар, 5-кора, 6-ордовик
                /*
                "QIV,aQIV,QII-III,QIII,N2,N2=QI,QIIIkr,QI-IIIek,Qюhn,a5QI," +
                        "a2QIII,a1QIII,QIII-IV,QIII[3]=QIV,a4QII,Qh,J1sn,J1sn!," +
                        "J1-2sn$,J1sn#,J1sn@,J1sn@_#,J1-2sn,J2jak,J1-2lh," +
                        "J1or,J1sn$,J1-2 sn@_$,J1-2 sn#_$,J1sn!_#,J1," +
                        "J1 or1,J1vk!,J1vk@,J2jak2,J2jak1;" +
                        "J1tn#,J1tn$,J1tn!,J1tn,J1tn@;J1uk;J1dh,T3=J1dh,T3=J1;T2-3;" +
                        "O1ol,O1bl,O2st,O2st1,O2st2,O1bl#,O1bl@,O1sh,O1sr",
                */

                "QIV,aQIV,QII-III,QIII,N2,N2=QI,QIIIkr,QI-IIIek,Qюhn,a5QI," +
                        "a2QIII,a1QIII,QIII-IV,QIII[3]=QIV,a4QII,Qh;J1sn,J1sn!," +
                        "J1-2sn$,J1sn#,J1sn@,J1sn@_#,J1-2sn,J2jak,J1-2lh," +
                        "J1or,J1sn$,J1-2 sn@_$,J1-2 sn#_$,J1sn!_#,J1," +
                        "J1 or1,J1vk!,J1vk@,J2jak2,J2jak1;" +
                        "J1tn#,J1tn$,J1tn!,J1tn,J1tn@;J1uk;J1dh,T3=J1dh,T3=J1;T2-3;" +
                        "O1ol,O1bl,O2st,O2st1,O2st2,O1bl#,O1bl@,O1sh,O1sr",




                "не записывать точки с пустыми значениями ГИС",
                //"записывать точки с пустыми значениями ГИС",
                "." + separator + "test output"};

        task = TypeOfTask.getType(args);
        task.toSolve();

        assertTrue(Files.exists(points));
        assertTrue(Files.exists(pathTopWells));
    }
}
