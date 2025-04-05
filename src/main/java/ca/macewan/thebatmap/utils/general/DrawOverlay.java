package ca.macewan.thebatmap.utils.general;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DrawOverlay {
    private static final CalculatePixelValue pixels = new CalculatePixelValue();
    private static final int width = CoordinateToPixel.getMapWidth() + 1;
    private static final int height = CoordinateToPixel.getMapHeight() + 1;
    private static final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private String mapType = "";
    private String categoryOrGroup = "";
    private String filter = "";
    private String assessment = "";
    private final String[] crimeCategoryArray = new String[]{"Category", "Group", "Type", "None"};
    private final String[] propertyCategoryArray = new String[]{"Ward", "Neighbourhood", "None"};
    private final Map<String, String> titleCaseToOriginalMap = new HashMap<>();

    public DrawOverlay() {
        try { pixels.loadData(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void setMapType(String mapType) { this.mapType = mapType; }

    public void setCategoryOrGroup(String categoryOrGroup) { this.categoryOrGroup = categoryOrGroup; }

    public void setFilter(String filter) { this.filter = filter; }

    public void setAssessment(String assessment) { this.assessment = assessment; }

    public String[] getCategoryOrGroup(String newValue) {
        if (newValue.equals("Crime")) { return crimeCategoryArray; }
        else { return propertyCategoryArray; }
    }

    public String[] getFilters(String newValue) {
        Set<String> filterSet = new HashSet<>();
        titleCaseToOriginalMap.clear(); // Clear previous mappings

        // Populate the filter set based on the selected category/group
        switch (newValue) {
            case "Category" -> filterSet = pixels.getCrimeCategories();
            case "Group" -> filterSet = pixels.getCrimeGroups();
            case "Type" -> filterSet = pixels.getCrimeTypes();
            case "Ward" -> filterSet = pixels.getWards();
            case "Neighbourhood" -> filterSet = pixels.getNeighborhoods();
            case null, default -> {}
        }
        return setToArray(filterSet);
    }

    public String[] getAssessmentClass(String newValue) {
        if (newValue.equals("Property")) {
            Set<String> assessmentClasses = pixels.getAssessmentClasses();
            return setToArray(assessmentClasses);
        }
        return new String[]{"None"};
    }

    private String[] setToArray(Set<String> set) {
        // Create the final list with "None" at top
        List<String> result = new ArrayList<>();
        result.add("None");

        // Check for "Other" to add at the end
        boolean hasOther = set.remove("Other");

        // Add remaining items in alphabetical order
        List<String> sortedItems = new ArrayList<>(set);
        Collections.sort(sortedItems);

        // Apply title case to each item and map to original
        for (String item : sortedItems) {
            if (item.equals("None") || item.equals("Other")) {
                result.add(item);
            } else {
                String titleCased = toTitleCase(item);
                result.add(titleCased);
                // Store mapping from title case to original
                titleCaseToOriginalMap.put(titleCased, item);
            }
        }
        if (hasOther) { result.add("Other"); }

        return result.toArray(new String[0]);
    }

    /**
     * Generates and saves a correlation heatmap overlay showing relationship
     * between crime rates and property values
     * @return Path to the generated image file, or null if generation failed
     */
    public String drawCorrelationImage() {
        Map<String, Double> correlationValues = getCorrelationValues();

        if (correlationValues.isEmpty()) {
            System.out.println("No correlation data available");
            return null;
        } else {
            colorImage(correlationValues, "Correlation", 0);

            // Save the image
            String fileName = "correlation_" + System.currentTimeMillis() + ".png";
            return createImageFile(fileName);
        }
    }

    private Map<String, Double> getCorrelationValues() {
        Map<String, Double> correlationValues = new HashMap<>();

        // Calculate correlation values for each pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get property value and crime count for this pixel
                String key = x + "," + y;
                double propertyValue = pixels.getPropertyIntensity(x, y);
                double crimeIntensity = pixels.getCrimeIntensity(x, y);

                // Skip pixels with no data
                if (propertyValue == 0.0 && crimeIntensity == 0.0) { continue; }

                // Calculate correlation:
                // 1 = high property, low crime (blue)
                // 0 = balanced (green)
                // -1 = low property, high crime (red)
                double correlation = propertyValue - crimeIntensity;

                // Only store pixels with significant data
                if (Math.abs(correlation) > 0.05) { correlationValues.put(key, correlation); }
            }
        }
        return correlationValues;
    }

    /**
     * Generates and saves a heat map overlay based on current filter settings
     * @return Path to the generated image file, or null if generation failed
     */
    public String drawImage() {
        // Convert title case filter back to original if needed
        String originalFilter = titleCaseToOriginalMap.getOrDefault(filter, filter);
        String originalAssessment = titleCaseToOriginalMap.getOrDefault(assessment, assessment);

        if (!originalAssessment.equals("None")) { originalAssessment = originalAssessment.toUpperCase(); }

        System.out.println("DEBUG: Using filter: " + filter + " -> " + originalFilter);
        System.out.println("DEBUG: Using assessment: " + assessment + " -> " + originalAssessment);

        Map<String, Double> pixelValues = getPixelValues(originalFilter);

        String safeFilter = replaceSymbols(filter);

        if (pixelValues.isEmpty()) {
            // Create a simple message for "no data" case
            System.out.println("No data matches filter: " + mapType + "_" + categoryOrGroup + "_" + filter + "_" + assessment);
            return null;
        } else {
            List<Double> bounds = detectOutlier(pixelValues);
            colorImage(pixelValues, "notCorrelation", bounds.get(1));

            // Save the image
            String fileName = mapType + "_" + categoryOrGroup + "_" + safeFilter + "_" + assessment + ".png";
            return createImageFile(fileName);
        }
    }

    private Map<String, Double> getPixelValues(String filterValue) {
        Map<String, Double> pixelValues;

        if (mapType.equals("Crime")) {
            pixelValues = getPixelCrimeCount(filterValue);
        }
        else { // mapType.equals("Property")
            pixelValues = getPixelPropertyCount(filterValue);
        }

        System.out.println("Found " + pixelValues.size() + " matching properties for filter: " +
                mapType + "/" + categoryOrGroup + "/" + filterValue + "/" + assessment);

        return pixelValues;
    }

    private Map<String, Double> getPixelCrimeCount(String filterValue) {
        Map<String, Double> pixelValues = new HashMap<>();
        double count;

        for (Map.Entry<String, CalculatePixelValue.CrimePixelData> entry : pixels.getCrimePixels().entrySet()) {
            CalculatePixelValue.CrimePixelData crimeData = entry.getValue();

            count = switch (categoryOrGroup) {
                case "Category" -> crimeData.getCategoryCount(filterValue);
                case "Group" -> crimeData.getGroupCount(filterValue);
                case "Type" -> crimeData.getGroupTypeCount(filterValue);
                default -> crimeData.getCount();
            };
            if (count > 0) pixelValues.put(entry.getKey(), count);
        }

        return pixelValues;
    }

    private Map<String, Double> getPixelPropertyCount(String filterValue) {
        Map<String, Double> pixelValues = new HashMap<>();
        double count;

        for (Map.Entry<String, CalculatePixelValue.PropertyPixelData> entry : pixels.getPropertyPixels().entrySet()) {
            CalculatePixelValue.PropertyPixelData propertyValues = entry.getValue();

            Map<String, Integer> propertyMap = null;
            Map<String, Integer> assessmentMap = null;

            // If category/group is "None", we don't filter by it
            if (!categoryOrGroup.equals("None")) {
                if (categoryOrGroup.equals("Ward")) { propertyMap = propertyValues.getWardCount(); }
                else if (categoryOrGroup.equals("Neighbourhood")) { propertyMap = propertyValues.getNeighborhoodCount(); }
            }

            if (!assessment.equals("None")) { assessmentMap = propertyValues.getAssessmentClassCount(); }

            // When category/group is "None", we don't check propertyMap
            // When filter is "None", we don't check for a specific key
            boolean includeProperty = true;

            if (propertyMap != null && !filterValue.equals("None")) {
                includeProperty = propertyMap.containsKey(filterValue);
            }

            if (assessmentMap != null && !assessment.equals("None")) {
                includeProperty = includeProperty && assessmentMap.containsKey(assessment.toUpperCase());
            }

            if (includeProperty) {
                count = propertyValues.getAverageValue();
                pixelValues.put(entry.getKey(), count);
            }
        }
        return pixelValues;
    }

    /**
     * Convert normalized value into RGB values. Blue (min) -> Green -> Red (max).
     * @param value - Normalized value between 0 - 1
     * @return - Color(r,g,b,a)
     */
    private static Color getColor(double value, double upperBound, String mapType) {
        int r, g, b, a;
        boolean isCorrelation = mapType.equals("Correlation");
        boolean valueThreshold;
        double posRatio;
        double negRatio;

        if (isCorrelation) {
            valueThreshold = value > 0;
            posRatio = Math.min(1.0, value);
            negRatio = Math.min(1.0, -value);
            a = 204;
        } else {
            value = Math.min(1.0, value / upperBound);
            valueThreshold = value < 0.5;
            posRatio = value / 0.5;
            negRatio = (value - 0.5) / 0.5;
            a = 255;
        }

        if (valueThreshold) {
            r = isCorrelation ? 0 : (int) (255 * posRatio);
            g = (int) (255 * posRatio);
            b = (int) (255 * (1 - posRatio));
        } else {
            r = isCorrelation ? (int) (255 * negRatio) : 255;
            g = (int) (255 * (1 - negRatio));
            b = 0;
        }
        return new Color(r, g, b, a);
    }

    private static List<Double> detectOutlier(Map<String, Double> pixelValues) {
        // Sort data
        List<Double> sortedData = pixelValues.values().stream().sorted().collect(Collectors.toList());

        // Compute Q1 and Q3
        //TODO Change thresholds
        double q1 = getPercentile(sortedData, 5); //25
        double q3 = getPercentile(sortedData, 95); //75
        double iqr = q3 - q1;

        // Define thresholds
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        System.out.println("q1: " + q1 + " q3: " + q3 + " iqr: " + iqr);
        System.out.println("Lower bound: " + lowerBound + " Upper bound: " + upperBound);

        List<Double> bounds = new ArrayList<>();
        bounds.add(lowerBound);
        bounds.add(upperBound);
        return bounds;
    }

    private static double getPercentile(List<Double> sortedData, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedData.size()) - 1;
        return sortedData.get(index);
    }

    private void colorImage(Map<String, Double> stringDoubleMap, String mapType, double bound) {
        // Create a fresh image
        Graphics2D g2d = img.createGraphics();

        // Clear the image completely
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Draw each data point with color based on correlation value
        for (Map.Entry<String, Double> entry : stringDoubleMap.entrySet()) {
            double entryValue = entry.getValue();
            Color color = getColor(entryValue, bound, mapType);
            g2d.setColor(color);

            String[] coordinate = entry.getKey().split(",");
            int x = Integer.parseInt(coordinate[0]);
            int y = Integer.parseInt(coordinate[1]);
            g2d.fillRect(x, y, 5, 5); // Draw each data point as a 5x5 pixel rectangle
        }
        g2d.dispose();
    }

    private String createImageFile(String fileName) {
        String outputDir = "src/main/resources/ca/macewan/thebatmap/assets/";

        // Ensure directory exists
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File outputFile = new File(outputDir + fileName);

        try {
            // Write using PNG format which supports transparency
            ImageIO.write(img, "png", outputFile);
            System.out.println("Image created at " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Error creating image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-' || c == '/') {
                nextTitleCase = true;
                titleCase.append(c);
            } else if (nextTitleCase) {
                titleCase.append(Character.toTitleCase(c));
                nextTitleCase = false;
            } else {
                titleCase.append(Character.toLowerCase(c));
            }
        }

        return titleCase.toString();
    }

    private String replaceSymbols(String string) {
        return string.replace('/', '_').replace('\\', '_').replace(':', '_')
                .replace('*', '_').replace('?', '_').replace('"', '_')
                .replace('<', '_').replace('>', '_').replace('|', '_');
    }
}