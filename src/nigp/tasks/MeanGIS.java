package nigp.tasks;

import nigp.tasks.micromine.amendment.Coefficient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Классы, реализующие данный интерфейс, направлены на решение
 * задач, связанных с вычислением среднего значения ГИС
 * @author NovopashinAV
 *
 */
public interface MeanGIS {

    /* название ключа с суммой измерений ГИС по геофизическому пласту */
    String sumValuesGIS = "Сумма значений ГИС";

    /*
     * название ключа с суммой количества измерений ГИС
     * по геофиическому пласту
     */
    String sumDepthsGIS = "Количество измерений ГИС";

    /*
     * название ключа со средним арифметическим измерений ГИС
     * по геофизическому пласту
     */
    String arithmeticMeanGIS = "Среднее значений ГИС";

    /*
     * название ключа с минимальным значением ГИС-измерений
     * по геофизическому пласту
     */
    String minValueGIS = "Минимальное значение ГИС";

    /*
     * название ключа с максимальным значением ГИС-измерений
     * по геофизическому пласту
     */
    String maxValueGIS = "Максимальное значение ГИС";

    /*
     * название ключа со среднеквадратическим отклонением для значений ГИС
     * по геофизическому пласту
     */
    String squaredError = "Среднеквадратическое отклонение";

    /*
     * название ключа с ошибкой среднего для значений ГИС
     * по геофизическому пласту
     */
    String errorOfMean = "Ошибка среднего";

    /* название ключа с медианой измерений ГИС по геофизическому пласту */
    String medianGIS = "Медиана значений ГИС";

    /*
     * название ключа с коэффициентом вариации значений ГИС
     * по геофизическому пласту
     */
    String variation = "Коэффициент вариации";


    String stratigraphicField = "Стратиграфия";

