package main;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

public class Cubo extends JFrame {

    private Graficos graficos;
    private Subcubo[][][] cuboRubik;
    private double anguloX = 30, anguloY = 30, anguloZ = 0; //Angulo de rotacion
    private double escala = 1; // Factor de escala para la separación
    // Centrar el cubo en la ventana
    private int trasX = 400, trasY = 300, trasZ = 0; // Coordenadas de traslacion
    private int size = 80;
    private boolean lines = true;
    private boolean ejeSubcubo = false;
    private int lastX;
    private int lastY;
    private boolean gameMode = false;
    private int selX = -1, selY = -1, selZ = -1;

    private int[][] buttons = {
        {700, 50, 80, 20},  // front
        {700, 80, 80, 20},  // back
        {700, 110, 80, 20}, // left
        {700, 140, 80, 20}, // right
        {700, 170, 80, 20}, // up
        {700, 200, 80, 20}, // down
        {700, 230, 80, 20}  // mix
    };

    private static class RenderInfo {
        final Subcubo cubo;
        final int x, y;
        final double depth;
        final double ex, ey, ez;
        final boolean highlight;

        RenderInfo(Subcubo c, int x, int y, double depth, double ex, double ey, double ez, boolean h) {
            this.cubo = c;
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.ex = ex;
            this.ey = ey;
            this.ez = ez;
            this.highlight = h;
        }
    }

    public Cubo() {
        initComponents();
        setSubcube();
        moverCubo();
    }

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

