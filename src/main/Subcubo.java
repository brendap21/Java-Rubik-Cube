package main;

import java.awt.Color;
import java.util.Arrays;

public class Subcubo {

    public int x, y, z, size;
    private final double[][] vertices;
    private final int[][] aristas;
    private final Color[] colores;
    private final int[][] caras;
    private final int[][] screenVertices;

    private double rotX = 0, rotY = 0, rotZ = 0;

    public Subcubo(int x, int y, int z, int size) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;

        vertices = new double[][]{
            {-size / 2.0, -size / 2.0, -size / 2.0},
            {size / 2.0, -size / 2.0, -size / 2.0},
            {size / 2.0, size / 2.0, -size / 2.0},
            {-size / 2.0, size / 2.0, -size / 2.0},
            {-size / 2.0, -size / 2.0, size / 2.0},
            {size / 2.0, -size / 2.0, size / 2.0},
            {size / 2.0, size / 2.0, size / 2.0},
            {-size / 2.0, size / 2.0, size / 2.0}
        };

        aristas = new int[][]{
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        caras = new int[][]{
            {0, 1, 2, 3}, // back
            {4, 5, 6, 7}, // front
            {0, 1, 5, 4}, // bottom
            {2, 3, 7, 6}, // top
            {0, 3, 7, 4}, // left
            {1, 2, 6, 5} // right
        };

        colores = new Color[]{
            new Color(255, 105, 180), // rosa
            Color.CYAN, // cian
            Color.WHITE, // blanco
            new Color(128, 0, 128), // morado
            new Color(135, 206, 235), // azul cielo
            new Color(128, 0, 0) // tinto
        };

        screenVertices = new int[8][2];
    }

    /**
     * Sets the color of a specific face.
     * Face order matches the {@code caras} array: 0 back, 1 front,
     * 2 bottom, 3 top, 4 left, 5 right.
     */
    public void setFaceColor(int face, Color color) {
        if (face >= 0 && face < colores.length) {
            colores[face] = color;
        }
    }

    public void rotateColors(int axis, boolean clockwise) {
        Color[] c = Arrays.copyOf(colores, colores.length);
        switch (axis) {
            case 0: // X axis
                if (clockwise) {
                    colores[1] = c[3]; // front = top
                    colores[2] = c[1]; // bottom = front
                    colores[0] = c[2]; // back = bottom
                    colores[3] = c[0]; // top = back
                } else {
                    colores[1] = c[2];
                    colores[2] = c[0];
                    colores[0] = c[3];
                    colores[3] = c[1];
                }
                break;
            case 1: // Y axis
                if (clockwise) {
                    colores[5] = c[1]; // right = front
                    colores[0] = c[5]; // back = right
                    colores[4] = c[0]; // left = back
                    colores[1] = c[4]; // front = left
                } else {
                    colores[4] = c[1];
                    colores[0] = c[4];
                    colores[5] = c[0];
                    colores[1] = c[5];
                }
                break;
            case 2: // Z axis
                if (clockwise) {
                    // top -> right -> bottom -> left -> top
                    colores[5] = c[3]; // right = top
                    colores[2] = c[5]; // bottom = right
                    colores[4] = c[2]; // left = bottom
                    colores[3] = c[4]; // top = left
                } else {
                    // top -> left -> bottom -> right -> top
                    colores[4] = c[3]; // left = top
                    colores[2] = c[4]; // bottom = left
                    colores[5] = c[2]; // right = bottom
                    colores[3] = c[5]; // top = right
                }
                break;
        }
    }

    public void rotateOrientation(int axis, boolean clockwise) {
        double ang = clockwise ? 90 : -90;
        switch (axis) {
            case 0:
                rotX = (rotX + ang) % 360;
                break;
            case 1:
                rotY = (rotY + ang) % 360;
                break;
            case 2:
                rotZ = (rotZ + ang) % 360;
                break;
        }
    }

    // Metodo de compatibilidad para versiones previas sin el parametro highlight
    public void dibujar(Graficos g, double escala, double anguloX, double anguloY, double anguloZ, int trasX, int trasY, int trasZ, boolean lines) {
        dibujar(g, escala, anguloX, anguloY, anguloZ, trasX, trasY, trasZ, lines, false, 0, 0, 0);
    }

    public void dibujar(Graficos g, double escala, double anguloX, double anguloY, double anguloZ, int trasX, int trasY, int trasZ, boolean lines, boolean highlight) {
        dibujar(g, escala, anguloX, anguloY, anguloZ, trasX, trasY, trasZ, lines, highlight, 0, 0, 0);
    }

