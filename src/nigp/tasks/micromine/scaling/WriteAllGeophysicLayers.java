package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация шаблонного метода для записи данных по всем
 * геофизическим пластам. Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public class WriteAllGeophysicLayers extends ScalingData {

    private TextFileForMicromine file;

    public WriteAllGeophysicLayers(TextFileForMicromine file) {
        this.file = file;

    }

    @Override
    /**
     * Записывает данные подколлекции в файл, используя многострочный
     * формат.
     */
    public void prepare(List<Map<String, String>> miniList)
            throws TaskException {
        List<Map<String, String>> allWellsManyString = oneStringToManyString(miniList);
        file.write(allWellsManyString);
    }

    /*
     * Переводит данные по данным геопластов из однострочного
     * формата в многострочный
     */

    protected List<Map<String, String>> oneStringToManyString(List<Map<String, String>> list) {
        List<Map<String, String>> manyStringList = new ArrayList<>();

        list.forEach(e -> {
            String[] from = e.get("Кровля геопласта").split("/");
            String[] to = e.get("Подошва геопласта").split("/");
            String[] geoplast = e.get("Геопласт").split("/");
            String[] strat = e.get("Стратиграфия геопласта").split("/");
            String[] descript = e.get("Описание геопласта").split("/");

            int size = from.length;
            if (to.length == size && geoplast.length == size
                    && strat.length == size && descript.length == size) {
                for (int i = 0; i < size; i++) {
                    Map<String, String> m = new HashMap<>();
                    m.put("ID ТН", e.get("ID ТН"));
                    m.put("Кровля геопласта", from[i]);
                    m.put("Подошва геопласта", to[i]);
                    m.put("Геопласт", geoplast[i]);
                    m.put("Стратиграфия геопласта", strat[i]);
                    m.put("Описание геопласта", descript[i]);
                    manyStringList.add(m);
                }
            }
        });
        return manyStringList;
    }
}
