package ca.macewan.thebatmap.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the map image
        Image mapImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ca/macewan/thebatmap/assets/edmonton.png")));
        ImageView mapView = new ImageView(mapImage);
        mapView.setPreserveRatio(true);

        // Create header with dark background and centered text
        Label titleLabel = new Label("The BatMap: Crime Statistics Across Edmonton");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        BorderPane header = new BorderPane();
        header.setCenter(titleLabel);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #333333;");

        // Create main content container
        BorderPane contentLayout = new BorderPane();

        // Create a StackPane to hold the map and any overlays
        StackPane mapContainer = new StackPane();
        mapContainer.getChildren().add(mapView);

        // Loading the fxml
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ca/macewan/thebatmap/views/MainView.fxml"));
            mapContainer.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
        }

        // Create the toggle button (yellow triangle)
        StackPane toggleButton = createToggleButton();
        toggleButton.setOnMouseClicked(e -> {
            // This is where you'll add code to open your side panel later
            System.out.println("Toggle button clicked!");
        });

        // Position the toggle button on the left side
        AnchorPane leftSideLayout = new AnchorPane();
        leftSideLayout.getChildren().add(toggleButton);
        AnchorPane.setTopAnchor(toggleButton, 300.0); // Position vertically in the middle
        AnchorPane.setLeftAnchor(toggleButton, 0.0);  // Stick to the left edge

        // Create a StackPane to hold everything together
        StackPane mainContentStack = new StackPane();
        mainContentStack.getChildren().addAll(mapContainer, leftSideLayout);
        contentLayout.setCenter(mainContentStack);

        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c2c2c;");
        root.setTop(header);
        root.setCenter(contentLayout);

        // Set the scene
        Scene scene = new Scene(root, 1000, 700);

        // Load CSS
        try {
            String cssPath = "/ca/macewan/thebatmap/styles/styles.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }

        // Make map fill available space
        mapView.fitWidthProperty().bind(scene.widthProperty());
        mapView.fitHeightProperty().bind(scene.heightProperty().subtract(header.heightProperty()));

        stage.setTitle("The BatMap");
        stage.setScene(scene);
        stage.show();
    }

    private StackPane createToggleButton() {
        // Create right-pointing triangle arrow shape (since it's on the left)
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(
                0.0, 0.0,    // Top left
                0.0, 20.0,   // Bottom left
                10.0, 10.0   // Right middle
        );
        arrow.setFill(Color.YELLOW);

        // Create button container
        StackPane toggleButton = new StackPane(arrow);
        toggleButton.setPrefWidth(30);
        toggleButton.setPrefHeight(60);
        toggleButton.setStyle("-fx-background-color: #333333; -fx-cursor: hand;");
        toggleButton.setPadding(new Insets(5));

        return toggleButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}