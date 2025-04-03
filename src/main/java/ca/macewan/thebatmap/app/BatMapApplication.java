package ca.macewan.thebatmap.app;

import ca.macewan.thebatmap.utils.general.DrawOverlay;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
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
    private static final DrawOverlay overlay = new DrawOverlay();

    // Application components
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private ImageView mapView;
    private ImageView overlayView;

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

        // Create a StackPane to hold the map content
        StackPane mainContentStack = new StackPane();
        mainContentStack.getChildren().add(mapContainer);
        contentLayout.setCenter(mainContentStack);

        // Create the left side panel with controls
        VBox leftSideControls = CreateLeftPanel();
        contentLayout.setLeft(leftSideControls);

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
     * Creates the left side control panel with a VBox containing
     * a combo box and filter controls
     * @return A configured VBox for the left side panel
     */
    private VBox CreateLeftPanel() {
        // Create a VBox with spacing between elements
        VBox leftControls = new VBox(10);
        leftControls.setPrefWidth(200);
        leftControls.setPadding(new Insets(10));
        leftControls.setStyle("-fx-background-color: #333333;");

        // Create a title for the panel
        Label panelTitle = new Label("Control Panel");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Create map type selection buttons
        Label mapTypeLabel = new Label("Map Type");
        mapTypeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox mapTypeButtons = new HBox(10);
        Button crimeButton = new Button("Crime");
        Button propertyButton = new Button("Property");

        // Set equal widths for buttons
        crimeButton.setPrefWidth(90);
        propertyButton.setPrefWidth(90);

        mapTypeButtons.getChildren().addAll(crimeButton, propertyButton);

        // Add correlation button
        Button correlationButton = new Button("Crime-Property Correlation");
        correlationButton.setPrefWidth(190);
        correlationButton.setStyle("-fx-background-color: #6200EA; -fx-text-fill: white;");
        Tooltip correlationTooltip = new Tooltip("Show relationship between crime rates and property values");
        Tooltip.install(correlationButton, correlationTooltip);

        // Style selected button
        String selectedStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white;";
        String unselectedStyle = "-fx-background-color: #555555; -fx-text-fill: white;";
        String correlationStyle = "-fx-background-color: #6200EA; -fx-text-fill: white;";
        String correlationSelectedStyle = "-fx-background-color: #3700B3; -fx-text-fill: white;";
        String disabledStyle = "-fx-background-color: #888888; -fx-text-fill: #CCCCCC;";

        // Initial state
        crimeButton.setStyle(selectedStyle);
        propertyButton.setStyle(unselectedStyle);
        String currentMapType = "Crime"; // Default selection
        final boolean[] correlationMode = {false}; // Using array as a mutable container

        Label categoryOrGroupLabel = new Label("Filter Group");
        categoryOrGroupLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label filterLabel = new Label("Filter");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label assessmentClassLabel = new Label("Assessment Class");
        assessmentClassLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Set up ComboBoxes
        String[] categoryOrGroup = overlay.getCategoryOrGroup(currentMapType);
        ComboBox<String> categoryOrGroupComboBox = new ComboBox<>(FXCollections.observableArrayList(categoryOrGroup));
        categoryOrGroupComboBox.setPrefWidth(180);
        categoryOrGroupComboBox.getSelectionModel().selectFirst();

        String[] filter = overlay.getFilters(categoryOrGroupComboBox.getValue());
        ComboBox<String> filterComboBox = new ComboBox<>(FXCollections.observableArrayList(filter));
        filterComboBox.setPrefWidth(180);
        filterComboBox.getSelectionModel().selectFirst();

        String[] assessmentClass = overlay.getAssessmentClass(currentMapType);
        ComboBox<String> assessmentComboBox = new ComboBox<>(FXCollections.observableArrayList(assessmentClass));
        assessmentComboBox.setPrefWidth(180);
        assessmentComboBox.getSelectionModel().selectFirst();

        // Default selections to "None" where applicable
        if (categoryOrGroupComboBox.getItems().contains("None")) {
            categoryOrGroupComboBox.setValue("None");
        }
        if (filterComboBox.getItems().contains("None")) {
            filterComboBox.setValue("None");
        }
        if (assessmentComboBox.getItems().contains("None")) {
            assessmentComboBox.setValue("None");
        }

        // Hide assessment class for Crime mode
        assessmentClassLabel.setVisible(currentMapType.equals("Property"));
        assessmentComboBox.setVisible(currentMapType.equals("Property"));

        // Add button event handlers
        crimeButton.setOnAction(e -> {
            if (correlationMode[0]) {
                // Exit correlation mode
                correlationMode[0] = false;
                correlationButton.setStyle(correlationStyle);
            }

            crimeButton.setStyle(selectedStyle);
            propertyButton.setStyle(unselectedStyle);
            String mapType = "Crime";

            // Enable filter controls
            categoryOrGroupLabel.setDisable(false);
            categoryOrGroupComboBox.setDisable(false);
            filterLabel.setDisable(false);
            filterComboBox.setDisable(false);

            // Update ComboBoxes
            categoryOrGroupComboBox.setItems(FXCollections.observableArrayList(overlay.getCategoryOrGroup(mapType)));

            // Make sure "None" is selected if available
            if (categoryOrGroupComboBox.getItems().contains("None")) {
                categoryOrGroupComboBox.setValue("None");
            } else {
                categoryOrGroupComboBox.getSelectionModel().selectFirst();
            }

            // Update filter items based on selected category
            String[] updatedFilters = overlay.getFilters(categoryOrGroupComboBox.getValue());
            filterComboBox.setItems(FXCollections.observableArrayList(updatedFilters));

            if (filterComboBox.getItems().contains("None")) {
                filterComboBox.setValue("None");
            } else {
                filterComboBox.getSelectionModel().selectFirst();
            }

            assessmentComboBox.setItems(FXCollections.observableArrayList(overlay.getAssessmentClass(mapType)));
            if (assessmentComboBox.getItems().contains("None")) {
                assessmentComboBox.setValue("None");
            } else {
                assessmentComboBox.getSelectionModel().selectFirst();
            }

            // Show/hide assessment class based on map type
            assessmentClassLabel.setVisible(false);
            assessmentComboBox.setVisible(false);
        });

        propertyButton.setOnAction(e -> {
            if (correlationMode[0]) {
                // Exit correlation mode
                correlationMode[0] = false;
                correlationButton.setStyle(correlationStyle);
            }

            propertyButton.setStyle(selectedStyle);
            crimeButton.setStyle(unselectedStyle);
            String mapType = "Property";

            // Enable filter controls
            categoryOrGroupLabel.setDisable(false);
            categoryOrGroupComboBox.setDisable(false);
            filterLabel.setDisable(false);
            filterComboBox.setDisable(false);

            // Update ComboBoxes
            categoryOrGroupComboBox.setItems(FXCollections.observableArrayList(overlay.getCategoryOrGroup(mapType)));

            // Make sure "None" is selected if available
            if (categoryOrGroupComboBox.getItems().contains("None")) {
                categoryOrGroupComboBox.setValue("None");
            } else {
                categoryOrGroupComboBox.getSelectionModel().selectFirst();
            }

            // Update filter items based on selected category
            String[] updatedFilters = overlay.getFilters(categoryOrGroupComboBox.getValue());
            filterComboBox.setItems(FXCollections.observableArrayList(updatedFilters));

            if (filterComboBox.getItems().contains("None")) {
                filterComboBox.setValue("None");
            } else {
                filterComboBox.getSelectionModel().selectFirst();
            }

            assessmentComboBox.setItems(FXCollections.observableArrayList(overlay.getAssessmentClass(mapType)));
            if (assessmentComboBox.getItems().contains("None")) {
                assessmentComboBox.setValue("None");
            } else {
                assessmentComboBox.getSelectionModel().selectFirst();
            }

            // Show/hide assessment class based on map type
            assessmentClassLabel.setVisible(true);
            assessmentComboBox.setVisible(true);
        });

        // Add correlation button event handler
        correlationButton.setOnAction(e -> {
            correlationMode[0] = !correlationMode[0];

            if (correlationMode[0]) {
                // Enter correlation mode
                correlationButton.setStyle(correlationSelectedStyle);
                crimeButton.setStyle(disabledStyle);
                propertyButton.setStyle(disabledStyle);

                // Disable filter controls
                categoryOrGroupLabel.setDisable(true);
                categoryOrGroupComboBox.setDisable(true);
                filterLabel.setDisable(true);
                filterComboBox.setDisable(true);
                assessmentClassLabel.setDisable(true);
                assessmentComboBox.setDisable(true);

                // Generate correlation overlay immediately
                String imagePath = overlay.drawCorrelationImage();
                displayOverlay(imagePath);
            } else {
                // Exit correlation mode
                correlationButton.setStyle(correlationStyle);

                // Restore previous selection
                if (currentMapType.equals("Crime")) {
                    crimeButton.setStyle(selectedStyle);
                    propertyButton.setStyle(unselectedStyle);
                    assessmentClassLabel.setVisible(false);
                    assessmentComboBox.setVisible(false);
                } else {
                    propertyButton.setStyle(selectedStyle);
                    crimeButton.setStyle(unselectedStyle);
                    assessmentClassLabel.setVisible(true);
                    assessmentComboBox.setVisible(true);
                }

                // Re-enable filter controls
                categoryOrGroupLabel.setDisable(false);
                categoryOrGroupComboBox.setDisable(false);
                filterLabel.setDisable(false);
                filterComboBox.setDisable(false);
                assessmentClassLabel.setDisable(false);
                assessmentComboBox.setDisable(false);

                // Clear overlay
                try {
                    StackPane mapContainer = (StackPane) mapView.getParent();
                    mapContainer.getChildren().removeIf(node ->
                            node != mapView && node instanceof ImageView);
                } catch (Exception ex) {
                    System.err.println("Error clearing overlay: " + ex.getMessage());
                }
            }
        });

        // Create buttons for additional actions
        Button applyFilterButton = new Button("Apply Filter");
        Button resetButton = new Button("Reset");

        // Create an HBox to hold the buttons side by side
        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(applyFilterButton, resetButton);

        // Add event handlers for the other controls
        categoryOrGroupComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            filterComboBox.setItems(FXCollections.observableArrayList(overlay.getFilters(newValue)));

            // Make sure "None" is selected if available
            if (filterComboBox.getItems().contains("None")) {
                filterComboBox.setValue("None");
            } else {
                filterComboBox.getSelectionModel().selectFirst();
            }
        });

        applyFilterButton.setOnAction(e -> {
            if (correlationMode[0]) {
                // In correlation mode, just regenerate correlation overlay
                String imagePath = overlay.drawCorrelationImage();
                displayOverlay(imagePath);
            } else {
                // Determine which map type is selected
                String mapType = crimeButton.getStyle().equals(selectedStyle) ? "Crime" : "Property";

                overlay.setMapType(mapType);
                overlay.setCategoryOrGroup(categoryOrGroupComboBox.getValue());
                overlay.setFilter(filterComboBox.getValue());
                overlay.setAssessment(assessmentComboBox.getValue());

                // Add the heat map image
                String imagePath = overlay.drawImage();
                displayOverlay(imagePath);
            }
        });

        // Reset Button functionality
        resetButton.setOnAction(e -> {
            // Exit correlation mode if active
            correlationMode[0] = false;
            correlationButton.setStyle(correlationStyle);

            // Reset button selection (Crime as default)
            crimeButton.setStyle(selectedStyle);
            propertyButton.setStyle(unselectedStyle);

            // Enable filter controls
            categoryOrGroupLabel.setDisable(false);
            categoryOrGroupComboBox.setDisable(false);
            filterLabel.setDisable(false);
            filterComboBox.setDisable(false);
            assessmentClassLabel.setDisable(false);
            assessmentComboBox.setDisable(false);

            // Reset all combo box selections to the first item
            categoryOrGroupComboBox.setItems(FXCollections.observableArrayList(overlay.getCategoryOrGroup("Crime")));

            // Make sure "None" is selected if available
            if (categoryOrGroupComboBox.getItems().contains("None")) {
                categoryOrGroupComboBox.setValue("None");
            } else {
                categoryOrGroupComboBox.getSelectionModel().selectFirst();
            }

            // Update filter options based on selected category
            String[] updatedFilters = overlay.getFilters(categoryOrGroupComboBox.getValue());
            filterComboBox.setItems(FXCollections.observableArrayList(updatedFilters));

            if (filterComboBox.getItems().contains("None")) {
                filterComboBox.setValue("None");
            } else {
                filterComboBox.getSelectionModel().selectFirst();
            }

            // Reset assessment class dropdown
            assessmentComboBox.setItems(FXCollections.observableArrayList(overlay.getAssessmentClass("Crime")));
            if (assessmentComboBox.getItems().contains("None")) {
                assessmentComboBox.setValue("None");
            } else {
                assessmentComboBox.getSelectionModel().selectFirst();
            }

            // Hide assessment controls for Crime mode
            assessmentClassLabel.setVisible(false);
            assessmentComboBox.setVisible(false);

            // Reset the overlay object's internal state
            overlay.setMapType("");
            overlay.setCategoryOrGroup("");
            overlay.setFilter("");
            overlay.setAssessment("");

            // Remove the overlay from the UI
            try {
                // Get the map container
                StackPane mapContainer = (StackPane) mapView.getParent();

                // Remove any overlay views
                mapContainer.getChildren().removeIf(node ->
                        node != mapView && node instanceof ImageView);

                System.out.println("Overlay cleared");
            } catch (Exception ex) {
                System.err.println("Error clearing overlay: " + ex.getMessage());
                ex.printStackTrace();
            }

            System.out.println("Filters reset");
        });

        leftControls.getChildren().addAll(
                panelTitle,
                new Separator(),
                mapTypeLabel,
                mapTypeButtons,
                correlationButton,
                new Separator(),
                categoryOrGroupLabel,
                categoryOrGroupComboBox,
                filterLabel,
                filterComboBox,
                assessmentClassLabel,
                assessmentComboBox,
                buttonContainer
        );

        return leftControls;
    }

    /**
     * Gets crime level information based on pixel color
     */
    private String getCrimeLevelFromColor(javafx.scene.paint.Color color) {
        // RGB values (0-255 scale)
        int red = (int)(color.getRed() * 255);
        int green = (int)(color.getGreen() * 255);
        int blue = (int)(color.getBlue() * 255);

        // Determine crime level based on color
        if (red > 200 && green < 100 && blue < 100) {
            return "High Crime Area (>200 incidents/year)";
        } else if (red > 150 || (red > 100 && green > 150 && green < 200)) {
            return "Medium-High Crime Area (100-200 incidents/year)";
        } else if (green > 150 && red > 100) {
            return "Medium Crime Area (50-100 incidents/year)";
        } else if (blue > 200 && red < 100 && green < 100) {
            return "Low Crime Area (<50 incidents/year)";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "General area";
        }
    }

    /**
     * Gets property information based on pixel color
     */
    private String getPropertyInfoFromColor(javafx.scene.paint.Color color) {
        // RGB values (0-255 scale)
        int red = (int)(color.getRed() * 255);
        int green = (int)(color.getGreen() * 255);
        int blue = (int)(color.getBlue() * 255);

        // Based on your property map color scheme (blue = low density, red = high density)
        if (red > 200 && green < 100 && blue < 100) {
            return "High Density (>500 properties)";
        } else if (red > 150 && green > 100) {
            return "Medium-High Density (300-500 properties)";
        } else if (green > 200 && red < 100) {
            return "Medium Density (100-300 properties)";
        } else if (blue > 200 && red < 100 && green < 100) {
            return "Low Density (<100 properties)";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "Cluster of properties";
        }
    }

    /**
     * Gets correlation information based on pixel color
     */
    private String getCorrelationInfoFromColor(javafx.scene.paint.Color color) {
        // RGB values (0-255 scale)
        int red = (int)(color.getRed() * 255);
        int green = (int)(color.getGreen() * 255);
        int blue = (int)(color.getBlue() * 255);

        // Based on your correlation map color scheme (as described)
        if (blue > 200 && red < 100 && green < 100) {
            return "Positive Correlation\nHigh property value, Low crime rate";
        } else if (green > 200 && red < 100 && blue < 100) {
            return "Values Near Zero\nNo strong correlation between property values and crime";
        } else if (red > 200 && green < 100 && blue < 100) {
            return "Negative Correlation\nLow property value, High crime rate";
        } else if (green > 150 && blue > 150) {
            return "Slightly Positive/Neutral Correlation";
        } else if (red > 150 && green > 150) {
            return "Slightly Negative/Neutral Correlation";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "Mixed correlation";
        }
    }

    /**
     * Displays the overlay image on top of the map
     * @param imagePath Path to the overlay image
     */
    private void displayOverlay(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                System.err.println("Error displaying overlay: No image path provided");
                return;
            }

            // Get the map container
            StackPane mapContainer = (StackPane) mapView.getParent();

            // Remove previous overlay if it exists
            mapContainer.getChildren().removeIf(node ->
                    node != mapView && node instanceof ImageView && "overlay".equals(node.getId()));

            // Create a File object from the path
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Error displaying overlay: File not found at " + imagePath);
                return;
            }

            // Load the image
            String imageUrl = imageFile.toURI().toString();
            Image overlayImage = new Image(imageUrl);

            // Create the overlay ImageView
            ImageView overlayView = new ImageView(overlayImage);
            overlayView.setId("overlay"); // Set an ID to identify it later for removal
            overlayView.setPreserveRatio(true);
            overlayView.fitWidthProperty().bind(mapView.fitWidthProperty());
            overlayView.fitHeightProperty().bind(mapView.fitHeightProperty());

            // Create tooltip for hover information
            Tooltip hoverTooltip = new Tooltip();
            hoverTooltip.setShowDelay(javafx.util.Duration.millis(100));
            hoverTooltip.setShowDuration(javafx.util.Duration.seconds(10));
            hoverTooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.8); " +
                    "-fx-text-fill: white; -fx-padding: 5px;");

            // Determine the current map type
            String currentMapType;
            if (imagePath.contains("correlation")) {
                currentMapType = "Crime-Property Correlation";
            } else {

                if (imagePath.toLowerCase().contains("crime")) {
                    currentMapType = "Crime";
                } else if (imagePath.toLowerCase().contains("property")) {
                    currentMapType = "Property";
                } else {
                    // Default case
                    currentMapType = "Crime"; // Default to Crime if unknown
                }
            }

            // Get pixel reader to detect colors under cursor
            javafx.scene.image.PixelReader pixelReader = overlayImage.getPixelReader();

            // Add mouse movement handler
            overlayView.setOnMouseMoved(event -> {
                // Get pixel coordinates relative to the image view
                double xScale = overlayImage.getWidth() / overlayView.getBoundsInLocal().getWidth();
                double yScale = overlayImage.getHeight() / overlayView.getBoundsInLocal().getHeight();

                int x = (int) (event.getX() * xScale);
                int y = (int) (event.getY() * yScale);

                // Make sure coordinates are within image bounds
                if (x >= 0 && x < overlayImage.getWidth() && y >= 0 && y < overlayImage.getHeight()) {
                    // Read the color at cursor position
                    javafx.scene.paint.Color color = pixelReader.getColor(x, y);

                    // Only show tooltip if the pixel has data (is not transparent)
                    if (color.getOpacity() > 0.1) {
                        // Determine tooltip content based on the current map type
                        String tooltipText;

                        if (currentMapType.equals("Crime-Property Correlation")) {
                            tooltipText = getCorrelationInfoFromColor(color);
                        } else if (currentMapType.equals("Property")) {
                            tooltipText = "Property: " + getPropertyInfoFromColor(color);
                        } else {
                            // Default to Crime type
                            tooltipText = "Crime Level: " + getCrimeLevelFromColor(color);
                        }

                        // Update tooltip text
                        hoverTooltip.setText(tooltipText);

                        // Show tooltip near cursor
                        hoverTooltip.show(overlayView,
                                event.getScreenX() + 15,
                                event.getScreenY() + 15);
                    } else {
                        // Hide tooltip when over transparent areas
                        hoverTooltip.hide();
                    }
                }
            });

            // Hide tooltip when mouse exits the overlay
            overlayView.setOnMouseExited(event -> hoverTooltip.hide());

            // Add the overlay on top of the map
            mapContainer.getChildren().add(overlayView);

            System.out.println("Overlay successfully displayed");

        } catch (Exception e) {
            System.err.println("Error displaying overlay: " + e.getMessage());
            e.printStackTrace();
        }
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