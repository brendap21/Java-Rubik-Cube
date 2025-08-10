package main;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class RotateLayerCoordinateTest {

    @Test
    public void subcubeIndicesUpdateOnRotateLayer() throws Exception {
        System.setProperty("java.awt.headless", "true");
        Method rotateLayer = Cubo.class.getDeclaredMethod("rotateLayer", int.class, int.class, boolean.class);
        rotateLayer.setAccessible(true);
        Field cuboField = Cubo.class.getDeclaredField("cuboRubik");
        cuboField.setAccessible(true);

        for (int axis = 0; axis < 3; axis++) {
            for (int layer = 0; layer < 3; layer++) {
                Cubo c = new Cubo();
                Subcubo[][][] cubo = (Subcubo[][][]) cuboField.get(c);
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            cubo[x][y][z].x = x;
                            cubo[x][y][z].y = y;
                            cubo[x][y][z].z = z;
                        }
                    }
                }
                rotateLayer.invoke(c, axis, layer, true);
                cubo = (Subcubo[][][]) cuboField.get(c);
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Subcubo sc = cubo[x][y][z];
                            assertEquals(x, sc.x);
                            assertEquals(y, sc.y);
                            assertEquals(z, sc.z);
                        }
                    }
                }
            }
        }
    }
}
