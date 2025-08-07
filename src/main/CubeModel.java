package main;

import java.util.Random;

/**
 * Encapsula el estado del cubo de Rubik y operaciones básicas sobre el mismo.
 */
public class CubeModel {

    /** Matriz tridimensional de subcubos. */
    private Subcubo[][][] cuboRubik;
    /** Tamaño de cada subcubo en píxeles. */
    private final int size;
    /** Factor de escala para separar los subcubos. */
    private double escala = 1.0;

    /**
     * Crea un nuevo modelo del cubo.
     *
     * @param size tamaño de cada subcubo en píxeles
     */
    public CubeModel(int size) {
        this.size = size;
        setSubcube();
    }

    /**
     * Devuelve la matriz de subcubos que representa el cubo.
     */
    public Subcubo[][][] getCube() {
        return cuboRubik;
    }

    /**
     * Inicializa todas las piezas del cubo en su posición original.
     */
    public final void setSubcube() {
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
     * Rota una capa del cubo.
     *
     * @param axis     eje de rotación (0=X,1=Y,2=Z)
     * @param layer    índice de la capa (0-2)
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
                    if ((axis == 0 && x == layer) ||
                        (axis == 1 && y == layer) ||
                        (axis == 2 && z == layer)) {
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
                        nuevo[nx][ny][nz].rotateColors(axis, clockwise);
                        nuevo[nx][ny][nz].rotateOrientation(axis, clockwise);
                    }
                }
            }
        }
        cuboRubik = nuevo;
    }

    /**
     * Mezcla el cubo realizando rotaciones aleatorias sin animación.
     */
    public void scramble() {
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            rotateLayer(r.nextInt(3), r.nextInt(3), r.nextBoolean());
        }
    }
}

