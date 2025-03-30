package ca.macewan.thebatmap.app;

import ca.macewan.thebatmap.utils.general.DrawOverlay;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private static final DrawOverlay overlay = new DrawOverlay();

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

        // Create a label for the filter
        Label filterLabel = new Label("Filter Crime Types");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        String[] mapTypeArray = overlay.getMapTypeArray();
        ComboBox<String> mapTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(mapTypeArray));
        mapTypeComboBox.setPrefWidth(180);
        mapTypeComboBox.getSelectionModel().selectFirst();

        String[] categoryOrGroup = overlay.getCategoryOrGroup(mapTypeComboBox.getValue());
        ComboBox<String> categoryOrGroupComboBox = new ComboBox<>(FXCollections.observableArrayList(categoryOrGroup));
        categoryOrGroupComboBox.setPrefWidth(180);
        categoryOrGroupComboBox.getSelectionModel().selectFirst();

        String[] filter = overlay.getFilters(categoryOrGroupComboBox.getValue());
        ComboBox<String> filterComboBox = new ComboBox<>(FXCollections.observableArrayList(filter));
        filterComboBox.setPrefWidth(180);
        filterComboBox.getSelectionModel().selectFirst();

        String[] assessmentClass = overlay.getAssessmentClass(mapTypeComboBox.getValue());
        ComboBox<String> assessmentComboBox = new ComboBox<>(FXCollections.observableArrayList(assessmentClass));
        assessmentComboBox.setPrefWidth(180);
        assessmentComboBox.getSelectionModel().selectFirst();

        // Create a combo box for crime types
//        String[] crimeTypes = {"All", "Theft Under $5000", "Theft Over $5000", "Theft of Motor Vehicle",
//                "Assault", "Break and Enter Residential", "Break and Enter Commercial",
//                "Drugs", "Robbery Personal", "Robbery Commercial", "Fraud - Financial",
//                "Fraud Personal", "Fraud General", "Internet Fraud", "Mischief - Property",
//                "Weapons Complaint", "Weapons Complaint Firearm", "Criminal Flight Event",
//                "Impaired Driving", "Homicide", "Graffiti", "Trespassing", "Possession Stolen Property",
//                "Fire Arson", "Recovered Motor Vehicle", "Trouble with Person", "Dispute",
//                "Disturbance", "Suspicious Person", "Suspicious Vehicle", "Intoxicated Person",
//                "Liquor Act", "Counterfeit Money", "Indecent Act", "Public Mischief",
//                "Property Damage", "Abandoned Vehicle", "Dangerous Condition", "Bomb Threat",
//                "Technology/Internet Crime", "Labour Dispute", "Workplace Accident", "Public Health Act"};
//        ComboBox<String> crimeTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(crimeTypes));
//        crimeTypeComboBox.setPrefWidth(180);
//        crimeTypeComboBox.getSelectionModel().selectFirst(); // Select "All" by default

        // Create buttons for additional actions
        Button applyFilterButton = new Button("Apply Filter");
        Button resetButton = new Button("Reset");

        // Create an HBox to hold the buttons side by side
        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(applyFilterButton, resetButton);

        // Add event handlers
        mapTypeComboBox.getSelectionModel().selectedItemProperty().addListener((_, _,
                                                                                newValue) -> {
            categoryOrGroupComboBox.setItems(FXCollections.observableArrayList(overlay.getCategoryOrGroup(newValue)));
            categoryOrGroupComboBox.getSelectionModel().selectFirst();

            assessmentComboBox.setItems(FXCollections.observableArrayList(overlay.getAssessmentClass(newValue)));
            assessmentComboBox.getSelectionModel().selectFirst();
        });

        categoryOrGroupComboBox.getSelectionModel().selectedItemProperty().addListener((_, _,
                                                                                newValue) -> {
            filterComboBox.setItems(FXCollections.observableArrayList(overlay.getFilters(newValue)));
            filterComboBox.getSelectionModel().selectFirst();
        });

        applyFilterButton.setOnAction(e -> {
            //String selectedType = crimeTypeComboBox.getValue();
            //System.out.println("Filtering by crime type: " + selectedType);
            // Add your filtering logic here
            String mapType = mapTypeComboBox.getValue();
            overlay.setMapType(mapType);
            overlay.setCategoryOrGroup(categoryOrGroupComboBox.getValue());
            overlay.setFilter(filterComboBox.getValue());
            if (mapType.equals("Crime")) {
                overlay.setAssessment("");
            }
            else {
                overlay.setAssessment(assessmentComboBox.getValue());
            }
            overlay.drawImage();
        });

        resetButton.setOnAction(e -> {
            //crimeTypeComboBox.getSelectionModel().selectFirst();
            mapTypeComboBox.getSelectionModel().selectFirst();
            categoryOrGroupComboBox.getSelectionModel().selectFirst();
            filterComboBox.getSelectionModel().selectFirst();
            assessmentComboBox.getSelectionModel().selectFirst();
            System.out.println("Filters reset");
            // Add your reset logic here
        });

        // Add all components to the VBox
        leftControls.getChildren().addAll(
                panelTitle,
                new Separator(), // Add a separator for visual distinction
                filterLabel,
                mapTypeComboBox,
                categoryOrGroupComboBox,
                filterComboBox,
                assessmentComboBox,
                //crimeTypeComboBox,
                buttonContainer
        );

        return leftControls;
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