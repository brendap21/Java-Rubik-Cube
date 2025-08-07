package main;

/**
 * Ventana principal que gestiona la interacción con el usuario y el renderizado
 * completo del cubo de Rubik.
 */
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.RenderPanel;
import main.CubeModel;
import main.CubeRenderer;
import main.InputController;

public class Cubo extends JFrame {

    /**
     * Contenedor de utilidades de dibujo.
     */
    private Graficos graficos;
    /** Modelo que contiene el estado del cubo. */
    private CubeModel model;
    /**
     * Matriz de subcubos que conforman el cubo de Rubik.
     */
    private Subcubo[][][] cuboRubik;
    /** Encargado de renderizar el cubo. */
    private CubeRenderer renderer;
    /** Controlador de entradas. */
    private InputController inputController;
    /** Panel de render. */
    private RenderPanel panel;
    /**
     * Rotaciones globales del cubo.
     */
    private double anguloX = 30, anguloY = 30, anguloZ = 0;
    /**
     * Matriz de rotación acumulada para mantener la orientación del cubo. Se
     * actualiza cada vez que se aplica una rotación para conservar la misma
     * dirección independientemente de la vista actual.
     */
    private double[][] rotMatrix;
    /**
     * Factor de escala para separar los subcubos.
     */
    private double escala = 1;
    /**
     * Traslación para centrar el cubo en la ventana.
     */
    private int trasX = 400, trasY = 300, trasZ = 0;
    /**
     * Tamaño de cada subcubo en píxeles.
     */
    private int size = 80;
    /**
     * Indica si se dibujan las líneas de las caras.
     */
    private boolean lines = true;
    /**
     * Si está activo, las rotaciones se hacen pieza por pieza.
     */
    private boolean ejeSubcubo = false;
    /**
     * Posiciones del ratón para calcular arrastre.
     */
    private int lastX;
    private int lastY;
    /**
     * Indica si se está arrastrando desde una esquina para rotar en Z.
     */
    private boolean draggingCorner = false;
    /**
     * Indica si se está arrastrando un subcubo de cara para rotar en X/Y.
     */
    private boolean draggingFace = false;
    /**
     * Indica si se está arrastrando una capa frontal para rotar en Z.
     */
    private boolean draggingLayerZ = false;
    /**
     * Modo de juego en el que se puede seleccionar subcubos.
     */
    private boolean gameMode = false;
    private int selX = -1, selY = -1, selZ = -1;
    private double selTX = 0, selTY = 0, selTZ = 0;
    private int selFace = -1;
    private int selMX = -1, selMY = -1;
    /**
     * Indica si se muestran las etiquetas de las caras.
     */
    private boolean showLabels = false;
    /**
     * Indica si se muestran los textos de ayuda en pantalla.
     */
    private boolean showControls = true;

    /**
     * True mientras se ejecuta una animación de rotación.
     */
    private boolean animating = false;

    /**
     * Información auxiliar usada durante el renderizado para ordenar las piezas
     * por profundidad.
     */
    private static class RenderInfo {

        final Subcubo cubo;
        final int x, y;
        final double depth;
        final double ex, ey, ez;
        final double tx, ty, tz;
        final boolean highlight;
        final int ix, iy, iz;

        RenderInfo(Subcubo c, int x, int y, double depth,
                double ex, double ey, double ez,
                double tx, double ty, double tz,
                boolean h, int ix, int iy, int iz) {
            this.cubo = c;
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.ex = ex;
            this.ey = ey;
            this.ez = ez;
            this.tx = tx;
            this.ty = ty;
            this.tz = tz;
            this.highlight = h;
            this.ix = ix;
            this.iy = iy;
            this.iz = iz;
        }
    }

    /**
     * Inicializa la ventana y el cubo de Rubik.
     */
    public Cubo() {
        initComponents();
        model = new CubeModel(size);
        cuboRubik = model.getCube();
        renderer = new CubeRenderer(graficos);
        inputController = new InputController(model, renderer, this);
        panel.addKeyListener(inputController);
        panel.addMouseListener(inputController);
        panel.addMouseMotionListener(inputController);
        panel.addMouseWheelListener(inputController);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        moverCubo();
    }