    /**
     * Сверяет измеренное значение ГИС с определенными числами,
     * свидетельствующими об отсутствии данных.
     * @param survey список, содержащий два элемента.
     * Первый элемент - глубина измерений ГИС, второй элемент -
     * величина измерений ГИС.
     * @return <code>true</code> если измеренное значение ГИС имеет
     * корректный формат.
     */
    default boolean isExistValue(List<Double> survey) {
        if (survey.get(1) != -999999.0 && survey.get(1) != -999.75
                && survey.get(1) != -995.75) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Сверяет измеренное значение ГИС с определенными числами,
     * свидетельствующими об отсутствии данных.
     * @param value измеренное значение ГИС
     * @return <code>true</code> если измеренное значение ГИС имеет
     * корректный формат.
     */
    default boolean isValidValue(Double value) {
        if (value != -999999.0 && value != -999.75 && value != -995.75) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * @param survey список, содержащий два элемента.
     * Первый элемент - глубина измерений ГИС, второй элемент -
     * величина измерений ГИС.
     * @param upperContact кровля геофизического пласта
     * (или стратиграфического подразделения)
     * @param lowerContact подошва геофизического пласта
     * (или стратиграфического подразделения)
     * @return <code>true</code> если глубина измеренного значения ГИС
     * попадает в интервал геологической/геофизической структуры.
     */
    default boolean isOverlap(List<Double> survey, Double upperContact,
                              Double lowerContact) {
        if (survey.get(0) >= upperContact && survey.get(0) <= lowerContact) {
            return true;
        }
        return false;
    }

    /**
     * Вычисляет медианное значение для набора измерений ГИС.
     * @param valuesGIS массив значений ГИС-измерений
     * @return Медианное значение для набора измерений ГИС.
     */
    default Double getMedian(Double[] valuesGIS) {
        Arrays.sort(valuesGIS);
        int middle = valuesGIS.length / 2;
        Double median = valuesGIS[middle];
        if (valuesGIS.length % 2 == 0) {
            median = (median + valuesGIS[middle - 1]) / 2;
        }
        median = Math.round(median * 100.0) / 100.0;
        return median;
    }

    /**
     * Вычисляет среднее квадратическое значение по генеральной
     * совокупности с измерениями ГИС (Для выборочной совокупности
     * в знаменателе формулы вместо n, должно быть n - 1)
     * @param valuesGIS массив с данными измерений ГИС.
     * @param average среднее арифметическое
     * @return среднее квадратическое значение по генеральной
     * совокупности с измерениями ГИС
     */
    default Double getSquaredErrorDistance(Double[] valuesGIS, Double average) {
        Double squaredError = 0.0;
        for (int i = 0; i < valuesGIS.length; i++) {
            squaredError += Math.pow(valuesGIS[i] - average, 2);
        }
        squaredError = Math.sqrt(squaredError/valuesGIS.length);
        return squaredError;
    }

    /**
     * Вычисляет коэффициент вариации для совокупности измерений ГИС
     * @param squaredError среднеквадратическое отклонение
     * @param average среднее арифметическое
     * @return коэффициент вариации для совокупности измерений ГИС
     */
    default String getVariabilityIndex(Double squaredError, Double average) {
        double variation = (squaredError / average) * 100;
        variation = Math.round(variation);
        String variationPercent = String.valueOf((int) variation);
        return variationPercent;
    }

    /**
     * Метод округляет вещественное число до сотых и приводит к
     * текстовому формату
     * @param v вещественное число
     * @return строковое представление числа, округленного до сотых
     */
    default String strRoundHundred(Double v) {
        Double d = Math.round(v * 100.0) / 100.0;
        return d.toString();
    }

    /**
     * Выбирает физические значения ГИС-измерений, глубины измерения
     * которых соответствуют интервалам оцениваемых стратиграфических
     * подразделений, и вычисляет для них статистические параметры
     * @param point текущая ТН
     * @param stratigraphyIntervals стратиграфические интервалы для которых
     * осуществляется поиск данных ГИС
     * @param nameMethodGIS название метода ГИС, для которого осуществляется поиск
     */
    default void getMeanGISforIntervals(Map<String, String> point, String stratigraphyIntervals,
                                        String nameMethodGIS) {

        if (point.containsKey(stratigraphyIntervals) == false
                || point.containsKey(nameMethodGIS) == false
                || point.containsKey("Глубина ГИС") == false) {
            return;
        }

        String[] intervals = point.get(stratigraphyIntervals).split("/");
        String[] valuesGIS = point.get(nameMethodGIS).split("/");
        String[] depthsGIS = point.get("Глубина ГИС").split("/");

        Double[][] jointArray = new Double[depthsGIS.length][2];

        for (int i = 0; i < depthsGIS.length; i++) {
            jointArray[i][0] = Double.valueOf(depthsGIS[i]);
            jointArray[i][1] = Double.valueOf(valuesGIS[i]);
        }

        List<List<Double>> measurementsGIS = Arrays.stream(jointArray)
                .map(Arrays::asList)
                .collect(Collectors.toList());

        List<List<Double>> selectGIS = new ArrayList<>();

        for (String interval : intervals) {
            String[] contacts = interval.split("-");
            Double upperContact = Double.valueOf(contacts[0]);
            Double lowerContact = Double.valueOf(contacts[1]);
            List<List<Double>> intervalGIS = measurementsGIS.stream()
                    .filter(survey -> isOverlap(survey, upperContact, lowerContact))
                    .filter(this::isExistValue)
                    .collect(Collectors.toList());
            selectGIS.addAll(intervalGIS);
        }

        if (selectGIS.size() != 0) {
            calcStatistic(selectGIS, point);
        }

    }

    /**
     * Выбирает физические значения ГИС-измерений, глубины измерения
     * которых соответствуют всему стволу скважины,
     * и вычисляет для них статистические параметры
     * @param point текущая ТН
     * @param nameMethodGIS название метода ГИС, для которого осуществляется поиск
     */
    default void getMeanGISforAllWell(Map<String, String> point, String nameMethodGIS) {
        if (point.containsKey(nameMethodGIS) == false
                || point.containsKey("Глубина ГИС") == false) {
            return;
        }

        String[] valuesGIS = point.get(nameMethodGIS).split("/");
        String[] depthsGIS = point.get("Глубина ГИС").split("/");

        Double[][] jointArray = new Double[depthsGIS.length][2];

        for (int i = 0; i < depthsGIS.length; i++) {
            jointArray[i][0] = Double.valueOf(depthsGIS[i]);
            jointArray[i][1] = Double.valueOf(valuesGIS[i]);
        }

        List<List<Double>> measurementsGIS = Arrays.stream(jointArray)
                .map(Arrays::asList)
                .collect(Collectors.toList());

        List<List<Double>> intervalGIS = measurementsGIS.stream()
                .filter(this::isExistValue)
                .collect(Collectors.toList());

        if (intervalGIS.size() != 0) {
            calcStatistic(intervalGIS, point);
        }
    }

    /**
     * Метод вычисляет статистические характеристики для выборки ГИС-измерений,
     * такие как: среднее арифметическое, минимальное и максимальное
     * значения, среднеквадратическое отклонение, ошибка среднего,
     * коэффициент вариации, медианное значение
     * @param selectGIS массив с выбранными данными по ГИС
     * @param point текущая ТН
     */
     default void calcStatistic(List<List<Double>> selectGIS, Map<String, String> point) {
        DoubleSummaryStatistics d = selectGIS.stream()
                .collect(Collectors.summarizingDouble(e -> e.get(1)));

        point.put(sumValuesGIS, strRoundHundred(d.getSum()));
        point.put(sumDepthsGIS, Integer.toString(selectGIS.size()));
        point.put(arithmeticMeanGIS, strRoundHundred(d.getAverage()));
        point.put(minValueGIS, strRoundHundred(d.getMin()));
        point.put(maxValueGIS, strRoundHundred(d.getMax()));

        Double[] values = selectGIS.stream()
                .map(survey -> survey.get(1))
                .toArray(Double[]::new);

        Double squaredError = getSquaredErrorDistance(values, d.getAverage());
        Double errorOfMean = squaredError/Math.sqrt(values.length);
        String variation = getVariabilityIndex(squaredError, d.getAverage());
        Double median = getMedian(values);

        point.put(medianGIS, strRoundHundred(median));
        point.put(this.squaredError, strRoundHundred(squaredError));
        point.put(this.errorOfMean, strRoundHundred(errorOfMean));
        point.put(this.variation, variation);
    }

    /**
     * Метод удаляет повторяющиеся пары глубин-значений ГИС. Такие повторяющиеся
     * пары могут появиться при соединении двух подразделений, идущих друг за другом.
     * Например глубины первого подразделения 10.2 - 18.8, а второго 18.8 - 20.6,
     * При объединении этих подразделений, глубины общего подразделения
     * будут: 10.2 - 18.8;18.8 - 20.6. Здесь одна глубина 18.8
     * (и значение ГИС на этой глубине) будет лишней, ее и следует удалить
     * @param point текущая ТН (id, depths, values)
     * @param nameMethodGIS название метода ГИС
     */
    default void deleteRepeatValues(Map<String, String> point, String nameMethodGIS) {

        String depths = point.get("Глубина ГИС");
        String values = point.get( nameMethodGIS);

        if (point.size() == 3 && depths.contains("/") && values.contains("/")) {

            String[] depthsArray = depths.split("/");
            String[] valuesArray = values.split("/");
            StringBuilder newDepths = new StringBuilder();
            StringBuilder newValues = new StringBuilder();

            newDepths.append(depthsArray[0]); newDepths.append("/");
            newValues.append(valuesArray[0]); newValues.append("/");
            for (int i = 1; i < depthsArray.length; i++) {
                if (! depthsArray[i].equals(depthsArray[i - 1])) {
                    newDepths.append(depthsArray[i]); newDepths.append("/");
                    newValues.append(valuesArray[i]); newValues.append("/");
                }
            }
            point.put("Глубина ГИС", newDepths.toString());
            point.put(nameMethodGIS, newValues.toString());
        }
    }

    /**
     * Проверяет корректность значения ГИС
     * @param value значение ГИС
     * @return <code>true</code> - если значение ГИС корректно, <code>false</code> -
     * в обратном случае
     */
    default boolean isCorrectValueGIS(String value) {
        if (value.equals("-999999.0") || value.equals("-999.75")
                || value.equals("-995.75") || value.equals(" ")
                || value.equals("")) {
            return false;
        }
        return true;
    }

    /**
     * Оставляет минимальное и максимальное значения глубин ГИС-измерений
     * Метод необходим для создания файла интервалов, в который записываются
     * какие-либо параметры по ГИС для всего ствола скважины
     * @param point текущая ТН
     */
    default void leaveMinAndMaxDepthGIS(Map<String, String> point) {
        if (point.containsKey("Глубина ГИС")) {
            String[] depths = point.get("Глубина ГИС").split("/");
            if (depths.length > 1) {
                point.put("Начало измерений", depths[0]);
                point.put("Конец измерений", depths[depths.length - 1]);
                point.remove("Глубина ГИС");
            }
        }

    }

    /**
     * Вычитает определенное значение ГИС (медиана, минимальное значение)
     * из всех значений ГИС
     * @param point текущая ТН
     * @param nameMethodGIS метод ГИС, для которого производятся вычисления
     */
    default void minusValue(Map<String, String> point, String nameMethodGIS,
                            String typeOfMinusValue) {
        if (!point.containsKey(nameMethodGIS)) return;

        String[] valuesGIS = point.get(nameMethodGIS).split("/");

        List<String> stringValuesGIS = Arrays.stream(valuesGIS)
                .map(String::valueOf)
                .collect(Collectors.toList());

        List<Double> validValuesGIS = stringValuesGIS.stream()
                .filter(e -> isCorrectValueGIS(e))
                .map(Double::valueOf)
                .collect(Collectors.toList());

        if (validValuesGIS.size() == 0) return;

        Double[] v2 = validValuesGIS.toArray(new Double[validValuesGIS.size()]);
        Double coefficient = getMinusCoefficient(v2, typeOfMinusValue);

        StringBuilder valuesMinusCoefficient = new StringBuilder();

        for (String currentValue : stringValuesGIS) {
            if (isCorrectValueGIS(currentValue)) {
                Double d = Double.valueOf(currentValue) - coefficient;
                d = Math.round(d * 100.0) / 100.0;
                valuesMinusCoefficient.append(String.valueOf(d));
            } else {
                valuesMinusCoefficient.append(currentValue);
            }
            valuesMinusCoefficient.append("/");
        }

        point.put(nameMethodGIS, valuesMinusCoefficient.toString());
    }

    /**
     * Возвращает коэффициент для вычитания(медиана, минимальное , максимальное и т. д.)
     * @param values массив значений ГИС, который содержит только корректные значения
     * @param typeOfMinusValue тип вычитаемого значения(медиана, среднее, максимальное и т.д.)
     * @return коэффициент для вычитания(медиана, минимальное , максимальное и т. д.)
     */
    default Double getMinusCoefficient(Double[] values, String typeOfMinusValue) {
        Double coefficient = 0.0;
        if (typeOfMinusValue.equals("отнять медиану")) {
            coefficient = getMedian(values);
        } else if (typeOfMinusValue.equals("отнять среднее значение")) {
            coefficient = getMeanValue(values);
        } else if (typeOfMinusValue.equals("отнять минимальное значение")) {
            coefficient = getMinValue(values);
        } else if (typeOfMinusValue.equals("отнять максимальное значение")) {
            coefficient = getMaxValue(values);
        }
        return coefficient;
    }

    /**
     * Возвращает среднее значение из массива чисел
     * @param values массив чисел (значений ГИС)
     * @return минимальное значение
     */
    default Double getMeanValue(Double[] values) {
        Double sum = 0.0;
        for (Double value : values) {
            sum = sum + Double.valueOf(value);
        }
        Double middleValue = sum/values.length;
        return Math.round(middleValue * 100.0)/100.0;
    }

    /**
     * Возвращает минимальное значение из массива чисел
     * @param values массив чисел (значений ГИС)
     * @return минимальное значение
     */
    default Double getMinValue(Double[] values) {
        Double minValue = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (Double.valueOf(values[i]) < minValue) {
                minValue = Double.valueOf(values[i]);
            }
        }
        return Math.round(minValue * 100.0) / 100.0;
    }

    /**
     * Возвращает максимальное значение из массива чисел
     * @param values массив чисел (значений ГИС)
     * @return максимальное значение
     */
    default Double getMaxValue(Double[] values) {
        Double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (Double.valueOf(values[i]) > maxValue) {
                maxValue = Double.valueOf(values[i]);
            }
        }
        return Math.round(maxValue * 100.0) / 100.0;
    }

