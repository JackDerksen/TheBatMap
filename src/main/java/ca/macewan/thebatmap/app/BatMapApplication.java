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
    private static final LeftPanelUtils leftPanel = new LeftPanelUtils();

    // Application components
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private ImageView mapView;
    private StackPane mapViewParent;

    private final ComboBox<String> categoryOrGroupComboBox = leftPanel.getCategoryOrGroupComboBox();
    private final ComboBox<String> filterComboBox = leftPanel.getFilterComboBox();
    private final ComboBox<String> assessmentComboBox = leftPanel.getAssessmentComboBox();
    private final VBox legendPanel = leftPanel.createLegend();

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

        mapViewParent = (StackPane) mapView.getParent();

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
        VBox leftControls = leftPanel.getLeftControls();

        // Create a title for the panel
        Label panelTitle = leftPanel.getPanelTitle();

        // Create map type selection buttons
        Label mapTypeLabel = leftPanel.getMapTypeLabel();
        Button crimeButton = leftPanel.getCrimeButton();
        Button propertyButton = leftPanel.getPropertyButton();

        // Create an HBox to hold the buttons side by side
        HBox mapTypeButtons = new HBox(10);
        mapTypeButtons.getChildren().addAll(crimeButton, propertyButton);

        // Add correlation button
        Button correlationButton = leftPanel.getCorrelationButton();

        // Create labels
        Label categoryOrGroupLabel = leftPanel.getCategoryOrGroupLabel();
        Label filterLabel = leftPanel.getFilterLabel();
        Label assessmentClassLabel = leftPanel.getAssessmentClassLabel();

        // Create buttons for additional actions
        Button applyFilterButton = new Button("Create Map");
        Button resetButton = new Button("Reset");

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(applyFilterButton, resetButton);

        // Create a legend panel
        Separator legendSeparator = new Separator();
        legendSeparator.setPadding(new Insets(10, 0, 5, 0));

        // Set up ComboBoxes
        updateComboBoxes("Crime");

        assessmentClassLabel.setVisible(false);
        assessmentComboBox.setVisible(false);

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
                buttonContainer,
                legendSeparator,
                legendPanel
        );

        // Add button event handlers
        crimeButton.setOnAction(_ -> {
            String mapType = "Crime";
            buttonClick(mapType);
        });

        propertyButton.setOnAction(_ -> {
            String mapType = "Property";
            buttonClick(mapType);
        });

        // Add correlation button event handler
        correlationButton.setOnAction(_ -> {
            removeOverlay();
            
            leftPanel.enterCorrelation();

            // Generate correlation overlay immediately
            String imagePath = overlay.drawCorrelationImage();
            displayOverlay(imagePath);

            updateLegend("Crime-Property Correlation");
        });

        // Add event handlers for the other controls
        categoryOrGroupComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            String[] updatedFilters = overlay.getFilters(newValue);
            leftPanel.updateComboBox(filterComboBox, updatedFilters);
        });

        applyFilterButton.setOnAction(_ -> {
            removeOverlay();

            // Determine which map type is selected
            String mapType = crimeButton.getStyle().equals(leftPanel.getSelectedStyle()) ? "Crime" : "Property";

            overlay.setMapType(mapType);
            overlay.setCategoryOrGroup(categoryOrGroupComboBox.getValue());
            overlay.setFilter(filterComboBox.getValue());
            overlay.setAssessment(assessmentComboBox.getValue());

            // Add the heat map image
            String imagePath = overlay.drawImage();
            displayOverlay(imagePath);
        });

        // Reset Button functionality
        resetButton.setOnAction(_ -> {
            // Default to crime button
            crimeButton.fire();

            // Reset the overlay object's internal state
            overlay.setMapType("");
            overlay.setCategoryOrGroup("");
            overlay.setFilter("");
            overlay.setAssessment("");

            // Remove the overlay from the UI
            removeOverlay();

            System.out.println("Filters reset");
        });

        return leftControls;
    }

    private void updateComboBoxes(String mapType) {
        // Update ComboBoxes
        String[] updatedCategoryOrGroup = overlay.getCategoryOrGroup(mapType);
        leftPanel.updateComboBox(categoryOrGroupComboBox, updatedCategoryOrGroup);

        // Update filter items based on selected category
        String[] updatedFilters = overlay.getFilters(categoryOrGroupComboBox.getValue());
        leftPanel.updateComboBox(filterComboBox, updatedFilters);

        String[] updatedAssessmentClass = overlay.getAssessmentClass(mapType);
        leftPanel.updateComboBox(assessmentComboBox, updatedAssessmentClass);

        updateLegend(mapType);
    }

    private void updateLegend(String mapType) {
        VBox legendItems = (VBox) legendPanel.getUserData();
        leftPanel.updateLegendItems(legendItems, mapType);
    }

    private void buttonClick(String mapType) {
        leftPanel.exitCorrelation();
        leftPanel.buttonClick(mapType);
        updateComboBoxes(mapType);
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
    
    private void removeOverlay() {
        mapViewParent.getChildren().removeIf(node ->
                node != mapView && node instanceof ImageView && "overlay".equals(node.getId())
        );
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

            // Create a File object from the path
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Error displaying overlay: File not found at " + imagePath);
                return;
            }

            removeOverlay();
            
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
            mapViewParent.getChildren().add(overlayView);

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
}