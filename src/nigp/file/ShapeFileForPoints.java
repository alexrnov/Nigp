package nigp.file;

import nigp.tasks.TaskException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Осуществляет запись выходных данных в шейп файл
 */
public class ShapeFileForPoints {

    private final String ERROR_MESSAGE = "Error create shape file: ";
    private String path;

    private Transaction transaction;
    private SimpleFeatureSource featureSource;
    private SimpleFeatureType type;
    private List<List<String>> nameAndTypeKeys;

    private Logger logger = Logger.getLogger(ShapeFileForPoints.class.getName());

    private String nameKeyForX;
    private String nameKeyForY;

    public ShapeFileForPoints(String path) throws TaskException {
        this.path = path;
    }

    /**
     * Создает шейп-файл, в соответствии с набором атрибутивных полей, указанных в
     * коллекции nameAndTypeKeys
     * @param nameAndTypeKeys набор атрибутивных полей в формате
     * [Название атрибута в ИСИХОГИ, название атрибута в shp-файле, тип атрибута
     * (текстовый, целый, вещественный)]
     * @throws TaskException
     */
    public void create(List<List<String>> nameAndTypeKeys) throws TaskException {
        this.nameAndTypeKeys = nameAndTypeKeys;
        StringBuilder attributes = new StringBuilder();
        nameAndTypeKeys.forEach(e -> {
            attributes.append(correctNameForShapeKey(e.get(1)));
            attributes.append(":");
            attributes.append(e.get(2));
            attributes.append(",");
        });

        attributes.deleteCharAt(attributes.length() - 1);
        //System.out.println(attributes.toString());

        try {

            type = DataUtilities.createType("Location",
                    "the_geom:Point," + attributes.toString());

        } catch (SchemaException e) {
            throw new TaskException(ERROR_MESSAGE + "create type shape file");
        }

        File newFile = new File(path);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();

        try {
            params.put("url" , newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(type);
            transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];
            featureSource = newDataStore.getFeatureSource(typeName);
            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                featureStore.setTransaction(transaction);
            }
        } catch (MalformedURLException e) {
            throw new TaskException(ERROR_MESSAGE + "get URL");
        } catch (IOException e) {
            throw new TaskException(ERROR_MESSAGE + "create schema");
        } catch (Exception e) {
            throw new TaskException(ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Записывает (с возможностью дозаписи) текущую коллекцию с ТН
     * @param table текущая коллекция ТН
     * @throws TaskException
     */
    public void write(List<Map<String, String>> table) throws TaskException {
        List<SimpleFeature> features = new ArrayList<>();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);

        for (Map<String, String> currentPoint: table) {
            Point point = geometryFactory.createPoint(new Coordinate(
                    Double.valueOf(currentPoint.get(nameKeyForX)),
                    Double.valueOf(currentPoint.get(nameKeyForY))));
            featureBuilder.add(point);
            for (List<String> key: nameAndTypeKeys) {
                if (!currentPoint.containsKey(key.get(0))) { //если текущая ТН не содержит необходимого атрибута
                    featureBuilder.add("");
                } else {
                    if (key.get(2).equals("Double")) { //если атрибут должен иметь вещественный тип
                        Double d = Double.valueOf(currentPoint.get(key.get(0)));
                        featureBuilder.add(d);
                    } else {
                        featureBuilder.add(encode(currentPoint.get(key.get(0))));
                    }
                }
            }
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }

        try {
            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
                featureStore.setTransaction(transaction);
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();
                } catch (Exception e) {
                    transaction.rollback();
                    logger.log(Level.SEVERE,
                            ERROR_MESSAGE + "error write", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, ERROR_MESSAGE + "error write", e);
        }
    }

    /*
     * заменяет символы, не подходящие для названий атрибутивных полей в шейп-файлах
     * и устанавливает кодировку ISO_8859_1
     */
    private String correctNameForShapeKey(String nameKey) {

        nameKey = nameKey.replace(" ", "_");
        nameKey = nameKey.replace("\n","");
        nameKey = nameKey.replace("№", "N");
        nameKey = nameKey.replace("/", "_");
        nameKey = nameKey.replace("-", "_");
        nameKey = nameKey.replace(".", "");

        nameKey = encode(nameKey);
        return nameKey;
    }

    private String encode(String text) {
        String out = "";
        try {
            out = new String(text.getBytes(), "ISO_8859_1");
        } catch(UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, ERROR_MESSAGE + "correctNameForShapeKey", e);
        }
        return out;
    }

    /** Закрывает шейп-файл */
    public void close() {
        try {
            transaction.close();
        } catch(IOException e) {
            logger.log(Level.SEVERE, ERROR_MESSAGE + "error close", e);
        }
    }

    /**
     * Устанавливает название атрибута с координатой X
     * @param nameKeyForX название атрибута X
     */
    public void setNameKeyForX(String nameKeyForX) {
        this.nameKeyForX = nameKeyForX;
    }

    /**
     * Устанавливает название атрибута с координатой Y
     * @param nameKeyForY название атрибута Y
     */
    public void setNameKeyForY(String nameKeyForY) {
        this.nameKeyForY = nameKeyForY;
    }
}
