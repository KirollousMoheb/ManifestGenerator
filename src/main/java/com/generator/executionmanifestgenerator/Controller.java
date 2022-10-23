package com.generator.executionmanifestgenerator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {

    private static int count=0;
    private List<TextField> fg_name_container = new ArrayList();
    private List<TextField> fg_modes_container = new ArrayList();
    private List<TextField> sch_policy_container = new ArrayList();
    private List<TextField> sch_priority_container = new ArrayList();
    private List<TextField> args_container = new ArrayList();
    private List<TextField> env_container = new ArrayList();

    @FXML
    Accordion accordion;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField filename;
    @FXML
    ChoiceBox choice;
    public void addConfig() throws Exception {
        addPane(accordion,scroll);
    }
    public void removeConfig() throws Exception{
        removePane(accordion);
    }
    public void generateManifest()  {
        System.out.println("printing");
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
            fng_st.put(fg_name_container.get(i).getText(),fg_modes_container.get(i).getText());
            Config_contents.put("function_groups_states", fng_st);
            Config_contents.put("scheduling_policy", sch_policy_container.get(i).getText());
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
        try (FileWriter Data = new FileWriter(filename.getText()+".json")) {
            Data.write(formatJSONStr(main_obj.toString(),4)); // setting spaces for indent
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Execution Manifest Generated");
            alert.setHeaderText("File Saved Successfully");
            Path path = Paths.get(filename.getText()+".json");

            alert.setContentText("Path: "+path.toAbsolutePath().toString());
            alert.showAndWait();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    public static String formatJSONStr(final String json_str, final int indent_width) {
        final char[] chars = json_str.toCharArray();
        final String newline = System.lineSeparator();

        String ret = "";
        boolean begin_quotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"') {
                ret += c;
                begin_quotes = !begin_quotes;
                continue;
            }

            if (!begin_quotes) {
                switch (c) {
                    case '{':
                    case '[':
                        ret += c + newline + String.format("%" + (indent += indent_width) + "s", "");
                        continue;
                    case '}':
                    case ']':
                        ret += newline + ((indent -= indent_width) > 0 ? String.format("%" + indent + "s", "") : "") + c;
                        continue;
                    case ':':
                        ret += c + " ";
                        continue;
                    case ',':
                        ret += c + newline + (indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            ret += c + (c == '\\' ? "" + chars[++i] : "");
        }

        return ret;
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
        TitledPane expandedPane = accordion.getExpandedPane();

        if (expandedPane != null) {
            int expandedIndex = accordion.getPanes().indexOf(expandedPane);
            accordion.getPanes().remove(expandedPane);

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
            count--;

        }
    }

    private Parent createTitledPaneContent() {

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));

        grid.add(new Label("Function Group Name: "), 0, 0);
        TextField fg_name=new TextField();
        fg_name.setId("fg_name_"+count);
        grid.add(fg_name, 1, 0);
        fg_name_container.add(fg_name);

        TextField fg_modes=new TextField();
        grid.add(new Label("Function Group Modes: "), 0, 1);
        fg_modes.setId("fg_modes_"+count);
        grid.add(fg_modes, 1, 1);
        fg_modes_container.add(fg_modes);

        grid.add(new Label("Scheduling Policy: "), 0, 2);
        TextField sch_policy=new TextField();
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
    private TitledPane createTitledPane() {
        return new TitledPane(
                "startup_config_"+(count+1),
                createTitledPaneContent()
        );
    }
}
