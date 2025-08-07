package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Encargado de dibujar el cubo de Rubik utilizando {@link Graficos}.
 */
public class CubeRenderer {

    /** Contenedor de utilidades de dibujo. */
    private final Graficos graficos;

    public CubeRenderer(Graficos graficos) {
        this.graficos = graficos;
    }

    /** Información auxiliar usada para ordenar piezas por profundidad. */
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
            this.cubo = c; this.x = x; this.y = y; this.depth = depth;
            this.ex = ex; this.ey = ey; this.ez = ez;
            this.tx = tx; this.ty = ty; this.tz = tz;
            this.highlight = h; this.ix = ix; this.iy = iy; this.iz = iz;
        }
    }

    /**
     * Redibuja el cubo aplicando las rotaciones y traslaciones actuales.
     */
    public void moverCubo(Subcubo[][][] cuboRubik,
            double anguloX, double anguloY, double anguloZ,
            int trasX, int trasY, int trasZ,
            int size, boolean lines, boolean ejeSubcubo,
            boolean gameMode, int selX, int selY, int selZ,
            double selTX, double selTY, double selTZ,
            boolean showLabels, boolean showControls) {
        if (!ejeSubcubo) {
            graficos.clear();
            List<RenderInfo> infos = new ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    for (int z = 0; z < 3; z++) {
                        double posX = (x - 1) * size;
                        double posY = (y - 1) * size;
                        double posZ = (z - 1) * size;
                        double[] rotatedPos = cuboRubik[x][y][z].rotar(
                                new double[]{posX, posY, posZ}, anguloX, anguloY, anguloZ);
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
                info.cubo.dibujar(graficos, 1, anguloX, anguloY, anguloZ,
                        info.x, info.y, (int) info.depth, lines, opt);
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
                        RenderOptions opt = new RenderOptions();
                        opt.highlight = highlight;
                        opt.extraTX = tX;
                        opt.extraTY = tY;
                        opt.extraTZ = tZ;
                        opt.showLabels = showLabels;
                        opt.idxX = x;
                        opt.idxY = y;
                        opt.idxZ = z;
                        cuboRubik[x][y][z].dibujar(graficos, 1.0, anguloX, anguloY, anguloZ,
                                trasX, trasY, trasZ, lines, opt);
                    }
                }
            }
        }
        drawUI(showControls, gameMode);
        graficos.render();
    }

    /**
     * Dibuja los textos de interfaz.
     */
    public void drawUI(boolean showControls, boolean gameMode) {
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

