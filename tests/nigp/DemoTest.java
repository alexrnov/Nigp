package nigp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DemoTest {

    private Demo demo;

    @Before
    public void tearUp() throws Exception {
        demo = new Demo();
    }

    @After
    public void tearDown() throws Exception {
        demo = null;
    }

    @Test
    public void unionSameNames() throws Exception {
        demo.UnionSameNames();
    }

    @Test
    public void linkOnSubList() throws Exception {
        demo.linkOnSubList();
    }

    @Test (expected = UnsupportedOperationException.class)
    public void addElementToAbstractList() throws Exception {
        demo.addElementToAbstractList();

    }

    @Test
    public void hashCodeOfStrings() {
        demo.hashCodeOfStrings();
        /*
        int i = 1566968861;

        for(int k = 0; k < 1000; k++) {
            if (demo.hashCodeOfStrings() != i) {
                System.out.println("false");
            } else {
                System.out.println("true");
            }
        }
        */

    }

    @Test
    public void geo() {
        demo.shp();
    }

    @Test
    public void fun() {
        demo.fun();
    }
}
