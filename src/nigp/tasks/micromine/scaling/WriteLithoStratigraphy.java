package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация шаблонного метода для записи данных литостратиграфии.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public class WriteLithoStratigraphy extends ScalingData {

    /*
     * Символы, переводящие текст на следующую строку. Эти символы
     * встречаются в описании пород в таблице литостратиграфии. Их
     * еобходимо удалять, поскольку они сбивают разделение полей
     * таблицы в micromine
     */
    private final String s1 = String.valueOf((char) 10);
    private final String s2 = String.valueOf((char) 13);

    private TextFileForMicromine file;

    public WriteLithoStratigraphy(TextFileForMicromine file) {
        this.file = file;
    }

    /**
     * Записывает данные подколлекции в файл, используя многострочный формат
     */
    public void prepare(List<Map<String, String>> miniList)
            throws TaskException {
        List<Map<String, String>> allLithoStratManyString =
                oneStringToManyString(miniList);
        file.write(allLithoStratManyString);
    }

    /*
     * Переводит данные по стратиграфии, литологии, кровле и подошве
     * из однострочного формата в многострочный
     */
    private List<Map<String, String>> oneStringToManyString(List<Map<String, String>> list) {
        List<Map<String, String>> manyStringList = new ArrayList<>();

        list.forEach(e -> {
            String[] from = e.get("Кровля стратопласта").split("/");
            String[] to = e.get("Подошва стратопласта").split("/");
            String[] stratigraphy = e.get("Стратиграфия").split("/");
            String[] lithology = e.get("Литология").split("/");
            String[] description = e.get("Описание породы").split("/");
            for (int i = 0; i < stratigraphy.length; i++) {
                Map<String, String> m = new HashMap<>();
                m.put("ID ТН", e.get("ID ТН"));
                m.put("Кровля стратопласта", from[i]);
                m.put("Подошва стратопласта", to[i]);
                m.put("Стратиграфия", stratigraphy[i]);
                m.put("Литология", lithology[i]);
                String d = description[i];
                d = d.replace(s1, "_");
                d = d.replace(s2, "_");
                m.put("Описание породы", d);
                manyStringList.add(m);
            }
        });
        return manyStringList;
    }
}
