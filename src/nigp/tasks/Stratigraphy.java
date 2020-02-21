package nigp.tasks;

import java.util.*;
import java.util.stream.Stream;

/**
 * Классы, реализующие данный интерфейс, направлены на решение
 * задач, связанных с анализом материалов стратиграфии
 * @author NovopashinAV
 */
public interface Stratigraphy {

    String MARKS_UPPER_CONTACTS = "Отметки кровли";
    String MARKS_LOWER_CONTACTS = "Отметки подошвы";
    String AMOUNT_LAYERS = "Количество пластов";
    /**
     * Получить интервалы (глубина залегания кровли и подошвы)
     * оцениваемых стратиграфических подразделений
     * @param point Текущая ТН
     * @param findRocks массив индексов искомых стратиграфических подразделений
     * @param keyOfFindRocks название поля для ТН, куда записывается строка с
     * найденными интервалами для искомых стратиграфических подразделений
     */
    default void inputIntervalsForFindRocks(Map<String, String> point, String[] findRocks, String keyOfFindRocks) {

        if (point.containsKey("Стратиграфия") == false
                || point.containsKey("Кровля стратопласта") == false) {
            return;
        }

        String[] allStratigraphicIndexes = point.get("Стратиграфия").split("/");
        String[] upperContactStratigraphic = point.get("Кровля стратопласта").split("/");
        String[] lowerContactStratigraphic = point.get("Подошва стратопласта").split("/");

        if (allStratigraphicIndexes.length != upperContactStratigraphic.length) {
            return;
        }

        StringBuilder intervals = new StringBuilder();

        for (int j = 0; j < allStratigraphicIndexes.length; j++) {
            for (int i = 0; i < findRocks.length; i++) {
                if (allStratigraphicIndexes[j].contains(findRocks[i])) {
                    intervals.append(upperContactStratigraphic[j]);
                    intervals.append("-");
                    intervals.append(lowerContactStratigraphic[j]);
                    intervals.append("/");
                }
            }
        }

        if (intervals.length() != 0) {
            point.put(keyOfFindRocks, intervals.toString());
        }
    }

    /**
     * Получить интервалы (глубина залегания кровли и подошвы)
     * оцениваемого стратиграфического или литологического подразделения
     * и записать их в ассоциатнивный массив текущей ТН с
     * именем ключей "Отметки кровли" и "Отметки подошвы".
     * @param point Текущая ТН
     * @param nameOfStratOrLithIndex индекс искомого стратиграфического или
     * литологического подразделения
     */
    default void inputUpperAndLowerContacts(Map<String, String> point,
                                            String nameOfStratOrLithIndex,
                                            String KeyOfNameIndex) {
        String[] allIndexes = point.get(KeyOfNameIndex).split("/");
        String[] upperContactStratigraphic = point.get("Кровля стратопласта").split("/");
        String[] lowerContactStratigraphic = point.get("Подошва стратопласта").split("/");

        if (allIndexes.length != upperContactStratigraphic.length) {
            return;
        }

        StringBuilder upperContacts = new StringBuilder();
        StringBuilder lowerContacts = new StringBuilder();

        for (int j = 0; j < allIndexes.length; j++) {
            if (allIndexes[j].equals(nameOfStratOrLithIndex)) {
            //if (allIndexes[j].contains(nameOfStratOrLithIndex)) {
                upperContacts.append(upperContactStratigraphic[j]);
                upperContacts.append("/");
                lowerContacts.append(lowerContactStratigraphic[j]);
                lowerContacts.append("/");
            }
        }

        if (upperContacts.length() != 0 && lowerContacts.length() != 0) {
            point.put(MARKS_UPPER_CONTACTS, upperContacts.toString());
            point.put(MARKS_LOWER_CONTACTS, lowerContacts.toString());
        }
    }

