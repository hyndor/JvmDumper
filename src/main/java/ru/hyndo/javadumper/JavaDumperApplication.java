package ru.hyndo.javadumper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.PrintStream;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaDumperApplication extends Application {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static final java.lang.reflect.Field LIBRARIES;

    static {
        try {
            LIBRARIES = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            LIBRARIES.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

    public static final String APPLICATION_NAME = "JVM DUMPER";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (Float.parseFloat(System.getProperty("java.class.version")) != 52.0) {
            JOptionPane.showMessageDialog(null, "Java Dumper requires Java 8 to work correctly");
            System.exit(0);
            return;
        }
        try {
            JavaCompiler c = ToolProvider.getSystemJavaCompiler();
            if (c == null) {
                JOptionPane.showMessageDialog(null, "Probably you are running on JRE. Java Dumper requires JDK to work correctly");
                System.exit(1);
                return;
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "Probably you are running on JRE. Java Dumper requires JDK to work correctly");
            System.exit(1);
            return;
        }
        System.setProperty("sun.jvm.hotspot.runtime.VM.disableVersionCheck", "true");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene.fxml"));
        Parent root = loader.load();
        MainFormController mainFormController = loader.getController();
        ProxiedSout proxy = new ProxiedSout(System.out);
        System.setErr(new PrintStream(proxy, true));
        proxy.addNewLineHandler((str, a) -> {
            if(str.contains("Error attaching to process")) {
                executorService.submit(() -> {
                    JOptionPane.showMessageDialog(null, "Error attaching to process: " + "\"" + str + "\"");
                });
            }
        });
        mainFormController.setVMsProvider(new VMsProviderImpl());
        mainFormController.init();
        Scene scene = new Scene(root);
        //noinspection ConstantConditions
        scene.getStylesheets().add(JavaDumperApplication.class.getClassLoader().getResource("java-keywords.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.show();
    }

    public static String[] getLoadedLibraries(final ClassLoader loader) {
        final Vector<String> libraries;
        try {
            //noinspection unchecked
            libraries = (Vector<String>) LIBRARIES.get(loader);
            return libraries.toArray(new String[]{});
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
