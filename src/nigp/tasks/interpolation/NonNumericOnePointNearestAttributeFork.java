package nigp.tasks.interpolation;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

import nigp.tasks.auxiliary.PointXYZ;

/**
 * @author NovopashinAV
 * Класс использует архитектуру вилочного соединения (работает в многопоточном режиме)
 * Записывает в массив с точками матрицы matrixPoints значения нечисловых атрибутов.
 * Эти значения вычисляются исходя из следующего принципа: производится поиск точки
 * входного слоя, которая располагается на минимальном расстоянии от текущей точки
 * матрицы, читается ее нечисловой атрибут (координата Z) и присваивается точке матрицы.
 */
public class NonNumericOnePointNearestAttributeFork extends RecursiveAction {

    private static final long serialVersionUID = 1L;
    private List<PointXYZ<String>> inputPoints = new CopyOnWriteArrayList<>();
    private List<PointXYZ<String>> matrixPoints = new CopyOnWriteArrayList<>();
    /*
     * Дистанция между самыми удаленными точками матрицы(первой и
     * последней). Параметр используется при поиске минимальной
     * дистанции методом сравнения
     */
    double maxDistance;
    /*
     * Порог, делящий задачу на подзадачи обработки по 1000 точет матрицы
     */
    private final Integer THRESHOLD = 1000;
    private int from;
    private int to;

    public NonNumericOnePointNearestAttributeFork(List<PointXYZ<String>> inputPoints,
                                                  List<PointXYZ<String>> matrixPoints,
                                                  double maxDistance, int from, int to) {
        this.inputPoints = inputPoints;
        this.matrixPoints = matrixPoints;
        this.maxDistance = maxDistance;
        this.from = from;
        this.to = to;
    }

    /*
     * Опредляет нечисловой атрибут точки, которая расположена ближе
     * всех к точке матрицы.
     */
    public void compute() {
        if (to - from < THRESHOLD) {
            for (int i = from; i < to; i++) {
                PointXYZ<String> matrixPoint = matrixPoints.get(i);
                String attribute = "";

                double matrixX = matrixPoint.getX();
                double matrixY = matrixPoint.getY();
                double minDistance = maxDistance;

                for (PointXYZ<String> inputPoint: inputPoints) {
                    double inputX = Double.valueOf(inputPoint.getX());
                    double inputY = Double.valueOf(inputPoint.getY());
                    double distance = sqrt(pow(inputX - matrixX, 2) + pow(inputY - matrixY, 2));

                    if (distance < minDistance) {
                        minDistance = distance;
                        attribute = inputPoint.getZ();
                    }
                }
                matrixPoint.setZ(attribute);
            }
        } else {
            int mid = (to + from) / 2;
            NonNumericOnePointNearestAttributeFork first = new NonNumericOnePointNearestAttributeFork(inputPoints, matrixPoints,
                    maxDistance, from, mid);

            NonNumericOnePointNearestAttributeFork second = new NonNumericOnePointNearestAttributeFork(inputPoints, matrixPoints,
                    maxDistance, mid, to);
            invokeAll(first, second);
        }
    }
}
