package nigp.tables;

import java.util.Map;

public interface ReplacementCodes {

    /** заменить коды текущей строки на расшифровку из excel-листов с кодами */
    void decode();

    /** заменить коды на расшифровки */
    default void replacement(Map<String, String> row, String fieldWithCode,
                             Map<String, String> tableWithTranscript) {
        String key = row.get(fieldWithCode);
        String transcript = (key == "Нет данных")
                ? "Нет данных"
                : tableWithTranscript.get(key);
        row.replace(fieldWithCode, transcript);
    }
}
