# Manual Test Plan for Arrow Key Rotations

1. **Launch Application**
   - Compile and run `src/main/Cubo.java`.
   - Ensure the window opens displaying the 3D cube.

2. **Select Subcube**
   - Press `Enter` to enable game mode.
   - Click on a subcube to select it (highlighted).

3. **UP Arrow Test**
   - Select the front face subcube in the upper-left corner (`F1`).
   - Rotate the whole cube arbitrarily using mouse drag or `I/J/K/L` keys.
   - Press `UP` arrow.
   - Verify that the layer containing `F1`, `F4`, and `F7` rotates upward regardless of cube orientation.

4. **DOWN Arrow Test**
   - With a subcube on any face selected, press `DOWN`.
   - The layer containing the selected cube should rotate downward (opposite of `UP`).

5. **LEFT/RIGHT Arrow Tests**
   - Select subcubes on different faces and press `LEFT` and `RIGHT`.
   - The layer containing the selected cube should rotate left or right relative to the selected face orientation.

6. **Regression**
   - Repeat steps with the cube oriented differently or after prior rotations to ensure arrow keys continue to use the selected face orientation.
