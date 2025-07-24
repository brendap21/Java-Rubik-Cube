package main;

/**
 * Ventana principal que gestiona la interacción con el usuario y el renderizado
 * completo del cubo de Rubik.
 */

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

import main.RenderPanel;

public class Cubo extends JFrame {

    /** Contenedor de utilidades de dibujo. */
    private Graficos graficos;
    /** Matriz de subcubos que conforman el cubo de Rubik. */
    private Subcubo[][][] cuboRubik;
    /** Rotaciones globales del cubo. */
    private double anguloX = 30, anguloY = 30, anguloZ = 0;
    /** Factor de escala para separar los subcubos. */
    private double escala = 1;
    /** Traslación para centrar el cubo en la ventana. */
    private int trasX = 400, trasY = 300, trasZ = 0;
    /** Tamaño de cada subcubo en píxeles. */
    private int size = 80;
    /** Indica si se dibujan las líneas de las caras. */
    private boolean lines = true;
    /** Si está activo, las rotaciones se hacen pieza por pieza. */
    private boolean ejeSubcubo = false;
    /** Posiciones del ratón para calcular arrastre. */
    private int lastX;
    private int lastY;
    /** Modo de juego en el que se puede seleccionar subcubos. */
    private boolean gameMode = false;
    private int selX = -1, selY = -1, selZ = -1;

    /**
     * Información auxiliar usada durante el renderizado para ordenar las
     * piezas por profundidad.
     */
    private static class RenderInfo {

        final Subcubo cubo;
        final int x, y;
        final double depth;
        final double ex, ey, ez;
        final boolean highlight;
        final int ix, iy, iz;

