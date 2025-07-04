package main;

/**
 * Utilidad de dibujo por software utilizada por el programa.
 * Provee operaciones básicas para pintar en un {@link BufferedImage} sin
 * utilizar APIs gráficas de alto nivel. Todas las posiciones se traducen
 * usando {@code translateX} y {@code translateY} para permitir desplazamientos
 * temporales.
 */

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class Graficos {

    /** Ancho del lienzo en píxeles. */
    private final int WIDTH;
    /** Alto del lienzo en píxeles. */
    private final int HEIGHT;
    /** Imagen que actúa como framebuffer donde se dibuja todo. */
    private BufferedImage buffer;

    // Variables de traslación aplicadas de forma global a las operaciones
    // de dibujo. Permiten mover temporalmente el origen de coordenadas.
    private int translateX = 0;
    private int translateY = 0;

    /**
     * Crea un nuevo contexto de dibujo para un área de las dimensiones
     * indicadas.
     *
     * @param width  ancho del lienzo
     * @param height alto del lienzo
     */
    public Graficos(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Rellena un rectángulo en el buffer aplicando la traslación actual.
     *
     * @param x0 coordenada X inicial
     * @param y0 coordenada Y inicial
     * @param x1 coordenada X final
     * @param y1 coordenada Y final
     * @param color color con el que rellenar
     */
    public void fillRect(int x0, int y0, int x1, int y1, Color color) {
        // Ajustar las coordenadas según la traslación actual
        x0 += translateX;
        y0 += translateY;
        x1 += translateX;
        y1 += translateY;

        // Normalizar los extremos para recorrer correctamente el área
        int nx0 = Math.min(x0, x1);
        int ny0 = Math.min(y0, y1);
        int nx1 = Math.max(x0, x1);
        int ny1 = Math.max(y0, y1);

        // Rellenar pixel a pixel el área calculada
        for (int y = ny0; y <= ny1; y++) {
            for (int x = nx0; x <= nx1; x++) {
                putPixel(x, y, color);
            }
        }
    }

    /**
     * Dibuja los bordes de un rectángulo utilizando {@link #drawLine}.
     */
    public void drawRect(int x0, int y0, int x1, int y1, Color color) {
        drawLine(x0, y0, x1, y0, color);
        drawLine(x0, y1, x1, y1, color);
        drawLine(x0, y0, x0, y1, color);
        drawLine(x1, y0, x1, y1, color);
    }
    
    
    /**
     * Rellena un polígono convexo utilizando el algoritmo de scan-line.
     *
     * @param xPoints arreglo de coordenadas X de los vértices
     * @param yPoints arreglo de coordenadas Y de los vértices
     * @param nPoints número de puntos válidos en los arreglos
     * @param color   color de relleno
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints, Color color) {
        if (nPoints < 3) return;

        // Encontrar el límite del polígono
        // Determinar el rango vertical del polígono
        int minY = HEIGHT - 1, maxY = 0;
        for (int i = 0; i < nPoints; i++) {
            if (yPoints[i] < minY) minY = yPoints[i];
            if (yPoints[i] > maxY) maxY = yPoints[i];
        }

        // Escanear líneas
        for (int y = minY; y <= maxY; y++) {
            // Lista temporal de intersecciones con la línea actual
            int[] nodes = new int[nPoints];
            int nodesCount = 0;
            int j = nPoints - 1;
            for (int i = 0; i < nPoints; i++) {
                // Comprobar si la arista cruza la línea actual
                if ((yPoints[i] < y && yPoints[j] >= y) || (yPoints[j] < y && yPoints[i] >= y)) {
                    nodes[nodesCount++] = (xPoints[i] + (y - yPoints[i]) * (xPoints[j] - xPoints[i]) / (yPoints[j] - yPoints[i]));
                }
                j = i;
            }

            // Ordenar nodos
            // Ordenar las intersecciones de izquierda a derecha
            for (int i = 0; i < nodesCount - 1; i++) {
                for (int k = i + 1; k < nodesCount; k++) {
                    if (nodes[i] > nodes[k]) {
                        int temp = nodes[i];
                        nodes[i] = nodes[k];
                        nodes[k] = temp;
                    }
                }
            }

            // Dibujar las líneas entre los pares de nodos
            // Rellenar la línea entre cada par de intersecciones
            for (int i = 0; i < nodesCount; i += 2) {
                if (nodes[i] >= WIDTH) break;
                if (nodes[i + 1] > 0) {
                    if (nodes[i] < 0) nodes[i] = 0;
                    if (nodes[i + 1] > WIDTH) nodes[i + 1] = WIDTH;
                    for (int x = nodes[i]; x < nodes[i + 1]; x++) {
                        putPixel(x, y, color);
                    }
                }
            }
        }
    }

    /**
     * Dibuja una línea usando el algoritmo de Bresenham.
     */
    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        x1 += translateX;
        y1 += translateY;
        x2 += translateX;
        y2 += translateY;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            putPixel(x1, y1, color);

            // Si se alcanzó el destino se termina
            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) { // Ajuste horizontal
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) { // Ajuste vertical
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * Dibuja una línea con grosor calculando desplazamientos perpendiculares
     * a la dirección original.
     */
    public void drawThickLine(int x0, int y0, int x1, int y1, int thickness, Color color) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double unitDx = dx / distance;
        double unitDy = dy / distance;
        int halfThickness = thickness / 2;

        for (int i = -halfThickness; i <= halfThickness; i++) {
            int xOffset = (int) (i * unitDy);   // desplazamiento perpendicular en X
            int yOffset = (int) (i * unitDx);   // desplazamiento perpendicular en Y
            drawLine(x0 + xOffset, y0 - yOffset, x1 + xOffset, y1 - yOffset, color);
        }
    }

    /**
     * Rellena un círculo completo comprobando cada píxel dentro del radio.
     */
    public void fillCircle(int x0, int y0, int RADIO, Color fillColor) {
        // Recorremos un cuadrado que circunscribe al círculo
        for (int y = y0 - RADIO; y <= y0 + RADIO; y++) {
            for (int x = x0 - RADIO; x <= x0 + RADIO; x++) {
                if (Math.pow(x - x0, 2) + Math.pow(y - y0, 2) <= Math.pow(RADIO, 2)) {
                    putPixel(x, y, fillColor);
                }
            }
        }
    }

    /**
     * Dibuja la circunferencia de un círculo mediante simetría octogonal.
     */
    public void drawCircle(int x0, int y0, int RADIO, Color color) {
        // Solo calculamos 1/8 del círculo y repetimos por simetría
        for (int t = 0; t <= 45; t++) {
            int x = (int) (RADIO * Math.sin(Math.toRadians(t)));
            int y = (int) (RADIO * Math.cos(Math.toRadians(t)));

            // Octantes positivos
            putPixel(x0 + x, y0 + y, color);
            putPixel(x0 + y, y0 + x, color);
            putPixel(x0 + y, y0 - x, color);
            putPixel(x0 + x, y0 - y, color);

            // Octantes negativos
            putPixel(x0 - x, y0 - y, color);
            putPixel(x0 - y, y0 - x, color);
            putPixel(x0 - y, y0 + x, color);
            putPixel(x0 - x, y0 + y, color);
        }
    }

    /**
     * Dibuja la circunferencia de un círculo con un patrón de puntos.
     */
    public void drawDottedCircle(int x0, int y0, int radio, Color color) {
        int x = 0;
        int y = radio;
        int d = 3 - 2 * radio; // Parámetro de decisión
        int counter = 0;

        while (x <= y) {
            if (counter % 10 < 5) { // Controla el patrón de puntos
                drawDots(x0, y0, x, y, color);
            }
            counter++;

            x++;
            if (d < 0) {
                d = d + 4 * x + 6;
            } else {
                y--;
                d = d + 4 * (x - y) + 10;
            }
            if (counter % 10 < 5) { // Controla el patrón de puntos
                drawDots(x0, y0, x, y, color);
            }
        }
    }

    /**
     * Pinta los ocho puntos simétricos de un círculo.
     */
    private void drawDots(int xc, int yc, int x, int y, Color c) {
        putPixel(xc + x, yc + y, c);
        putPixel(xc - x, yc + y, c);
        putPixel(xc + x, yc - y, c);
        putPixel(xc - x, yc - y, c);
        putPixel(xc + y, yc + x, c);
        putPixel(xc - y, yc + x, c);
        putPixel(xc + y, yc - x, c);
        putPixel(xc - y, yc - x, c);
    }

    /**
     * Dibuja la circunferencia de una elipse mediante el algoritmo de
     * Bresenham para elipses.
     */
    public void drawOval(int x0, int y0, int x1, int y1, Color color) {
        int a = Math.abs(x1 - x0) / 2;
        int b = Math.abs(y1 - y0) / 2;
        int xCenter = (x0 + x1) / 2;
        int yCenter = (y0 + y1) / 2;

        int x = 0;
        int y = b;
        int aSquared = a * a;
        int bSquared = b * b;
        int twoASquared = 2 * aSquared;
        int twoBSquared = 2 * bSquared;
        int xChange = bSquared * (1 - 2 * a) + 2 * aSquared * y;
        int yChange = aSquared * (1 + 2 * b) - 2 * bSquared * x;
        int ellipseError = (int) (bSquared - aSquared * b + 0.25 * aSquared);

        while (x * bSquared <= y * aSquared) {
            putPixel(xCenter + x, yCenter + y, color);
            putPixel(xCenter - x, yCenter + y, color);
            putPixel(xCenter + x, yCenter - y, color);
            putPixel(xCenter - x, yCenter - y, color);

            x++;
            if (ellipseError < 0) {
                ellipseError += twoBSquared * x + bSquared;
            } else {
                y--;
                ellipseError += twoBSquared * x - twoASquared * y + bSquared;
            }
        }

        x = a;
        y = 0;
        xChange = aSquared * (1 - 2 * b) + 2 * bSquared * x;
        yChange = bSquared * (1 + 2 * a) - 2 * aSquared * y;
        ellipseError = (int) (aSquared - bSquared * a + 0.25 * bSquared);

        while (x * bSquared > y * aSquared) {
            putPixel(xCenter + x, yCenter + y, color);
            putPixel(xCenter - x, yCenter + y, color);
            putPixel(xCenter + x, yCenter - y, color);
            putPixel(xCenter - x, yCenter - y, color);

            y++;
            if (ellipseError < 0) {
                ellipseError += twoASquared * y + aSquared;
            } else {
                x--;
                ellipseError += twoASquared * y - twoBSquared * x + aSquared;
            }
        }

    }

    /**
     * Coloca un único píxel en el buffer si las coordenadas están dentro del
     * área válida.
     */
    public void putPixel(int x, int y, Color color) {
        if (x >= 0 && x < buffer.getWidth() && y >= 0 && y < buffer.getHeight()) {
            buffer.setRGB(x, y, color.getRGB());
        }
    }
    
    /** Panel Swing donde se mostrará la imagen resultante. */
    private javax.swing.JPanel panel;

    /**
     * Asocia un panel Swing para poder repintarlo cuando sea necesario.
     */
    public void setPanel(javax.swing.JPanel panel) {
        this.panel = panel;
    }
    
    /**
     * Solicita el repintado del panel asociado.
     */
    public void render() {
        if (panel != null) {
            panel.repaint();
        }
    }

    
    /**
     * Limpia el buffer rellenándolo con un color oscuro.
     */
    public void clear() {
        fillRect(0, 0, WIDTH, HEIGHT, new Color(5, 5, 20));
    }

    /** Devuelve la imagen interna usada como buffer. */
    public BufferedImage getBuffer() {
        return buffer;
    }

    /** Ancho del buffer. */
    public int getWidth() {
        return WIDTH;
    }

    /** Alto del buffer. */
    public int getHeight() {
        return HEIGHT;
    }
    
    /**
     * Establece un desplazamiento global que se aplica a todas las
     * operaciones de dibujo.
     */
    public void translate(int x, int y) {
        this.translateX = x;
        this.translateY = y;
    }
    
    /** Libera los recursos de una imagen auxiliar. */
    public void dispose(BufferedImage image) {
        image.flush();
    }

}
