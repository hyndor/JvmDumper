package ru.hyndo.javadumper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.File;

public class JavaDumperApplication extends Application {

    public static final String APPLICATION_NAME = "JVM DUMPER";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

}
