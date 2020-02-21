package nigp.tasks.auxiliary;

import java.util.List;

/**
 * Класс вычисляет экстент для набора точек
 * @author NovopashinAV
 *
 */
public class Extent {

    /* координаты углов экстента */
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    public Extent(List<List<String>> points) {

        xMin = Double.MAX_VALUE;
        xMax = -xMin;
        yMin = xMin;
        yMax = -yMin;

        points.forEach(point -> {
            double x = Double.valueOf(point.get(0));
            double y = Double.valueOf(point.get(1));
            xMin = min(xMin, x);
            xMax = max(xMax, x);
            yMin = min(yMin, y);
            yMax = max(yMax, y);
        });
    }

    /* выбирает максимальное число из пары чисел */
    private double max(double value1, double value2) {
        return (Double.compare(value1, value2) > 0) ? value1 : value2;
    }

    /* выбирает минимальное чилсо из пары чисел */
    private double min(double value1, double value2) {
        return (Double.compare(value1, value2) < 0) ? value1 : value2;
    }

    /**
     * Расширяет границы экстента на величину value. Это делается для
     * того, чтобы границы экстента не прилегали вплотную к точкам,
     * лежащим у самого края, поскольку это выглядит не очень красиво
     * при интерполяции. Обучно в качестве значения value используется
     * значение параметра растровой ячейки.
     * @param value
     */
    public void expandBorders(double value) {
        xMin = xMin - value;
        xMax = xMax + value;
        yMin = yMin - value;
        yMax = yMax + value;
    }

    public double getMinX() {
        return xMin;
    }

    public double getMaxX() {
        return xMax;
    }

    public double getMinY() {
        return yMin;
    }

    public double getMaxY() {
        return yMax;
    }
}
