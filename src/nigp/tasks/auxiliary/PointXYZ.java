package nigp.tasks.auxiliary;

/**
 * Класс описывает структуру данных - точку с координатами X и Y,
 * а также атрибутом Z, который может быть как числовым, так и строковым
 * @author NovopashinAV
 */
public class PointXYZ <T> {

    private double x;
    private double y;
    private T z = null;

    public PointXYZ(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointXYZ(double x, double y, T z) {
        this(x, y);
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public T getZ() {
        return z;
    }

    public void setZ(T z) {
        this.z = z;
    }

}
