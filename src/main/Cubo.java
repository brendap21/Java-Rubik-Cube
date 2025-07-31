package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Ventana principal encargada únicamente de inicializar y coordinar los
 * distintos componentes del cubo: modelo, renderizador y controlador de
 * entrada.
 */
public class Cubo extends JFrame {

    public Cubo() {
        Graficos graficos = new Graficos(800, 600);
        CubeModel model = new CubeModel(80, 1.0);
        CubeRenderer renderer = new CubeRenderer(graficos, model);
        InputController controller = new InputController(model, renderer);

        RenderPanel panel = new RenderPanel(graficos);
        panel.addKeyListener(controller);
        add(panel);

        pack();
        setTitle("Cubo Rubik 3D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        renderer.render();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cubo());
    }
}
