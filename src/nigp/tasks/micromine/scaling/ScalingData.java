package nigp.tasks.micromine.scaling;

import nigp.tasks.TaskException;

import java.util.List;
import java.util.Map;

/**
 * Класс определяет общий алгоритм дробления большой коллекции
 * (для этого применяется рекурсия). В субклассах определяется
 * алгоритм обработки получинных небольших коллекций.
 * Используется паттерн ШАБЛОННЫЙ МЕТОД.
 */
public abstract class ScalingData {

    /**
     * Метод масштабирования данных разделяет большую коллекцию
     * (т.е. коллекцию, размер которой больше threshold) на подколлекции
     * размером threshold/2. Это делается для того, чтобы не возникала
     * ошибка переполнения памяти. Коллекция дробится с помощью рекурсии.
     * @param list большая коллекция
     * @param threshold размер коллекции, при котором коллекцию нужно делить
     * @param from начало коллекции(подколлекции)
     * @param to конец коллекции(подколлекции)
     * @throws TaskException
     */
    public final void perform(List<Map<String, String>> list, int threshold,
                              int from, int to) throws TaskException {

        if (to - from < threshold) {
            List<Map<String, String>> miniList = list.subList(from, to);
            prepare(miniList);
        } else {
            int mid = (to + from) / 2;
            perform(list, threshold, from, mid);
            perform(list, threshold, mid, to);
        }
    }

    /**
     * Метод определяется в субклассе. Устанавливает алгоритм обработки
     * субколлекции
     * @param miniList субколлекция
     * @throws TaskException
     */
    public abstract void prepare(List<Map<String, String>> miniList) throws TaskException;
}
