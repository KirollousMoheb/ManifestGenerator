package com.generator.manifestgenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MachineManifestController {
    private final ArrayList<String> fg_names=new ArrayList<>();
    private final List<CheckComboBox<String>> fg_modes_checkboxes = new ArrayList<>();

    @FXML
    Accordion accordion;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField filename;
    @FXML
    TextField fgname;
    @FXML
    public void initialize()  {
        addPane(accordion,scroll,"machineState");
        fg_names.add("machineState");
    }
    public void generateManifest(ActionEvent e){
        if(filename.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Machine Manifest Error");
            alert.setHeaderText("Missing Machine Manifest Id");
            alert.setContentText("You must Enter Machine Manifest Id");
            alert.showAndWait();
            return;
        }
        JSONObject fgs_objs = new JSONObject();
        JSONObject main_obj=new JSONObject();
        for(int i=0;i<fg_names.size();i++){
            JSONObject fg_obj = new JSONObject();
            if(getSelectedItems(fg_modes_checkboxes.get(i)).size()==0){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Machine Manifest Error");
                alert.setHeaderText("Missing Modes for Function Group: "+fg_names.get(i));
                alert.setContentText("You must Enter at least one mode for it");
                alert.showAndWait();
                return;
            }
            fg_obj.put("states",getSelectedItems(fg_modes_checkboxes.get(i)));
            fgs_objs.put(fg_names.get(i),fg_obj);
        }
        main_obj.put("function_groups",fgs_objs);
        Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();

        FileChooser fileChooser=new FileChooser();
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("JSON file(*.json)","*.json");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(filename.getText().trim());
        File file=fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            saveTextToFile(prettifyJSON(main_obj.toString(),4), file);
            Path path= Path.of(file.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Machine Manifest Generated");
            alert.setHeaderText("File Saved Successfully");
            alert.setContentText("Path: "+ path.toAbsolutePath());
            alert.showAndWait();
        }


    }
    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static String prettifyJSON(final String json_str, final int indent_width) {
        final char[] chars = json_str.toCharArray();
        final String newline = System.lineSeparator();

        StringBuilder ret = new StringBuilder();
        boolean begin_quotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"') {
                ret.append(c);
                begin_quotes = !begin_quotes;
                continue;
            }

            if (!begin_quotes) {
                switch (c) {
                    case '{':
                    case '[':
                        ret.append(c).append(newline).append(String.format("%" + (indent += indent_width) + "s", ""));
                        continue;
                    case '}':
                    case ']':
                        ret.append(newline).append((indent -= indent_width) > 0 ? String.format("%" + indent + "s", "") : "").append(c);
                        continue;
                    case ':':
                        ret.append(c).append(" ");
                        continue;
                    case ',':
                        ret.append(c).append(newline).append(indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            ret.append(c).append(c == '\\' ? "" + chars[++i] : "");
        }

        return ret.toString();
    }

    public void clickBack(ActionEvent e) throws IOException {
    Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
    FXMLLoader MainScreenPaneLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    Parent MainScreenPane = MainScreenPaneLoader.load();
    Scene MainScreenScene = new Scene(MainScreenPane, 620, 500);
    primaryStage.setTitle("Manifest Generator");
    primaryStage.setScene(MainScreenScene);
}
public void removeFunctionGroup(){
    removePane(accordion);
}
public void addFunctionGroup(){
    if(fgname.getText()==null|fgname.getText().trim().isEmpty()){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Machine Manifest Error");
        alert.setHeaderText("Function Group name is Empty");
        alert.setContentText("Please Add a Function Group name");
        alert.showAndWait();
        return;
    }else if(checkFGName()){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Machine Manifest Error");
        alert.setHeaderText("Function Group name already exists");
        alert.setContentText("Please Add a different Function Group name");
        alert.showAndWait();
        return;
    }
    fg_names.add(fgname.getText().trim());
    addPane(accordion,scroll,fgname.getText().trim());
    fgname.clear();


}
    private void removePane(Accordion accordion) {
        TitledPane expandedPane = accordion.getExpandedPane();

        if (expandedPane != null) {
            int expandedIndex = accordion.getPanes().indexOf(expandedPane);
            if(expandedIndex==0) {
            return;
            }
            accordion.getPanes().remove(expandedPane);
            fg_names.remove(expandedIndex);
            fg_modes_checkboxes.remove(expandedIndex);

            int nPanes = accordion.getPanes().size();

            if (nPanes > 0) {
                TitledPane nextPane = accordion.getPanes().get(
                        Math.max(0, expandedIndex - 1)
                );

                accordion.setExpandedPane(
                        nextPane
                );

                nextPane.requestFocus();
            }

        }
    }

public boolean checkFGName(){
    for (String fg_name : fg_names) {
        if (fg_name.equals(fgname.getText().trim())) {
            return true;
        }
    }
    return false;
}

    private void addPane(Accordion accordion, ScrollPane scrollPane,String paneLabel) {
        TitledPane newPane = createTitledPane(paneLabel);

        accordion.getPanes().add(newPane);
        accordion.setExpandedPane(newPane);
        newPane.requestFocus();

        scrollPane.applyCss();
        scrollPane.layout();

        scrollPane.setVvalue(scrollPane.getVmax());
    }
    private TitledPane createTitledPane(String paneLabel) {
        return new TitledPane(
                paneLabel,
                createTitledPaneContent()
        );
    }
    private Parent createTitledPaneContent() {

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Function Group Modes: "), 0, 0);
        CheckComboBox<String> checkComboBox=createCheckComboBox();
        grid.add(checkComboBox,1,0);
        fg_modes_checkboxes.add(checkComboBox);

        return grid;
    }
    private List<String> getSelectedItems(CheckComboBox<String> checkComboBox) {
        return checkComboBox.getCheckModel().getCheckedItems();
    }
    private CheckComboBox<String> createCheckComboBox() {
        ObservableList<String> states = FXCollections.observableArrayList(
                "off",
                "startup",
                "running",
                "shutdown",
                "restart"
        );

        return new CheckComboBox<>(states);
    }
}
