package main;

import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/** Simple panel to display the buffer from Graficos. */
public class RenderPanel extends JPanel {
    private final Graficos graficos;

    public RenderPanel(Graficos graficos) {
        this.graficos = graficos;
        graficos.setPanel(this);
        setPreferredSize(new java.awt.Dimension(graficos.getWidth(), graficos.getHeight()));
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        BufferedImage img = graficos.getBuffer();
        g.drawImage(img, 0, 0, null);
    }
}