    /**
     * Rota una capa completa del cubo modificando orientación y colores de las
     * piezas que la componen.
     */
    private double[] rotatePointAroundAxis(double[] p, int axis, double angleDeg, double offset) {
        double rad = Math.toRadians(angleDeg);
        double x = p[0];
        double y = p[1];
        double z = p[2];
        switch (axis) {
            case 0: // X axis
                x -= offset;
                double ty = y;
                double tz = z;
                y = ty * Math.cos(rad) - tz * Math.sin(rad);
                z = ty * Math.sin(rad) + tz * Math.cos(rad);
                x += offset;
                break;
            case 1: // Y axis
                y -= offset;
                double tx = x;
                double tz2 = z;
                x = tx * Math.cos(rad) + tz2 * Math.sin(rad);
                z = -tx * Math.sin(rad) + tz2 * Math.cos(rad);
                y += offset;
                break;
            case 2: // Z axis
                z -= offset;
                double tx2 = x;
                double ty2 = y;
                x = tx2 * Math.cos(rad) - ty2 * Math.sin(rad);
                y = tx2 * Math.sin(rad) + ty2 * Math.cos(rad);
                z += offset;
                break;
        }
        return new double[]{x, y, z};
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
        int layer = axis == 0 ? selX : axis == 1 ? selY : selZ;
        boolean cw = negClockwise ? v[axis] < 0 : v[axis] > 0;
        return new int[]{axis, layer, cw ? 1 : 0};
    }


    // Calcula el eje y sentido de rotación a partir de un vector de flecha en
    // pantalla y la cara seleccionada de un subcubo
    private int[] getArrowRotation(double[] arrowVec, Subcubo sc, int face) {
        double[] rArrow = rotateVector(arrowVec, -anguloX, -anguloY, -anguloZ);
        double[] normal = sc.getFaceNormalWorld(face);
        // Use the face normal and arrow to obtain the rotation axis so that
        // pressing an arrow key rotates the selected layer following the
        // arrow direction regardless of the cube orientation.
        double[] axisVec = cross(normal, rArrow);
        double thr = 1e-6;
        if (Math.abs(axisVec[0]) < thr && Math.abs(axisVec[1]) < thr && Math.abs(axisVec[2]) < thr) {
            double[] up = {-rotMatrix[0][1], -rotMatrix[1][1], -rotMatrix[2][1]};
            axisVec = cross(normal, up);
            if (Math.abs(axisVec[0]) < thr && Math.abs(axisVec[1]) < thr && Math.abs(axisVec[2]) < thr) {
                double[] right = {rotMatrix[0][0], rotMatrix[1][0], rotMatrix[2][0]};
                axisVec = cross(normal, right);
            }
            int[] res = mapDirection(axisVec, true);
            boolean cw = res[2] == 1;
            if (dot(rArrow, normal) < 0) {
                cw = !cw;
            }
            return new int[]{res[0], cw ? 1 : 0};
        }
        int[] res = mapDirection(axisVec, true);
        return new int[]{res[0], res[2]};
    }

    // ----- Ayudas para detectar la cara y esquinas visibles -----
    private static final double EPS = 0.1;

    // Determina cuál eje local está orientado hacia el observador
    private int[] getFrontAxis() {
        double[] dir = {rotMatrix[0][2], rotMatrix[1][2], rotMatrix[2][2]};
        int axis = 0;
        double max = Math.abs(dir[0]);
        for (int i = 1; i < 3; i++) {
            if (Math.abs(dir[i]) > max) {
                axis = i;
                max = Math.abs(dir[i]);
            }
        }
        return new int[]{axis, dir[axis] >= 0 ? 1 : -1};
    }

    // Comprueba si un subcubo pertenece a la cara frontal visible
    private boolean isFrontFace(int x, int y, int z) {
        int[] front = getFrontAxis();
        int axis = front[0];
        int pos = front[1] > 0 ? 2 : 0;
        switch (axis) {
            case 0:
                return x == pos;
            case 1:
                return y == pos;
            default:
                return z == pos;
        }
    }

    // Comprueba si un subcubo está en la esquina visible de la cara frontal
    private boolean isFrontCorner(int x, int y, int z) {
        int[] front = getFrontAxis();
        int axis = front[0];
        int pos = front[1] > 0 ? 2 : 0;
        switch (axis) {
            case 0:
                return x == pos && (y == 0 || y == 2) && (z == 0 || z == 2);
            case 1:
                return y == pos && (x == 0 || x == 2) && (z == 0 || z == 2);
            default:
                return z == pos && (x == 0 || x == 2) && (y == 0 || y == 2);
        }
    }

    // Comprueba si un subcubo es una esquina del cubo independientemente de la cara visible
    private boolean isCorner(int x, int y, int z) {
        return (x == 0 || x == 2) && (y == 0 || y == 2) && (z == 0 || z == 2);
    }

