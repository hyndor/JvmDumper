package ru.hyndo.javadumper.outmode;

import javafx.application.Platform;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

public class FernFlowerOutMode implements OutMode {

    private Path outputPath;
    private CodeArea codeArea;

    public FernFlowerOutMode(Path outputPath, CodeArea codeArea) {
        this.outputPath = outputPath;
        this.codeArea = codeArea;
    }

    @Override
    public String name() {
        return "Fern Flower (Java code)";
    }

    @Override
    public void print(Path classFile, String className) {
        Platform.runLater(() -> {
            ConsoleDecompiler.main(new String[]{classFile.toString(), outputPath.toString()});
            try {
                Optional<Path> opt =
                        Files.walk(outputPath)
                                .filter(path -> path.toString().endsWith(className.substring(className.lastIndexOf(".") + 1) + ".java"))
                                .findFirst();
                if (opt.isPresent()) {
                    codeArea.replaceText(Files.lines(opt.get()).collect(Collectors.joining("\n")));
                } else {
                    codeArea.replaceText("Can not find decompiled java file in " + outputPath.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
