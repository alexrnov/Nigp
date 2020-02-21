package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nigp.tasks.Stratigraphy.AMOUNT_LAYERS;
import static nigp.tasks.Stratigraphy.MARKS_LOWER_CONTACTS;
import static nigp.tasks.Stratigraphy.MARKS_UPPER_CONTACTS;

/**
 * Реализация шаблонного метода для записи данных по абсолютным
 * отметкам кровли и подошвы искомого стратиграфического подразделения.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public class WriteStratOrLithPoints extends ScalingData {
    private TextFileForMicromine file;
    /* название анализируемого стратиграфического или литологического индекса */
    private String nameOfStratOrLithSubject = "Возраст";
    public WriteStratOrLithPoints(TextFileForMicromine file) {
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
                m.put(AMOUNT_LAYERS, e.get(AMOUNT_LAYERS));
                m.put(nameOfStratOrLithSubject, e.get(nameOfStratOrLithSubject));
                manyStringList.add(m);
            }

        });
        return manyStringList;
    }

    public void setNameOfStratOrLithSubject(String nameOfStratOrLithSubject) {
        this.nameOfStratOrLithSubject = nameOfStratOrLithSubject;
    }
}
