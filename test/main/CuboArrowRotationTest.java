package main;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class CuboArrowRotationTest {

    private Cubo cubo;
    private Method getArrow;
    private Method applyRot;

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
        cubo = new Cubo();
        getArrow = Cubo.class.getDeclaredMethod("getArrowRotation", double[].class, Subcubo.class, int.class);
        getArrow.setAccessible(true);
        applyRot = Cubo.class.getDeclaredMethod("applyRotation", int.class, double.class);
        applyRot.setAccessible(true);
    }

    private int[] call(double[] arrow, int face) throws Exception {
        Subcubo sc = new Subcubo(0, 0, 0, 1);
        return (int[]) getArrow.invoke(cubo, arrow, sc, face);
    }

    @Test
    public void testArrowRotationWithCubeRotated() throws Exception {
        // Rotate cube 90 degrees around Y axis and ensure mapping remains
        // consistent for the left and right faces regardless of orientation.
        applyRot.invoke(cubo, 1, 90.0); // rotate around Y
        Object[][] cases = new Object[][]{
            // left face (4) after rotation
            {4, new double[]{0, -1, 0}, 0, 0},
            {4, new double[]{0, 1, 0}, 0, 1},
            {4, new double[]{-1, 0, 0}, 1, 0},
            {4, new double[]{1, 0, 0}, 1, 1},
            // right face (5) after rotation
            {5, new double[]{0, -1, 0}, 0, 1},
            {5, new double[]{0, 1, 0}, 0, 0},
            {5, new double[]{-1, 0, 0}, 1, 1},
            {5, new double[]{1, 0, 0}, 1, 0}
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

    @Test
    public void testArrowRotation() throws Exception {
        // expected mapping: face index, arrow vector, axis, cw flag
        Object[][] cases = new Object[][]{
            // front face (1)
            {1, new double[]{0, -1, 0}, 0, 0},
            {1, new double[]{0, 1, 0}, 0, 1},
            {1, new double[]{-1, 0, 0}, 1, 1},
            {1, new double[]{1, 0, 0}, 1, 0},
            // back face (0)
            {0, new double[]{0, -1, 0}, 0, 1},
            {0, new double[]{0, 1, 0}, 0, 0},
            {0, new double[]{-1, 0, 0}, 1, 0},
            {0, new double[]{1, 0, 0}, 1, 1},
            // left face (4)
            {4, new double[]{0, -1, 0}, 0, 0},
            {4, new double[]{0, 1, 0}, 0, 1},
            {4, new double[]{-1, 0, 0}, 1, 1},
            {4, new double[]{1, 0, 0}, 1, 0},
            // right face (5)
            {5, new double[]{0, -1, 0}, 0, 1},
            {5, new double[]{0, 1, 0}, 0, 0},
            {5, new double[]{-1, 0, 0}, 1, 0},
            {5, new double[]{1, 0, 0}, 1, 1},
            // top face (3)
            {3, new double[]{0, -1, 0}, 2, 0},
            {3, new double[]{0, 1, 0}, 2, 1},
            {3, new double[]{-1, 0, 0}, 2, 1},
            {3, new double[]{1, 0, 0}, 2, 0},
            // bottom face (2)
            {2, new double[]{0, -1, 0}, 2, 0},
            {2, new double[]{0, 1, 0}, 2, 1},
            {2, new double[]{-1, 0, 0}, 2, 0},
            {2, new double[]{1, 0, 0}, 2, 1}
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

    @Test
    public void testArrowRotationTopBottomDegenerate() throws Exception {
        Field rotField = Cubo.class.getDeclaredField("rotMatrix");
        rotField.setAccessible(true);
        Field ax = Cubo.class.getDeclaredField("anguloX");
        Field ay = Cubo.class.getDeclaredField("anguloY");
        Field az = Cubo.class.getDeclaredField("anguloZ");
        ax.setDouble(cubo, 0.0);
        ay.setDouble(cubo, 0.0);
        az.setDouble(cubo, 0.0);

        double[][] mTop = new double[][]{
            {0, 0, 1},
            {-1, 1, 0},
            {0, 0, 0}
        };
        rotField.set(cubo, mTop);
        int[] resTop = call(new double[]{0, -1, 0}, 3);
        assertEquals(0, resTop[0]);
        assertEquals(1, resTop[1]);

        double[][] mBottom = new double[][]{
            {0, 0, 1},
            {1, -1, 0},
            {0, 0, 0}
        };
        rotField.set(cubo, mBottom);
        int[] resBottom = call(new double[]{0, 1, 0}, 2);
        assertEquals(0, resBottom[0]);
        assertEquals(1, resBottom[1]);
    }
}