    // ----- Utilidades para el manejo de rotaciones globales -----
    private double[][] matrixFromAngles(double ax, double ay, double az) {
        double radX = Math.toRadians(ax);
        double radY = Math.toRadians(ay);
        double radZ = Math.toRadians(az);

        double cx = Math.cos(radX), sx = Math.sin(radX);
        double cy = Math.cos(radY), sy = Math.sin(radY);
        double cz = Math.cos(radZ), sz = Math.sin(radZ);

        double[][] m = new double[3][3];
        m[0][0] = cy * cz;
        m[0][1] = cz * sy * sx - sz * cx;
        m[0][2] = cz * sy * cx + sz * sx;
        m[1][0] = cy * sz;
        m[1][1] = sz * sy * sx + cz * cx;
        m[1][2] = sz * sy * cx - cz * sx;
        m[2][0] = -sy;
        m[2][1] = cy * sx;
        m[2][2] = cy * cx;
        return m;
    }

    private double[] anglesFromMatrix(double[][] m) {
        double ay = Math.asin(-m[2][0]);
        double cy = Math.cos(ay);
        double ax, az;
        if (Math.abs(cy) > 1e-6) {
            ax = Math.atan2(m[2][1], m[2][2]);
            az = Math.atan2(m[1][0], m[0][0]);
        } else {
            ax = Math.atan2(-m[1][2], m[1][1]);
            az = 0;
        }
        return new double[]{Math.toDegrees(ax), Math.toDegrees(ay), Math.toDegrees(az)};
    }

