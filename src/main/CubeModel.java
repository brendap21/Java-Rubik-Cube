package main;

/**
 * Representa el estado lógico del cubo de Rubik. Mantiene la matriz de
 * {@link Subcubo} y provee operaciones para manipularla.
 */
public class CubeModel {

    /** Matriz de subcubos que conforma el cubo completo. */
    private Subcubo[][][] cuboRubik;
    /** Tamaño de cada subcubo en píxeles. */
    private final int size;
    /** Factor de escala utilizado al crear las piezas. */
    private final double escala;

    /**
     * Crea un modelo de cubo inicializado con todas sus piezas en la posición
     * de resuelto.
     *
     * @param size   tamaño de cada subcubo
     * @param escala factor de escala para separarlos
     */
    public CubeModel(int size, double escala) {
        this.size = size;
        this.escala = escala;
        setSubcube();
    }

    /**
     * Devuelve la matriz interna de subcubos.
     */
    public Subcubo[][][] getCubo() {
        return cuboRubik;
    }

    /**
     * Devuelve el tamaño de cada subcubo.
     */
    public int getSize() {
        return size;
    }

    /**
     * Crea todas las piezas del cubo de Rubik en sus posiciones iniciales.
     */
    private void setSubcube() {
        cuboRubik = new Subcubo[3][3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    int posX = (int) ((x - 1) * size * escala);
                    int posY = (int) ((y - 1) * size * escala);
                    int posZ = (int) ((z - 1) * size * escala);
                    cuboRubik[x][y][z] = new Subcubo(posX, posY, posZ, size);
                }
            }
        }
    }

    /**
     * Rota una capa completa del cubo modificando orientación y posición de sus
     * piezas.
     *
     * @param axis      eje de rotación (0=X, 1=Y, 2=Z)
     * @param layer     índice de la capa a rotar
     * @param clockwise true para sentido horario
     */
    public void rotateLayer(int axis, int layer, boolean clockwise) {
        Subcubo[][][] nuevo = new Subcubo[3][3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    nuevo[x][y][z] = cuboRubik[x][y][z];
                }
            }
        }

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    if ((axis == 0 && x == layer)
                            || (axis == 1 && y == layer)
                            || (axis == 2 && z == layer)) {
                        int nx = x, ny = y, nz = z;
                        switch (axis) {
                            case 0:
                                if (clockwise) {
                                    ny = z;
                                    nz = 2 - y;
                                } else {
                                    ny = 2 - z;
                                    nz = y;
                                }
                                break;
                            case 1:
                                if (clockwise) {
                                    nx = 2 - z;
                                    nz = x;
                                } else {
                                    nx = z;
                                    nz = 2 - x;
                                }
                                break;
                            case 2:
                                if (clockwise) {
                                    nx = y;
                                    ny = 2 - x;
                                } else {
                                    nx = 2 - y;
                                    ny = x;
                                }
                                break;
                        }
                        nuevo[nx][ny][nz] = cuboRubik[x][y][z];
                        nuevo[nx][ny][nz].rotateOrientation(axis, clockwise);
                    }
                }
            }
        }
        cuboRubik = nuevo;
    }
}
