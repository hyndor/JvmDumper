package ru.hyndo.javadumper;

import javax.swing.*;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {

    public static void main(String[] args) {
        ProxiedSout proxy = new ProxiedSout(System.out);
        System.setErr(new PrintStream(proxy, true));
        proxy.addNewLineHandler((str, a) -> {
            proxy.getSource().println("PROXY:" + str);
        });
    }


}
