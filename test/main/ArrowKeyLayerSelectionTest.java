package main;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class ArrowKeyLayerSelectionTest {

    private void pressKeyAndWait(Cubo cubo, int keyCode) throws Exception {
        Component panel = cubo.getContentPane().getComponent(0);
        KeyListener kl = panel.getKeyListeners()[0];
        KeyEvent ev = new KeyEvent(panel, KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        kl.keyPressed(ev);
        Field animField = Cubo.class.getDeclaredField("animating");
        animField.setAccessible(true);
        int guard = 0;
        while ((boolean) animField.get(cubo) && guard < 100) {
            Thread.sleep(20);
            guard++;
        }
    }

    @Test
    public void arrowKeysRotateSelectedFace() throws Exception {
        System.setProperty("java.awt.headless", "true");
        Field gameModeF = Cubo.class.getDeclaredField("gameMode");
        Field selXF = Cubo.class.getDeclaredField("selX");
        Field selYF = Cubo.class.getDeclaredField("selY");
        Field selZF = Cubo.class.getDeclaredField("selZ");
        Field selFaceF = Cubo.class.getDeclaredField("selFace");
        Field cuboField = Cubo.class.getDeclaredField("cuboRubik");
        Method getArrow = Cubo.class.getDeclaredMethod("getArrowRotation", double[].class, Subcubo.class, int.class);
        gameModeF.setAccessible(true);
        selXF.setAccessible(true);
        selYF.setAccessible(true);
        selZF.setAccessible(true);
        selFaceF.setAccessible(true);
        cuboField.setAccessible(true);
        getArrow.setAccessible(true);

        int[] keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};
        for (int key : keys) {
            Cubo c = new Cubo();
            gameModeF.setBoolean(c, true);
            selXF.setInt(c, 1);
            selYF.setInt(c, 1);
            selZF.setInt(c, 2);
            selFaceF.setInt(c, 1); // front face
            Subcubo[][][] cubo = (Subcubo[][][]) cuboField.get(c);
            Subcubo selected = cubo[1][1][2];
            double[] arrow;
            switch (key) {
                case KeyEvent.VK_UP: arrow = new double[]{0, -1, 0}; break;
                case KeyEvent.VK_DOWN: arrow = new double[]{0, 1, 0}; break;
                case KeyEvent.VK_LEFT: arrow = new double[]{-1, 0, 0}; break;
                default: arrow = new double[]{1, 0, 0};
            }
            int[] res = (int[]) getArrow.invoke(c, arrow, selected, 1);
            int axis = res[0];
            int originalLayer = axis == 0 ? 1 : axis == 1 ? 1 : 2;

            pressKeyAndWait(c, key);

            cubo = (Subcubo[][][]) cuboField.get(c);
            int nx = -1, ny = -1, nz = -1;
            outer:
            for (int ix = 0; ix < 3; ix++) {
                for (int iy = 0; iy < 3; iy++) {
                    for (int iz = 0; iz < 3; iz++) {
                        if (cubo[ix][iy][iz] == selected) {
                            nx = ix; ny = iy; nz = iz;
                            break outer;
                        }
                    }
                }
            }
            int newLayer = axis == 0 ? nx : axis == 1 ? ny : nz;
            assertEquals("arrow key " + key, originalLayer, newLayer);
        }
    }
}