    private double[][] multiply(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    public void applyRotation(int axis, double degrees) {
        double rad = Math.toRadians(degrees);
        double c = Math.cos(rad), s = Math.sin(rad);
        double[][] r = new double[3][3];
        switch (axis) {
            case 0: // X
                r = new double[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
                break;
            case 1: // Y
                r = new double[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
                break;
            case 2: // Z
                r = new double[][]{{c, -s, 0}, {s, c, 0}, {0, 0, 1}};
                break;
        }
        rotMatrix = multiply(r, rotMatrix);
        double[] angs = anglesFromMatrix(rotMatrix);
        anguloX = angs[0];
        anguloY = angs[1];
        anguloZ = angs[2];
    }

    /** Ajusta la traslación del cubo en pantalla. */
    public void moveTranslation(int dx, int dy) {
        trasX += dx;
        trasY += dy;
    }

    /**
     * Rota una capa con animación y ejecuta una acción al finalizar.
     */
    private void rotateLayerAnimated(int axis, int layer, boolean clockwise, Runnable done) {
        if (animating) {
            return;
        }
        animating = true;
        int dir = clockwise ? 1 : -1;
        double offset = (layer - 1) * size;

        final int[] ang = {0};
        javax.swing.Timer timer = new javax.swing.Timer(20, null);
        timer.addActionListener(e -> {
            graficos.clear();
            java.util.List<RenderInfo> infos = new java.util.ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        double posX = (x - 1) * size;
                        double posY = (y - 1) * size;
                        double posZ = (z - 1) * size;
                        double extraX = 0, extraY = 0, extraZ = 0;
                        if (axis == 0 && x == layer) {
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 0, dir * ang[0], offset);
                            posX = r[0];
                            posY = r[1];
                            posZ = r[2];
                            extraX = dir * ang[0];
                        } else if (axis == 1 && y == layer) {
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 1, dir * ang[0], offset);
                            posX = r[0];
                            posY = r[1];
                            posZ = r[2];
                            extraY = dir * ang[0];
                        } else if (axis == 2 && z == layer) {
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 2, dir * ang[0], offset);
                            posX = r[0];
                            posY = r[1];
                            posZ = r[2];
                            extraZ = dir * ang[0];
                        }
                        double[] rotatedPos = cuboRubik[x][y][z].rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                        int finalX = (int) (rotatedPos[0] + trasX);
                        int finalY = (int) (rotatedPos[1] + trasY);
                        int finalZ = (int) (rotatedPos[2] + trasZ);
                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        double tX = highlight ? selTX : 0;
                        double tY = highlight ? selTY : 0;
                        double tZ = highlight ? selTZ : 0;
                        double depthVal = finalZ + tZ;
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, depthVal,
                                extraX, extraY, extraZ, tX, tY, tZ,
                                highlight, x, y, z));
                    }
                }
            }
            infos.sort((aInfo, bInfo) -> Double.compare(bInfo.depth, aInfo.depth));
            for (RenderInfo info : infos) {
                RenderOptions opt = new RenderOptions();
                opt.highlight = info.highlight;
                opt.extraRotX = info.ex;
                opt.extraRotY = info.ey;
                opt.extraRotZ = info.ez;
                opt.extraTX = info.tx;
                opt.extraTY = info.ty;
                opt.extraTZ = info.tz;
                opt.showLabels = showLabels;
                opt.idxX = info.ix;
                opt.idxY = info.iy;
                opt.idxZ = info.iz;
                info.cubo.dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, opt);
            }
            drawUI();
            graficos.render();

            ang[0] += 10;
            if (ang[0] > 90) {
                timer.stop();
                Subcubo selected = null;
                if (selX != -1) {
                    selected = cuboRubik[selX][selY][selZ];
                }
                model.rotateLayer(axis, layer, clockwise);
                if (selected != null) {
                    for (int ix = 0; ix < 3; ix++) {
                        for (int iy = 0; iy < 3; iy++) {
                            for (int iz = 0; iz < 3; iz++) {
                                if (cuboRubik[ix][iy][iz] == selected) {
                                    selX = ix;
                                    selY = iy;
                                    selZ = iz;
                                }
                            }
                        }
                    }
                }
                moverCubo();
                if (selected != null && selMX != -1) {
                    selFace = cuboRubik[selX][selY][selZ].faceAt(selMX, selMY);
                }
                animating = false;
                if (done != null) {
                    done.run();
                }
            }
        });
        timer.start();
    }

    private void rotateLayerAnimated(int axis, int layer, boolean clockwise) {
        rotateLayerAnimated(axis, layer, clockwise, null);
    }
    /**
     * Realiza una pequeña animación de selección desplazando temporalmente el
     * subcubo escogido hacia fuera.
     */
    private void animateSelection() {
        if (selX == -1) {
            return;
        }
        final int[] step = {0};
        javax.swing.Timer timer = new javax.swing.Timer(20, null);
        timer.addActionListener(e -> {
            double amount = Math.sin(Math.PI * step[0] / 10.0) * 10.0;
            double dx = selX - 1;
            double dy = selY - 1;
            double dz = selZ - 1;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len == 0) len = 1;
            double[] dir = rotateVector(new double[]{dx / len, dy / len, dz / len},
                    anguloX, anguloY, anguloZ);
            selTX = dir[0] * amount;
            selTY = dir[1] * amount;
            selTZ = dir[2] * amount;
            moverCubo();
            step[0]++;
            if (step[0] > 10) {
                timer.stop();
                selTX = selTY = selTZ = 0;
                moverCubo();
            }
        });
        timer.start();
    }

    private void swapSubcubes(int x1, int y1, int z1, int x2, int y2, int z2) {
        Subcubo tmp = cuboRubik[x1][y1][z1];
        cuboRubik[x1][y1][z1] = cuboRubik[x2][y2][z2];
        cuboRubik[x2][y2][z2] = tmp;
    }
    /**
     * Redibuja el cubo aplicando las rotaciones y traslaciones actuales.
     */
    public void moverCubo() {
        renderer.moverCubo(cuboRubik, anguloX, anguloY, anguloZ,
                trasX, trasY, trasZ, size, lines, ejeSubcubo,
                gameMode, selX, selY, selZ, selTX, selTY, selTZ,
                showLabels, showControls);
    }

    /**
     * Mezcla aleatoriamente el cubo realizando varias rotaciones animadas.
     */
    private void scrambleAnimation() {
        java.util.Random r = new java.util.Random();
        java.util.List<int[]> moves = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            moves.add(new int[]{r.nextInt(3), r.nextInt(3), r.nextBoolean() ? 1 : 0});
        }
        scrambleStep(moves, 0);
    }

    /**
     * Ejecuta recursivamente los pasos de mezclado.
     */
    private void scrambleStep(java.util.List<int[]> moves, int idx) {
        if (idx >= moves.size()) {
            return;
        }
        int[] m = moves.get(idx);
        rotateLayerAnimated(m[0], m[1], m[2] == 1, () -> scrambleStep(moves, idx + 1));
    }

    public static void main(String[] args) {
        new Cubo();
    }

        private void initComponents() {
        setTitle("Cubo de Rubik en 3D");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        graficos = new Graficos(800, 600);
        panel = new RenderPanel(graficos);
        add(panel);

        // Matriz de rotación inicial basada en los ángulos predeterminados
        rotMatrix = matrixFromAngles(anguloX, anguloY, anguloZ);

        setVisible(true);
    }

    /**
     * Dibuja textos y botones de ayuda sobre la imagen generada.
     */
    private void drawUI() {
        renderer.drawUI(showControls, gameMode);
    }
}
