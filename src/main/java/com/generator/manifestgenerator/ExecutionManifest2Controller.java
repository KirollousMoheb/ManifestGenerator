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
import javafx.scene.layout.HBox;
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

public class ExecutionManifest2Controller {
    private  int count=0;
    private final List<TextField> config_name_container = new ArrayList<>();
    private final List<TextField> exec_depend_container = new ArrayList<>();
    private final List<ChoiceBox> sch_policy_container = new ArrayList<>();
    private final List<TextField> sch_priority_container = new ArrayList<>();
    private final List<TextField> args_container = new ArrayList<>();
    private final List<TextField> env_container = new ArrayList<>();
    private final List<ChoiceBox> fg_name_container = new ArrayList<>();
    private final List<TextField> enter_time_container = new ArrayList<>();
    private final List<TextField> exit_time_container = new ArrayList<>();

    private final List<CheckComboBox<String>> machine_states_container = new ArrayList<>();
    private final List<CheckComboBox<String>> fg_states_container = new ArrayList<>();

    @FXML
    Accordion accordion;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField filename;
    @FXML
    ChoiceBox isfunctionclusterchoice;
    @FXML
    ChoiceBox isstateclientchoice;

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
        if(!checkConfigName()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText("Configurations Must Have Different Names");
            alert.setContentText("Please choose Different Configuration Names");
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
        if(isfunctionclusterchoice.getValue().equals("True")){
            main_obj.put("functional_cluster_process","true");

        }else{
            main_obj.put("functional_cluster_process","false");
        }
        if(isstateclientchoice.getValue().equals("True")){
            main_obj.put("state_client_process","true");

        }else{
            main_obj.put("state_client_process","false");
        }

        JSONArray all_config_objects = new JSONArray();
        for (int i=0;i<count;i++){
            JSONObject Config_object=new JSONObject();
            JSONObject function_group_states=new JSONObject();
            JSONObject time_out=new JSONObject();
            String user_args=args_container.get(i).getText();
            String user_envs=env_container.get(i).getText();
            String exec_depend=exec_depend_container.get(i).getText();

            List<String> items = Arrays.asList(user_args.split("\\s*,\\s*"));
            List<String> items2 = Arrays.asList(user_envs.split("\\s*,\\s*"));
            List<String> items3 = Arrays.asList(exec_depend.split("\\s*,\\s*"));


            if(getSelectedItems(machine_states_container.get(i)).size()==0){
                function_group_states.put("function_group_name",fg_name_container.get(i).getValue());
                function_group_states.put("function_group_state",getSelectedItems(fg_states_container.get(i)));

            }
            Config_object.put("function_group_states",function_group_states);

            Config_object.put("config_name", config_name_container.get(i).getText());
            Config_object.put("execution_dependency",items3);
            Config_object.put("machine_states",getSelectedItems(machine_states_container.get(i)));
            Config_object.put("scheduling_policy", sch_policy_container.get(i).getValue());
            Config_object.put("scheduling_priority", sch_priority_container.get(i).getText());
            Config_object.put("arguments", items);
            Config_object.put("environments", items2);
            time_out.put("enter_timeout_ns",Integer.parseInt(enter_time_container.get(i).getText()));
            time_out.put("exit_timeout_ns",Integer.parseInt(exit_time_container.get(i).getText()));
            Config_object.put("timeout",time_out);

            all_config_objects.add(Config_object);


        }
        main_obj.put("startup_configs",all_config_objects);
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
            config_name_container.remove(config_name_container.size()-1);
            exec_depend_container.remove(exec_depend_container.size()-1);
            sch_priority_container.remove(sch_priority_container.size()-1);
            sch_policy_container.remove(sch_policy_container.size()-1);
            args_container.remove(args_container.size()-1);
            env_container.remove(env_container.size()-1);
            fg_name_container.remove(fg_name_container.size()-1);
            fg_states_container.remove(fg_states_container.size()-1);
            enter_time_container.remove(enter_time_container.size()-1);
            exit_time_container.remove(exit_time_container.size()-1);
            machine_states_container.remove(machine_states_container.size()-1);

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

        grid.setVgap(5);
        grid.setHgap(5);

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Configuration Name: "), 0, 0);
        TextField config_name=new TextField();
        config_name.setId("config_name_"+count);
        grid.add(config_name, 1, 0);
        config_name_container.add(config_name);
        config_name.setPromptText("Enter Configuration Name"); //to set the hint text
        config_name.getParent().requestFocus(); //to not setting the focus on that node so that the hint will display immediately
        config_name.setPrefWidth(180);


        TextField exec_depend=new TextField();
        grid.add(new Label("Execution Dependency: "), 0, 1);
        exec_depend.setId("exec_depend"+count);
        grid.add(exec_depend, 1, 1);
        exec_depend_container.add(exec_depend);
        exec_depend.setPromptText("eg: vdp_config_3.Terminated"); //to set the hint text
        exec_depend.getParent().requestFocus(); //to not setting the focus on that node so that the hint will display immediately


        grid.add(new Label("Machine States: "), 0, 2);
        CheckComboBox<String> machine_states_combo=createCheckComboBox();
        grid.add(machine_states_combo,1,2);
        machine_states_combo.setId("machine_states_"+count);
        machine_states_container.add(machine_states_combo);

        grid.add(new Label("Function Group Name: "), 0, 3);
       // TextField fg_name=new TextField();
        ChoiceBox<String> fg_name=new ChoiceBox();
        fg_name.getItems().add("FunctionGroup1");
        fg_name.getItems().add("FunctionGroup2");
        fg_name.getItems().add("FunctionGroup3");
         fg_name.setId("fg_name"+count);
         HBox hBox=new HBox();
         Button clear=new Button("clear");
         clear.setOnAction(event -> fg_name.setValue(null));
         hBox.getChildren().add(fg_name);
         hBox.getChildren().add(clear);

        grid.add(hBox, 1, 3);
        fg_name_container.add(fg_name);


        grid.add(new Label("Function Group States: "), 0, 4);

        CheckComboBox<String> fg_states_combo=createCheckComboBox();
        grid.add(fg_states_combo,1,4);
        fg_states_combo.setId("fg_state"+count);
        fg_states_container.add(fg_states_combo);


        grid.add(new Label("Scheduling Policy: "), 4, 0);
        //TextField sch_policy=new TextField();
        ChoiceBox sch_policy=new ChoiceBox();
        sch_policy.getItems().add("SCHED_FIFO");
        sch_policy.getItems().add("SCHED_RR");
        sch_policy.getItems().add("SCHED_OTHER");
        sch_policy.setId("sch_policy_"+count);
        grid.add(sch_policy, 5, 0);
        sch_policy_container.add(sch_policy);
        sch_policy.getParent().requestFocus();
        sch_policy.setPrefWidth(180);

        grid.add(new Label("Scheduling Priority: "), 4, 1);
        TextField sch_algo=new TextField();
        sch_algo.setId("sch_priority_"+count);
        grid.add(sch_algo, 5, 1);
        sch_priority_container.add(sch_algo);

        grid.add(new Label("Arguments: "), 4, 2);
        TextField args=new TextField();
        args.setId("args_"+count);
        grid.add(args, 5, 2);
        args_container.add(args);

        grid.add(new Label("Environments: "), 4, 3);
        TextField env=new TextField();
        env.setId("env_"+count);
        grid.add(env, 5, 3);
        env_container.add(env);

        grid.add(new Label("Enter TimeOut: "), 4, 4);
        TextField enter_time_out=new TextField();
        enter_time_out.setId("enter_time_out"+count);
        grid.add(enter_time_out, 5, 4);
        enter_time_container.add(enter_time_out);
        enter_time_out.setPromptText("Enter in NanoSeconds"); //to set the hint text
        enter_time_out.getParent().requestFocus(); //to not setting the focus on that node so that the hint will display immediately

        grid.add(new Label("Exit TimeOut: "), 4, 5);
        TextField exit_time_out=new TextField();
        exit_time_out.setId("exit_time_out"+count);
        grid.add(exit_time_out, 5, 5);
        exit_time_container.add(exit_time_out);
        exit_time_out.setPromptText("Enter in NanoSeconds"); //to set the hint text
        exit_time_out.getParent().requestFocus(); //to not setting the focus on that node so that the hint will display immediately
        return grid;
    }
    private TitledPane createTitledPane() {
        return new TitledPane(
                "startup_config_"+(count+1),
                createTitledPaneContent()
        );
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
    private boolean checkConfigName(){
        for (int i = 0; i < config_name_container.size(); i++) {
            for (int j = i+1; j <config_name_container.size() ; j++) {
                if (config_name_container.get(i).getText().trim().equals(config_name_container.get(j).getText().trim())){
                    return false;
                }
            }
        }
        return true;
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
    private String validateFields(){

        for(int i=0;i<count;i++){

            if ((getSelectedItems(fg_states_container.get(i)).size()==0)&&
                    (fg_name_container.get(i).getSelectionModel().isEmpty())
                    &&getSelectedItems(machine_states_container.get(i)).size()==0) {
                return "Can't Have Empty Function Group States and Machine States,Must Enter only one of them!";
            }else{
                if ((getSelectedItems(machine_states_container.get(i)).size()>0)&&((!fg_name_container.get(i).getSelectionModel().isEmpty())||(getSelectedItems(fg_states_container.get(i)).size()>0))){
                    return "Can't Enter Machine States and Function Group Together";
                }else if((getSelectedItems(machine_states_container.get(i)).size()==0)&&((!fg_name_container.get(i).getSelectionModel().isEmpty())&&(getSelectedItems(fg_states_container.get(i)).size()==0))){
                    return "Enter the Empty Function Group States Fields";

                }else if((getSelectedItems(machine_states_container.get(i)).size()==0)&&((fg_name_container.get(i).getSelectionModel().isEmpty())&&(getSelectedItems(fg_states_container.get(i)).size()>0))){
                    return "Enter the Empty Function Group Name Fields";

                }

            }


            if (config_name_container.get(i).getText().trim().isEmpty()|| config_name_container.get(i).getText()==null){
                return config_name_container.get(i).getId()+" is Empty";
            } else if (exec_depend_container.get(i).getText().trim().isEmpty()|| exec_depend_container.get(i).getText()==null) {
                return exec_depend_container.get(i).getId()+" is Empty";
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
            else if (enter_time_container.get(i).getText().trim().isEmpty()||enter_time_container.get(i).getText()==null) {
                return enter_time_container.get(i).getId()+" is Empty";
        }
            else if (exit_time_container.get(i).getText().trim().isEmpty()||exit_time_container.get(i).getText()==null) {
                return exit_time_container.get(i).getId()+" is Empty";
            }




        }
        return "OK";
    }
}
