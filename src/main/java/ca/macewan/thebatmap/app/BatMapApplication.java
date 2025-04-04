package ca.macewan.thebatmap.app;

import ca.macewan.thebatmap.utils.general.DrawOverlay;
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
     */
    public void initialize(Stage primaryStage) {
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
     */
    private BorderPane createContentLayout() {
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
        leftControls.setPrefWidth(220);
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
        crimeButton.setPrefWidth(100);
        propertyButton.setPrefWidth(100);

        mapTypeButtons.getChildren().addAll(crimeButton, propertyButton);

        // Add correlation button
        Button correlationButton = new Button("Crime-Property Correlation");
        correlationButton.setPrefWidth(200);
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
        categoryOrGroupComboBox.setPrefWidth(200);
        categoryOrGroupComboBox.getSelectionModel().selectFirst();

        String[] filter = overlay.getFilters(categoryOrGroupComboBox.getValue());
        ComboBox<String> filterComboBox = new ComboBox<>(FXCollections.observableArrayList(filter));
        filterComboBox.setPrefWidth(200);
        filterComboBox.getSelectionModel().selectFirst();

        String[] assessmentClass = overlay.getAssessmentClass(currentMapType);
        ComboBox<String> assessmentComboBox = new ComboBox<>(FXCollections.observableArrayList(assessmentClass));
        assessmentComboBox.setPrefWidth(200);
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

        // Create a legend panel
        VBox legendPanel = createLegend();

        // Create buttons for additional actions
        Button applyFilterButton = new Button("Create Map");
        Button resetButton = new Button("Reset");

        // Create an HBox to hold the buttons side by side
        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(applyFilterButton, resetButton);

        // Add the legend to left controls panel
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

        // Add legend components
        Separator legendSeparator = new Separator();
        legendSeparator.setPadding(new Insets(10, 0, 5, 0));
        leftControls.getChildren().addAll(legendSeparator, legendPanel);

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

            // Update legend
            VBox legendItems = (VBox) legendPanel.getUserData();
            updateLegendItems(legendItems, "Crime");
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

            // Update legend
            VBox legendItems = (VBox) legendPanel.getUserData();
            updateLegendItems(legendItems, "Property");
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

                // Update legend
                VBox legendItems = (VBox) legendPanel.getUserData();
                updateLegendItems(legendItems, "Crime-Property Correlation");
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
                // Restore legend based on previous selection
                VBox legendItems = (VBox) legendPanel.getUserData();
                String mapType = crimeButton.getStyle().equals(selectedStyle) ? "Crime" : "Property";
                updateLegendItems(legendItems, mapType);
            }
        });


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

            // Reset legend to crime (default)
            VBox legendItems = (VBox) legendPanel.getUserData();
            updateLegendItems(legendItems, "Crime");
        });

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
            return "High Crime Area (>50 incidents/year)\nPotential safety concern";
        } else if (red > 150 || (red > 100 && green > 150 && green < 200)) {
            return "Medium-High Crime Area (35-50 incidents/year)";
        } else if (green > 150 && red > 100) {
            return "Medium Crime Area (20-35 incidents/year)";
        } else if (blue > 200 && red < 100 && green < 100) {
            return "Low Crime Area (<20 incidents/year)\nGenerally safe neighborhood";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "General area - Click 'Apply Filter' to see more specific crime data";
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

        // Based on property map color scheme
        if (red > 200 && green < 100 && blue < 100) {
            return "High Property Value (>$1.5M)\nTypically high-end neighborhoods";
        } else if (red > 150 && green > 100) {
            return "Medium-High Property Value ($1M-$1.5M)";
        } else if (green > 200 && red < 100) {
            return "Medium Property Value ($500K-$1M)";
        } else if (blue > 200 && red < 100 && green < 100) {
            return "Low Property Value (<$500K)\nMore affordable housing options";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "Mixed property values - Apply filters for more specific information";
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

        // Based on the actual implementation in DrawOverlay.getCorrelationColor
        if (green > 200 && blue < 100 && red < 100) {
            return "Strong Positive Correlation\nHigh property value, low crime rate\nTypically desirable neighborhoods";
        } else if (green > 150 && blue < 150 && red < 150) {
            return "Moderate Positive Correlation\nAbove average property value, below average crime";
        } else if (red > 200 && green < 100 && blue < 100) {
            return "Strong Negative Correlation\nLow property value, high crime rate\nPotentially concerning areas";
        } else if (red > 150 && green < 150 && blue < 150) {
            return "Moderate Negative Correlation\nBelow average property value, above average crime";
        } else if (green > 150 && blue > 150) {
            return "Slightly Positive/Neutral Correlation\nBalanced property values and crime rates";
        } else if (red > 150 && green > 150) {
            return "Slightly Negative/Neutral Correlation\nMixed indicators";
        } else if (color.getBrightness() < 0.3) {
            return "Unknown/No Data";
        } else {
            // Default case
            return "Neutral correlation - No strong pattern between property values and crime rates";
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
            } else if (imagePath.toLowerCase().contains("crime")) {
                currentMapType = "Crime";
            } else if (imagePath.toLowerCase().contains("property")) {
                currentMapType = "Property";
            } else {
                // Default case
                currentMapType = "Crime"; // Default to Crime if unknown
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
                        hoverTooltip.show(overlayView, event.getScreenX() + 15, event.getScreenY() + 15);
                    } else {
                        // Hide tooltip when over transparent areas
                        hoverTooltip.hide();
                    }
                }
            });

            // Hide tooltip when mouse exits the overlay
            overlayView.setOnMouseExited(_ -> hoverTooltip.hide());

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
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS_FILE_PATH)).toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }
    }

    /**
     * Creates a legend panel that updates based on the current map type
     * @return VBox containing the legend
     */
    private VBox createLegend() {
        VBox legendPanel = new VBox(8);
        legendPanel.setPadding(new Insets(10));
        legendPanel.setStyle("-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 1;");

        Label legendTitle = new Label("Map Legend");
        legendTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Container for legend items that will be updated
        VBox legendItems = new VBox(5);

        // Initially populate with crime legend (default)
        updateLegendItems(legendItems, "Crime");

        legendPanel.getChildren().addAll(legendTitle, new Separator(), legendItems);

        // Store reference to legendItems for updates
        legendPanel.setUserData(legendItems);

        return legendPanel;
    }

    /**
     * Updates the legend items based on the map type
     * @param legendItems VBox containing legend items
     * @param mapType The current map type (Crime, Property, or Crime-Property Correlation)
     */
    private void updateLegendItems(VBox legendItems, String mapType) {
        legendItems.getChildren().clear();

        if (mapType.equals("Crime")) {
            // Create legend items for crime map
            legendItems.getChildren().addAll(
                    createLegendItem("High Crime", Color.rgb(220, 50, 50), ">50 incidents/year"),
                    createLegendItem("Medium-High Crime", Color.rgb(220, 150, 50), "35-50 incidents/year"),
                    createLegendItem("Medium Crime", Color.rgb(150, 220, 50), "20-35 incidents/year"),
                    createLegendItem("Low Crime", Color.rgb(50, 50, 220), "<20 incidents/year")
            );
        }
        else if (mapType.equals("Property")) {
            // Create legend items for property map
            legendItems.getChildren().addAll(
                    createLegendItem("High Value", Color.rgb(220, 50, 50), ">$1.5M"),
                    createLegendItem("Medium-High Value", Color.rgb(220, 150, 50), "$1M-$1.5M"),
                    createLegendItem("Medium Value", Color.rgb(50, 220, 50), "$500K-$1M"),
                    createLegendItem("Low Value", Color.rgb(50, 50, 220), "<$500K")
            );
        }
        else if (mapType.equals("Crime-Property Correlation")) {
            // Create legend items for correlation map
            legendItems.getChildren().addAll(
                    createLegendItem("Strong Positive", Color.rgb(0, 255, 0), "High property value, low crime"),
                    createLegendItem("Moderate Positive", Color.rgb(100, 255, 100), "Above avg. property, below avg. crime"),
                    createLegendItem("Neutral", Color.rgb(50, 50, 220), "No strong correlation"),
                    createLegendItem("Moderate Negative", Color.rgb(255, 100, 0), "Below avg. property, above avg. crime"),
                    createLegendItem("Strong Negative", Color.rgb(255, 0, 0), "Low property value, high crime")
            );
        }
    }

    /**
     * Creates a single legend item with color box and description
     * @param label Text label for the legend item
     * @param color Color for the legend item box
     * @param description Optional description text
     * @return HBox containing the legend item
     */
    private HBox createLegendItem(String label, Color color, String description) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        // Create color box
        Rectangle colorBox = new Rectangle(15, 15);
        colorBox.setFill(color);
        colorBox.setStroke(Color.WHITE);
        colorBox.setStrokeWidth(0.5);

        // Create VBox for label and description
        VBox textBox = new VBox(2);
        Label itemLabel = new Label(label);
        itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        textBox.getChildren().add(itemLabel);

        // Add description if provided
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setStyle("-fx-text-fill: #BBBBBB; -fx-font-size: 10px;");
            textBox.getChildren().add(descLabel);
        }

        item.getChildren().addAll(colorBox, textBox);
        return item;
    }
}