package ru.hyndo.javadumper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;

public class JavaDumperApplication extends Application {

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
        Path bin = Paths.get(System.getProperty("java.home"), "bin").toAbsolutePath();
        System.out.println(bin.resolve("attach.dll").toString());
        System.load(bin.resolve("attach.dll").toString());
        Thread.sleep(300);
        if(!Arrays.toString(getLoadedLibraries(getClass().getClassLoader())).contains("attach")) {
            System.err.println("Can not load attach.dll");
            System.exit(-1);
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene.fxml"));
        Parent root = loader.load();
        MainFormController mainFormController = loader.getController();
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
            return libraries.toArray(new String[] {});
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
