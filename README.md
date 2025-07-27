# Rubik Cube 3D

This project is a small Java application that renders and lets you interact with a Rubik cube in 3D.

## Prerequisites

- Java Development Kit (JDK) 8 or later
- Apache Ant (for building the jar)

## Building

Run the following command from the project root to compile the classes and package them in a jar file:

```bash
ant jar
```

The jar will be created under `dist/CuboRubik3D.jar`.

## Running

After building, launch the application with:

```bash
java -jar dist/CuboRubik3D.jar
```

Alternatively you can compile and run the `Cubo` class directly.

## Gameplay

Press `Enter` to toggle **game mode**. In game mode you can click a sub‑cube and rotate its layer using the arrow keys. Outside of game mode you can rotate the entire cube with the mouse or the `I/J/K/L` keys. `W`, `A`, `S`, `D` move the cube and the mouse wheel scales it.

Refer to the in‑game help (`H`) for the complete list of controls.
