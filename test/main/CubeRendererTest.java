package main;

import static org.junit.Assert.*;

import org.junit.Test;

public class CubeRendererTest {

    @Test
    public void testApplyRotationChangesAngles() {
        CubeModel model = new CubeModel(1, 1.0);
        Graficos g = new Graficos(10, 10);
        CubeRenderer renderer = new CubeRenderer(g, model);
        renderer.applyRotation(0, 90);
        assertEquals(90.0, renderer.getAnguloX(), 0.001);
    }
}
