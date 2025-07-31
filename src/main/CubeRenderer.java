package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Encargado de dibujar el cubo en pantalla utilizando la clase
 * {@link Graficos}. Mantiene el ángulo de visión y la matriz de rotación
 * acumulada del cubo.
 */
public class CubeRenderer {

    private final Graficos graficos;
    private final CubeModel model;

    // Rotaciones globales del cubo.
    private double anguloX = 0, anguloY = 0, anguloZ = 0;
    // Matriz de rotación acumulada.
    private double[][] rotMatrix;

    // Traslación para centrar el cubo.
    private int trasX = 400, trasY = 300, trasZ = 0;
    private boolean lines = true;
    private boolean showLabels = false;
    private boolean showControls = true;

    public CubeRenderer(Graficos graficos, CubeModel model) {
        this.graficos = graficos;
        this.model = model;
        this.rotMatrix = matrixFromAngles(anguloX, anguloY, anguloZ);
    }

    public double getAnguloX() { return anguloX; }
    public double getAnguloY() { return anguloY; }
    public double getAnguloZ() { return anguloZ; }
    public double[][] getRotMatrix() { return rotMatrix; }

    public void translate(int dx, int dy) {
        trasX += dx;
        trasY += dy;
    }

    /**
     * Aplica una rotación global al cubo.
     */
    public void applyRotation(int axis, double degrees) {
        double rad = Math.toRadians(degrees);
        double c = Math.cos(rad), s = Math.sin(rad);
        double[][] r;
        switch (axis) {
            case 0:
                r = new double[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
                break;
            case 1:
                r = new double[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
                break;
            case 2:
            default:
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
     * Redibuja el cubo completo.
     */
    public void render() {
        graficos.clear();
        Subcubo[][][] cuboRubik = model.getCubo();
        List<RenderInfo> infos = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    double posX = (x - 1) * model.getSize();
                    double posY = (y - 1) * model.getSize();
                    double posZ = (z - 1) * model.getSize();
                    double[] rotatedPos = cuboRubik[x][y][z].rotar(
                            new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
                    int finalX = (int) (rotatedPos[0] + trasX);
                    int finalY = (int) (rotatedPos[1] + trasY);
                    int finalZ = (int) (rotatedPos[2] + trasZ);
                    infos.add(new RenderInfo(cuboRubik[x][y][z], finalX, finalY, finalZ));
                }
            }
        }
        infos.sort((a, b) -> Double.compare(b.depth, a.depth));
        for (RenderInfo info : infos) {
            info.cubo.dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                    info.x, info.y, (int) info.depth, lines, false);
        }
        drawUI();
        graficos.render();
    }

    private void drawUI() {
        PixelFont.drawString(graficos, "RUBIK 3D", 10, 20, 5, Color.WHITE);
        if (showControls) {
            PixelFont.drawString(graficos, "WASD MOVE", 10, 60, 2, Color.WHITE);
            PixelFont.drawString(graficos, "IJKL ROTATE", 10, 84, 2, Color.WHITE);
        }
    }

    // ----- utilidades de matrices -----
    private double[][] multiply(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    private double[][] matrixFromAngles(double ax, double ay, double az) {
        double cx = Math.cos(Math.toRadians(ax));
        double sx = Math.sin(Math.toRadians(ax));
        double cy = Math.cos(Math.toRadians(ay));
        double sy = Math.sin(Math.toRadians(ay));
        double cz = Math.cos(Math.toRadians(az));
        double sz = Math.sin(Math.toRadians(az));
        return new double[][]{
            {cy * cz, -cy * sz, sy},
            {cx * sz + sx * sy * cz, cx * cz - sx * sy * sz, -sx * cy},
            {sx * sz - cx * sy * cz, sx * cz + cx * sy * sz, cx * cy}
        };
    }

    private double[] anglesFromMatrix(double[][] m) {
        double ay = Math.asin(m[0][2]);
        double cy = Math.cos(ay);
        double ax = Math.atan2(-m[1][2] / cy, m[2][2] / cy);
        double az = Math.atan2(-m[0][1] / cy, m[0][0] / cy);
        return new double[]{Math.toDegrees(ax), Math.toDegrees(ay), Math.toDegrees(az)};
    }

    private static class RenderInfo {
        final Subcubo cubo;
        final int x, y;
        final double depth;
        RenderInfo(Subcubo c, int x, int y, double depth) {
            this.cubo = c;
            this.x = x;
            this.y = y;
            this.depth = depth;
        }
    }
}
