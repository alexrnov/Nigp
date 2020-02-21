package nigp;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    public Logging(Logger logger) {
        if (System.getProperty("java.util.logging.config.class") == null
                && System.getProperty("java.util.logging.config.file") == null) {
            try {
                Logger.getLogger("").setLevel(Level.ALL);
                final int LOG_ROTATION_COUNT = 1;
                final int SIZE_LOG_FILE_LIMIT = 1_048_576; // 1 мБайт
                Handler handler = new FileHandler("%t/NIGPTOOLS.log",
                        SIZE_LOG_FILE_LIMIT, LOG_ROTATION_COUNT);
                Logger.getLogger("").addHandler(handler);
            } catch(SecurityException e) {
                logger.log(Level.SEVERE, "Can't create log file because of security policy");
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Can't create log file handler", e);
            }
        }
    }
}