    /**
     * Проверяет наличие всех ключей keys для точки наблюдения point
     * @param point текущая ТН
     * @param keys список ключей, наличие которых проверяется в данном методе
     * @return <code>false</code> - если не все ключи keys присутствуют для данной ТН.
     * <code>true</code> - если для ТН найдены все ключи keys
     */
    default boolean hasExistKeys(Map<String, String> point, List<String> keys) {
        for (String key : keys) {
            if (!point.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Вычисляет абсолютные отметки для кровли и подошвы стратиграфических
     * подразделений по текущей точке наблюдений и записывает новые значения
     * для ключей "Отметки кровли" и "Отметки подошвы". Также записывается
     * количество пластов для искомого стратиграфического подразделения по текущей
     * ТН (ключ "Количество пластов")
     * @param point текущая точка наблюдений
     */
    default void calculateAbsoluteMarksOfContacts(Map<String, String> point) {
        if (!point.containsKey(MARKS_UPPER_CONTACTS) || !point.containsKey(MARKS_LOWER_CONTACTS)) {
            return;
        }
        final Double z = Double.valueOf(point.get("Z"));
        String[] upperContacts = point.get(MARKS_UPPER_CONTACTS).split("\\/");
        String[] lowerContacts = point.get(MARKS_LOWER_CONTACTS).split("\\/");
        String absoluteUpperContacts = getAbsoluteContacts(upperContacts, z);
        String absoluteLowerContacts = getAbsoluteContacts(lowerContacts, z);
        if (absoluteUpperContacts.length() != 0 && absoluteLowerContacts.length() != 0) {
            point.put(MARKS_UPPER_CONTACTS, absoluteUpperContacts);
            point.put(MARKS_LOWER_CONTACTS, absoluteLowerContacts);
            point.put(AMOUNT_LAYERS, String.valueOf(upperContacts.length));
        }
    }

    /**
     * Вычисляет абсолютные отметки
     * @param contacts относительные отметки кровли/подошвы (т.е. за 0 берется устье скважины)
     * @param z абсолютная высотная отметка устья скважины
     * @return абсолютные отметки кровли/подошвы
     */
    default String getAbsoluteContacts(String[] contacts, final Double z) {
        Optional<String> absoluteContacts = Stream.of(contacts) //или Arrays.stream(upperContacts);
                .map(e -> {
                    Double w = z - Double.valueOf(e);
                    w = Math.round(w * 100.0) / 100.0;
                    return String.valueOf(w);
                })
                .reduce((e1, e2) -> e1 + "/" + e2); //или (a, b) -> new StringBuilder().append(a).append("/").append(b).toString()
        return (absoluteContacts.isPresent()) ? absoluteContacts.get() + "/" : "";
    }

    /**
     * Объединяет одинаковые стратиграфические индексы, если они следуют
     * друг за другом. Такая потребность может возникнуть, поскольку в базе
     * ИСИХОГИ для одной ТН может быть много пластов, следующих друг за другом,
     * и при этом, имеющих одинаковый индекс но различную литологию
     * @param point
     */
    default void unionSameStratOrLithIndexes(Map<String, String> point, String KeyOfNameIndex) {
        String[] stratOrLithIndexes = point.get(KeyOfNameIndex).split("/");
        String[] upperContacts = point.get("Кровля стратопласта").split("/");
        String[] lowerContacts = point.get("Подошва стратопласта").split("/");

        if (stratOrLithIndexes.length != upperContacts.length
                || upperContacts.length != lowerContacts.length) {
            return;
        }

        List<List<String>> unionStratOrLithIndexes = new ArrayList<>();
        List<List<String>> unionUpperContacts = new ArrayList<>();
        List<List<String>> unionLowerContacts = new ArrayList<>();

        List<String> stratOrLithUnion = new ArrayList<>();
        List<String> upperUnion = new ArrayList<>();
        List<String> lowerUnion = new ArrayList<>();
        for (int i = 0; i < stratOrLithIndexes.length; i++) {
            stratOrLithUnion.add(stratOrLithIndexes[i]);
            upperUnion.add(upperContacts[i]);
            lowerUnion.add(lowerContacts[i]);
            if (i == stratOrLithIndexes.length - 1) {
                unionStratOrLithIndexes.add(stratOrLithUnion);
                unionUpperContacts.add(upperUnion);
                unionLowerContacts.add(lowerUnion);
                break;
            }
            if (!stratOrLithIndexes[i].equals(stratOrLithIndexes[i + 1])) {
                unionStratOrLithIndexes.add(stratOrLithUnion);
                unionUpperContacts.add(upperUnion);
                unionLowerContacts.add(lowerUnion);
                stratOrLithUnion = new ArrayList<>();
                upperUnion = new ArrayList<>();
                lowerUnion = new ArrayList<>();
            }
        }

        StringBuilder newStratOrLithIndexes = new StringBuilder();
        StringBuilder newUpperContacts = new StringBuilder();
        StringBuilder newLowerContacts = new StringBuilder();

        for (int i = 0; i < unionStratOrLithIndexes.size(); i++) {

            newStratOrLithIndexes.append(unionStratOrLithIndexes.get(i).get(0));
            newStratOrLithIndexes.append("/");
            newUpperContacts.append(unionUpperContacts.get(i).get(0));
            newUpperContacts.append("/");
            if (unionStratOrLithIndexes.size() > 0) {
                newLowerContacts.append(unionLowerContacts.get(i).get(unionLowerContacts.get(i).size() - 1));
            } else {
                newLowerContacts.append(unionLowerContacts.get(i).get(0));
            }
            newLowerContacts.append("/");
        }

        point.put(KeyOfNameIndex, newStratOrLithIndexes.toString());
        point.put("Кровля стратопласта", newUpperContacts.toString());
        point.put("Подошва стратопласта", newLowerContacts.toString());

    }

    /**
     * В зависимости от индексов перекрывающих отложений, определяет принадлежность
     * стратиграфических подразделений текущей точки наблюдений к перекрывающим (P)
     * или вмещающим отложениям
     * @param point текущая точка наблюдений
     * @param overlappingRocks массив, содержащий индексы перекрывающих отложений
     * @param keyOfNameIndex название ключа, где хранятся стратиграфические индексы для
     * текущей точки наблюдений (значение перезаписывается в формат P/V)
     */
    default void unionStratIndexesToStructure(Map<String, String> point,
                                              String[] overlappingRocks, String keyOfNameIndex) {
        String[] allStratigraphicIndexes = point.get("Стратиграфия").split("/");
        if (allStratigraphicIndexes.length == 0) {
            return;
        }
        StringBuilder elementsOfStructure = new StringBuilder();
        boolean b;
        for (int j = 0; j < allStratigraphicIndexes.length; j++) {
            b = false;
            for (int i = 0; i < overlappingRocks.length; i++) {
                if (allStratigraphicIndexes[j].contains(overlappingRocks[i])) {
                    b = true;
                    break;
                }
            }
            if (b) {
                elementsOfStructure.append("P");
            } else {
                elementsOfStructure.append("V");
            }
            elementsOfStructure.append("/");
        }
        point.put(keyOfNameIndex, elementsOfStructure.toString());
    }

    /**
     * Метод берет данные стратиграфии (стратиграфические индексы), и присваивает
     * им, в зависимости от принадлежности к геологической структуре, определенные
     * номера, затем количество номеров сокращается - из списка убираются одинаковые
     * номера, следующие друг за другом. При этом их интервалы объединяются. Если
     * для какого-то стратиграфического подраздления не найдено соответсвия по
     * геологической структуре - ему присвается значение 9
     * @param point текущая ТН
     * @param stratigraphicUnits массив строк, каждая из которых входит в
     * тратиграфический индекс определенного типа или равна ему.
     * переменная должна иметь формат типа "Q,N,J;T2-3;O1,G", где через точку с запятой
     * перечисляются символы, которые входят в стратиграфическиt индекс, отнесенный
     * к определенной структуре (перекрывающие отложения, кора выветривания,
     * вмещающие отложения и т. п.). Если для определенной структуры указаны несколько
     * символов (т.е. стратиграфических подразделений), то они перечисляются через
     * запятую
     * @param keyOfNameIndex название ключа, соответствующего данным стратиграфии
     * (по результатам работы методы значение перезаписывается
     * в формат 0/1/2...)
     */
    default void renameStratIndexesToUnionAndAbbreviate(Map<String, String> point,
                                                        String[] stratigraphicUnits,
                                                        String keyOfNameIndex) {
        if (!point.containsKey(keyOfNameIndex) ||
                !point.containsKey("Кровля стратопласта") ||
                !point.containsKey("Подошва стратопласта")) {
            return;
        }

        String[] allStratigraphicIndexes = point.get(keyOfNameIndex).split("/");
        if (allStratigraphicIndexes.length == 0) {
            return;
        }

        String[] strat = point.get(keyOfNameIndex).split("/");
        StringBuilder unitIndexes = new StringBuilder();
        boolean b;
        for (int i = 0; i < strat.length; i++) {
            b = false;
            int j = 0;
            int numberOfUnit = 9;
            while(j < stratigraphicUnits.length && b == false) {
                String[] elementsOfUnit = stratigraphicUnits[j].split(",");
                int h = 0;
                while (h < elementsOfUnit.length && b == false) {
                    //if (strat[i].contains(elementsOfUnit[h])) {
                    if (strat[i].equals(elementsOfUnit[h])) {
                        numberOfUnit = j;
                        b = true;
                    }
                    h++;
                }
                j++;
            }
            unitIndexes.append(numberOfUnit);
            unitIndexes.append("/");
        }
        point.put(keyOfNameIndex, unitIndexes.toString());
        unionSameStratOrLithIndexes(point, keyOfNameIndex);
    }

    /**
     * Вычисляет абсолютную отметку рельефа для осредненного значения ГИС. Отметка
     * распологается между кровлей и подошвой оцениваемой структуры (перекрывающий комлекс,
     * кора выветривания, вмещающие отложения и т.д.). Расположение является условным,
     * и необходимо для упрощения блочного моделирования в Micromine
     * @param point текущая точка наблюдений
     * @param keyNameOfABSForGISValue название ключа с абсолютной отметкой рельефа
     * для осредненного значения ГИС
     */
    default void calcABSforMeanGISValueBetweenContacts(Map<String, String> point,
                                                       String keyNameOfABSForGISValue) {
        if (!point.containsKey(MARKS_UPPER_CONTACTS) ||
                !point.containsKey(MARKS_LOWER_CONTACTS)) {
            return;
        }
        Double upper = Double.valueOf(point.get(MARKS_UPPER_CONTACTS).split("/")[0]);
        Double lower = Double.valueOf(point.get(MARKS_LOWER_CONTACTS).split("/")[0]);
        Double abs = upper - ((upper - lower) / 2);
        abs = Math.round(abs * 100.0) / 100.0;
        point.put(keyNameOfABSForGISValue, String.valueOf(abs));
    }

    /**
     * Метод для текущей точки наблюдений(ТН) проверяет, совпадает ли ее стратиграфия
     * с шаблоном structureTemplate, если нет, проводится дополнительная проверка
     * на вхождение шаблона в стратиграфию точки наблюдений. Т.е. если, например,
     * стратиграфией вида является строка 9/0/1/2/3/9, а шаблоном строка 0/1/2/3/,
     * можно сказать, что шаблон входит в стратиграфию ТН. Тогда происходит отсев, всех
     * тех индексов, которые лежат за пределами шаблона. При этом вычисляется местоположение
     * шаблона в стратиграфии ТН, для того, чтобы можно было отбросить абсолютные отметки
     * кровли и подошвы тех стратиграфических подразделений, которые лежат вне шаблона.
     * @param point текущая точка наблюдений
     * @param structureTemplate использующийся шаблон структурных подразделений
     * формата 0/1/2/3...
     */
    default void transformSomeWellsToCorrectStructure(Map<String, String> point,
                                                 String structureTemplate) {
        String currentStructure = point.get("Стратиграфия");
        //System.out.println("old = " + currentStructure);
        if (currentStructure.contains(structureTemplate)
                && !currentStructure.equals(structureTemplate)) {
            removeExcess(point, structureTemplate, currentStructure);
        }
        //System.out.println("---------------------");
    }

    /**
     * Метод для текущей точки наблюдений(ТН) проверяет, совпадает ли ее стратиграфия
     * с шаблоном structureTemplate, если нет, проводится дополнительная проверка
     * на вхождение шаблона в стратиграфию точки наблюдений. Т.е. если, например,
     * стратиграфией вида является строка 9/0/1/2/3/9, а шаблоном строка 0/1/2/3/,
     * можно сказать, что шаблон входит в стратиграфию ТН. Тогда происходит отсев, всех
     * тех индексов, которые лежат за пределами шаблона. При этом вычисляется местоположение
     * шаблона в стратиграфии ТН, для того, чтобы можно было отбросить абсолютные отметки
     * кровли и подошвы тех стратиграфических подразделений, которые лежат вне шаблона.
     * Данный метод отличается от метода transformSomeWellsToCorrectStructure тем, что
     * здесь указываются конкретные геологические обстановки - юра/дяхтар/кора/ордовик,
     * юра/дяхтар/ордовик, юра/кора/ордовик, дяхтар/кора/ордовик, юра/ордовик,
     * дяхтар/ордовик, кора/ордовик.
     * @param point текущая точка наблюдений
     */
    default void transformSomeWellsToCorrectStructureConcrete(Map<String, String> point) {
        String currentStructure = point.get("Стратиграфия");
        if (currentStructure.contains("0/1/2/3/")
                && !currentStructure.equals("0/1/2/3/")) {
            removeExcess(point, "0/1/2/3/", currentStructure);
        } else if (currentStructure.contains("0/1/3/") && !currentStructure.equals("0/1/3/")) {
            removeExcess(point, "0/1/3/", currentStructure);
        } else if (currentStructure.contains("0/2/3/") && !currentStructure.equals("0/2/3/")) {
            removeExcess(point, "0/2/3/", currentStructure);
        } else if (currentStructure.contains("1/2/3/") && !currentStructure.equals("1/2/3/")
                        && !currentStructure.equals("0/1/2/3/")) {
            removeExcess(point, "1/2/3/", currentStructure);
        } else if (currentStructure.contains("0/3/") && !currentStructure.equals("0/3/")) {
            removeExcess(point, "0/3/", currentStructure);
        } else if (currentStructure.contains("1/3/") && !currentStructure.equals("1/3/")
                && !currentStructure.equals("0/1/3/")) {
            removeExcess(point, "1/3/", currentStructure);
        } else if (currentStructure.contains("2/3/") && !currentStructure.equals("2/3/")
                && !currentStructure.equals("0/2/3/") && !currentStructure.equals("0/1/2/3/")) {
            removeExcess(point, "2/3/", currentStructure);
        }
    }

    /**
     * Метод производит отсев всех индексов, которые лежат за пределами шаблона.
     * При этом вычисляется местоположение шаблона в стратиграфии ТН, для того,
     * чтобы можно было отбросить абсолютные отметки кровли и подошвы тех
     * стратиграфических подразделений, которые лежат вне шаблона.
     * @param point текущая точка наблюдений
     * @param structureTemplate шаблон стратиграфического разреза
     * @param currentStructure стратиграфический разрез текущей скважины
     */
    default void removeExcess(Map<String, String> point, String structureTemplate, String currentStructure) {
        String[] currentArr = currentStructure.split("/");
        String[] templateArr = structureTemplate.split("/");
        int sizeCurrentArr = currentArr.length;
        int sizeTemplateArr = templateArr.length;
        int amount = sizeCurrentArr - sizeTemplateArr;
        String[] selectArr = new String[sizeTemplateArr];
        for (int i = 0; i < amount + 1; i++) {
            for (int j = i; j < sizeTemplateArr + i; j++) {
                selectArr[j - i] = currentArr[j];
            }
            if (Arrays.equals(selectArr, templateArr)) {
                String[] upperContacts = point.get("Кровля стратопласта").split("/");
                String[] lowerContacts = point.get("Подошва стратопласта").split("/");
                StringBuilder selectUpperContacts = new StringBuilder();
                StringBuilder selectLowerContacts = new StringBuilder();
                for (int k = i; k <= (i + sizeTemplateArr - 1); k++) {
                    selectUpperContacts.append(upperContacts[k]);
                    selectUpperContacts.append("/");
                    selectLowerContacts.append(lowerContacts[k]);
                    selectLowerContacts.append("/");
                }
                point.put("Кровля стратопласта", selectUpperContacts.toString());
                point.put("Подошва стратопласта", selectLowerContacts.toString());
                point.put("Стратиграфия", structureTemplate);
                //System.out.println("new = " + structureTemplate);
                break;
            }
        }
    }

    /**
     * Если имеется неполный стратиграфический разрез, как например: 0/1/3/, 0/2/3/,
     * 1/2/3, 0/3/, 1/3, 2/3, программа делает из него разрез 0/1/2/3/
     * добавляя 1 и/или 2 разреза с минимально возможной мощностью (0.1 метра).
     * Соответсвтенно меняются значения ключей "Стратиграфия",
     * "Кровля стратопласта", "Подошва стратопласта"
     * @param point текущая точка наблюдений
     */
    default void establishMinWidth(Map<String, String> point) {
        String strat = point.get("Стратиграфия");
        if (strat.equals("0/1/2/3/")) {
            return;
        }
        /*
        System.out.println("old strat = " + strat);
        System.out.println("old up = " + point.get("Кровля стратопласта"));
        System.out.println("old low = " + point.get("Подошва стратопласта"));
        */
        String[] upperCase = point.get("Кровля стратопласта").split("/");
        String[] lowerCase = point.get("Подошва стратопласта").split("/");
        StringBuilder up = new StringBuilder();
        StringBuilder low = new StringBuilder();
        if (strat.equals("0/1/3/")) {
            Double value = Double.valueOf(lowerCase[1]) + 0.2;
            value = Math.round(value * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(upperCase[2]);up.append("/");
            up.append(upperCase[2]);up.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(lowerCase[1]);low.append("/");
            low.append(value);low.append("/");
            low.append(lowerCase[2]);low.append("/");
        } else if (strat.equals("0/2/3/")) {
            Double value = Double.valueOf(upperCase[1]) + 0.2;
            value = Math.round(value * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(upperCase[2]);up.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(value);low.append("/");
            low.append(lowerCase[1]);low.append("/");
            low.append(lowerCase[2]);low.append("/");
        } else if(strat.equals("1/2/3/")) {
            Double value = Double.valueOf(upperCase[0]) + 0.2;
            value = Math.round(value * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(upperCase[2]);up.append("/");
            low.append(value);low.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(lowerCase[1]);low.append("/");
            low.append(lowerCase[2]);low.append("/");
        } else if (strat.equals("0/3/")) {
            Double value1 = Double.valueOf(upperCase[1]) + 0.2;
            value1 = Math.round(value1 * 100.0) / 100.0;
            Double value2 = Double.valueOf(upperCase[1]) + 0.4;
            value2 = Math.round(value2 * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(value1);up.append("/");
            up.append(upperCase[1]);up.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(value1);low.append("/");
            low.append(value2);low.append("/");
            low.append(lowerCase[1]);low.append("/");
        } else if (strat.equals("1/3/")) {
            Double value1 = Double.valueOf(upperCase[0]) + 0.2;
            value1 = Math.round(value1 * 100.0) / 100.0;
            Double value2 = Double.valueOf(upperCase[1]) + 0.2;
            value2 = Math.round(value2 * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            low.append(value1);low.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(value2);low.append("/");
            low.append(lowerCase[1]);low.append("/");
        } else if (strat.equals("2/3/")) {
            Double value1 = Double.valueOf(upperCase[0]) + 0.2;
            value1 = Math.round(value1 * 100.0) / 100.0;
            Double value2 = Double.valueOf(upperCase[0]) + 0.4;
            value2 = Math.round(value2 * 100.0) / 100.0;
            up.append(upperCase[0]);up.append("/");
            up.append(value1);up.append("/");
            up.append(upperCase[0]);up.append("/");
            up.append(upperCase[1]);up.append("/");
            low.append(value1);low.append("/");
            low.append(value2);low.append("/");
            low.append(lowerCase[0]);low.append("/");
            low.append(lowerCase[1]);low.append("/");
        }
        point.put("Стратиграфия", "0/1/2/3/");
        point.put("Кровля стратопласта", up.toString());
        point.put("Подошва стратопласта", low.toString());
        //System.out.println(point.get("Стратиграфия"));
        /*
        System.out.println(point.get("Кровля стратопласта"));
        System.out.println(point.get("Подошва стратопласта"));
        System.out.println("-------------------------");
        */
    }
}
