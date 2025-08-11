package main;

import static org.junit.Assert.*;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class RotateLayerFullCycleTest {

    @Test
    public void layerReturnsToOriginalAfterFourRotations() throws Exception {
        System.setProperty("java.awt.headless", "true");
        Method rotateLayer = Cubo.class.getDeclaredMethod("rotateLayer", int.class, int.class, boolean.class);
        rotateLayer.setAccessible(true);
        Field cuboField = Cubo.class.getDeclaredField("cuboRubik");
        cuboField.setAccessible(true);
        Field colorField = Subcubo.class.getDeclaredField("colores");
        colorField.setAccessible(true);
        Field matrixField = Subcubo.class.getDeclaredField("rotMatrix");
        matrixField.setAccessible(true);

        for (int axis = 0; axis < 3; axis++) {
            for (int layer = 0; layer < 3; layer++) {
                Cubo c = new Cubo();
                Subcubo[][][] cubo = (Subcubo[][][]) cuboField.get(c);

                Subcubo[][][] origRef = new Subcubo[3][3][3];
                Color[][][][] origColors = new Color[3][3][3][];
                double[][][][][] origMatrix = new double[3][3][3][3][3];

                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Subcubo sc = cubo[x][y][z];
                            origRef[x][y][z] = sc;
                            origColors[x][y][z] = ((Color[]) colorField.get(sc)).clone();
                            double[][] m = (double[][]) matrixField.get(sc);
                            double[][] mc = new double[m.length][m[0].length];
                            for (int r = 0; r < m.length; r++) {
                                mc[r] = m[r].clone();
                            }
                            origMatrix[x][y][z] = mc;
                        }
                    }
                }

                for (int i = 0; i < 4; i++) {
                    rotateLayer.invoke(c, axis, layer, true);
                }
                cubo = (Subcubo[][][]) cuboField.get(c);

                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Subcubo sc = cubo[x][y][z];
                            assertSame("axis=" + axis + " layer=" + layer + " pos=" + x + "," + y + "," + z,
                                    origRef[x][y][z], sc);
                            Color[] expectedColors = origColors[x][y][z];
                            Color[] actualColors = (Color[]) colorField.get(sc);
                            assertArrayEquals(expectedColors, actualColors);
                            double[][] expectedM = origMatrix[x][y][z];
                            double[][] actualM = (double[][]) matrixField.get(sc);
                            for (int r = 0; r < 3; r++) {
                                assertArrayEquals(expectedM[r], actualM[r], 1e-9);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void layerReturnsToOriginalAfterFourCounterRotations() throws Exception {
        System.setProperty("java.awt.headless", "true");
        Method rotateLayer = Cubo.class.getDeclaredMethod("rotateLayer", int.class, int.class, boolean.class);
        rotateLayer.setAccessible(true);
        Field cuboField = Cubo.class.getDeclaredField("cuboRubik");
        cuboField.setAccessible(true);
        Field colorField = Subcubo.class.getDeclaredField("colores");
        colorField.setAccessible(true);
        Field matrixField = Subcubo.class.getDeclaredField("rotMatrix");
        matrixField.setAccessible(true);

        for (int axis = 0; axis < 3; axis++) {
            for (int layer = 0; layer < 3; layer++) {
                Cubo c = new Cubo();
                Subcubo[][][] cubo = (Subcubo[][][]) cuboField.get(c);

                Subcubo[][][] origRef = new Subcubo[3][3][3];
                Color[][][][] origColors = new Color[3][3][3][];
                double[][][][][] origMatrix = new double[3][3][3][3][3];

                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Subcubo sc = cubo[x][y][z];
                            origRef[x][y][z] = sc;
                            origColors[x][y][z] = ((Color[]) colorField.get(sc)).clone();
                            double[][] m = (double[][]) matrixField.get(sc);
                            double[][] mc = new double[m.length][m[0].length];
                            for (int r = 0; r < m.length; r++) {
                                mc[r] = m[r].clone();
                            }
                            origMatrix[x][y][z] = mc;
                        }
                    }
                }

                for (int i = 0; i < 4; i++) {
                    rotateLayer.invoke(c, axis, layer, false);
                }
                cubo = (Subcubo[][][]) cuboField.get(c);

                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            Subcubo sc = cubo[x][y][z];
                            assertSame("axis=" + axis + " layer=" + layer + " pos=" + x + "," + y + "," + z,
                                    origRef[x][y][z], sc);
                            Color[] expectedColors = origColors[x][y][z];
                            Color[] actualColors = (Color[]) colorField.get(sc);
                            assertArrayEquals(expectedColors, actualColors);
                            double[][] expectedM = origMatrix[x][y][z];
                            double[][] actualM = (double[][]) matrixField.get(sc);
                            for (int r = 0; r < 3; r++) {
                                assertArrayEquals(expectedM[r], actualM[r], 1e-9);
                            }
                        }
                    }
                }
            }
        }
    }
}
