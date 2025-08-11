package main;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class ArrowParallelNormalTest {

    private Cubo cubo;
    private Method getArrow;

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
        cubo = new Cubo();
        getArrow = Cubo.class.getDeclaredMethod("getArrowRotation", double[].class, Subcubo.class, int.class);
        getArrow.setAccessible(true);
        // reset orientation to identity for deterministic normals
        Field ax = Cubo.class.getDeclaredField("anguloX");
        Field ay = Cubo.class.getDeclaredField("anguloY");
        Field az = Cubo.class.getDeclaredField("anguloZ");
        ax.setAccessible(true);
        ay.setAccessible(true);
        az.setAccessible(true);
        ax.setDouble(cubo, 0.0);
        ay.setDouble(cubo, 0.0);
        az.setDouble(cubo, 0.0);
        Field rot = Cubo.class.getDeclaredField("rotMatrix");
        rot.setAccessible(true);
        rot.set(cubo, new double[][]{{1,0,0},{0,1,0},{0,0,1}});
    }

    private int[] call(double[] arrow, int face) throws Exception {
        Subcubo sc = new Subcubo(0,0,0,1);
        return (int[]) getArrow.invoke(cubo, arrow, sc, face);
    }

    @Test
    public void testParallelToNormalAllFaces() throws Exception {
        Object[][] cases = new Object[][]{
            {0, Subcubo.getFaceNormal(0), 0, 1},
            {1, Subcubo.getFaceNormal(1), 0, 0},
            {2, Subcubo.getFaceNormal(2), 2, 0},
            {3, Subcubo.getFaceNormal(3), 2, 0},
            {4, Subcubo.getFaceNormal(4), 1, 1},
            {5, Subcubo.getFaceNormal(5), 1, 1}
        };
        for (Object[] c : cases) {
            int face = (Integer) c[0];
            double[] arrow = (double[]) c[1];
            int expAxis = (Integer) c[2];
            int expCw = (Integer) c[3];
            int[] res = call(arrow, face);
            assertEquals("axis for face " + face, expAxis, res[0]);
            assertEquals("cw for face " + face, expCw, res[1]);
        }
    }
}
