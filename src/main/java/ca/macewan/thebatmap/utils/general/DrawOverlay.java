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
        if (newValue.equals("Crime")) {
            return crimeCategoryArray;
        } else {
            return propertyCategoryArray;
        }
    }

    public String[] getFilters(String newValue) {
        Set<String> filterSet = new LinkedHashSet<>();
        titleCaseToOriginalMap.clear(); // Clear previous mappings

        // Populate the filter set based on the selected category/group
        switch (newValue) {
            case "Category" -> filterSet = pixels.getCrimeCategories();
            case "Group" -> filterSet = pixels.getCrimeGroups();
            case "Type" -> filterSet = pixels.getCrimeTypes();
            case "Ward" -> filterSet = pixels.getWards();
            case "Neighbourhood" -> filterSet = pixels.getNeighborhoods();
            case null, default -> filterSet.add("None");
        }

        // Create the final list with "None" at top
        List<String> result = new ArrayList<>();

        // Always add "None" at the top
        result.add("None"); // Always include None at the top

        // Check for "Other" to add at the end
        boolean hasOther = filterSet.remove("Other");

        // Add remaining items in alphabetical order
        List<String> sortedItems = new ArrayList<>(filterSet);
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

        // Add "Other" at the end if it existed
        if (hasOther) {
            result.add("Other");
        }

        return result.toArray(new String[0]);
    }

    public String[] getAssessmentClass(String newValue) {
        if (newValue.equals("Property")) {
            Set<String> assessmentClasses = pixels.getAssessmentClasses();

            // Special handling for Other
            boolean hasOther = assessmentClasses.remove("Other");

            // Convert to list for sorting
            List<String> sortedList = new ArrayList<>(assessmentClasses);
            Collections.sort(sortedList);

            // Create final list with special values in special positions
            List<String> finalList = new ArrayList<>();
            finalList.add("None"); // Always have None at the top

            // Apply title case to each entry and map to original
            for (String item : sortedList) {
                // Keep "None" and "Other" as is
                if (item.equals("None") || item.equals("Other")) {
                    finalList.add(item);
                } else {
                    String titleCased = toTitleCase(item);
                    finalList.add(titleCased);
                    // Store mapping from title case to original
                    titleCaseToOriginalMap.put(titleCased, item);
                }
            }

            if (hasOther) {
                finalList.add("Other");
            }

            return finalList.toArray(new String[0]);
        }
        return new String[]{"None"};
    }

    /**
     * Generates and saves a correlation heatmap overlay showing relationship
     * between crime rates and property values
     * @return Path to the generated image file, or null if generation failed
     */
    public String drawCorrelationImage() {
        Map<String, Double> correlationValues = new HashMap<>();

        // Calculate correlation values for each pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                String key = x + "," + y;

                // Get property value and crime count for this pixel
                double propertyValue = pixels.getPropertyIntensity(x, y);
                double crimeIntensity = pixels.getCrimeIntensity(x, y);

                // Skip pixels with no data
                if (propertyValue == 0.0 && crimeIntensity == 0.0) {
                    continue;
                }

                // Calculate correlation:
                // 1 = high property, low crime (blue)
                // 0 = balanced (green)
                // -1 = low property, high crime (red)
                double correlation = propertyValue - crimeIntensity;

                // Only store pixels with significant data
                if (Math.abs(correlation) > 0.05) {
                    correlationValues.put(key, correlation);
                }
            }
        }

        if (!correlationValues.isEmpty()) {
            // Create a fresh image
            Graphics2D g2d = img.createGraphics();

            // Clear the image completely
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, width, height);
            g2d.setComposite(AlphaComposite.SrcOver);

            // Draw each data point with color based on correlation value
            for (Map.Entry<String, Double> entry : correlationValues.entrySet()) {
                Color color = getCorrelationColor(entry.getValue());
                g2d.setColor(color);

                String[] coordinate = entry.getKey().split(",");
                int x = Integer.parseInt(coordinate[0]);
                int y = Integer.parseInt(coordinate[1]);
                g2d.fillRect(x, y, 5, 5); // Draw each data point as a 5x5 pixel rectangle
            }

            g2d.dispose();

            // Save the image
            String fileName = "correlation_" + System.currentTimeMillis() + ".png";
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
                System.out.println("Correlation image created at " + outputFile.getAbsolutePath());
                return outputFile.getAbsolutePath();
            } catch (IOException e) {
                System.err.println("Error creating correlation image: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("No correlation data available");
            return null;
        }
    }

    /**
     * Gets a color representing the correlation value
     * @param value Correlation value from -1 to 1
     * @return Color representing the correlation
     */
    private Color getCorrelationColor(double value) {
        int r, g, b;

        // Positive correlation (high property value, low crime) = blue
        // Negative correlation (low property value, high crime) = red
        // Values near zero = green
        if (value > 0) {
            // Positive correlation (0 to 1): Green to Blue
            double ratio = Math.min(1.0, value);
            r = 0;
            g = (int)(255 * ratio);
            b = (int)(255 * (1 - ratio));
        } else {
            // Negative correlation (-1 to 0): Red to Green
            double ratio = Math.min(1.0, -value);
            r = (int)(255 * ratio);
            g = (int)(255 * (1 - ratio));
            b = 0;
        }

        // Add some alpha transparency (80% opaque)
        return new Color(r, g, b, 204);
    }

    /**
     * Generates and saves a heat map overlay based on current filter settings
     * @return Path to the generated image file, or null if generation failed
     */
    public String drawImage() {
        // Convert title case filter back to original if needed
        String originalFilter = titleCaseToOriginalMap.getOrDefault(filter, filter);
        String originalAssessment = titleCaseToOriginalMap.getOrDefault(assessment, assessment);

        if (!originalAssessment.equals("None")) {
            originalAssessment = originalAssessment.toUpperCase();
        }

        System.out.println("DEBUG: Using filter: " + filter + " -> " + originalFilter);
        System.out.println("DEBUG: Using assessment: " + assessment + " -> " + originalAssessment);

        // Temporarily store the original assessment
        String tempAssessment = assessment;

        // Replace the assessment with original version for filtering
        assessment = originalAssessment;

        Map<String, Double> pixelValues = getPixelValues(originalFilter);

        // Restore the display version
        assessment = tempAssessment;

        // Handle both cases: data exists or no data
        Graphics2D g2d = img.createGraphics();

        // Clear the image completely
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        String safeFilter = filter.replace('/', '_').replace('\\', '_')
                .replace(':', '_').replace('*', '_')
                .replace('?', '_').replace('"', '_')
                .replace('<', '_').replace('>', '_')
                .replace('|', '_');

        String fileName = mapType + "_" + categoryOrGroup + "_" + safeFilter + "_" + assessment + ".png";
        String outputDir = "src/main/resources/ca/macewan/thebatmap/assets/";

        // Ensure directory exists
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!pixelValues.isEmpty()) {
            // Draw data points when we have matching data
            Map<String, Color> gradientMap = gradientMap(pixelValues);

            for (Map.Entry<String, Color> current : gradientMap.entrySet()) {
                g2d.setColor(current.getValue());
                String[] coordinate = current.getKey().split(",");
                int x = Integer.parseInt(coordinate[0]);
                int y = Integer.parseInt(coordinate[1]);
                g2d.fillRect(x, y, 5, 5); // Draw each data point as a 5x5 pixel rectangle
            }
        } else {
            // Create a simple message for "no data" case
            System.out.println("No data matches filter: " + mapType + "_" + categoryOrGroup + "_" + filter + "_" + assessment);

            // We'll keep the image completely transparent and just create a blank overlay
            fileName = "no_data_" + System.currentTimeMillis() + ".png";
        }

        g2d.dispose();

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

    private Map<String, Double> getPixelValues(String filterValue) {
        Map<String, Double> pixelValues = new HashMap<>();
        double count;

        if (mapType.equals("Crime")) {
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
        }
        else { // mapType.equals("Property")
            for (Map.Entry<String, CalculatePixelValue.PropertyPixelData> entry : pixels.getPropertyPixels().entrySet()) {
                CalculatePixelValue.PropertyPixelData propertyValues = entry.getValue();

                Map<String, Integer> propertyMap = null;
                Map<String, Integer> assessmentMap = null;

                // If category/group is "None", we don't filter by it
                if (!categoryOrGroup.equals("None")) {
                    if (categoryOrGroup.equals("Ward")) {
                        propertyMap = propertyValues.getWardCount();
                    }
                    else if (categoryOrGroup.equals("Neighbourhood")) {
                        propertyMap = propertyValues.getNeighborhoodCount();
                    }
                }

                if (!assessment.equals("None")) {
                    assessmentMap = propertyValues.getAssessmentClassCount();
                }

                // When category/group is "None", we don't check propertyMap
                // When filter is "None", we don't check for a specific key
                boolean includeProperty = true;

                if (propertyMap != null && !filterValue.equals("None")) {
                    includeProperty = propertyMap.containsKey(filterValue);
                }

                if (assessmentMap != null && !assessment.equals("None")) {
                    includeProperty = includeProperty && assessmentMap.containsKey(assessment);
                }

                if (includeProperty) {
                    count = propertyValues.getAverageValue();
                    pixelValues.put(entry.getKey(), count);
                }
            }
        }

        System.out.println("Found " + pixelValues.size() + " matching properties for filter: " +
                mapType + "/" + categoryOrGroup + "/" + filterValue + "/" + assessment);

        return pixelValues;
    }

    /**
     * Normalizes neighbourhoods' mean values into RGB values from Blue (min) -> Green -> Red (max).
     *
     * @return Map of Coordinate as Key, Color as Value
     */
    public static Map<String, Color> gradientMap(Map<String, Double> pixelValues) {
        Map<String, Color> colorMap = new HashMap<>();
        double normalized;
        List<Double> bounds = detectOutlier(pixelValues);

        for (Map.Entry<String, Double> current : pixelValues.entrySet()) {
            normalized = Math.min(1.0, current.getValue() / bounds.get(1));
            colorMap.put(current.getKey(), getGradientColor(normalized));
        }
        return colorMap;
    }

    /**
     * Convert normalized value into RGB values. Blue (min) -> Green -> Red (max).
     * @param value - Normalized value between 0 - 1
     * @return - Color(r,g,b)
     */
    private static Color getGradientColor(double value) {
        int r, g, b;

        if (value < 0.5) {
            // Blue (0, 0, 255) to Yellow (255, 255, 0)
            double ratio = value / 0.5;
            r = (int) (255 * ratio);
            g = (int) (255 * ratio);
            b = (int) (255 * (1 - ratio));
        } else {
            // Yellow (255, 255, 0) to Red (255, 0, 0)
            double ratio = (value - 0.5) / 0.5;
            r = 255;
            g = (int) (255 * (1 - ratio));
            b = 0;
        }
        return new Color(r, g, b);
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
}