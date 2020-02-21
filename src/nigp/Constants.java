package nigp;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержит общеупотребимые константы
 * @author NovopashinAV
 */
public class Constants {

    /**
     * Список названий листов Excel-файла с документацией ИСИХОГИ
     */
    public static List<String> allNamesOfSheets;

    static {
		/*
		 * заполнить список с названиями листов Excel-файла ИСИХОГИ
		 */
        allNamesOfSheets = new ArrayList<>();
        NameSheet[] names = NameSheet.values();
        for (NameSheet name : names) {
            allNamesOfSheets.add(name.format());
        }
    }

    /**
     * тип хранит названия excel-листов с документацией ИСИХОГИ.
     * Для каждой константы переопределяется абстрактный метод
     * format(), который возвращает правильное название листа.
     * Сразу задать константам правильные названия листов нельзя,
     * поскольку многие названия содержат пробелы и точки.
     */
    public enum NameSheet {
        Точки_наблюдений {
            public String format() {
                return "Точки наблюдений";
            }
        },
        Стратиграфия_Литология {
            public String format() {
                return "Стратиграфия Литология";
            }
        },
        Справ_Стратиграфия {
            public String format() {
                return "Справ. Стратиграфия";
            }
        },
        Справ_Литология {
            public String format() {
                return "Справ. Литология";
            }
        },
        Справ_ТИП_ТОЧКИ_НАБЛЮДЕНИЯ {
            public String format() {
                return "Справ. ТИП ТОЧКИ НАБЛЮДЕНИЯ";
            }
        },
        Справ_СОСТОЯНИЕ_ТН {
            public String format() {
                return "Справ. СОСТОЯНИЕ ТН";
            }
        },
        Справ_СОСТОЯНИЕ_ДОКУМЕНТИРОВАН {
            public String format() {
                return "Справ. СОСТОЯНИЕ ДОКУМЕНТИРОВАН";
            }
        },
        Справ_СОСТОЯНИЕ_ВЫРАБОТКИ {
            public String format() {
                return "Справ. СОСТОЯНИЕ ВЫРАБОТКИ";
            }
        },
        Справ_СОСТОЯНИЕ_ГИС {
            public String format() {
                return "Справ. СОСТОЯНИЕ ГИС";
            }
        },
        Справ_СОСТОЯНИЕ_ОПРОБОВАНИЯ {
            public String format() {
                return "Справ. СОСТОЯНИЕ ОПРОБОВАНИЯ";
            }
        },
        Справ_Система_координат {
            public String format() {
                return "Справ. Система координат";
            }
        },
        Справ_Тип_системы_координат {
            public String format() {
                return "Справ. Тип системы координат";
            }
        },
        Справ_ТИП_ДОКУМЕНТИРОВАНИЯ {
            public String format() {
                return "Справ. ТИП ДОКУМЕНТИРОВАНИЯ";
            }
        },
        Геофизический_пласт {
            public String format() {
                return "Геофизический пласт";
            }
        },
        Справ_ТН_ГЕОПЛАСТ_ИМЯ {
            public String format() {
                return "Справ. ТН ГЕОПЛАСТ ИМЯ";
            }
        },
        Опробование_инт {
            public String format() {
                return "Опробование инт";
            }
        },
        Опробование_точ {
            public String format() {
                return "Опробование точ";
            }
        },
        Справ_ТИП_ПРОБЫ {
            public String format() {
                return "Справ. ТИП ПРОБЫ";
            }
        },
        Справ_МЕСТО_ОТБОРА {
            public String format() {
                return "Справ. МЕСТО ОТБОРА";
            }
        },
        ГИС {
            public String format() {
                return "ГИС";
            }
        },
        Справ_МЕТОД_ГИС {
            public String format() {
                return "Справ. МЕТОД ГИС";
            }
        },
        Конструкция {
            public String format() {
                return "Конструкция";
            }
        },
        Справ_Диаметр_бурения {
            public String format() {
                return "Справ. Диаметр бурения";
            }
        },
        Вторичные_изменения {
            public String format() {
                return "Вторичные изменения";
            }
        },
        Справ_Вторичные_изменения {
            public String format() {
                return "Справ. Вторичные изменения";
            }
        },
        Текстура_породы {
            public String format() {
                return "Текстура породы";
            }
        },
        Справ_Текстура_породы {
            public String format() {
                return "Справ. Текстура породы";
            }
        },
        Структура_породы {
            public String format() {
                return "Структура породы";
            }
        },
        Справ_Структура_породы {
            public String format() {
                return "Справ. Структура породы";
            }
        },
        Слоистость_породы {
            public String format() {
                return "Слоистость породы";
            }
        },
        Справ_Слоистость_породы {
            public String format() {
                return "Справ. Слоистость породы";
            }
        },
        Трещиноватость {
            public String format() {
                return "Трещиноватость";
            }
        },
        Справ_Трещиноватость {
            public String format() {
                return "Справ. Трещиноватость";
            }
        },
        Сортированность {
            public String format() {
                return "Сортированность";
            }
        },
        Справ_Сортированность {
            public String format() {
                return "Справ. Сортированность";
            }
        },
        Разновид_по_мин_составу {
            public String format() {
                return "Разновид.по мин составу";
            }
        },
        Справ_Разновид_по_мин_составу {
            public String format() {
                return "Справ. Разновид.по мин составу";
            }
        },
        Находки_флоры {
            public String format() {
                return "Находки флоры";
            }
        },
        Справ_Находки_флоры {
            public String format() {
                return "Справ. Находки флоры";
            }
        },
        Находки_фауны {
            public String format() {
                return "Находки фауны";
            }
        },
        Справ_Находки_фауны {
            public String format() {
                return "Справ. Находки фауны";
            }
        },
        Результаты_минералогии {
            public String format() {
                return "Результаты минералогии";
            }
        },
        Результаты_геохимии {
            public String format() {
                return "Результаты геохимии";
            }
        },
        Справ_Тип_химэлемента {
            public String format() {
                return "Справ. Тип химэлемента";
            }
        };
        public abstract String format();
    }
}
