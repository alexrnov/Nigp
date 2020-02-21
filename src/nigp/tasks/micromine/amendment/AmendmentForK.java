package nigp.tasks.micromine.amendment;

/**
 * Определяет размер поправки для значений ГИС по калию (K).
 * Используется паттерн СТРАТЕГИЯ.
 */
public class AmendmentForK implements Coefficient {

    public AmendmentForK() {

    }

    public Double getForMiddleValue(Double middleValue) {
        Double k = 0.0;
        if (middleValue < 2) {
            k = 10.0;
        } else if (middleValue < 200) {
            k = 1.0;
        } else { //middleValue >= 200
            k = 0.1;
        }
        return k;
    }
}
