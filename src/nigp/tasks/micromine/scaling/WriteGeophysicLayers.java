package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.*;

/**
 * Реализация шаблонного метода для записи данных по мощности
 * геофгеофизических пластов и сопутствующих статистических параметров.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД. Класс реализует функцию заполнения
 * пустых значений специальным символом. Это необходимо, поскольку
 * геофизические пласты могут содержать различные наборы параметров - какие то
 * будут иметь информацию только по мощности пласта, а какие-то по мощности
 * пласта и ГИС
 */
public class WriteGeophysicLayers extends ScalingData {

    /* обозначение для записи отсутствующего значения какого-либо из числовых параметров */
    protected final String missingValueCode = "-1";

    /* выходной текстовый файл интервалов Micromine */
    protected TextFileForMicromine file;

    /* названия числовых полей, где могут быть пустые значения */
    protected List<String> namesOfNumbersFields = new ArrayList<>();

    public WriteGeophysicLayers(TextFileForMicromine file) {
        this.file = file;
        namesOfNumbersFields.addAll(Arrays.asList(new String[]{"Кровля геофизического пласта",
                "Подошва геофизического пласта", "Мощность пласта",
                "Медиана значений ГИС", "Среднее значений ГИС", "Коэффициент вариации",
                "Ошибка среднего", "Среднеквадратическое отклонение",
                "Максимальное значение ГИС", "Минимальное значение ГИС"}));
    }

    /**
     * Записывает данные подколлекции в файл
     */
    public void prepare(List<Map<String, String>> miniList)
            throws TaskException {
        List<Map<String, String>> allWells =
                replaceEmptyNumbersValues(miniList);

        file.write(allWells);
    }

    /* Заменяет пустые числовые значения на значение переменной missingValueCode */
    protected List<Map<String, String>> replaceEmptyNumbersValues(List<Map<String, String>> list) {
        List<Map<String, String>> allStrings = new ArrayList<>();

        list.forEach(StringForWrite -> {

            Map<String, String> currentString = new HashMap<>();
            currentString.put("ID ТН", StringForWrite.get("ID ТН"));

            namesOfNumbersFields.forEach(numberField -> {
                if (StringForWrite.containsKey(numberField)) {
                    currentString.put(numberField, StringForWrite.get(numberField));
                } else {
                    currentString.put(numberField, missingValueCode);
                }

            });

            allStrings.add(currentString);
        });
        return allStrings;
    }

}

