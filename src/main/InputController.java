package main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Gestiona la entrada de teclado y traduce las acciones a operaciones sobre el
 * modelo y el renderizador del cubo.
 */
public class InputController extends KeyAdapter {

    private final CubeModel model;
    private final CubeRenderer renderer;

    public InputController(CubeModel model, CubeRenderer renderer) {
        this.model = model;
        this.renderer = renderer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                renderer.translate(-5, 0);
                break;
            case KeyEvent.VK_D:
                renderer.translate(5, 0);
                break;
            case KeyEvent.VK_W:
                renderer.translate(0, -5);
                break;
            case KeyEvent.VK_S:
                renderer.translate(0, 5);
                break;
            case KeyEvent.VK_I:
                renderer.applyRotation(0, -5);
                break;
            case KeyEvent.VK_K:
                renderer.applyRotation(0, 5);
                break;
            case KeyEvent.VK_J:
                renderer.applyRotation(1, 5);
                break;
            case KeyEvent.VK_L:
                renderer.applyRotation(1, -5);
                break;
            case KeyEvent.VK_U:
                renderer.applyRotation(2, -5);
                break;
            case KeyEvent.VK_O:
                renderer.applyRotation(2, 5);
                break;
            default:
                return;
        }
        renderer.render();
    }

    /**
     * Calcula el eje y sentido de rotación a partir de un vector de flecha en
     * pantalla y la cara seleccionada de un subcubo.
     * Devuelve un arreglo de dos posiciones: eje y sentido horario (1) o
     * antihorario (0).
     */
    public int[] getArrowRotation(double[] arrowVec, Subcubo sc, int face) {
        double[] rArrow = rotateVector(arrowVec,
                -renderer.getAnguloX(), -renderer.getAnguloY(), -renderer.getAnguloZ());
        double[] normal = sc.getFaceNormalWorld(face);
        double[] axisVec = cross(normal, rArrow);
        double thr = 1e-6;
        if (Math.abs(axisVec[0]) < thr && Math.abs(axisVec[1]) < thr && Math.abs(axisVec[2]) < thr) {
            double[] up = {-renderer.getRotMatrix()[0][1], -renderer.getRotMatrix()[1][1], -renderer.getRotMatrix()[2][1]};
            axisVec = cross(normal, up);
            if (Math.abs(axisVec[0]) < thr && Math.abs(axisVec[1]) < thr && Math.abs(axisVec[2]) < thr) {
                double[] right = {renderer.getRotMatrix()[0][0], renderer.getRotMatrix()[1][0], renderer.getRotMatrix()[2][0]};
                axisVec = cross(normal, right);
            }
            int[] res = mapDirection(axisVec, true);
            boolean cw = res[1] == 1;
            if (dot(rArrow, normal) < 0) {
                cw = !cw;
            }
            return new int[]{res[0], cw ? 1 : 0};
        }
        int[] res = mapDirection(axisVec, true);
        return new int[]{res[0], res[1]};
    }

    private int[] mapDirection(double[] v, boolean negClockwise) {
        double ax = Math.abs(v[0]);
        double ay = Math.abs(v[1]);
        double az = Math.abs(v[2]);
        int axis;
        if (ax >= ay && ax >= az) {
            axis = 0;
        } else if (ay >= ax && ay >= az) {
            axis = 1;
        } else {
            axis = 2;
        }
        boolean cw = negClockwise ? v[axis] < 0 : v[axis] > 0;
        return new int[]{axis, cw ? 1 : 0};
    }

    private double[] rotateVector(double[] v, double ax, double ay, double az) {
        double[] r = java.util.Arrays.copyOf(v, 3);
        double radX = Math.toRadians(ax);
        double radY = Math.toRadians(ay);
        double radZ = Math.toRadians(az);
        double t = r[1] * Math.cos(radX) - r[2] * Math.sin(radX);
        r[2] = r[1] * Math.sin(radX) + r[2] * Math.cos(radX);
        r[1] = t;
        t = r[0] * Math.cos(radY) + r[2] * Math.sin(radY);
        r[2] = -r[0] * Math.sin(radY) + r[2] * Math.cos(radY);
        r[0] = t;
        t = r[0] * Math.cos(radZ) - r[1] * Math.sin(radZ);
        r[1] = r[0] * Math.sin(radZ) + r[1] * Math.cos(radZ);
        r[0] = t;
        return r;
    }

    private double[] cross(double[] a, double[] b) {
        return new double[]{
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    private double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
