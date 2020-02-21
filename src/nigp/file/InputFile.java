package nigp.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Интерфейс реализут служебные методы для чтения файлов
 * @author NovopashinAV
 */
public interface InputFile {

    default List<List<String>> getXYZ(Path path) throws IOException {
        final String SEPARATOR = ";;";
        List<List<String>> points = new ArrayList<>();
        try (BufferedReader in = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                List<String> xyz = Arrays.asList(line.split(SEPARATOR));
                points.add(xyz);
            }
        }
        return points;
    }

}
