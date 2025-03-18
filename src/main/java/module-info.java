module ca.macewan.batmap.thebatmap {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens ca.macewan.batmap.thebatmap to javafx.fxml;
    exports ca.macewan.batmap.thebatmap;
}