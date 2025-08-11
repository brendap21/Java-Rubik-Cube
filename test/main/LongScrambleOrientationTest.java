package main;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Random;
import org.junit.Test;

public class LongScrambleOrientationTest {

    @Test
    public void testOrientationStaysDiscreteAfterLongScramble() throws Exception {
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
        }
    }
}
