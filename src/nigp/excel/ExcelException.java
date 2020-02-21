package nigp.excel;

/**
 * Класс ошибки, объект которого может быть создан при чтении
 * excel-фвйла
 * @author NovopashinAV
 *
 */
public class ExcelException extends Exception {

    private static final long serialVersionUID = 1L;

    public ExcelException() {

    }

    public ExcelException(String message) {
        super(message);
    }

}
