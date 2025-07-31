package main;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InputControllerArrowRotationTest {

    private InputController controller;

    @Before
    public void setUp() {
        CubeModel model = new CubeModel(1, 1.0);
        Graficos g = new Graficos(10, 10);
        CubeRenderer renderer = new CubeRenderer(g, model);
        controller = new InputController(model, renderer);
    }

    private int[] call(double[] arrow, int face) {
        Subcubo sc = new Subcubo(0, 0, 0, 1);
        return controller.getArrowRotation(arrow, sc, face);
    }

    @Test
    public void testArrowRotation() {
        Object[][] cases = new Object[][]{
            {1, new double[]{0, -1, 0}, 0, 0},
            {1, new double[]{0, 1, 0}, 0, 1},
            {1, new double[]{-1, 0, 0}, 1, 1},
            {1, new double[]{1, 0, 0}, 1, 0},
            {0, new double[]{0, -1, 0}, 0, 1},
            {0, new double[]{0, 1, 0}, 0, 0},
            {0, new double[]{-1, 0, 0}, 1, 0},
            {0, new double[]{1, 0, 0}, 1, 1},
            {4, new double[]{0, -1, 0}, 2, 0},
            {4, new double[]{0, 1, 0}, 2, 1},
            {4, new double[]{-1, 0, 0}, 2, 0},
            {4, new double[]{1, 0, 0}, 2, 1},
            {5, new double[]{0, -1, 0}, 2, 1},
            {5, new double[]{0, 1, 0}, 2, 0},
            {5, new double[]{-1, 0, 0}, 2, 0},
            {5, new double[]{1, 0, 0}, 2, 1},
            {3, new double[]{0, -1, 0}, 2, 0},
            {3, new double[]{0, 1, 0}, 2, 1},
            {3, new double[]{-1, 0, 0}, 2, 1},
            {3, new double[]{1, 0, 0}, 2, 0},
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
