package nigp.tasks.micromine;

import java.math.BigDecimal;
import java.util.*;

public interface Micromine {

    String ID = "ID ТН";
    String NAME_OF_MEAN_VALUE = "Медиана значений ГИС";
    String NOT_VALUE = "notValue";

    /**
     * Полностью копирует список со вложенными коллекциями Map<String, String>
     * @param inputList копируемый список с вложенными отображениями
     * @return полная копия списка со вложенными отображениями
     */
    default List<Map<String, String>> copyListWithSubMap(List<Map<String, String>> inputList) {
        List<Map<String, String>> copyList = new ArrayList<>();
        inputList.forEach(e -> {
            Map<String, String> copyMap = new HashMap<>(e);
            copyList.add(copyMap);
        });
        return copyList;
    }

    default boolean hasID(Map<String, String> point) {
        if (point.get("ID ТН").equals("-999999.0")
                || point.get("ID ТН").equals("-999.75")
                || point.get("ID ТН").equals("-995.75")
                || point.get("ID ТН").equals("")
                || point.get("ID ТН").equals(" ")) {
            return false;
        }
        return true;
    }

    default boolean hasDepth(Map<String, String> point) {
        if (point.get("Глубина ТН").equals("-999999.0")
                || point.get("Глубина ТН").equals("-999.75")
                || point.get("Глубина ТН").equals("-995.75")
                || point.get("Глубина ТН").equals("")
                || point.get("Глубина ТН").equals(" ")) {
            return false;
        }
        return true;
    }

    default boolean hasX(Map<String, String> point) {
        if (point.get("X факт.").equals("-999999.0")
                || point.get("X факт.").equals("-999.75")
                || point.get("X факт.").equals("-995.75")
                || point.get("X факт.").equals("")
                || point.get("X факт.").equals(" ")) {
            return false;
        }
        return true;
    }

    default boolean hasY(Map<String, String> point) {
        if (point.get("Y факт.").equals("-999999.0")
                || point.get("Y факт.").equals("-999.75")
                || point.get("Y факт.").equals("-995.75")
                || point.get("Y факт.").equals("")
                || point.get("Y факт.").equals(" ")) {
            return false;
        }
        return true;
    }

    default boolean hasZ(Map<String, String> point) {
        if (point.get("Z").equals("-999999.0")
                || point.get("Z").equals("-999.75")
                || point.get("Z").equals("-995.75")
                || point.get("Z").equals("")
                || point.get("Z").equals(" ")) {
            return false;
        }
        return true;
    }

    default boolean hasVerticale(Map<String, String> point, Set<String> nonVerticaleWells) {
        if (nonVerticaleWells.contains(point.get("Код типа ТН"))) {
            return false;
        }
        return true;
    }

    /* поправка ИСИХОГИ */
    default void amendment(Map<String, String> point) {
        Double x = Double.valueOf(point.get("X факт."));
        Double y = Double.valueOf(point.get("Y факт."));
        Double z = Double.valueOf(point.get("Z"));
        x = x - 10000;
        y = y - 20000;
        /* округлить координаты до двух знаков */
        x = round(x);
        y = round(y);
        z = round(z);
        point.put("X факт.", x.toString());
        point.put("Y факт.", y.toString());
        point.put("Z", z.toString());
    }

    default Double round(Double value) {
        return Math.round(value * 100.0)/100.0;
    }

    /**
     * Получить отметки залегания кровли и подошвы для оцениваемых
     * геофизических/стратиграфических пластов
     * @param point текущая ТН
     */
    default void getABS(Map<String, String> point, String intervals) {
        if (point.get("Z").equals("-999999.0") || point.get("Z").equals("")
                || point.containsKey(intervals) == false) {
            return;
        }
        String[] allContacts = point.get(intervals).split("/");

        BigDecimal upperContact;
        BigDecimal lowerContact;
        if (allContacts.length == 1) {
            String[] contacts = allContacts[0].split("-");
            upperContact = new BigDecimal(contacts[0]);
            lowerContact = new BigDecimal(contacts[1]);
        /*
        если больше одного пласта, тогда взять кровлю первого пласта
        и подошву последнего пласта
         */
        } else if (allContacts.length > 1) {
            String[] firstContacts = allContacts[0].
                    split("-");
            String[] lastContacts = allContacts[allContacts.length - 1].split("-");
            upperContact = new BigDecimal(firstContacts[0]);
            lowerContact = new BigDecimal(lastContacts[1]);
        } else {
            return;
        }

        if (intervals.equals("Интервал пласта")) {
            point.put("Кровля геофизического пласта", upperContact.toString());
            point.put("Подошва геофизического пласта", lowerContact.toString());
        } else if (intervals.equals("Интервалы искомых отложений")) {
            point.put("Кровля искомых отложений", upperContact.toString());
            point.put("Подошва искомых отложений", lowerContact.toString());
        }

    }

