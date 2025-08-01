package main;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import sun.misc.Unsafe;

public class CuboArrowRotationTest {

    private Cubo cubo;
    private Method getArrow;

    @Before
    public void setUp() throws Exception {
        // allocate Cubo instance without invoking its constructor
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
        cubo = (Cubo) unsafe.allocateInstance(Cubo.class);

        // identity rotation matrix
        double[][] identity = new double[][]{
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        Field rm = Cubo.class.getDeclaredField("rotMatrix");
        rm.setAccessible(true);
        rm.set(cubo, identity);

        // angles set to zero
        for (String name : new String[]{"anguloX", "anguloY", "anguloZ"}) {
            Field ang = Cubo.class.getDeclaredField(name);
            ang.setAccessible(true);
            ang.setDouble(cubo, 0);
        }

        getArrow = Cubo.class.getDeclaredMethod("getArrowRotation", double[].class, Subcubo.class, int.class);
        getArrow.setAccessible(true);
    }

    private int[] call(double[] arrow, int face) throws Exception {
        Subcubo sc = new Subcubo(0, 0, 0, 1);
        return (int[]) getArrow.invoke(cubo, arrow, sc, face);
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
            {4, new double[]{0, -1, 0}, 2, 0},
            {4, new double[]{0, 1, 0}, 2, 1},
            {4, new double[]{-1, 0, 0}, 2, 0},
            {4, new double[]{1, 0, 0}, 2, 1},
            // right face (5)
            {5, new double[]{0, -1, 0}, 2, 1},
            {5, new double[]{0, 1, 0}, 2, 0},
            {5, new double[]{-1, 0, 0}, 2, 0},
            {5, new double[]{1, 0, 0}, 2, 1},
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
}
