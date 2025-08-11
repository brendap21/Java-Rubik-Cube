package main;

/**
 * Representa una pieza individual del cubo de Rubik. Se encarga de mantener su
 * orientación, colores y de dibujarse aplicando las transformaciones
 * necesarias.
 */
import java.awt.Color;
import java.util.Arrays;

public class Subcubo {

    /**
     * Índices de esta pieza dentro del cubo completo.
     */
    public int x, y, z, size;
    /**
     * Coordenadas de los 8 vértices en su espacio local.
     */
    private final double[][] vertices;
    /**
     * Conexiones entre vértices para dibujar aristas.
     */
    private final int[][] aristas;
    /**
     * Colores de cada una de las seis caras de la pieza.
     */
    private final Color[] colores;
    /**
     * Índices de los vértices que componen cada cara.
     */
    private final int[][] caras;
    /**
     * Coordenadas proyectadas en pantalla de cada vértice.
     */
    private final int[][] screenVertices;
    /**
     * Profundidad promedio de cada cara en la última proyección realizada.
     * Se usa para determinar la cara visible más cercana al hacer clic.
     */
    private final double[] faceDepths;

    /**
     * Matriz de rotación acumulada que representa la orientación del subcubo.
     */
    private double[][] rotMatrix;

    /**
     * Ciclos de rotación de las caras para cada eje.
     * Cada ciclo describe el orden en que las caras se desplazan al rotar 90°
     * en sentido horario alrededor del eje correspondiente.
     */
    private static final int[][] FACE_CYCLES = {
        {0, 3, 1, 2}, // X axis: back -> top -> front -> bottom
        {0, 4, 1, 5}, // Y axis: back -> left -> front -> right
        {3, 5, 2, 4}  // Z axis: top -> right -> bottom -> left
    };

    /**
     * Crea un subcubo identificándolo por sus índices dentro del cubo de Rubik.
     */
    public Subcubo(int ix, int iy, int iz, int size) {
        this.x = ix;
        this.y = iy;
        this.z = iz;
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
            new Color(114, 176, 29), // verde
            new Color(8, 127, 140), // azul 
            new Color(246, 247, 235), // blanco
            new Color(97, 41, 64), // morado
            new Color(242, 92, 84), 
            new Color(222, 26, 26) // rojo
        };

