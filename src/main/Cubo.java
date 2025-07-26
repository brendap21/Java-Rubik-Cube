package main;

/**
 * Ventana principal que gestiona la interacción con el usuario y el renderizado
 * completo del cubo de Rubik.
 */
import java.awt.Color;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.RenderPanel;

public class Cubo extends JFrame {

    /**
     * Contenedor de utilidades de dibujo.
     */
    private Graficos graficos;
    /**
     * Matriz de subcubos que conforman el cubo de Rubik.
     */
    private Subcubo[][][] cuboRubik;
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
    /**
     * Indica si se muestran las etiquetas de las caras.
     */
    private boolean showLabels = false;
    /**
     * Indica si se muestran los textos de ayuda en pantalla.
     */
    private boolean showControls = true;

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
        setSubcube();
        moverCubo();
    }

    /**
     * Crea todas las piezas del cubo de Rubik en sus posiciones iniciales.
     */
    private void setSubcube() {
        cuboRubik = new Subcubo[3][3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    int posX = (int) ((x - 1) * size * escala);
                    int posY = (int) ((y - 1) * size * escala);
                    int posZ = (int) ((z - 1) * size * escala);
                    cuboRubik[x][y][z] = new Subcubo(posX, posY, posZ, size);
                }
            }
        }

    }

    /**
     * Rota una capa completa del cubo modificando orientación y colores de las
     * piezas que la componen.
     */
    private void rotateLayer(int axis, int layer, boolean clockwise) {
        Subcubo[][][] nuevo = new Subcubo[3][3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    nuevo[x][y][z] = cuboRubik[x][y][z];
                }
            }
        }

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    if ((axis == 0 && x == layer) || (axis == 1 && y == layer) || (axis == 2 && z == layer)) {
                        int nx = x, ny = y, nz = z;
                        if (axis == 0) { // X
                            if (clockwise) {
                                ny = 2 - z;
                                nz = y;
                            } else {
                                ny = z;
                                nz = 2 - y;
                            }
                        } else if (axis == 1) { // Y
                            if (clockwise) {
                                nx = z;
                                nz = 2 - x;
                            } else {
                                nx = 2 - z;
                                nz = x;
                            }
                        } else { // Z
                            if (clockwise) {
                                nx = 2 - y;
                                ny = x;
                            } else {
                                nx = y;
                                ny = 2 - x;
                            }
                        }
                        nuevo[nx][ny][nz] = cuboRubik[x][y][z];
                        nuevo[nx][ny][nz].rotateColors(axis, clockwise);
                        nuevo[nx][ny][nz].rotateOrientation(axis, clockwise);
                    }
                }
            }
        }

        cuboRubik = nuevo;
    }

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

    private int[] mapDirection(double[] v) {
        double ax = Math.abs(v[0]);
        double ay = Math.abs(v[1]);
        double az = Math.abs(v[2]);
        if (ax >= ay && ax >= az) {
            return new int[]{0, v[0] >= 0 ? 2 : 0};
        } else if (ay >= ax && ay >= az) {
            return new int[]{1, v[1] >= 0 ? 2 : 0};
        } else {
            return new int[]{2, v[2] >= 0 ? 2 : 0};
        }
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

    private void applyRotation(int axis, double degrees) {
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

    /**
     * Rota una capa con animación y ejecuta una acción al finalizar.
     */
    private void rotateLayerAnimated(int axis, int layer, boolean clockwise, Runnable done) {
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
                info.cubo.dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, info.highlight,
                        info.ex, info.ey, info.ez,
                        info.tx, info.ty, info.tz,
                        showLabels, info.ix, info.iy, info.iz);
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
                rotateLayer(axis, layer, clockwise);
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
    private void moverCubo() {
        if (!ejeSubcubo) {
            graficos.clear();

            // Encontrar el centro del cubo
            int centroX = 1;
            int centroY = 1;
            int centroZ = 1; // Coordenadas del subcubo 14

            java.util.List<RenderInfo> infos = new java.util.ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        // Posición relativa al centro del cubo
                        double posX = (x - 1) * size;
                        double posY = (y - 1) * size;
                        double posZ = (z - 1) * size;

                        // Aplicar las rotaciones alrededor del subcubo 14
                        double[] rotatedPos = cuboRubik[x][y][z].rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);

                        // Traslación con respecto al movimiento general del cubo
                        int finalX = (int) (rotatedPos[0] + trasX);
                        int finalY = (int) (rotatedPos[1] + trasY);
                        int finalZ = (int) (rotatedPos[2] + trasZ);

                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        double tX = highlight ? selTX : 0;
                        double tY = highlight ? selTY : 0;
                        double tZ = highlight ? selTZ : 0;
                        double depthVal = finalZ + tZ;
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, depthVal,
                                0, 0, 0, tX, tY, tZ, highlight, x, y, z));
                    }
                }
            }
            infos.sort((a, b) -> Double.compare(b.depth, a.depth));
            for (RenderInfo info : infos) {
                info.cubo.dibujar(graficos, 1, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, info.highlight,
                        info.ex, info.ey, info.ez,
                        info.tx, info.ty, info.tz,
                        showLabels, info.ix, info.iy, info.iz);
            }
        } else {
            graficos.clear();

            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        double tX = highlight ? selTX : 0;
                        double tY = highlight ? selTY : 0;
                        double tZ = highlight ? selTZ : 0;
                        cuboRubik[x][y][z].dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                                trasX, trasY, trasZ, lines, highlight, 0, 0, 0,
                                tX, tY, tZ,
                                showLabels, x, y, z);
                    }
                }
            }
        }
        drawUI();
        graficos.render();
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
        RenderPanel panel = new RenderPanel(graficos);
        add(panel);

        // Matriz de rotación inicial basada en los ángulos predeterminados
        rotMatrix = matrixFromAngles(anguloX, anguloY, anguloZ);
        // Attach key events to the render panel so it receives them when
        // focus is requested on this component.
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        trasX -= 5;
                        break;
                    case KeyEvent.VK_D:
                        trasX += 5;
                        break;
                    case KeyEvent.VK_W:
                        trasY -= 5;
                        break;
                    case KeyEvent.VK_S:
                        trasY += 5;
                        break;

                    // — ROTACIÓN EN X (eje horizontal) —
                    case KeyEvent.VK_I:    // tecla I
                        if (!gameMode) {
                            applyRotation(0, -5);
                        } else if (selX != -1) {
                            double[] front = {rotMatrix[0][2], rotMatrix[1][2], rotMatrix[2][2]};
                            double[] dir = cross(new double[]{0, -1, 0}, front);
                            int[] m = mapDirection(dir);
                            int axis = m[0];
                            int layer = axis == 0 ? selX : axis == 1 ? selY : selZ;
                            boolean cw = dir[axis] < 0;
                            rotateLayerAnimated(axis, layer, cw);
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(
                                    rotateVector(new double[]{0, 1, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) {
                                layer = m[1];
                            }
                            rotateLayerAnimated(m[0], layer, true);
                        }
                        break;
                    case KeyEvent.VK_K:    // tecla K
                        if (!gameMode) {
                            applyRotation(0, 5);
                        } else if (selX != -1) {
                            double[] front = {rotMatrix[0][2], rotMatrix[1][2], rotMatrix[2][2]};
                            double[] dir = cross(new double[]{0, 1, 0}, front);
                            int[] m = mapDirection(dir);
                            int axis = m[0];
                            int layer = axis == 0 ? selX : axis == 1 ? selY : selZ;
                            boolean cw = dir[axis] < 0;
                            rotateLayerAnimated(axis, layer, cw);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(
                                    rotateVector(new double[]{0, -1, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) {
                                layer = m[1];
                            }
                            rotateLayerAnimated(m[0], layer, false);
                        }
                        break;

                    // — ROTACIÓN EN Y (eje vertical) —
                    case KeyEvent.VK_J:    // tecla J
                        if (!gameMode) {
                            applyRotation(1, 5);  // giro a la izquierda
                        } else if (selX != -1) {
                            double[] front = {rotMatrix[0][2], rotMatrix[1][2], rotMatrix[2][2]};
                            double[] dir = cross(front, new double[]{-1, 0, 0});
                            int[] m = mapDirection(dir);
                            int axis = m[0];
                            int layer = axis == 0 ? selX : axis == 1 ? selY : selZ;
                            boolean cw = dir[axis] > 0;
                            rotateLayerAnimated(axis, layer, cw);
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(
                                    rotateVector(new double[]{-1, 0, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) {
                                layer = m[1];
                            }
                            rotateLayerAnimated(m[0], layer, false);
                        }
                        break;
                    case KeyEvent.VK_L:    // tecla L
                        if (!gameMode) {
                            applyRotation(1, -5);  // giro a la derecha
                        } else if (selX != -1) {
                            double[] front = {rotMatrix[0][2], rotMatrix[1][2], rotMatrix[2][2]};
                            double[] dir = cross(front, new double[]{1, 0, 0});
                            int[] m = mapDirection(dir);
                            int axis = m[0];
                            int layer = axis == 0 ? selX : axis == 1 ? selY : selZ;
                            boolean cw = dir[axis] > 0;
                            rotateLayerAnimated(axis, layer, cw);
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(
                                    rotateVector(new double[]{1, 0, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) {
                                layer = m[1];
                            }
                            rotateLayerAnimated(m[0], layer, true);
                        }
                        break;

                    // — ROTACIÓN EN Z (profundidad) —
                    case KeyEvent.VK_O:
                        if (!gameMode) {
                            applyRotation(2, 5);
                        }
                        break;
                    case KeyEvent.VK_U:
                        if (!gameMode) {
                            applyRotation(2, -5);
                        }
                        break;

                    case KeyEvent.VK_R:
                        if (gameMode) {
                            scrambleAnimation();
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        gameMode = !gameMode;
                        if (!gameMode) {
                            selX = selY = selZ = -1;
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (gameMode) {
                            selX = selY = selZ = -1;
                            moverCubo();
                        }
                        break;
                    case KeyEvent.VK_B:
                        lines = !lines;
                        break;
                    case KeyEvent.VK_E:
                        ejeSubcubo = !ejeSubcubo;
                        break;
                    case KeyEvent.VK_N:
                        showLabels = !showLabels;
                        break;
                    case KeyEvent.VK_H:
                        showControls = !showControls;
                        break;
                }
                moverCubo();
            }
        });

        // --- CLICK IZQUIERDO: SELECCIONAR EN MODO JUEGO O ROTAR EN MODO VISTA ---
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && gameMode) {
                    int mx = e.getX(), my = e.getY();
                    double bestDepth = Double.MAX_VALUE;
                    int idxX = -1, idxY = -1, idxZ = -1;
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            for (int z = 0; z < 3; z++) {
                                Subcubo sc = cuboRubik[x][y][z];
                                if (sc.containsPoint(mx, my)) {
                                    double posX = (x - 1) * size, posY = (y - 1) * size, posZ = (z - 1) * size;
                                    double[] r = sc.rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                                    if (r[2] < bestDepth) {
                                        bestDepth = r[2];
                                        idxX = x;
                                        idxY = y;
                                        idxZ = z;
                                    }
                                }
                            }
                        }
                    }
                    if (idxX != -1) {
                        selX = idxX;
                        selY = idxY;
                        selZ = idxZ;
                        selFace = cuboRubik[idxX][idxY][idxZ].faceAt(mx, my);
                        animateSelection();
                    }
                    moverCubo();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    int mx = e.getX(), my = e.getY();
                    double bestDepth = Double.MAX_VALUE;
                    int idxX = -1, idxY = -1, idxZ = -1;
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            for (int z = 0; z < 3; z++) {
                                Subcubo sc = cuboRubik[x][y][z];
                                if (sc.containsPoint(mx, my)) {
                                    double posX = (x - 1) * size, posY = (y - 1) * size, posZ = (z - 1) * size;
                                    double[] r = sc.rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                                    if (r[2] < bestDepth) {
                                        bestDepth = r[2];
                                        idxX = x;
                                        idxY = y;
                                        idxZ = z;
                                    }
                                }
                            }
                        }
                    }
                    if (idxX != -1 && isCorner(idxX, idxY, idxZ)) {
                        draggingCorner = true;
                        draggingFace = draggingLayerZ = false;
                    } else {
                        draggingCorner = false;
                    }
                    lastX = e.getX();
                    lastY = e.getY();
                } else if (SwingUtilities.isLeftMouseButton(e) && !gameMode) {
                    int mx = e.getX(), my = e.getY();
                    int cx = 0, cy = 0;
                    double bestDepth = Double.MAX_VALUE;
                    int idxX = -1, idxY = -1, idxZ = -1;
                    // Busco el subcubo más cercano bajo el cursor
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            for (int z = 0; z < 3; z++) {
                                Subcubo sc = cuboRubik[x][y][z];
                                if (sc.containsPoint(mx, my)) {
                                    double posX = (x - 1) * size, posY = (y - 1) * size, posZ = (z - 1) * size;
                                    double[] r = sc.rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                                    if (r[2] < bestDepth) {
                                        bestDepth = r[2];
                                        idxX = x;
                                        idxY = y;
                                        idxZ = z;
                                        cx = cy = 0;
                                        int[][] verts = sc.getScreenVertices();
                                        for (int i = 0; i < 8; i++) {
                                            cx += verts[i][0];
                                            cy += verts[i][1];
                                        }
                                        cx /= 8;
                                        cy /= 8;
                                    }
                                }
                            }
                        }
                    }
                    if (idxX != -1 && isFrontFace(idxX, idxY, idxZ)) {
                        int dx = cx - trasX, dy = cy - trasY;
                        draggingCorner = false;
                        if (e.isShiftDown()) {
                            // Arrastre para rotar la capa frontal en Z
                            draggingLayerZ = true;
                            draggingFace = false;
                            lastX = e.getX();
                            lastY = e.getY();
                        } else {
                            // Rotación global en X/Y al arrastrar la cara
                            draggingFace = true;
                            draggingLayerZ = false;
                            lastX = e.getX();
                            lastY = e.getY();
                            if (Math.abs(dx) >= Math.abs(dy)) {
                                if (dx < 0) {
                                    // Izquierda (J/←)
                                    applyRotation(1, 5);
                                } else {
                                    // Derecha (L/→)
                                    applyRotation(1, -5);
                                }
                            } else {
                                if (dy < 0) {
                                    // Arriba (I/↑)
                                    applyRotation(0, -5);
                                } else {
                                    // Abajo (K/↓)
                                    applyRotation(0, 5);
                                }
                            }
                        }
                    }
                    moverCubo();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingCorner = false;
                draggingFace = false;
                draggingLayerZ = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingCorner && (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
                    double vx1 = lastX - trasX;
                    double vy1 = lastY - trasY;
                    double vx2 = e.getX() - trasX;
                    double vy2 = e.getY() - trasY;
                    double cross = vx1 * vy2 - vy1 * vx2;
                    double dot = vx1 * vx2 + vy1 * vy2;
                    double angle = Math.toDegrees(Math.atan2(cross, dot));
                    applyRotation(2, angle);
                    lastX = e.getX();
                    lastY = e.getY();
                    moverCubo();
                } else if (!gameMode && draggingLayerZ && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
                    double vx1 = lastX - trasX;
                    double vy1 = lastY - trasY;
                    double vx2 = e.getX() - trasX;
                    double vy2 = e.getY() - trasY;
                    double cross = vx1 * vy2 - vy1 * vx2;
                    double dot = vx1 * vx2 + vy1 * vy2;
                    double angle = Math.toDegrees(Math.atan2(cross, dot));
                    boolean clockwise = angle > 0;
                    int[] front = getFrontAxis();
                    int axis = front[0];
                    int layer = front[1] > 0 ? 2 : 0;
                    rotateLayerAnimated(axis, layer, clockwise);
                    draggingLayerZ = false;
                } else if (!gameMode && draggingFace && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
                    int dx = e.getX() - lastX, dy = e.getY() - lastY;
                    applyRotation(1, -dx / 2.0);
                    applyRotation(0, dy / 2.0);
                    lastX = e.getX();
                    lastY = e.getY();
                    moverCubo();
                } else if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
                    int dx = e.getX() - lastX, dy = e.getY() - lastY;
                    // --- ARRASTRE DERECHO: también invertido ---
                    applyRotation(1, -dx / 2.0); // antes era += dx/2.0
                    applyRotation(0, dy / 2.0);  // antes era -= dy/2.0
                    lastX = e.getX();
                    lastY = e.getY();
                    moverCubo();
                }
            }
        });

        panel.addMouseWheelListener(e -> {
            size -= e.getWheelRotation() * 5;
            if (size < 20) {
                size = 20;
            }
            setSubcube();
            moverCubo();
        });

        // El panel debe poder enfocarse para captar las teclas
        panel.setFocusable(true);
        panel.requestFocusInWindow(); // Asegura la recepción de eventos de teclado

        setVisible(true);
    }

    /**
     * Dibuja textos y botones de ayuda sobre la imagen generada.
     */
    private void drawUI() {
        if (!showControls) {
            PixelFont.drawString(graficos, "RUBIK 3D", 10, 20, 5, Color.WHITE);
            PixelFont.drawString(graficos, gameMode ? "MODE: PLAY" : "MODE: VIEW", 620, 20, 2, Color.YELLOW);
            return;
        }
        PixelFont.drawString(graficos, "RUBIK 3D", 10, 20, 5, Color.WHITE);
        PixelFont.drawString(graficos, gameMode ? "MODE: PLAY" : "MODE: VIEW", 620, 20, 2, Color.YELLOW);

        int y = 60;
        int step = 24;

        if (gameMode) {
            y += step;
            PixelFont.drawString(graficos, "RIGHT CLICK SELECTS A SUBCUBE", 10, y, 2, Color.WHITE);
            y += step;
            PixelFont.drawString(graficos, "ESC CLEARS SUBCUBE SELECTION", 10, y, 2, Color.WHITE);
            y += step;
            PixelFont.drawString(graficos, "PRESS ENTER TO VIEW MODE", 10, y, 2, Color.WHITE);
            y += step;
            PixelFont.drawString(graficos, "R MIX CUBE", 10, y, 2, Color.WHITE);
            y += step;
        } else {
            y += step;
            PixelFont.drawString(graficos, "PRESS ENTER TO PLAY MODE", 10, y, 2, Color.WHITE);
            y += step;
        }
        y += step;
        PixelFont.drawString(graficos, "WASD MOVE CUBE UP LEFT DOWN RIGHT", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "IJKL ROTATE CUBE UP LEFT DOWN RIGHT", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "UO ROTATE CUBE IN Z DIRECTION LEFT RIGHT", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "LEFT DRAG ROTATE CUBE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "MOUSE WHEEL SCALE", 10, y, 2, Color.WHITE);
        y += step;
        y += step;
        PixelFont.drawString(graficos, "B TOGGLE LINES", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "E CHANGE AXIS", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "N TOGGLE LABELS", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "H SHOW CONTROLLS", 10, y, 2, Color.WHITE);
        y += step;

    }
}
