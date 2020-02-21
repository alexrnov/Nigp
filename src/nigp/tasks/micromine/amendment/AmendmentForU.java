package nigp.tasks.micromine.amendment;

/**
 * Определяет размер поправки для значений ГИС по урану (U).
 * Используется паттерн СТРАТЕГИЯ.
 */
public class AmendmentForU implements Coefficient {

    public AmendmentForU() {

    }

    public Double getForMiddleValue(Double middleValue) {
        Double k = 0.0;
        if (middleValue < 2.1) {
            k = 10.0;
        } else if (middleValue < 40) {
            k = 1.0;
        } else if(middleValue < 500) {
            k = 0.1;
        } else if (middleValue > 500) {
            k = 0.01;
        }
        return k;
    }
}
