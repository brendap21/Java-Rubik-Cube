# Manual Test Plan for Arrow Key Rotations

1. **Launch Application**
   - Compile and run `src/main/Cubo.java`.
   - Ensure the window opens displaying the 3D cube.

2. **Select Subcube**
   - Press `Enter` to enable game mode.
   - Click on a subcube to select it (highlighted).

3. **Face and Arrow Coverage**
   - For each of the six faces (front, back, left, right, top, bottom):
     - Select a subcube on that face.
     - Press each arrow key (`UP`, `DOWN`, `LEFT`, `RIGHT`).
     - Ensure the rotation axis and direction match the documented clockwise convention for the current face.

4. **Regression**
   - Repeat the above with the cube oriented differently or after prior rotations to ensure arrow keys continue to use the selected face orientation.
