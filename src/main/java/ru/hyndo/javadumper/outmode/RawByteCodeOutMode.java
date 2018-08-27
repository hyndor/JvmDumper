package ru.hyndo.javadumper.outmode;

import javafx.application.Platform;
import org.fxmisc.richtext.CodeArea;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

public class RawByteCodeOutMode implements OutMode {

    private CodeArea codeArea;

    public RawByteCodeOutMode(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public String name() {
        return "Raw byte code";
    }

    @Override
    public void print(Path classFile, String className) {
        try {
            Platform.runLater(() -> codeArea.replaceText(""));
            print(classFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print(String classPath) throws Exception {
        int i = 0;
        int flags = ClassReader.SKIP_DEBUG;
        ClassReader cr = new ClassReader(new FileInputStream(classPath));
        StringWriter strWriter = new StringWriter();
        cr.accept(new TraceClassVisitor(new PrintWriter(strWriter)), flags);
        Platform.runLater(() -> codeArea.replaceText(strWriter.getBuffer().toString()));
    }
}
