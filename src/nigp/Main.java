package nigp;

import java.util.logging.Logger;

import nigp.tasks.Task;
import nigp.tasks.TaskException;
import nigp.tasks.TypeOfTask;

/**
 * Программа для решения картографических задач:
 * 1. Мощность и среднее значение ГИС для геофизического пласта
 * 2. Номер геофизических пластов, выходящих на поверхность карбонатного цоколя
 * 3. Интерполяция по нечисловому атрибуту
 * @author NovopashinAV
 */
public class Main {

    /* код для выхода из программы: 0 - программа выполнена успешно, 1 - с ошибкой */
    private static byte systemCode = 1;

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String... args) {
        try {
            new Logging(logger);

			Task task = TypeOfTask.getType(args);
			task.toSolve();

            systemCode = 0;
        } catch(TaskException e) {
            System.out.println("Ошибка при попытке выполнить вычисления");
            e.printStackTrace();
            logger.config(e.getMessage());
        } catch(Exception e) {
            System.out.println("Неизвестная ошибка");
            e.printStackTrace();
            logger.config(e.getMessage());
        } finally {
			/*
			System.out.print("Нажмите Enter для завершения ");
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
			scanner.close();
			*/
            System.exit(systemCode);
        }
    }
}
