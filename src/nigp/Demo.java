package nigp;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;

import javax.swing.UIManager;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Demo {

    public void UnionSameNames() {
        List<String> list = Arrays.asList("a", "b", "a", "a", "a", "b", "b",
                                            "a", "b", "a", "b", "b", "b",
                                            "c", "a", "c", "c");
        list.forEach(e -> System.out.print(e + " "));
        System.out.println("---------------");
        List<String> unionList = union(list);
        unionList.forEach(e -> System.out.print(e + " "));
    }

    private List<String> union(List<String> list) {
        List<String> newList = new ArrayList<>();
        String un = "";
        for (int i = 0; i < list.size(); i++) {
            un = un + list.get(i);
            if (i == list.size() - 1) {
                newList.add(un);
                System.out.println("last");
                break;
            }
            if (!list.get(i).equals(list.get(i + 1))) {
                newList.add(un);
                un = "";
            }
        }
        return newList;
    }

    public void linkOnSubList() {
        List<List<String>> list = new ArrayList<>();
        List<String> subList = new ArrayList<>();
        subList.add("1");

        list.add(subList);
        subList = new ArrayList<>(); //subList.clear() не подходит
        subList.add("2");
        list.add(subList);
        list.forEach(e -> System.out.print(e + " "));
        System.out.println("-----------------------");
        Integer p1;
        Integer p2;
        p2 = 5;
        p1 = p2;
        p2 = 6;
        System.out.println("p1 = " + p1 + " p2 = " + p2);
    }

    public void addElementToAbstractList() {
        //List<String> list = Arrays.asList("1", "2", "3", "4", "5");
        //list.add("6");

        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("1", "2", "3", "4", "5"));
        list.add("6");
        list.forEach(e -> System.out.println(e));
    }

    public int hashCodeOfStrings() {
        String a = "544";
        String b = "442";
        String c = a + b;
        System.out.println(c.hashCode());

        String d = "544";
        String e = "440";
        String f = d + e;
        System.out.println(f.toString());
        return c.hashCode();
    }

    public void shp() {
        File shp = new File("D:" + File.separator + "111.shp");
        //GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        //GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        //ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();


        try {
            /*
            final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                    "location:Point," + "name:String," + "number:Integer");
            */
            final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                    "the_geom:Point," + "name:String," + "number:Double");

            List<SimpleFeature> features = new ArrayList<>();
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

            List<Map<String, String>> data = new ArrayList<>();
            Map<String, String> p1 = new HashMap<>();
            p1.put("x", "500000");
            p1.put("y", "7207000");
            p1.put("name", "point_1");
            p1.put("val", "10.0");

            Map<String, String> p2 = new HashMap<>();
            p2.put("x", "500090");
            p2.put("y", "7207090");
            p2.put("name", "point_2");
            p2.put("val", "20.0");
            data.add(p1);
            data.add(p2);

            for (Map<String, String> p: data) {
                Point point = geometryFactory.createPoint(new Coordinate(
                        Integer.valueOf(p.get("x")), Integer.valueOf(p.get("y"))));
                featureBuilder.add(point);
                featureBuilder.add(p.get("name"));
                featureBuilder.add(Double.valueOf(p.get("val")));

                SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }

            List<SimpleFeature> features2 = new ArrayList<>();
            for (Map<String, String> p: data) {
                Point point = geometryFactory.createPoint(new Coordinate(
                        Integer.valueOf(p.get("x")), Integer.valueOf(p.get("y"))));
                featureBuilder.add(point);
                featureBuilder.add(p.get("name"));
                featureBuilder.add(Double.valueOf(p.get("val")));

                SimpleFeature feature = featureBuilder.buildFeature(null);
                features2.add(feature);
            }

            /*
            JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
            chooser.setDialogTitle("Сохранить shp-файл");
            chooser.setSelectedFile(new File("D:" + File.separator + "111.shp"));

            int returnVal = chooser.showSaveDialog(null);
            if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
                System.out.println("0");
            } else {
                System.out.println("1");
            }
            File newFile = chooser.getSelectedFile();

            */
            File newFile = new File("D:" + File.separator + "666.shp");

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<>();
            params.put("url" , newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(TYPE);


            Transaction transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];

            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
            SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();//INFO
            System.out.println("shape type: " + SHAPE_TYPE);

            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
                featureStore.setTransaction(transaction);
                System.out.println("111");
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();
                } catch(Exception e) {
                    e.printStackTrace();
                    transaction.rollback();
                } finally {
                    //transaction.close();
                }


                collection = new ListFeatureCollection(TYPE, features2);
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();
                } catch(Exception e) {
                    e.printStackTrace();
                    transaction.rollback();
                } finally {
                    transaction.close();
                }

            }


        } catch(SchemaException e) {
            e.printStackTrace();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void fun() {
        Map<String, String> m = new HashMap<>();
        m.put("1", "2");
        m.put("2", null);

        m.forEach((k,v) -> {
            System.out.println(k + v);
        });

    }
}