    /**
     * Добавить к массиву ТН по калию (калий выбран произвольно)
     * информацию по процентным соотношениям K, Th и U, а также по соотношеню
     * калия к урану. Соотношение к Торию не вычисляется, поскольку на
     * многих каротажных кривых Th стремится к нулю
     */
    default void arrDataOfRatioKThU(List<Map<String, String>> pointsForK,
                                    List<Map<String, String>> pointsForTh,
                                    List<Map<String, String>> pointsForU) {
        String k;
        String th;
        String u;

        for (Map<String, String> pointK: pointsForK) {
            k = pointK.get(NAME_OF_MEAN_VALUE);
            String id = pointK.get(ID);

            th = getComponentValue(pointsForTh, id);
            u = getComponentValue(pointsForU, id);

            if (! th.equals(NOT_VALUE) && ! u.equals(NOT_VALUE)) {
                Double[] percentages = getPercentages(k, th, u);
                pointK.put("K", String.valueOf(percentages[0]));
                pointK.put("Th", String.valueOf(percentages[1]));
                pointK.put("U", String.valueOf(percentages[2]));
                pointK.put("RatioKU", String.valueOf(getRatioKU(k, u)));

            } else {
                pointK.put("K", "-1");
                pointK.put("Th", "-1");
                pointK.put("U", "-1");
                pointK.put("RatioKU", "-1");
            }
        }
    }

    /**
     * Получить процентное соотношение по K, Th, U
     */
    default Double[] getPercentages(String k, String th, String u) {
        Double valueK;
        Double valueTh;
        Double valueU;
        Double allComponents;

        Double percentagesK;
        Double percentagesTh;
        Double percentagesU;

        valueK = Double.valueOf(k);
        valueTh = Double.valueOf(th);
        valueU = Double.valueOf(u);
        allComponents = valueK + valueTh + valueU;

        percentagesK = valueK / allComponents * 100.0;
        percentagesTh = valueTh / allComponents * 100.0;
        percentagesU = valueU / allComponents * 100.0;

        percentagesK = Math.round(percentagesK * 100.0) / 100.0;
        percentagesTh = Math.round(percentagesTh * 100.0) / 100.0;
        percentagesU = Math.round(percentagesU * 100.0) / 100.0;

        Double[] percentages = {percentagesK, percentagesTh, percentagesU};
        return percentages;
    }


    /**
     * Получить медианное значение Th или U для ТН с Id,
     * указанным во входных параметрах. Если ТН с Id не найдена
     * для методов Th или U, возвращается NOT_VALUE
     */
    default String getComponentValue(List<Map<String, String>> pointsForComponent,
                                     String id) {
        Optional<Map<String, String>> currentPointWithTh = pointsForComponent.stream()
                .filter(e -> e.get(ID).equals(id))
                .findFirst();
        String meanValue = NOT_VALUE;
        if (currentPointWithTh.isPresent()) {
            Map<String, String> list = currentPointWithTh.get();
            meanValue = list.get(NAME_OF_MEAN_VALUE);
        }

        return meanValue;
    }

    /** получить соотношение Калия к Урану */
    default Double getRatioKU(String k, String u) {
        Double k2 = Double.valueOf(k);
        Double u2 = Double.valueOf(u);
        Double ratio = -1.0;
        if (u2 > 0.0 && k2 > 0.0) {
            ratio = k2 / u2;
            ratio = Math.round(ratio * 100.0) / 100.0;
        }
        return ratio;
    }
}