        RenderInfo(Subcubo c, int x, int y, double depth, double ex, double ey,
                   double ez, boolean h, int ix, int iy, int iz) {
            this.cubo = c;
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.ex = ex;
            this.ey = ey;
            this.ez = ez;
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
     * Rota una capa completa del cubo modificando orientación y colores de
     * las piezas que la componen.
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
                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
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
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, finalZ,
                                extraX, extraY, extraZ, highlight, x, y, z));
                    }
                }
            }
            infos.sort((aInfo, bInfo) -> Double.compare(bInfo.depth, aInfo.depth));
            for (RenderInfo info : infos) {
                info.cubo.dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, info.highlight,
                        info.ex, info.ey, info.ez, true, info.ix, info.iy, info.iz);
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
                        // Calcular las nuevas coordenadas rotadas alrededor del subcubo 14
                        int newX = centroX + (x - centroX);
                        int newY = centroY + (y - centroY);
                        int newZ = centroZ + (z - centroZ);

                        double posX = (newX - 1) * size;
                        double posY = (newY - 1) * size;
                        double posZ = (newZ - 1) * size;

                        // Aplicar las rotaciones alrededor del subcubo 14
                        double[] rotatedPos = cuboRubik[x][y][z].rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);

                        // Traslación con respecto al movimiento general del cubo
                        int finalX = (int) (rotatedPos[0] + trasX);
                        int finalY = (int) (rotatedPos[1] + trasY);
                        int finalZ = (int) (rotatedPos[2] + trasZ);

                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, finalZ, 0, 0, 0, highlight, x, y, z));
                    }
                }
            }
            infos.sort((a, b) -> Double.compare(b.depth, a.depth));
            for (RenderInfo info : infos) {
                 info.cubo.dibujar(graficos, 1, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, info.highlight,
                        info.ex, info.ey, info.ez, true, info.ix, info.iy, info.iz);
            }
        } else {
            graficos.clear();

            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        cuboRubik[x][y][z].dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                                trasX, trasY, trasZ, lines, highlight, 0, 0, 0, true, x, y, z);
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
        addKeyListener(new KeyAdapter() {
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
                    case KeyEvent.VK_I:
                        anguloX -= 5;
                        break;
                    case KeyEvent.VK_K:
                        anguloX += 5;
                        break;
                    case KeyEvent.VK_L:
                        anguloY += 5;
                        break;
                    case KeyEvent.VK_J:
                        anguloY -= 5;
                        break;
                    case KeyEvent.VK_U:
                        anguloZ += 5;
                        break;
                    case KeyEvent.VK_O:
                        anguloZ -= 5;
                        break;
                    case KeyEvent.VK_Z:
                        size -= 5;
                        break;
                    case KeyEvent.VK_X:
                        size += 5;
                        break;
                    case KeyEvent.VK_B:
                        lines = !lines;
                        break;
                    case KeyEvent.VK_ENTER:
                        gameMode = !gameMode;
                        if (!gameMode) {
                            selX = selY = selZ = -1;
                        }
                        break;
                    case KeyEvent.VK_E:
                        if (!ejeSubcubo) {
                            ejeSubcubo = true;
                        } else {
                            ejeSubcubo = false;
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(rotateVector(new double[]{0, 1, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) layer = m[1];
                            rotateLayerAnimated(m[0], layer, false);
                        } else if (!gameMode) {
                            anguloX -= 5;
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(rotateVector(new double[]{0, -1, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) layer = m[1];
                            rotateLayerAnimated(m[0], layer, true);
                        } else if (!gameMode) {
                            anguloX += 5;
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(rotateVector(new double[]{-1, 0, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) layer = m[1];
                            rotateLayerAnimated(m[0], layer, true);
                        } else if (!gameMode) {
                            anguloY -= 5;
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (gameMode && selX != -1) {
                            int[] m = mapDirection(rotateVector(new double[]{1, 0, 0}, -anguloX, -anguloY, -anguloZ));
                            int layer = (m[0] == 0) ? selX : (m[0] == 1) ? selY : selZ;
                            if (layer < 0) layer = m[1];
                            rotateLayerAnimated(m[0], layer, false);
                        } else if (!gameMode) {
                            anguloY += 5;
                        }
                        break;
                    case KeyEvent.VK_R:
                        if (!gameMode) {
                            scrambleAnimation();
                        }
                        break;
                }
                moverCubo();
            }
        });

        // Registrar eventos de ratón sobre el lienzo de dibujo
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    lastX = e.getX();
                    lastY = e.getY();
                } else if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (gameMode) {
                        for (int x = 0; x < 3; x++) {
                            for (int y = 0; y < 3; y++) {
                                for (int z = 0; z < 3; z++) {
                                    if (cuboRubik[x][y][z].containsPoint(mx, my)) {
                                        selX = x;
                                        selY = y;
                                        selZ = z;
                                    }
                                }
                            }
                        }
                    } else {
                        int cx = 0, cy = 0;
                        double bestDepth = -Double.MAX_VALUE;
                        int idxX = -1, idxY = -1, idxZ = -1;
                        for (int x = 0; x < 3; x++) {
                            for (int y = 0; y < 3; y++) {
                                for (int z = 0; z < 3; z++) {
                                    Subcubo sc = cuboRubik[x][y][z];
                                    if (sc.containsPoint(mx, my)) {
                                        double posX = (x - 1) * size;
                                        double posY = (y - 1) * size;
                                        double posZ = (z - 1) * size;
                                        double[] r = sc.rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                                        if (r[2] > bestDepth) {
                                            bestDepth = r[2];
                                            idxX = x;
                                            idxY = y;
                                            idxZ = z;
                                            cx = 0;
                                            cy = 0;
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
                        if (idxX != -1) {
                            int dx = cx - trasX;
                            int dy = cy - trasY;
                            int t = size / 2;
                            if (Math.abs(dx) > t && Math.abs(dy) > t) {
                                if ((dx > 0 && dy < 0) || (dx < 0 && dy > 0)) {
                                    anguloZ -= 5;
                                } else {
                                    anguloZ += 5;
                                }
                            } else if (Math.abs(dx) <= t && dy < -t) {
                                anguloX -= 5;
                            } else if (Math.abs(dx) <= t && dy > t) {
                                anguloX += 5;
                            } else if (Math.abs(dy) <= t && dx < -t) {
                                anguloY -= 5;
                            } else if (Math.abs(dy) <= t && dx > t) {
                                anguloY += 5;
                            }
                        }
                    }
                    moverCubo();
                }
            }
        });

        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                // SwingUtilities.isRightMouseButton returns false for drag
                // events, so check the modifiers to know if the right button
                // is being held down
                if (!gameMode && (e.getModifiersEx() & java.awt.event.InputEvent.BUTTON3_DOWN_MASK) != 0) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    anguloY += dx / 2.0;
                    anguloX -= dy / 2.0;
                    lastX = e.getX();
                    lastY = e.getY();
                    moverCubo();
                }
            }
        });

        panel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                size -= e.getWheelRotation() * 5;
                if (size < 20) {
                    size = 20;
                }
                setSubcube();
                moverCubo();
            }
        });

        setVisible(true);
    }

    /**
     * Dibuja textos y botones de ayuda sobre la imagen generada.
     */
    private void drawUI() {
        PixelFont.drawString(graficos, "RUBIK 3D", 10, 20, 4, Color.WHITE);
        PixelFont.drawString(graficos, gameMode ? "MODE: PLAY" : "MODE: VIEW", 650, 20, 2, Color.YELLOW);
        int y = 60;
        int step = 18;
        PixelFont.drawString(graficos, "WASD MOVE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "RIGHT DRAG ROTATE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "MOUSE WHEEL SCALE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "B TOGGLE LINES", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "E CHANGE AXIS", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "ARROWS ROTATE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "ENTER PLAY MODE", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "CLICK CUBE SELECT", 10, y, 2, Color.WHITE);
        y += step;
        PixelFont.drawString(graficos, "R MIX CUBE", 10, y, 2, Color.WHITE);
    }
}
