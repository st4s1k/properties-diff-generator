package com.st4s1k.propertiesdiffgenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PropertiesDiffGeneratorApplication extends Application {

    private PropertiesDiffGeneratorController controller;

    @Override
    public void start(Stage stage) throws IOException {
        URL fxlmUrl = PropertiesDiffGeneratorApplication.class.getResource("diff-gen.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxlmUrl);
        Scene scene = new Scene(fxmlLoader.load());
        controller = fxmlLoader.getController();
        controller.load();
        stage.setTitle("Properties Diff Generator");
        stage.setMinWidth(500);
        stage.setMinHeight(250);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        controller.save();
    }

    public static void main(String[] args) {
        launch();
    }
}