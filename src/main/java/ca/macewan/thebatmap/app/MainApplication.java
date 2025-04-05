package ca.macewan.thebatmap.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Create and initialize the BatMap application
        BatMapApplication batMap = new BatMapApplication();
        batMap.initialize(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}