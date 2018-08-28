package ru.hyndo.javadumper;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;

public class ProxiedPrintStream extends OutputStream {

    private PrintStream source;
    private StringBuffer currentLine = new StringBuffer();
    private BiConsumer<String, ProxiedPrintStream> newLineHandler = (a, b) -> {};

    ProxiedPrintStream(PrintStream source) {
        this.source = source;
    }

    public void addNewLineHandler(BiConsumer<String, ProxiedPrintStream> newLineHandler) {
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
