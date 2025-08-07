package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Controlador de entrada encargado de traducir eventos de teclado y ratón
 * en acciones sobre el modelo y el renderizador.
 */
public class InputController extends MouseAdapter implements KeyListener, MouseWheelListener {

    private final CubeModel model;
    private final CubeRenderer renderer;
    private final Cubo cubo;

    public InputController(CubeModel model, CubeRenderer renderer, Cubo cubo) {
        this.model = model;
        this.renderer = renderer;
        this.cubo = cubo;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                cubo.moveTranslation(-5, 0);
                break;
            case KeyEvent.VK_D:
                cubo.moveTranslation(5, 0);
                break;
            case KeyEvent.VK_W:
                cubo.moveTranslation(0, -5);
                break;
            case KeyEvent.VK_S:
                cubo.moveTranslation(0, 5);
                break;
            case KeyEvent.VK_I:
                cubo.applyRotation(0, -5);
                break;
            case KeyEvent.VK_K:
                cubo.applyRotation(0, 5);
                break;
            case KeyEvent.VK_J:
                cubo.applyRotation(1, -5);
                break;
            case KeyEvent.VK_L:
                cubo.applyRotation(1, 5);
                break;
            case KeyEvent.VK_U:
                cubo.applyRotation(2, -5);
                break;
            case KeyEvent.VK_O:
                cubo.applyRotation(2, 5);
                break;
            case KeyEvent.VK_R:
                model.scramble();
                break;
            default:
                return;
        }
        cubo.moverCubo();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // no action
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // no action
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Placeholder for future scale control
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Se podrían implementar rotaciones con el ratón
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // no action
    }
}