        screenVertices = new int[8][2];
        faceDepths = new double[6];
        rotMatrix = identity();
    }

    /**
     * Devuelve la posición absoluta del centro del subcubo en función de su
     * índice y una escala dada. La escala representa la separación relativa
     * entre piezas.
     *
     * @param escala factor de escala aplicado al tamaño
     * @return vector de tres componentes con las coordenadas en el espacio
     */
    public double[] getPosition(double escala) {
        double posX = (x - 1) * size * escala;
        double posY = (y - 1) * size * escala;
        double posZ = (z - 1) * size * escala;
        return new double[]{posX, posY, posZ};
    }

    /**
     * Aplica una rotación global de 90° alrededor del eje indicado a la
     * orientación del subcubo.
     *
     * @param axis      eje de rotación (0=X, 1=Y, 2=Z)
     * @param clockwise sentido horario si es {@code true}
     */
    public void applyGlobalRotation(int axis, boolean clockwise) {
        double ang = clockwise ? 90 : -90;
        double[][] r = rotationAxis(axis, ang);
        rotMatrix = multiply(r, rotMatrix);
        normalizeRotMatrix();
    }

    /**
     * Reordena los colores de las caras tras una rotación alrededor de un eje.
     *
     * @param axis      eje de rotación (0=X, 1=Y, 2=Z)
     * @param clockwise sentido horario si es {@code true}
     */
    public void rotateColors(int axis, boolean clockwise) {
        if (axis < 0 || axis >= FACE_CYCLES.length) {
            return;
        }
        rotateCycle(FACE_CYCLES[axis], clockwise);
    }

    /**
     * Rota los colores según el ciclo indicado.
     *
     * @param cycle     arreglo con los índices de las caras a rotar
     * @param clockwise {@code true} para rotación horaria, {@code false} para
     *                  antihoraria
     */
    private void rotateCycle(int[] cycle, boolean clockwise) {
        Color[] orig = colores.clone();
        for (int i = 0; i < cycle.length; i++) {
            int from = cycle[i];
            int to = cycle[(i + 1) % cycle.length];
            if (clockwise) {
                colores[to] = orig[from];
            } else {
                colores[from] = orig[to];
            }
        }
    }

    /**
     * Ajusta el tamaño del subcubo sin reiniciar su orientación ni colores.
     *
     * @param newSize nuevo tamaño del subcubo
     */
    public void setSize(int newSize) {
        this.size = newSize;
        double half = newSize / 2.0;
        double[][] newVerts = new double[][]{
            {-half, -half, -half},
            {half, -half, -half},
            {half, half, -half},
            {-half, half, -half},
            {-half, -half, half},
            {half, -half, half},
            {half, half, half},
            {-half, half, half}
        };
        for (int i = 0; i < vertices.length; i++) {
            System.arraycopy(newVerts[i], 0, vertices[i], 0, 3);
        }
    }

    /**
     * Devuelve el vector normal local de una cara según su índice.
     */
    public static double[] getFaceNormal(int face) {
        switch (face) {
            case 0: // back
                return new double[]{0, 0, -1};
            case 1: // front
                return new double[]{0, 0, 1};
            case 2: // bottom
                return new double[]{0, 1, 0};
            case 3: // top
                return new double[]{0, -1, 0};
            case 4: // left
                return new double[]{-1, 0, 0};
            case 5: // right
                return new double[]{1, 0, 0};
            default:
                return new double[]{0, 0, 0};
        }
    }

    /**
     * Devuelve la normal de una cara en coordenadas mundiales teniendo en cuenta
     * la orientación actual del subcubo.
     */
    public double[] getFaceNormalWorld(int face) {
        double[] local = getFaceNormal(face);
        return rotar(local, rotMatrix);
    }

    /**
     * Devuelve la normal de una cara transformada por las rotaciones globales
     * del cubo.
     *
     * @param face    índice de la cara
     * @param anguloX rotación global en X
     * @param anguloY rotación global en Y
     * @param anguloZ rotación global en Z
     * @return vector normal transformado al espacio global
     */
    public double[] getFaceNormalGlobal(int face, double anguloX, double anguloY, double anguloZ) {
        double[] world = rotar(getFaceNormal(face), rotMatrix);
        double[][] g = rotation(anguloX, anguloY, anguloZ);
        return rotar(world, g);
    }

    /**
     * Dibuja el subcubo aplicando las transformaciones indicadas.
     */
    public void dibujar(Graficos g, double escala, double anguloX, double anguloY, double anguloZ,
            int trasX, int trasY, int trasZ, boolean lines, RenderOptions opt) {
        if (opt == null) {
            opt = new RenderOptions();
        }
        boolean highlight = opt.highlight;
        double extraRotX = opt.extraRotX;
        double extraRotY = opt.extraRotY;
        double extraRotZ = opt.extraRotZ;
        double extraTX = opt.extraTX;
        double extraTY = opt.extraTY;
        double extraTZ = opt.extraTZ;
        boolean showLabels = opt.showLabels;
        int idxX = opt.idxX;
        int idxY = opt.idxY;
        int idxZ = opt.idxZ;

        double[][] orientation = rotMatrix;
        if (extraRotX != 0) {
            orientation = multiply(rotationAxis(0, extraRotX), orientation);
        }
        if (extraRotY != 0) {
            orientation = multiply(rotationAxis(1, extraRotY), orientation);
        }
        if (extraRotZ != 0) {
            orientation = multiply(rotationAxis(2, extraRotZ), orientation);
        }
        double[][] globalRot = rotation(anguloX, anguloY, anguloZ);
        double[][] rotadas = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double[] local = rotar(vertices[i], orientation);
            rotadas[i] = rotar(local, globalRot);
        }

        // Aplicar traslación a los vértices rotados
        double[][] trasladadas = new double[8][3];
        for (int i = 0; i < 8; i++) {
            trasladadas[i][0] = rotadas[i][0] * escala + trasX + extraTX;
            trasladadas[i][1] = rotadas[i][1] * escala + trasY + extraTY;
            trasladadas[i][2] = rotadas[i][2] * escala + trasZ + extraTZ;
            screenVertices[i][0] = (int) trasladadas[i][0];
            screenVertices[i][1] = (int) trasladadas[i][1];
        }

        // Algoritmo del pintor
        double[] profundidades = new double[6];
        for (int i = 0; i < 6; i++) {
            profundidades[i] = (trasladadas[caras[i][0]][2] + trasladadas[caras[i][1]][2]
                    + trasladadas[caras[i][2]][2] + trasladadas[caras[i][3]][2]) / 4.0;
            faceDepths[i] = profundidades[i];
        }

        Integer[] indices = {0, 1, 2, 3, 4, 5};
        Arrays.sort(indices, (a, b) -> Double.compare(profundidades[b], profundidades[a]));

        int brightestFace = 0;
        double minDepth = profundidades[0];
        for (int f = 1; f < 6; f++) {
            if (profundidades[f] < minDepth) {
                minDepth = profundidades[f];
                brightestFace = f;
            }
        }

        for (int i : indices) {
            int[] xPoints = new int[4];
            int[] yPoints = new int[4];
            for (int j = 0; j < 4; j++) {
                xPoints[j] = (int) trasladadas[caras[i][j]][0];
                yPoints[j] = (int) trasladadas[caras[i][j]][1];
            }
            Color c = colores[i];
            if (highlight) {
                c = c.darker();
                if (i == brightestFace) {
                    c = c.brighter();
                }
            }
            g.fillPolygon(xPoints, yPoints, 4, c); // Pintar caras
            if (lines) {
                for (int j = 0; j < 4; j++) {
                    int next = (j + 1) % 4;
                    g.drawLine(xPoints[j], yPoints[j], xPoints[next], yPoints[next], Color.BLACK);
                }
            }
            if (showLabels) {
                String label = getFaceLabel(i, idxX, idxY, idxZ);
                if (label != null) {
                    int cx = (xPoints[0] + xPoints[1] + xPoints[2] + xPoints[3]) / 4;
                    int cy = (yPoints[0] + yPoints[1] + yPoints[2] + yPoints[3]) / 4;
                    PixelFont.drawString(g, label, cx - 4, cy - 4, 1, Color.BLACK);
                }
            }
        }
    }

    private String getFaceLabel(int face, int ix, int iy, int iz) {
        switch (face) {
            case 1: // front
                return "A" + (iy * 3 + (2 - ix) + 1);
            case 5: // right
                return "B" + (iy * 3 + iz + 1);
            case 3: // top
                return "C" + (iz * 3 + ix + 1);
            case 4: // left
                return "D" + (iy * 3 + (2 - iz) + 1);
            case 2: // bottom
                return "E" + ((2 - iz) * 3 + ix + 1);
            case 0: // back
                return "F" + (iy * 3 + ix + 1);
            default:
                return null;
        }
    }

    /**
     * Aplica una matriz de rotación a un punto en 3D.
     */
    public double[] rotar(double[] punto, double[][] matriz) {
        return multiply(matriz, punto);
    }

    /**
     * Devuelve la matriz identidad 3x3.
     */
    private static double[][] identity() {
        return new double[][]{
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
    }

    /**
     * Crea una matriz de rotación a partir de ángulos de Euler aplicados en
     * orden X, luego Y y finalmente Z.
     */
    public static double[][] rotation(double angX, double angY, double angZ) {
        double[][] rx = rotationAxis(0, angX);
        double[][] ry = rotationAxis(1, angY);
        double[][] rz = rotationAxis(2, angZ);
        return multiply(rz, multiply(ry, rx));
    }

    /**
     * Devuelve la matriz de rotación alrededor de uno de los ejes cartesianos.
     */
    public static double[][] rotationAxis(int axis, double degrees) {
        double rad = Math.toRadians(degrees);
        double c = Math.cos(rad);
        double s = Math.sin(rad);
        switch (axis) {
            case 0: // X
                return new double[][]{
                    {1, 0, 0},
                    {0, c, -s},
                    {0, s, c}
                };
            case 1: // Y
                return new double[][]{
                    {c, 0, s},
                    {0, 1, 0},
                    {-s, 0, c}
                };
            default: // Z
                return new double[][]{
                    {c, -s, 0},
                    {s, c, 0},
                    {0, 0, 1}
                };
        }
    }

    /**
     * Multiplica dos matrices 3x3.
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    /**
     * Multiplica una matriz 3x3 por un vector de 3 componentes.
     */
    public static double[] multiply(double[][] m, double[] v) {
        return new double[]{
            m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
            m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
            m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]
        };
    }

    /**
     * Producto cruz entre dos vectores de 3 componentes.
     */
    private static double[] cross(double[] a, double[] b) {
        return new double[]{
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    /**
     * Producto punto entre dos vectores de 3 componentes.
     */
    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    /**
     * Normaliza un vector a longitud 1.
     */
    private static void normalize(double[] v) {
        double len = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len == 0) {
            return;
        }
        v[0] /= len;
        v[1] /= len;
        v[2] /= len;
    }

    /**
     * Normaliza la matriz de rotación acumulada {@link #rotMatrix} corrigiendo
     * pequeñas desviaciones numéricas para mantenerla en un estado discreto
     * (valores -1, 0 o 1). Esto evita la acumulación de errores tras muchas
     * rotaciones.
     */
    private void normalizeRotMatrix() {
        double eps = 1e-9;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double v = rotMatrix[i][j];
                if (Math.abs(v) < eps) {
                    rotMatrix[i][j] = 0;
                } else if (Math.abs(v - 1) < eps) {
                    rotMatrix[i][j] = 1;
                } else if (Math.abs(v + 1) < eps) {
                    rotMatrix[i][j] = -1;
                } else {
                    rotMatrix[i][j] = Math.round(v);
                }
            }
        }
        // Reconstruir a partir de estados discretos garantizando una base
        // ortonormal y con determinante 1. Seleccionamos el eje dominante de
        // cada fila y usamos un producto cruz para obtener el tercer eje.
        double[][] axes = new double[3][3];
        for (int i = 0; i < 3; i++) {
            int maxIdx = 0;
            for (int j = 1; j < 3; j++) {
                if (Math.abs(rotMatrix[i][j]) > Math.abs(rotMatrix[i][maxIdx])) {
                    maxIdx = j;
                }
            }
            double sign = Math.signum(rotMatrix[i][maxIdx]);
            axes[i][maxIdx] = sign == 0 ? 1 : sign;
        }
        double[] cross = cross(axes[0], axes[1]);
        normalize(cross);
        // Alinear el tercer eje con el signo originalmente detectado para la
        // tercera fila
        if (dot(cross, axes[2]) < 0) {
            for (int i = 0; i < 3; i++) {
                cross[i] = -cross[i];
            }
        }
        axes[2] = cross;
        rotMatrix = axes;
    }

    /**
     * Determina si un punto en pantalla se encuentra dentro de la proyección de
     * este subcubo.
     */
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

    /**
     * Implementación del algoritmo even-odd para determinar si un punto está
     * dentro de un polígono.
     */
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

    /**
     * Devuelve el índice de la cara que contiene el punto dado o -1 si ninguna
     * coincide.
     */
    public int faceAt(int px, int py) {
        int selected = -1;
        double bestDepth = Double.MAX_VALUE;
        for (int faceIdx = 0; faceIdx < caras.length; faceIdx++) {
            int[] xs = new int[4];
            int[] ys = new int[4];
            for (int i = 0; i < 4; i++) {
                xs[i] = screenVertices[caras[faceIdx][i]][0];
                ys[i] = screenVertices[caras[faceIdx][i]][1];
            }
            if (pointInPolygon(px, py, xs, ys)) {
                double depth = faceDepths[faceIdx];
                if (selected == -1 || depth < bestDepth) {
                    bestDepth = depth;
                    selected = faceIdx;
                }
            }
        }
        return selected;
    }

    /**
     * Devuelve las coordenadas de los vértices proyectados en pantalla.
     */
    public int[][] getScreenVertices() {
        return screenVertices;
    }
}
