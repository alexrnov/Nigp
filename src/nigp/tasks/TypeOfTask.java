package nigp.tasks;

import java.util.Arrays;

import nigp.tasks.gis.geophysicallayer.AbsoluteABSForGeophysLayer;
import nigp.tasks.gis.geophysicallayer.WidthAndMeanGISForGeophysLayerMinusValue;
import nigp.tasks.gis.stratigraphiclayer.MeanGISForStructure;
import nigp.tasks.gis.stratigraphiclayer.MeanGISStratigraphicLayer;
import nigp.tasks.gis.stratigraphiclayer.MeanGISStratigraphicLayerMinusValue;
import nigp.tasks.interpolation.NonNumericOnePoint;
import nigp.tasks.gis.geophysicallayer.GeoNumberOnSurfaceOfCarbonate;
import nigp.tasks.gis.geophysicallayer.WidthAndMeanGISForGeophysLayer;
import nigp.tasks.micromine.databasewells.*;
import nigp.tasks.micromine.mcaOfAllObjects.FullDataMineralogy;
import nigp.tasks.micromine.mcaOfAllObjects.mineralogyPointsForStratigraphicLayer;
import nigp.tasks.micromine.points.ABSAndMeanGISForBlockModel;
import nigp.tasks.micromine.points.ABSAndMeanGISForBlockMinWidth;
import nigp.tasks.micromine.points.ABSForLithologyLayer;
import nigp.tasks.micromine.points.ABSForStratigraphicLayer;

/**
 * Класс предназначен для создания объекта с картографическими точками
 * того типа, который соответствует типу задачи
 * ПАТТЕРН: ПРОСТАЯ ФАБРИКА
 * @author NovopashinAV
 */
public class TypeOfTask {

    /**
     * Создает объект, соответствующий типу задачи.
     * @param args аргументы командной строки.
     * Первый аргумент командной строки должен соответствовать
     * типу задачи, такому как:
     * "Мощность и среднее значение ГИС для геофизического пласта" -
     * {@link WidthAndMeanGISForGeophysLayer};
     * "Номер геофизических пластов, выходящих на поверхность
     *  карбонатного цоколя" -
     * {@link GeoNumberOnSurfaceOfCarbonate};
     * "Интерполяция по нечисловому атрибуту" -
     * {@link NonNumericOnePoint}
     * @return объект задачи определенного типа.
     */
    public static Task getType(String[] args) throws TaskException {
        if (args.length < 2) {
            throw new TaskException("Number of input parameters less than two");
        }

        System.out.println("Входные параметры: ");
        for (String a: args) {
            System.out.println(a);
        }
        System.out.println();

        String typeOfTask = args[0];
        String[] inputParameters = Arrays.copyOfRange(args, 1, args.length);

        switch (typeOfTask) {
            case "Мощность и среднее значение ГИС для геофизического пласта":
                return new WidthAndMeanGISForGeophysLayer(inputParameters);
            case "Мощность и среднее значение ГИС для геофизического пласта, с вычитанием":
                return new WidthAndMeanGISForGeophysLayerMinusValue(inputParameters);
            case "Номер геофизических пластов, выходящих на поверхность "
                    + "карбонатного цоколя":
                return new GeoNumberOnSurfaceOfCarbonate(inputParameters);
            case "Интерполяция по нечисловому атрибуту":
                return new NonNumericOnePoint(inputParameters);
            case "Абсолютные отметки геофизического пласта":
                return new AbsoluteABSForGeophysLayer(inputParameters);
            case "Мощность и среднее значение ГИС для стратиграфического подразделения":
                return new MeanGISStratigraphicLayer(inputParameters);
            case "Мощность и среднее значение ГИС для стратиграфического подразделения, с вычитанием":
                return new MeanGISStratigraphicLayerMinusValue(inputParameters);
            case "Мощность и среднее значение ГИС для перекрывающих или вмещающих отложений":
                return new MeanGISForStructure(inputParameters);
            case "Файлы Micromine (весь ствол скважины)":
                return new FullIntervalGIS(inputParameters);
            case "Файлы Micromine по K, Th, U (весь ствол скважины)":
                return new FullIntervalGISForKThU(inputParameters);
            case "Файлы Micromine (стратиграфия и литология по всему стволу скважины)":
                return new FullIntervalStratigraphic(inputParameters);
            case "Файлы Micromine (геофизические пласты с абсолютной мощностью)":
                return new AbsoluteGeophysicalLayers(inputParameters);
            case "Файлы Micromine (один геофизический пласт)":
                return new OneGeophysicalLayer(inputParameters);
            case "Один геофизический пласт для условного моделирования":
                return new OneGeophysicalLayerImplicit(inputParameters);
            case "Файл устьев скважин для Micromine":
                return new OnlyTopWells(inputParameters);
            case "Файлы Micromine (все геофизические пласты)":
                return new AllGeophysicLayers(inputParameters);
            case "Файлы Micromine (ГИС по подразделениям и статистические параметры)":
                return new GISForStratigraphicLayers(inputParameters);
            case "Файлы Micromine (ГИС по подразделениям с вычитанием среднего и статистические параметры)":
                return new GISForStratigraphicLayersMinusValue(inputParameters);
            case "Файлы Micromine (соотношение K, Th и U по подразделениям)":
                return new GISForStratigraphicLayersRatioKThU(inputParameters);
            case "Файлы Micromine (ГИС по всему стволу скважины с вычитанием определенного значения)":
                return new FullIntervalGISMinusValue(inputParameters);
            case "Файлы Micromine (один геофизический пласт с соотношением по K, Th и U)":
                return new OneGeophysicalLayerRatioKThU(inputParameters);
            case "Файл точек для Micromine (ABS кровли и подошвы по стратиграфическим подразделениям)":
                return new ABSForStratigraphicLayer(inputParameters);
            case "Файл точек для Micromine (ABS кровли и подошвы по литологическим подразделениям)":
                return new ABSForLithologyLayer(inputParameters);
            case "Файл точек для Micromine (ABS по основным подразделениям и средние значения ГИС)":
                return new ABSAndMeanGISForBlockModel(inputParameters);
            case "Файл точек для Micromine (ABS по основным подразделениям и средние значения ГИС)." +
                    "по умолчанию используется минимальная мощность структур":
                return new ABSAndMeanGISForBlockMinWidth(inputParameters);
            case "Файлы Micromine (минералогия по всему стволу скважины)":
                return new FullDataMineralogy(inputParameters);
            case "Точки с данными минералогии для стратиграфического подразделения":
                return new mineralogyPointsForStratigraphicLayer(inputParameters);
            default:
                throw new TaskException("Incorrect name of calculations");
        }
    }
}
