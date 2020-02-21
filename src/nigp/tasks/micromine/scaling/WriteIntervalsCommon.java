package nigp.tasks.micromine.scaling;

import nigp.file.TextFileForMicromine;
import nigp.tasks.TaskException;

import java.util.List;
import java.util.Map;

/**
 * Реализация шаблонного метода для записи данных в файл интрервалов.
 * Может использоваться для вывода любых данных по интревалам, которые
 * не требую дополнительной обработки. Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public class WriteIntervalsCommon extends ScalingData {

    private TextFileForMicromine file;

    public WriteIntervalsCommon(TextFileForMicromine file) {
        this.file = file;
    }

    @Override
    public void prepare(List<Map<String, String>> miniList) throws TaskException {
        file.write(miniList);
    }
}
