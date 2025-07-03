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
    private int trasX = 430, trasY = 330, trasZ = 0; // Coordenadas de traslacion
    private int size = 80;
    private boolean lines = true;
    private boolean ejeSubcubo = false;
    private int lastX;
    private int lastY;

    private int[][] buttons = {
        {700, 50, 80, 20},  // front
        {700, 80, 80, 20},  // back
        {700, 110, 80, 20}, // left
        {700, 140, 80, 20}, // right
        {700, 170, 80, 20}, // up
        {700, 200, 80, 20}  // down
    };

    public Cubo() {
        initComponents();
        setSubcube();
        moverCubo();
    }

    private void setSubcube() {
        cuboRubik = new Subcubo[3][3][3];  // Cambiar a 3x3x3 para tener 27 subcubos

        int offsetX = -size;
        int offsetY = -size;
        int offsetZ = -size;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    // Ajustar las posiciones para los 27 subcubos
                    int posX = (int) ((x - 1) * size * escala) + offsetX;
                    int posY = (int) ((y - 1) * size * escala) + offsetY;
                    int posZ = (int) ((z - 1) * size * escala) + offsetZ;
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
                    }
                }
            }
        }

        cuboRubik = nuevo;
    }

    private void moverCubo() {
        if (!ejeSubcubo) {
            graficos.clear();

            // Encontrar el centro del cubo
            int centroX = 1;
            int centroY = 1;
            int centroZ = 1; // Coordenadas del subcubo 14

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

                        cuboRubik[x][y][z].dibujar(graficos, 1, anguloX, anguloY, anguloZ, finalX, finalY, finalZ, lines);
                    }
                }
            }
        } else {
            graficos.clear();

            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        cuboRubik[x][y][z].dibujar(graficos, 1.0, anguloX, anguloY, anguloZ, trasX, trasY, trasZ, lines);
                    }
                }
            }
        }
        drawUI();
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
                        if (!lines) {
                            lines = true;
                        } else {
                            lines = false;
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
                        size += 5;
                        setSubcube();
                        break;
                    case KeyEvent.VK_DOWN:
                        size -= 5;
                        setSubcube();
                        break;
                }
                moverCubo();
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    lastX = e.getX();
                    lastY = e.getY();
                } else if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (inButton(0, mx, my)) {
                        rotateLayer(2, 2, true);
                    } else if (inButton(1, mx, my)) {
                        rotateLayer(2, 0, true);
                    } else if (inButton(2, mx, my)) {
                        rotateLayer(0, 0, true);
                    } else if (inButton(3, mx, my)) {
                        rotateLayer(0, 2, true);
                    } else if (inButton(4, mx, my)) {
                        rotateLayer(1, 2, true);
                    } else if (inButton(5, mx, my)) {
                        rotateLayer(1, 0, true);
                    }
                    moverCubo();
                }
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
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

        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
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
        int y = 60;
        int step = 18;
        PixelFont.drawString(graficos, "WASD MOVE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "RIGHT DRAG ROTATE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "MOUSE WHEEL SCALE", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "B TOGGLE LINES", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "E CHANGE AXIS", 10, y, 2, Color.WHITE); y += step;
        PixelFont.drawString(graficos, "CLICK BUTTONS TO ROTATE", 10, y, 2, Color.WHITE);

        String[] names = {"FRONT", "BACK", "LEFT", "RIGHT", "UP", "DOWN"};
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
