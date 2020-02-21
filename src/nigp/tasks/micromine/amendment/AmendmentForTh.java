package nigp.tasks.micromine.amendment;

/**
 * Определяет размер поправки для значений ГИС по торию (Th).
 * Используется паттерн СТРАТЕГИЯ.
 */
public class AmendmentForTh implements Coefficient {

    public AmendmentForTh() {

    }

    public Double getForMiddleValue(Double middleValue) {
        Double k = 0.0;
        if (middleValue < 26) {
            k = 1.0;
        } else { //middleValue >= 26
            k = 0.1;
        }
        return k;
    }
}
