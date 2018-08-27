package ru.hyndo.javadumper.outmode;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface OutMode {

    String name();

    void print(Path classFile, String className);

}
