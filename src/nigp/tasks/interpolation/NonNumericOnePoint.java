package nigp.tasks.interpolation;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

import nigp.file.InputFile;
import nigp.file.TextFileForXYZLayer;
import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.auxiliary.Extent;
import nigp.tasks.auxiliary.MatrixXYZ;
import nigp.tasks.auxiliary.PointXYZ;

/**
 * Реализует интерполяцию по нечисловым значениям в многопоточном режиме.
 * Для точек в формате XYZ, где Z - нечисловой атрибут, вычисляется матрица
 * с интерполированными значениями.Интерполируемое значение, в данном
 * случае, вычисляется исходя из нечислового значения ближайшей точки XYZ
 * @author NovopashinAV
 */
public class NonNumericOnePoint extends Task implements InputFile, MatrixXYZ {

    /*
     * Путь к файлу с входными точками, для которых производится интерполяция
     * по ничисловому атрибуту (файл создается с помощью python-скрипта)
     */
    private Path directoryInputFile;

    private double cellSize;//размер растровой ячейки

    /*
     * Список входных точек в формате XYZ, где Z - нечисловой атрибут.
     * Для этих точек создается матрица с интерполированными
     * значениями.
     */
    private List<List<String>> inputXYZ;

    //Потокобезопасный список, содержащий объекты входных точек XYZ
    private List<PointXYZ<String>> inputPoints = new CopyOnWriteArrayList<>();

    /*
     * Дистанция между самыми удаленными точками матрицы(первой и
     * последней). Параметр используется при поиске минимальной
     * дистанции методом сравнения
     */
    volatile double maxDistance;

    /* текстовый файл для записи выходных данных */
    private TextFileForXYZLayer outputFile;

    /*
     * рабочий каталог, куда сохраняется текстовый файл с
     * результатами вычислений
     */
    private Path workingCatalog;

    public NonNumericOnePoint(String[] inputParameters) throws TaskException {
        super(inputParameters);
        readInputParameters();
        readInputFile();
    }

    private void readInputParameters() throws TaskException{
        if (inputParameters.length > 1) {
            directoryInputFile = Paths.get(inputParameters[0]);
            workingCatalog = directoryInputFile.getParent();
            cellSize = Double.valueOf(inputParameters[1]);
        } else {
            throw new TaskException("Incorrect input parameters");
        }
    }

    private void readInputFile() throws TaskException {

        try {
            inputXYZ = getXYZ(directoryInputFile);

        } catch(IOException e) {
            throw new TaskException("Error write error");
        }

        if (inputXYZ.size() == 0) {
            throw new TaskException("There are no points for interpolation");
        }
    }

    @Override
    public void toSolve() throws TaskException {

        Extent extent = new Extent(inputXYZ);
        extent.expandBorders(cellSize);

        inputXYZ.forEach(inputPoint -> {
            double x = Double.valueOf(inputPoint.get(0));
            double y = Double.valueOf(inputPoint.get(1));
            inputPoints.add(new PointXYZ<String>(x, y, inputPoint.get(2)));
        });

        List<PointXYZ<String>> matrixPoints = new CopyOnWriteArrayList<>();
        matrixPoints = getMatrixPoints(extent, cellSize);

        maxDistance = getMaxDistance(matrixPoints);

        //!!!!!! начало многопоточного кода (вилочное соединение)
        NonNumericOnePointNearestAttributeFork fork = new NonNumericOnePointNearestAttributeFork(inputPoints, matrixPoints,
                maxDistance,
                0, matrixPoints.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(fork);
        //!!!!!! конец многопоточного кода
        //while (Thread.activeCount() > 1);//ждать, пока потоки не остановятся(не обязательно)
        outputFile = new TextFileForXYZLayer(workingCatalog.toString());
        outputFile.create();
        outputFile.write(matrixPoints);
    }

    private double getMaxDistance(List <PointXYZ<String>> matrixPoints) {
        PointXYZ<String> firstElement = matrixPoints.get(0);
        PointXYZ<String> lastElement = matrixPoints.get(matrixPoints.size() - 1);
        double maxDistance = sqrt(pow(firstElement.getX() - lastElement.getX(), 2)
                + pow(firstElement.getY() - lastElement.getY(), 2));
        return maxDistance;
    }
}
