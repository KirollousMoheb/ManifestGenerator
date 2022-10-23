module com.generator.executionmanifestgenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens com.generator.executionmanifestgenerator to javafx.fxml;
    exports com.generator.executionmanifestgenerator;
}