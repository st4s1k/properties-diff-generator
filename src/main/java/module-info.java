module com.st4s1k.propertiesdiffgenerator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.st4s1k.propertiesdiffgenerator to javafx.fxml;
    exports com.st4s1k.propertiesdiffgenerator;
}