module ca.macewan.thebatmap {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;

    opens ca.macewan.thebatmap to javafx.fxml;
    exports ca.macewan.thebatmap.app;
    opens ca.macewan.thebatmap.app to javafx.fxml;
    exports ca.macewan.thebatmap.app.controllers;
    opens ca.macewan.thebatmap.app.controllers to javafx.fxml;
}