    private void rotateLayerAnimated(int axis, int layer, boolean clockwise) {
        int dir = clockwise ? 1 : -1;
        double offset = (layer - 1) * size;
        for (int a = 0; a <= 90; a += 10) {
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
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 0, dir * a, offset);
                            posX = r[0]; posY = r[1]; posZ = r[2];
                            extraX = dir * a;
                        } else if (axis == 1 && y == layer) {
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 1, dir * a, offset);
                            posX = r[0]; posY = r[1]; posZ = r[2];
                            extraY = dir * a;
                        } else if (axis == 2 && z == layer) {
                            double[] r = rotatePointAroundAxis(new double[]{posX, posY, posZ}, 2, dir * a, offset);
                            posX = r[0]; posY = r[1]; posZ = r[2];
                            extraZ = dir * a;
                        }
                        double[] rotatedPos = cuboRubik[x][y][z].rotar(new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                        int finalX = (int) (rotatedPos[0] + trasX);
                        int finalY = (int) (rotatedPos[1] + trasY);
                        int finalZ = (int) (rotatedPos[2] + trasZ);
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, finalZ,
                                extraX, extraY, extraZ, highlight));
                    }
                }
            }
            infos.sort((aInfo, bInfo) -> Double.compare(bInfo.depth, aInfo.depth));
            for (RenderInfo info : infos) {
                info.cubo.dibujar(graficos, 1.0, anguloX + info.ex, anguloY + info.ey, anguloZ + info.ez,
                        info.x, info.y, (int) info.depth, lines, info.highlight);
            }
            drawUI();
            graficos.repaint();
            try { Thread.sleep(20); } catch (InterruptedException ex) { /* ignore */ }
        }
        rotateLayer(axis, layer, clockwise);
        moverCubo();
    }

    private void swapSubcubes(int x1, int y1, int z1, int x2, int y2, int z2) {
        Subcubo tmp = cuboRubik[x1][y1][z1];
        cuboRubik[x1][y1][z1] = cuboRubik[x2][y2][z2];
        cuboRubik[x2][y2][z2] = tmp;
    }

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
                        infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, finalZ, 0, 0, 0, highlight));
                    }
                }
            }
            infos.sort((a, b) -> Double.compare(b.depth, a.depth));
            for (RenderInfo info : infos) {
                info.cubo.dibujar(graficos, 1, anguloX + info.ex, anguloY + info.ey, anguloZ + info.ez,
                        info.x, info.y, (int) info.depth, lines, info.highlight);
            }
        } else {
            graficos.clear();

            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        boolean highlight = gameMode && x == selX && y == selY && z == selZ;
                        cuboRubik[x][y][z].dibujar(graficos, 1.0, anguloX, anguloY, anguloZ, trasX, trasY, trasZ, lines, highlight);
                    }
                }
            }
        }
        drawUI();
        graficos.repaint();
    }

    private void scrambleAnimation() {
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            int axis = r.nextInt(3);
            int layer = r.nextInt(3);
            rotateLayerAnimated(axis, layer, r.nextBoolean());
        }
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
        add(graficos);

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
                        anguloY -= 5;
                        break;
                    case KeyEvent.VK_J:
                        anguloY += 5;
                        break;
                    case KeyEvent.VK_U:
                        anguloZ -= 5;
                        break;
                    case KeyEvent.VK_O:
                        anguloZ += 5;
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
                        if (!gameMode) { selX = selY = selZ = -1; }
                        break;
                    case KeyEvent.VK_E:
                        if (!ejeSubcubo) {
                            ejeSubcubo = true;
                        } else {
                            ejeSubcubo = false;
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (gameMode) {
                            rotateLayerAnimated(1, 2, true);
                        } else {
                            size += 5;
                            setSubcube();
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (gameMode) {
                            rotateLayerAnimated(1, 0, true);
                        } else {
                            size -= 5;
                            setSubcube();
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if (gameMode) {
                            rotateLayerAnimated(0, 0, true);
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (gameMode) {
                            rotateLayerAnimated(0, 2, true);
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
        graficos.addMouseListener(new java.awt.event.MouseAdapter() {
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
                                        selX = x; selY = y; selZ = z;
                                    }
                                }
                            }
                        }
                    } else {
                        if (inButton(0, mx, my)) {
                            rotateLayerAnimated(2, 2, true);
                        } else if (inButton(1, mx, my)) {
                            rotateLayerAnimated(2, 0, true);
                        } else if (inButton(2, mx, my)) {
                            rotateLayerAnimated(0, 0, true);
                        } else if (inButton(3, mx, my)) {
                            rotateLayerAnimated(0, 2, true);
                        } else if (inButton(4, mx, my)) {
                            rotateLayerAnimated(1, 2, true);
                        } else if (inButton(5, mx, my)) {
                            rotateLayerAnimated(1, 0, true);
                        } else if (inButton(6, mx, my)) {
                            scrambleAnimation();
                        }
                    }
                    moverCubo();
                }
            }
        });

        graficos.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                // SwingUtilities.isRightMouseButton returns false for drag
                // events, so check the modifiers to know if the right button
                // is being held down
                if (!gameMode && (e.getModifiersEx() & java.awt.event.InputEvent.BUTTON3_DOWN_MASK) != 0) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    anguloY += dx / 2.0;
                    anguloX += dy / 2.0;
                    lastX = e.getX();
                    lastY = e.getY();
                    moverCubo();
                }
            }
        });

        graficos.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                size -= e.getWheelRotation() * 5;
                if (size < 20) size = 20;
                setSubcube();
                moverCubo();
            }
        });

        setVisible(true);
    }

    private void drawUI() {
        PixelFont.drawString(graficos, "RUBIK 3D", 10, 20, 4, Color.WHITE);
        PixelFont.drawString(graficos, gameMode ? "MODE: PLAY" : "MODE: VIEW", 10, 40, 2, Color.YELLOW);
        int y = 60;
        int step = 18;
        PixelFont.drawString(graficos, "WASD MOVE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "RIGHT DRAG ROTATE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "MOUSE WHEEL SCALE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "B TOGGLE LINES", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "E CHANGE AXIS", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "ARROWS ROTATE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "ENTER PLAY MODE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "CLICK CUBE SELECT", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "R MIX CUBE", 10, y, 2, Color.WHITE);

        String[] names = {"FRONT", "BACK", "LEFT", "RIGHT", "UP", "DOWN", "MIX"};
        for (int i = 0; i < buttons.length; i++) {
            int[] b = buttons[i];
            graficos.drawRect(b[0], b[1], b[0] + b[2], b[1] + b[3], Color.WHITE);
            PixelFont.drawString(graficos, names[i], b[0] + 5, b[1] + 5, 2, Color.WHITE);
        }
    }

    private boolean inButton(int idx, int x, int y) {
        int[] b = buttons[idx];
        return x >= b[0] && x <= b[0] + b[2] && y >= b[1] && y <= b[1] + b[3];
    }
}
