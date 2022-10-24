module com.generator.executionmanifestgenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires org.controlsfx.controls;


    opens com.generator.manifestgenerator to javafx.fxml;
    exports com.generator.manifestgenerator;
}