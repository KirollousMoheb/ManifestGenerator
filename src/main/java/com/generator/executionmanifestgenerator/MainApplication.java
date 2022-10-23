package com.generator.executionmanifestgenerator;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class MainApplication extends Application {
    @FXML
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage.setScene(new Scene(root,720,600));
        stage.setTitle("Execution Manifest Generator");
        stage.setResizable(false);

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
