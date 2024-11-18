package org.example.modelui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.IntConsumer;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-frame.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 750);
        stage.setResizable(false);
        stage.setTitle("Social Dynamics Simulator");

        Image icon = new Image("/icon.png");

        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}