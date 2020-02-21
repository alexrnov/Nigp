package nigp.tables;

import java.util.*;

public interface TablesAction {

    /**
     * Если в таблице с геолого-геофизическими данными найдена точка с
     * таким же идентификатором, как и в главной таблице с точками наблюдений,
     * вернуть геолого-геофизическую информацию по этой точке.
     * @param id идентификатор скважины
     * @param table таблица с геолого-геофизическими данными
     * @return геолого-геофизическая информация, соответсвующая точке с id
     */
    default Map<String, String> overlap(String id, PointInfoToSingleLine table) {
        for (Map<String, String> m : table.getTableSingleFormat()) {
            if (id.equals(m.get("ID ТН"))) {
                return m;
            }
        }
        return new HashMap<String, String>();
    }

    /**
     * Удаляет коллекции в списке, если коллекции имеют значения ключа key,
     * которые уже встречались (данный метод необходим, например,
     * поскольку в базе ИСИХОГИ встречаются одинаковые скважины с одинаковыми ID,
     * но отнесенные к разным объектам ГРР)
     * @param list список элементов, где возможны повторы
     * @return список с удаленными дублированными объектами
     */
    default List<Map<String, String>> deleteRepeatElementsInSubCollection(
                List<Map<String, String>> list, String key) {

        List<Map<String, String>> listWithNonRepeatKey = new ArrayList<>();

        //уникальные значения ключа key
        Set<String> uniqueValues = new HashSet<>();
        list.forEach(currentMap -> {
            uniqueValues.add(currentMap.get(key));
        });

        uniqueValues.forEach(currentValue -> {
            Optional<Map<String, String>> CoincideMap = list.stream()
                    .filter(currentMap -> currentMap.get(key).equals(currentValue))
                    .findFirst();//найти первый элемент со значением currentValue ключа key

            if (CoincideMap.isPresent()) {
                listWithNonRepeatKey.add(CoincideMap.get());
            }
        });
        return listWithNonRepeatKey;
    }

}
