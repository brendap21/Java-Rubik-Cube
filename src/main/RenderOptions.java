package main;

/**
 * Opciones de renderizado para un Subcubo.
 */
public class RenderOptions {
    /** Indica si la pieza debe resaltarse. */
    public boolean highlight = false;
    /** Rotaciones adicionales en cada eje. */
    public double extraRotX = 0, extraRotY = 0, extraRotZ = 0;
    /** Traslaciones adicionales en el espacio. */
    public double extraTX = 0, extraTY = 0, extraTZ = 0;
    /** Mostrar etiquetas de las caras. */
    public boolean showLabels = false;
    /** Índices del subcubo para etiquetado. */
    public int idxX = 0, idxY = 0, idxZ = 0;
}