    /**
     * Вносит поправку для ГИС-измерений по текущей точке наблюдения
     * @param point текущая ТН
     * @param coefficient объект для коэффициента (для тория, калия, урана)
     * @param nameMethodGIS тип метода ГИС
     */
    default void setChangeForAllGISValues(Map<String, String> point,
                                          Coefficient coefficient,
                                          String nameMethodGIS) {
        if (!point.containsKey(nameMethodGIS)) return;

        String[] depth = point.get("Глубина ГИС").split("/");
        String[] values = point.get(nameMethodGIS).split("/");

        Double middleValue = middleValueGIS(depth, values);
        Double k = coefficient.getForMiddleValue(middleValue);

        StringBuilder newValues = new StringBuilder();
        for (int i = 0; i < depth.length; i++) {
            if (isCorrectValueGIS(values[i])) {
                Double d = Double.valueOf(values[i]) * k;
                d = Math.round(d * 100.0) / 100.0;
                newValues.append(d.toString());
                newValues.append("/");
            } else {
                newValues.append(values[i]);
                newValues.append("/");
            }
        }
        point.put(nameMethodGIS, newValues.toString());
    }

    /**
     * Получает среднее значение ГИС-измерений по всему стволу скважины
     * (это необходимо для того, чтобы знать общий уровень сигнала)
     * @param depth глубины текущей ТН
     * @param values значения ГИС-измерений текущей ТН
     * @return среднее значение
     */
    default Double middleValueGIS(String[] depth, String[] values) {
        Double middleValue = 0.0;
        Double sumValue = 0.0;
        int amount = 0;
        for (int i = 0; i < depth.length; i++) {
            if (isCorrectValueGIS(values[i])) {
                sumValue = sumValue + Double.valueOf(values[i]);
                amount++;
            }
        }
        if (sumValue != 0 && amount != 0) {
            middleValue = sumValue / amount;
        }
        return middleValue;
    }

}


