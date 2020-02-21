package nigp.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Классы, реализующие данный интерфейс, направлены на решение
 * задач, связанных с анализом материалов минералогии
 * @author NovopashinAV
 */
public interface Mineralogy {

    /**
     * Возвращает список скважин с уникальными именемами скважин(номер/линия).
     * Используется для создания файла устьев скважин - чтобы нескольким пробам
     * из одной скважины соответствовала только одна скважина в файле устьев
     * Используется для задач, связанных с обработкой данных минералогии
     * из excel-файла (сервис "МСА по всем объектам") и загрузкой их в Micromine
     * @param topWells список скважин, где возможны скважины
     * с повторяющимися именами
     * @return скважины с уникальными именами
     */
    default List<Map<String, String>> getWellsWithUniqueNames(
                                            List<Map<String, String>> topWells) {
        List<Map<String, String>> uniqueValues = new ArrayList<>();
        uniqueValues.add(new HashMap<>(topWells.get(0)));
        boolean meeting;
        for (int i = 1; i < topWells.size(); i++) {
            int k = 0;
            do {
                meeting = equalsNamesWells(topWells.get(i), uniqueValues.get(k));
                k++;
            } while (k < uniqueValues.size() && !meeting);
            if (!meeting) {
                uniqueValues.add(new HashMap<>(topWells.get(i)));
            }
        }
        return uniqueValues;
    }

    default boolean equalsNamesWells(Map<String, String> currentWell,
                                     Map<String, String> uniqueWell) {
        return (currentWell.get("Линия").equals(uniqueWell.get("Линия"))
                && currentWell.get("Точка").equals(uniqueWell.get("Точка")));
    }

    default void assignIDToIntervals(List<Map<String, String>> uniqueTopWells,
                                     List<Map<String, String>> intervalWells) {
        uniqueTopWells.forEach(top -> {
            intervalWells.forEach(interval -> {
                if (top.get("Точка").equals(interval.get("Точка"))
                        && top.get("Линия").equals(interval.get("Линия"))) {
                    interval.put("ID", top.get("ID"));
                }
            });
        });
    }

    default Integer sortById(Object e) {
        Map<String, String> line = (Map<String, String>) e;
        return Integer.valueOf(line.get("ID"));
    }

    default Double sortByUpperContact(Object e) {
        Map<String, String> line = (Map<String, String>) e;
        String s = (String) line.get("От");
        return Double.valueOf(s);
    }

    /* поправка ИСИХОГИ */
    default void amendmentForMCA(Map<String, String> point) {
        Double x = Double.valueOf(point.get("X"));
        Double y = Double.valueOf(point.get("Y"));
        Double z = Double.valueOf(point.get("Z"));
        x = x - 10000;
        y = y - 20000;
        /* округлить координаты до двух знаков */
        x = Math.round(x * 100.0) / 100.0;
        y = Math.round(y * 100.0) / 100.0;
        z = Math.round(z * 100.0) / 100.0;
        point.put("X", x.toString());
        point.put("Y", y.toString());
        point.put("Z", z.toString());
    }

}
