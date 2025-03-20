package ca.macewan.thebatmap.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Load the map image
        Image mapImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ca/macewan/thebatmap/assets/edmonton.png")));
        ImageView mapView = new ImageView(mapImage);

        // Get image dimensions
        double imageWidth = mapImage.getWidth();
        double imageHeight = mapImage.getHeight();

        // Ensure minimum window dimensions (just in case)
        double sceneWidth = Math.max(imageWidth, 800);
        double sceneHeight = Math.max(imageHeight, 600);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/ca/macewan/thebatmap/views/MainView.fxml"));

        // Create a StackPane to layer content
        StackPane root = new StackPane();
        root.getChildren().add(mapView);  // Add map as bottom layer for the background of the application
        root.getChildren().add(fxmlLoader.load());  // Add your FXML content on top of the map

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        stage.setTitle("The BatMap");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}