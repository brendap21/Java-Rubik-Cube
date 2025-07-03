# Java Rubik Cube

This project draws a Rubik's Cube using a custom rendering routine.

## Building

The repository ships precompiled class files under `build/classes`. To ensure
that the latest source is used you can rebuild them with:

```bash
javac -d build/classes $(find src -name '*.java')
```

After compiling run the application with:

```bash
java -cp build/classes main.Cubo
```

Rebuilding avoids runtime errors such as
`NoSuchMethodError` when the compiled classes do not match the source.
