package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация шаблонного метода для записи данных по искомому
 * геофизическому пласту и его условным пластам.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */

public class WriteImplicitGeophysicLayers extends WriteAllGeophysicLayers {
    public WriteImplicitGeophysicLayers(TextFileForMicromine file) {
        super(file);
    }

    @Override
    protected List<Map<String, String>> oneStringToManyString(List<Map<String, String>> list) {
        List<Map<String, String>> manyStringList = new ArrayList<>();

        list.forEach(e -> {
            String[] from = e.get("Кровли пластов").split("/");
            String[] to = e.get("Подошвы пластов").split("/");
            String[] geoLayers = e.get("Геопласты").split("/");

            int size = from.length;
            if (from.length == size && to.length == size && to.length == geoLayers.length) {
                for (int i = 0; i < size; i++) {
                    Map<String, String> m = new HashMap<>();
                    m.put("ID ТН", e.get("ID ТН"));
                    m.put("Кровли пластов", from[i]);
                    m.put("Подошвы пластов", to[i]);
                    m.put("Геопласты", geoLayers[i]);
                    manyStringList.add(m);
                }
            }
        });
        return manyStringList;
    }
}
