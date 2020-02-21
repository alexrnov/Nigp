package nigp.tasks.auxiliary;

import java.util.ArrayList;
import java.util.List;

public interface MatrixXYZ {

    /**
     * метод создает мартицу из точек в границах указанного экстента
     * @param extent эксент, в пределах которого генерируется матрица
     * @param step расстояние между соседними точками матрицы
     * @return
     */
    default <T> List<PointXYZ<T>> getMatrixPoints(Extent extent, double step) {

		/* список с точками матрицы */
        List<PointXYZ<T>> matrixPoints = new ArrayList<>();

        double xMin = extent.getMinX();
        double xMax = extent.getMaxX();
        double yMin = extent.getMinY();
        double yMax = extent.getMaxY();

        double x = xMin;
        double y = yMin;

        do { //приращение y-координаты
            do { //приращение x-координаты
                matrixPoints.add(new PointXYZ<T>(x, y));
                x += step;
            }
            while (x <= xMax);
            x = xMin;
            y += step;
        }
        while (y <= yMax);

        return matrixPoints;
    }
}
