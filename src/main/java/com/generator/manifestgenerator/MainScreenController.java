package com.generator.manifestgenerator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainScreenController {
    @FXML
    public void goToMachine(ActionEvent e) throws IOException {
        Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
        FXMLLoader MachineManifestPaneLoader = new FXMLLoader(getClass().getResource("machinemanifest.fxml"));
        Parent MachineManifestPane = MachineManifestPaneLoader.load();
        Scene MachineManifestScene = new Scene(MachineManifestPane, 800, 600);
        primaryStage.setTitle("Machine Manifest Generator");
        primaryStage.setScene(MachineManifestScene);
    }
    @FXML
    public void goToEM1(ActionEvent e) throws IOException{
        Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
        FXMLLoader ExecutionManifestPaneLoader = new FXMLLoader(getClass().getResource("executionmanifest.fxml"));
        Parent ExecutionManifestPane = ExecutionManifestPaneLoader.load();
        Scene ExecutionManifestScene = new Scene(ExecutionManifestPane, 800, 600);
        primaryStage.setTitle("Execution Manifest Generator Version 1");
        primaryStage.setScene(ExecutionManifestScene);
    }
    @FXML
    public void goToEM2(ActionEvent e) throws IOException{
        Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
        FXMLLoader ExecutionManifestPaneLoader = new FXMLLoader(getClass().getResource("executionmanifest2.fxml"));
        Parent ExecutionManifestPane = ExecutionManifestPaneLoader.load();
        Scene ExecutionManifestScene = new Scene(ExecutionManifestPane, 800, 600);
        primaryStage.setTitle("Execution Manifest Generator Version 2");
        primaryStage.setScene(ExecutionManifestScene);
    }
}
