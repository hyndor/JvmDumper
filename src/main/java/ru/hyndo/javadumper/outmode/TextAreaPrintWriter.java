package ru.hyndo.javadumper.outmode;

import javafx.scene.control.TextArea;

import java.io.*;

public class TextAreaPrintWriter extends PrintWriter {

    public TextAreaPrintWriter(TextArea textArea) {
        super(new TextAreaWriter(textArea));
    }

    private static class TextAreaWriter extends Writer {

        private final TextArea textArea;

        TextAreaWriter(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(String str) {
//            textArea.setText(textArea.getText() + str);
            synchronized (textArea) {
                textArea.appendText(str);
            }
//            System.out.println(str);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            write(str);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new UnsupportedEncodingException();
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {
        }
    }

}
