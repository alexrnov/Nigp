package nigp.tasks.micromine.amendment;

/**
 * Данный интерфейс реализуют классы, определяющие размер поправки для
 * значений ГИС. Используется паттерн СТРАТЕГИЯ.
 */
public interface Coefficient {
    /**
     * Возвращает коэффициент для значений ГИС по скважине
     * @param middleValue среднее значение измерений ГИС в целом по скважине
     * @return коэффициент для значений ГИС по скважине
     */
    Double getForMiddleValue(Double middleValue);
}
