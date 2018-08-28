package ru.hyndo.javadumper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ProxiedSout extends OutputStream {

    private PrintStream source;
    private StringBuffer currentLine = new StringBuffer();
    private BiConsumer<String, ProxiedSout> newLineHandler = (a, b) -> {};

    ProxiedSout(PrintStream source) {
        this.source = source;
    }

    public void addNewLineHandler(BiConsumer<String, ProxiedSout> newLineHandler) {
        this.newLineHandler = this.newLineHandler.andThen(newLineHandler);
    }

    @Override
    public void write(int b) {
        source.write(b);
        if(b == '\n') {
            newLineHandler.accept(currentLine.toString(), this);
            currentLine = new StringBuffer();
        } else {
            currentLine.append((char) b);
        }
    }

    public PrintStream getSource() {
        return source;
    }

    public StringBuffer getCurrentLine() {
        return currentLine;
    }

}
