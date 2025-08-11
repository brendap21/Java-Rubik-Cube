package main;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Random;
import org.junit.Test;

public class LongScrambleOrientationTest {

    @Test
    public void testOrientationStaysValidAfterLongScramble() throws Exception {
        Subcubo sc = new Subcubo(0, 0, 0, 1);
        Random rnd = new Random(42);
        for (int i = 0; i < 1000; i++) {
            int axis = rnd.nextInt(3);
            boolean cw = rnd.nextBoolean();
            sc.applyGlobalRotation(axis, cw);
            sc.rotateColors(axis, cw);
        }
        Field field = Subcubo.class.getDeclaredField("rotMatrix");
        field.setAccessible(true);
        double[][] m = (double[][]) field.get(sc);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double v = m[i][j];
                assertTrue("Matrix entry not discrete: " + v, v == 0.0 || v == 1.0 || v == -1.0);
            }
            double norm = m[i][0] * m[i][0] + m[i][1] * m[i][1] + m[i][2] * m[i][2];
            assertEquals("Row not unit length", 1.0, norm, 1e-9);
            for (int j = i + 1; j < 3; j++) {
                double dot = m[i][0] * m[j][0] + m[i][1] * m[j][1] + m[i][2] * m[j][2];
                assertEquals("Rows not orthogonal", 0.0, dot, 1e-9);
            }
        }
        double det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
        assertEquals("Determinant is not 1", 1.0, det, 1e-9);
    }
}
