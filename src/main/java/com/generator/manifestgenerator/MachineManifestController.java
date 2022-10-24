package com.generator.manifestgenerator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MachineManifestController {

public void clickBack(ActionEvent e) throws IOException {
    Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
    FXMLLoader MainScreenPaneLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    Parent MainScreenPane = MainScreenPaneLoader.load();
    Scene MainScreenScene = new Scene(MainScreenPane, 620, 500);
    primaryStage.setTitle("Manifest Generator");
    primaryStage.setScene(MainScreenScene);
}
}
