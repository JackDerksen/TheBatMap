package ca.macewan.thebatmap.app;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class LeftPanelUtils {
    private final VBox leftControls = new VBox(10);

    // Labels
    private final Label panelTitle = new Label("Control Panel");
    private final Label mapTypeLabel = new Label("Map Type");
    private final Label categoryOrGroupLabel = new Label("Filter Group");
    private final Label filterLabel = new Label("Filter");
    private final Label assessmentClassLabel = new Label("Assessment Class");

    // Buttons
    private final Button crimeButton = new Button("Crime");
    private final Button propertyButton = new Button("Property");
    private final Button correlationButton = new Button("Crime-Property Correlation");

    // Combo Boxes
    private final ComboBox<String> categoryOrGroupComboBox = createComboBox();
    private final ComboBox<String> filterComboBox = createComboBox();
    private final ComboBox<String> assessmentComboBox = createComboBox();

    // Style selected button
    private final String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;";
    private final String selectedStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white;";
    private final String unselectedStyle = "-fx-background-color: #555555; -fx-text-fill: white;";

    public LeftPanelUtils() {
        setLeftControls();
        setPanelTitle();
        setLabelStyles();
        setButton(crimeButton);
        setButton(propertyButton);
        setCorrelationButton();
    }

    public VBox getLeftControls() {
        return leftControls;
    }

    public Label getPanelTitle() {
        return panelTitle;
    }

    public Label getMapTypeLabel() {
        return mapTypeLabel;
    }

    public Button getCrimeButton() {
        return crimeButton;
    }

    public Button getPropertyButton() {
        return propertyButton;
    }

    public Button getCorrelationButton() {
        return correlationButton;
    }

    public Label getCategoryOrGroupLabel() {
        return categoryOrGroupLabel;
    }

    public Label getFilterLabel() {
        return filterLabel;
    }

    public Label getAssessmentClassLabel() {
        return assessmentClassLabel;
    }

    public ComboBox<String> getCategoryOrGroupComboBox() {
        return categoryOrGroupComboBox;
    }

    public ComboBox<String> getFilterComboBox() {
        return filterComboBox;
    }

    public ComboBox<String> getAssessmentComboBox() {
        return assessmentComboBox;
    }

    public String getSelectedStyle() {
        return selectedStyle;
    }

    private void setLeftControls() {
        leftControls.setPrefWidth(220);
        leftControls.setPadding(new Insets(10));
        leftControls.setStyle("-fx-background-color: #333333;");
    }

    private void setPanelTitle() {
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
    }

    private void setLabelStyles() {
        mapTypeLabel.setStyle(labelStyle);
        categoryOrGroupLabel.setStyle(labelStyle);
        filterLabel.setStyle(labelStyle);
        assessmentClassLabel.setStyle(labelStyle);
    }

    private void setButton(Button button) {
        button.setPrefWidth(100);
        button.setStyle(button.getText().equals("Crime") ? selectedStyle : unselectedStyle);
    }

    private void setCorrelationButton() {
        correlationButton.setPrefWidth(200);
        correlationButton.setStyle("-fx-background-color: #6200EA; -fx-text-fill: white;");
        Tooltip correlationTooltip = new Tooltip("Show relationship between crime rates and property values");
        Tooltip.install(correlationButton, correlationTooltip);
    }

    public ComboBox<String> createComboBox() {
        ComboBox<String> returnComboBox = new ComboBox<>();
        returnComboBox.setPrefWidth(200);
        returnComboBox.getSelectionModel().selectFirst();
        return returnComboBox;
    }

    public void updateComboBox(ComboBox<String> comboBox, String[] input) {
        comboBox.setItems(FXCollections.observableArrayList(input));
        setNoneOrFirst(comboBox);
    }

    public void setNoneOrFirst(ComboBox<String> comboBox) {
        if (comboBox.getItems().contains("None")) {
            comboBox.setValue("None");
        } else {
            comboBox.getSelectionModel().selectFirst();
        }
    }

    public void exitCorrelation() {
        correlationButton.setStyle("-fx-background-color: #6200EA; -fx-text-fill: white;");
        enableButtons();
    }

    public void enterCorrelation() {
        correlationButton.setStyle("-fx-background-color: #3700B3; -fx-text-fill: white;");
        disableButtons();
    }

    public void buttonClick(String mapType) {
        exitCorrelation();

        boolean isProperty = mapType.equals("Property");
        Button unselectButton = isProperty ? crimeButton : propertyButton;

        unselectButton.setStyle(unselectedStyle);
        assessmentClassLabel.setVisible(isProperty);
        assessmentComboBox.setVisible(isProperty);
    }

    private void disableButtons() {
        String disabledStyle = "-fx-background-color: #888888; -fx-text-fill: #CCCCCC;";
        crimeButton.setStyle(disabledStyle);
        propertyButton.setStyle(disabledStyle);

        categoryOrGroupLabel.setDisable(true);
        categoryOrGroupComboBox.setDisable(true);
        filterLabel.setDisable(true);
        filterComboBox.setDisable(true);
        assessmentClassLabel.setDisable(true);
        assessmentComboBox.setDisable(true);
    }

    private void enableButtons() {
        crimeButton.setStyle(selectedStyle);
        propertyButton.setStyle(selectedStyle);

        categoryOrGroupLabel.setDisable(false);
        categoryOrGroupComboBox.setDisable(false);
        filterLabel.setDisable(false);
        filterComboBox.setDisable(false);
        assessmentClassLabel.setDisable(false);
        assessmentComboBox.setDisable(false);
    }

    /**
     * Creates a legend panel that updates based on the current map type
     * @return VBox containing the legend
     */
    public VBox createLegend() {
        VBox legendPanel = new VBox(8);
        legendPanel.setPadding(new Insets(10));
        legendPanel.setStyle("-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 1;");

        Label legendTitle = new Label("Map Legend");
        legendTitle.setStyle(labelStyle);

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
    public void updateLegendItems(VBox legendItems, String mapType) {
        legendItems.getChildren().clear();

        switch (mapType) {
            case "Crime" ->
                // Create legend items for crime map
                    legendItems.getChildren().addAll(
                            createLegendItem("High Crime", Color.rgb(220, 50, 50), ">50 incidents/year"),
                            createLegendItem("Medium-High Crime", Color.rgb(220, 150, 50), "35-50 incidents/year"),
                            createLegendItem("Medium Crime", Color.rgb(150, 220, 50), "20-35 incidents/year"),
                            createLegendItem("Low Crime", Color.rgb(50, 50, 220), "<20 incidents/year")
                    );
            case "Property" ->
                // Create legend items for property map
                    legendItems.getChildren().addAll(
                            createLegendItem("High Value", Color.rgb(220, 50, 50), ">$1.5M"),
                            createLegendItem("Medium-High Value", Color.rgb(220, 150, 50), "$1M-$1.5M"),
                            createLegendItem("Medium Value", Color.rgb(50, 220, 50), "$500K-$1M"),
                            createLegendItem("Low Value", Color.rgb(50, 50, 220), "<$500K")
                    );
            case "Crime-Property Correlation" ->
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
    public HBox createLegendItem(String label, Color color, String description) {
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
