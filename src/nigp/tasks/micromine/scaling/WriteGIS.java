package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация шаблонного метода для записи данных ГИС. Используется паттерн
 * ШАБЛОННЫЙ МЕТОД.
 */
public class WriteGIS extends ScalingData {

    private TextFileForMicromine file;
    private String nameMethodGIS;

    public WriteGIS(TextFileForMicromine file, String nameMethodGIS) {
        this.file = file;
        this.nameMethodGIS = nameMethodGIS;
    }

    @Override
    /**
     * Записывает данные подколлекции в файл, используя многострочный
     * формат. Некорректные данные удаляются (такие как -999999.0 и т.п.)
     */
    public void prepare(List<Map<String, String>> miniList)
            throws TaskException {
        List<Map<String, String>> allGISWellsManyString = oneStringToManyString(miniList);
        List<Map<String, String>> allGISWellsManyStringCorrect = allGISWellsManyString.stream()
                .filter(this::correctValue)
                .filter(this::correctDepth)
                .collect(Collectors.toList());

        file.write(allGISWellsManyStringCorrect);
    }

    /*
     * Переводит данные по значениям и глубине ГИС измерений из однострочного
     * формата в многострочный
     */
    private List<Map<String, String>> oneStringToManyString(List<Map<String, String>> list) {
        List<Map<String, String>> manyStringList = new ArrayList<>();

        list.forEach(e -> {
            String[] depth = e.get("Глубина ГИС").split("/");
            String[] values = e.get(nameMethodGIS).split("/");

            for (int i = 0; i < depth.length; i++) {
                Map<String, String> m = new HashMap<>();
                m.put("ID ТН", e.get("ID ТН"));
                m.put("Глубина ГИС", depth[i]);
                m.put(nameMethodGIS, values[i]);
                manyStringList.add(m);
            }
        });
        return manyStringList;
    }

    private boolean correctValue(Map<String, String> point) {
        if (point.get(nameMethodGIS).equals("-999999.0")
                || point.get(nameMethodGIS).equals("-999.75")
                || point.get(nameMethodGIS).equals("-995.75")
                || point.get(nameMethodGIS).equals("")
                || point.get(nameMethodGIS).equals(" ")) {
            return false;
        }
        return true;
    }

    private boolean correctDepth(Map<String, String> point) {
        if (point.get("Глубина ГИС").equals("-999999.0")
                || point.get("Глубина ГИС").equals("-999.75")
                || point.get("Глубина ГИС").equals("-995.75")
                || point.get("Глубина ГИС").equals("")
                || point.get("Глубина ГИС").equals(" ")) {
            return false;
        }
        return true;
    }

}
