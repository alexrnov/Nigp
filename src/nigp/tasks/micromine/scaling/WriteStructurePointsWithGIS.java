package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nigp.tasks.Stratigraphy.MARKS_LOWER_CONTACTS;
import static nigp.tasks.Stratigraphy.MARKS_UPPER_CONTACTS;

/**
 * Реализация шаблонного метода для записи данных по абсолютным
 * отметкам кровли и подошвы рассматриваемой структуры. Записываются
 * также абсолютные отметки для осредненных значений ГИС, которые
 * распологаются между кровлей и подошвой структуры. Такие точки могут
 * потребоваться при создании блочной модели.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public class WriteStructurePointsWithGIS extends ScalingData {
    private TextFileForMicromine file;

    public WriteStructurePointsWithGIS(TextFileForMicromine file) {
        this.file = file;
    }

    @Override
    public void prepare(List<Map<String, String>> miniList) throws TaskException {
        List<Map<String, String>> allWellsManyString = oneStringToManyString(miniList);
        file.write(allWellsManyString);
    }

    protected List<Map<String, String>> oneStringToManyString(List<Map<String, String>> list) {
        List<Map<String, String>> manyStringList = new ArrayList<>();

        list.forEach(e -> {
            String[] from = e.get(MARKS_UPPER_CONTACTS).split("/");
            String[] to = e.get(MARKS_LOWER_CONTACTS).split("/");

            for (int i = 0; i < from.length; i++) {
                Map<String, String> m = new HashMap<>();
                m.put("ID ТН", e.get("ID ТН"));
                m.put("UIN", e.get("UIN"));
                m.put("X факт.", e.get("X факт."));
                m.put("Y факт.", e.get("Y факт."));
                m.put(MARKS_UPPER_CONTACTS, from[i]);
                m.put(MARKS_LOWER_CONTACTS, to[i]);
                m.put("Код Типа документирования", e.get("Код Типа документирования"));
                m.put("Код типа ТН", e.get("Код типа ТН"));
                m.put("Участок", e.get("Участок"));
                m.put("Объект", e.get("Объект"));
                m.put("Среднее значений ГИС", e.get("Среднее значений ГИС"));
                m.put("Медиана значений ГИС", e.get("Медиана значений ГИС"));
                m.put("Коэффициент вариации", e.get("Коэффициент вариации"));
                m.put("Максимальное значение ГИС", e.get("Максимальное значение ГИС"));
                m.put("Минимальное значение ГИС", e.get("Минимальное значение ГИС"));
                m.put("Среднеквадратическое отклонение", e.get("Среднеквадратическое отклонение"));
                m.put("Ошибка среднего", e.get("Ошибка среднего"));
                m.put("Отметки ГИС", e.get("Отметки ГИС"));
                m.put("Метод ГИС", e.get("Метод ГИС"));
                m.put("Z", e.get("Z"));
                m.put("Глубина ТН", e.get("Глубина ТН"));
                manyStringList.add(m);
            }
        });
        return manyStringList;
    }

}
