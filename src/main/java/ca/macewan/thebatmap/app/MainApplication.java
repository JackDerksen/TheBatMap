package ca.macewan.thebatmap.app;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Create and initialize the BatMap application
        BatMapApplication batMap = new BatMapApplication();
        batMap.initialize(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}