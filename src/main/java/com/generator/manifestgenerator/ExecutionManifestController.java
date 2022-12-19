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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExecutionManifestController {
    private  int count=0;
    private final List<ChoiceBox> fg_name_container = new ArrayList<>();
    private final List<TextField> fg_states_container = new ArrayList<>();
    private final List<ChoiceBox> sch_policy_container = new ArrayList<>();
    private final List<TextField> sch_priority_container = new ArrayList<>();
    private final List<TextField> args_container = new ArrayList<>();
    private final List<TextField> env_container = new ArrayList<>();
    @FXML
    Accordion accordion;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField filename;
    @FXML
    ChoiceBox choice;

    public void clickBack(ActionEvent e) throws IOException {
        Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
        FXMLLoader MainScreenPaneLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent MainScreenPane = MainScreenPaneLoader.load();
        Scene MainScreenScene = new Scene(MainScreenPane, 620, 500);
        primaryStage.setTitle("Manifest Generator");
        primaryStage.setScene(MainScreenScene);
    }
    public void addConfig() {
        addPane(accordion,scroll);
    }
    public void removeConfig() {
        removePane(accordion);
    }
    public void generateManifest(ActionEvent e)  {

        if(count==0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText("Must have at least one Startup Configuration");
            alert.setContentText("Please Add at least one Startup Configuration");
            alert.showAndWait();
            return;
        }
        String validationMessage=validateFields();
        if (!validationMessage.equals("OK")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText(validationMessage);
            alert.setContentText("Please complete the missing fields");
            alert.showAndWait();
            return;
        }
        if(!checkSameFunctionGroup()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText("Misconfigured process - Assigned to more than one Function Group [SWS_EM_02254]");
            alert.setContentText("Please choose only one Function Group");
            alert.showAndWait();
            return;
        }
        if(filename.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText("Missing Execution Manifest Id");
            alert.setContentText("You must Enter Execution Manifest Id");
            alert.showAndWait();
            return;
        }
        JSONObject main_obj = new JSONObject();
        if(choice.getValue().equals("Adaptive Application")){
            main_obj.put("platform_application","false");

        }else{
            main_obj.put("platform_application","true");
        }
        JSONObject Config = new JSONObject();
        for (int i=0;i<count;i++){
            JSONObject fng_st=new JSONObject();
            JSONArray machine_states=new JSONArray();
            JSONObject depends=new JSONObject();
            JSONObject Config_contents=new JSONObject();
            Config_contents.put("depends",depends);
            Config_contents.put("machine_states",machine_states);
            fng_st.put(fg_name_container.get(i).getValue(), fg_states_container.get(i).getText());
            Config_contents.put("function_groups_states", fng_st);
            Config_contents.put("scheduling_policy", sch_policy_container.get(i).getValue());
            Config_contents.put("scheduling_priority", sch_priority_container.get(i).getText());
            String user_args=args_container.get(i).getText();
            String user_envs=env_container.get(i).getText();
            List<String> items = Arrays.asList(user_args.split("\\s*,\\s*"));
            List<String> items2 = Arrays.asList(user_envs.split("\\s*,\\s*"));
            Config_contents.put("arguments", items);
            Config_contents.put("environments", items2);
            String config_no="Config_"+(i+1);
            System.out.println(config_no);
            Config.put(config_no,Config_contents);
        }
        main_obj.put("startup_configs",Config);
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
            alert.setTitle("Execution Manifest Generated");
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
    private boolean checkSameFunctionGroup(){
        for (int i = 0; i < fg_name_container.size(); i++) {
            for (int j = i+1 ;j <fg_name_container.size() ; j++) {
                if (fg_name_container.get(i).getValue()!=null&&!fg_name_container.get(i).getValue().equals(fg_name_container.get(j).getValue())){
                    return false;
                }
            }
        }
        return true;
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
    private void addPane(Accordion accordion, ScrollPane scrollPane) {
        TitledPane newPane = createTitledPane();
        count++;

        accordion.getPanes().add(newPane);
        accordion.setExpandedPane(newPane);
        newPane.requestFocus();

        scrollPane.applyCss();
        scrollPane.layout();

        scrollPane.setVvalue(scrollPane.getVmax());
    }
    private void removePane(Accordion accordion) {

        if (count >0) {
            count--;

            accordion.getPanes().remove(count);
            fg_states_container.remove(fg_states_container.size()-1);
            fg_name_container.remove(fg_name_container.size()-1);
            sch_priority_container.remove(sch_priority_container.size()-1);
            sch_policy_container.remove(sch_policy_container.size()-1);
            args_container.remove(args_container.size()-1);
            env_container.remove(env_container.size()-1);

            int nPanes = accordion.getPanes().size();

            if (nPanes > 0) {
                TitledPane nextPane = accordion.getPanes().get(
                        Math.max(0, count-1)
                );

                accordion.setExpandedPane(
                        nextPane
                );

                nextPane.requestFocus();
            }

        }
    }

    private Parent createTitledPaneContent() {

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));

        grid.add(new Label("Function Group Name: "), 0, 0);

        ChoiceBox<String> fg_name=new ChoiceBox();
        fg_name.getItems().add("FunctionGroup1");
        fg_name.getItems().add("FunctionGroup2");
        fg_name.getItems().add("FunctionGroup3");
        fg_name.setId("fg_name"+count);
        grid.add(fg_name, 1, 0);
        fg_name_container.add(fg_name);

        TextField fg_states=new TextField();
        grid.add(new Label("Function Group States: "), 0, 1);
        fg_states.setId("fg_states_"+count);
        grid.add(fg_states, 1, 1);
        fg_states_container.add(fg_states);



        grid.add(new Label("Scheduling Policy: "), 0, 2);
        ChoiceBox sch_policy=new ChoiceBox();
        sch_policy.getItems().add("SCHED_FIFO");
        sch_policy.getItems().add("SCHED_RR");
        sch_policy.getItems().add("SCHED_OTHER");
        sch_policy.setId("sch_policy_"+count);
        grid.add(sch_policy, 1, 2);
        sch_policy_container.add(sch_policy);

        grid.add(new Label("Scheduling Priority: "), 0, 3);
        TextField sch_algo=new TextField();
        sch_algo.setId("sch_algo_"+count);
        grid.add(sch_algo, 1, 3);
        sch_priority_container.add(sch_algo);

        grid.add(new Label("Arguments: "), 0, 4);
        TextField args=new TextField();
        args.setId("args_"+count);
        grid.add(args, 1, 4);
        args_container.add(args);

        grid.add(new Label("Environments: "), 0, 5);
        TextField env=new TextField();
        env.setId("env_"+count);
        grid.add(env, 1, 5);
        env_container.add(env);


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
    private TitledPane createTitledPane() {
        return new TitledPane(
                "startup_config_"+(count+1),
                createTitledPaneContent()
        );
    }
    private String validateFields(){
        for(int i=0;i<count;i++){
            if (fg_name_container.get(i).getSelectionModel().isEmpty()){
                return fg_name_container.get(i).getId()+" is Empty";
            } else if (fg_states_container.get(i).getText().trim().isEmpty()|| fg_states_container.get(i).getText()==null) {
                return fg_states_container.get(i).getId()+" is Empty";
            }
            else if (sch_policy_container.get(i).getSelectionModel().isEmpty()) {
                return sch_policy_container.get(i).getId()+" is Empty";
            }
            else if (sch_priority_container.get(i).getText().trim().isEmpty()||sch_priority_container.get(i).getText()==null) {
                return sch_priority_container.get(i).getId()+" is Empty";
            }
            else if (args_container.get(i).getText().trim().isEmpty()||args_container.get(i).getText()==null) {
                return args_container.get(i).getId()+" is Empty";
            }
            else if (env_container.get(i).getText().trim().isEmpty()||env_container.get(i).getText()==null) {
                return env_container.get(i).getId()+" is Empty";
            }
        }
        return "OK";
    }
}
