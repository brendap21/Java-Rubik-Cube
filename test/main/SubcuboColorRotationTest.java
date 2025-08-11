package main;

import static org.junit.Assert.*;
import java.awt.Color;
import java.lang.reflect.Field;
import org.junit.Test;

public class SubcuboColorRotationTest {

    @Test
    public void testRotateColorsRepositionsFaces() throws Exception {
        Field field = Subcubo.class.getDeclaredField("colores");
        field.setAccessible(true);

        for (int axis = 0; axis < 3; axis++) {
            for (boolean cw : new boolean[]{true, false}) {
                Subcubo sc = new Subcubo(0, 0, 0, 1);
                Color[] orig = ((Color[]) field.get(sc)).clone();
                sc.rotateColors(axis, cw);
                Color[] expected = expected(orig, axis, cw);
                Color[] actual = (Color[]) field.get(sc);
                assertArrayEquals("axis=" + axis + " cw=" + cw, expected, actual);
            }
        }
    }

    private Color[] expected(Color[] c, int axis, boolean cw) {
        Color[] r = c.clone();
        switch (axis) {
            case 0:
                if (cw) {
                    r[0] = c[2];
                    r[1] = c[3];
                    r[2] = c[1];
                    r[3] = c[0];
                } else {
                    r[0] = c[3];
                    r[1] = c[2];
                    r[2] = c[0];
                    r[3] = c[1];
                }
                break;
            case 1:
                if (cw) {
                    r[0] = c[4];
                    r[1] = c[5];
                    r[4] = c[1];
                    r[5] = c[0];
                } else {
                    r[0] = c[5];
                    r[1] = c[4];
                    r[4] = c[0];
                    r[5] = c[1];
                }
                break;
            case 2:
                if (cw) {
                    r[2] = c[5];
                    r[3] = c[4];
                    r[4] = c[2];
                    r[5] = c[3];
                } else {
                    r[2] = c[4];
                    r[3] = c[5];
                    r[4] = c[3];
                    r[5] = c[2];
                }
                break;
        }
        return r;
    }

    @Test
    public void testFourRotationsReturnOriginal() throws Exception {
        Field field = Subcubo.class.getDeclaredField("colores");
        field.setAccessible(true);

        for (int axis = 0; axis < 3; axis++) {
            for (boolean cw : new boolean[]{true, false}) {
                Subcubo sc = new Subcubo(0, 0, 0, 1);
                Color[] orig = ((Color[]) field.get(sc)).clone();
                for (int i = 0; i < 4; i++) {
                    sc.rotateColors(axis, cw);
                }
                Color[] actual = (Color[]) field.get(sc);
                assertArrayEquals("axis=" + axis + " cw=" + cw, orig, actual);
            }
        }
    }
}

