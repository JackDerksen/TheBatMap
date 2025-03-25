package ca.macewan.thebatmap.app;

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

public class BatMapApplication {
    // Constants
    private static final int WIN_WIDTH = 1000;
    private static final int WIN_HEIGHT = 700;
    private static final String TITLE = "The BatMap: Crime Statistics Across Edmonton";
    private static final String CITY_MAP_PATH = "/ca/macewan/thebatmap/assets/edmonton.png";
    private static final String VIEW_FILE_PATH = "/ca/macewan/thebatmap/views/MainView.fxml";
    private static final String CSS_FILE_PATH = "/ca/macewan/thebatmap/styles/MainStyle.css";

    // Application components
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private ImageView mapView;

    /**
     * Initialize and start the application
     * @param primaryStage The primary stage provided by JavaFX
     * @throws IOException If resources cannot be loaded
     */
    public void initialize(Stage primaryStage) throws IOException {
        this.stage = primaryStage;

        // Create the UI components
        root = new BorderPane();
        root.setStyle("-fx-background-color: #2c2c2c;");

        // Set up the header
        BorderPane header = createHeader();
        root.setTop(header);

        // Set up the main content
        BorderPane contentLayout = createContentLayout();
        root.setCenter(contentLayout);

        // Set up the scene
        scene = new Scene(root, WIN_WIDTH, WIN_HEIGHT);
        loadStylesheet();

        // Make map fill available space
        mapView.fitWidthProperty().bind(scene.widthProperty());
        mapView.fitHeightProperty().bind(scene.heightProperty().subtract(header.heightProperty()));

        // Configure and show the stage
        stage.setTitle("The BatMap");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates the header with title
     * @return A configured BorderPane for the header
     */
    private BorderPane createHeader() {
        Label titleLabel = new Label(TITLE);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        BorderPane header = new BorderPane();
        header.setCenter(titleLabel);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #333333;");

        return header;
    }

    /**
     * Creates the main content layout
     * @return A configured BorderPane for the content
     * @throws IOException If the FXML file cannot be loaded
     */
    private BorderPane createContentLayout() throws IOException {
        BorderPane contentLayout = new BorderPane();

        // Load the map image
        Image mapImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(CITY_MAP_PATH)));
        mapView = new ImageView(mapImage);
        mapView.setPreserveRatio(true);

        // Create a StackPane to hold the map and any overlays
        StackPane mapContainer = new StackPane();
        mapContainer.getChildren().add(mapView);

        // Load the FXML
        loadFXML(mapContainer);

        // Create the toggle button
        StackPane toggleButton = createToggleButton();
        AnchorPane leftSideLayout = positionToggleButton(toggleButton);

        // Create a StackPane to hold everything together
        StackPane mainContentStack = new StackPane();
        mainContentStack.getChildren().addAll(mapContainer, leftSideLayout);
        contentLayout.setCenter(mainContentStack);

        return contentLayout;
    }

    /**
     * Loads the FXML file and adds it to the container
     * @param container The container to add the FXML content to
     */
    private void loadFXML(StackPane container) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_FILE_PATH));
            container.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
    }

    /**
     * Creates a toggle button with a triangle shape
     * @return A configured StackPane containing the toggle button
     */
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

        // Add click handler
        toggleButton.setOnMouseClicked(e -> {
            // Add code here for button functionality
            System.out.println("Toggle button clicked!");
        });

        return toggleButton;
    }

    /**
     * Positions the toggle button on the left side of the screen
     * @param toggleButton The button to position
     * @return An AnchorPane containing the positioned button
     */
    private AnchorPane positionToggleButton(StackPane toggleButton) {
        AnchorPane leftSideLayout = new AnchorPane();
        leftSideLayout.getChildren().add(toggleButton);
        AnchorPane.setTopAnchor(toggleButton, 300.0); // Position vertically in the middle
        AnchorPane.setLeftAnchor(toggleButton, 0.0);  // Stick to the left edge

        return leftSideLayout;
    }

    /**
     * Loads the CSS stylesheet
     */
    private void loadStylesheet() {
        try {
            if (getClass().getResource(CSS_FILE_PATH) != null) {
                scene.getStylesheets().add(getClass().getResource(CSS_FILE_PATH).toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }
    }
}