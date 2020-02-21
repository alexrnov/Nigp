package nigp.file;

import java.io.PrintWriter;
import java.util.List;

import nigp.tasks.auxiliary.PointXYZ;

/**
 * Класс реализует запись в текстовый файл данных по точкам в
 * формате XYZ. Z в данном случае является нечисловым атрибутом.
 * Координаты и атрибут разделяются символами ";;"
 * @author NovopashinAV
 */
public class TextFileForXYZLayer extends TextFileForMapLayer {

    public TextFileForXYZLayer(String workingCatalog) {
        super(workingCatalog);
        super.nameOfFile = "OutputFileForXYZLayer.txt";
    }

    @SuppressWarnings("unchecked")
    protected void writeCurrentContent(PrintWriter out, Object xyz) {
        List<PointXYZ<String>> list = (List<PointXYZ<String>>) xyz;
        list.forEach(e -> {
            out.println(e.getX() + ";;" + e.getY() + ";;" + e.getZ());
        });
    }
}
