package main;

import static org.junit.Assert.*;

import org.junit.Test;

public class CubeModelTest {

    @Test
    public void testRotateLayerMovesPieces() {
        CubeModel model = new CubeModel(1, 1.0);
        Subcubo before = model.getCubo()[0][2][0];
        model.rotateLayer(0, 0, true);
        assertSame(before, model.getCubo()[0][0][0]);
    }
}
