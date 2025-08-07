package main;

import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Panel ligero encargado únicamente de dibujar el {@link BufferedImage}
 * generado por la clase {@link Graficos}.
 */
public class RenderPanel extends JPanel {
    /** Referencia al sistema de dibujo del que se obtendrá el buffer. */
    private final Graficos graficos;

    /**
     * Crea el panel y lo asocia al contexto gráfico.
     */
    public RenderPanel(Graficos graficos) {
        this.graficos = graficos;
        graficos.setPanel(this);
        setPreferredSize(new java.awt.Dimension(graficos.getWidth(), graficos.getHeight()));
    }

    /**
     * Pinta en pantalla la última imagen generada.
     */
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        BufferedImage img = graficos.getBuffer();
        g.drawImage(img, 0, 0, null);
    }
}