    public void dibujar(Graficos g, double escala, double anguloX, double anguloY, double anguloZ,
            int trasX, int trasY, int trasZ, boolean lines, boolean highlight,
            double extraRotX, double extraRotY, double extraRotZ) {
        double[][] rotadas = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double[] local = rotar(vertices[i], rotX + extraRotX, rotY + extraRotY, rotZ + extraRotZ);
            rotadas[i] = rotar(local, anguloX, anguloY, anguloZ);
        }

        // Aplicar traslación a los vértices rotados
        double[][] trasladadas = new double[8][3];
        for (int i = 0; i < 8; i++) {
            trasladadas[i][0] = rotadas[i][0] * escala + trasX;
            trasladadas[i][1] = rotadas[i][1] * escala + trasY;
            trasladadas[i][2] = rotadas[i][2] * escala + trasZ;
            screenVertices[i][0] = (int) trasladadas[i][0];
            screenVertices[i][1] = (int) trasladadas[i][1];
        }

        // Algoritmo del pintor
        double[] profundidades = new double[6];
        for (int i = 0; i < 6; i++) {
            profundidades[i] = (trasladadas[caras[i][0]][2] + trasladadas[caras[i][1]][2] + trasladadas[caras[i][2]][2] + trasladadas[caras[i][3]][2]) / 2.0;
        }

        Integer[] indices = {0, 1, 2, 3, 4, 5};
        Arrays.sort(indices, (a, b) -> Double.compare(profundidades[b], profundidades[a]));

        for (int i : indices) {
            int[] xPoints = new int[4];
            int[] yPoints = new int[4];
            for (int j = 0; j < 4; j++) {
                xPoints[j] = (int) trasladadas[caras[i][j]][0];
                yPoints[j] = (int) trasladadas[caras[i][j]][1];
            }
            g.fillPolygon(xPoints, yPoints, 4, colores[i]); // Pintar caraas
            if (lines) {
                for (int j = 0; j < 4; j++) {
                    int next = (j + 1) % 4;
                    g.drawLine(xPoints[j], yPoints[j], xPoints[next], yPoints[next], Color.BLACK);
                }
            }
        }

        if (highlight) {
            int minX = screenVertices[0][0], maxX = screenVertices[0][0];
            int minY = screenVertices[0][1], maxY = screenVertices[0][1];
            for (int i = 1; i < 8; i++) {
                if (screenVertices[i][0] < minX) {
                    minX = screenVertices[i][0];
                }
                if (screenVertices[i][0] > maxX) {
                    maxX = screenVertices[i][0];
                }
                if (screenVertices[i][1] < minY) {
                    minY = screenVertices[i][1];
                }
                if (screenVertices[i][1] > maxY) {
                    maxY = screenVertices[i][1];
                }
            }
            g.drawRect(minX, minY, maxX, maxY, Color.YELLOW);
        }
    }

    public double[] rotar(double[] punto, double anguloX, double anguloY, double anguloZ) {
        double[] resultado = Arrays.copyOf(punto, 3);
        double radX = Math.toRadians(anguloX);
        double radY = Math.toRadians(anguloY);
        double radZ = Math.toRadians(anguloZ);

        double temp = resultado[1] * Math.cos(radX) - resultado[2] * Math.sin(radX);
        resultado[2] = resultado[1] * Math.sin(radX) + resultado[2] * Math.cos(radX);
        resultado[1] = temp;

        temp = resultado[0] * Math.cos(radY) + resultado[2] * Math.sin(radY);
        resultado[2] = -resultado[0] * Math.sin(radY) + resultado[2] * Math.cos(radY);
        resultado[0] = temp;

        temp = resultado[0] * Math.cos(radZ) - resultado[1] * Math.sin(radZ);
        resultado[1] = resultado[0] * Math.sin(radZ) + resultado[1] * Math.cos(radZ);
        resultado[0] = temp;

        return resultado;
    }

    public boolean containsPoint(int px, int py) {
        for (int[] face : caras) {
            int[] xs = new int[4];
            int[] ys = new int[4];
            for (int i = 0; i < 4; i++) {
                xs[i] = screenVertices[face[i]][0];
                ys[i] = screenVertices[face[i]][1];
            }
            if (pointInPolygon(px, py, xs, ys)) {
                return true;
            }
        }
        return false;
    }

    private boolean pointInPolygon(int x, int y, int[] polyX, int[] polyY) {
        boolean inside = false;
        for (int i = 0, j = polyX.length - 1; i < polyX.length; j = i++) {
            boolean intersect = ((polyY[i] > y) != (polyY[j] > y))
                    && (x < (double) (polyX[j] - polyX[i]) * (y - polyY[i]) / (polyY[j] - polyY[i]) + polyX[i]);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }
}
