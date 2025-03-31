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
    private final String[] mapTypeArray = new String[]{"Crime", "Property"};
    private final String[] crimeCategoryArray = new String[]{"Category", "Group", "Type", "None"};
    private final String[] propertyCategoryArray = new String[]{"Ward", "Neighbourhood", "None"};

    public DrawOverlay() {
        try { pixels.loadData(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void setMapType(String mapType) { this.mapType = mapType; }

    public void setCategoryOrGroup(String categoryOrGroup) { this.categoryOrGroup = categoryOrGroup; }

    public void setFilter(String filter) { this.filter = filter; }

    public void setAssessment(String assessment) { this.assessment = assessment; }

    public String[] getMapTypeArray() { return mapTypeArray; }

    public String[] getCategoryOrGroup(String newValue) {
        if (newValue.equals("Crime")) { return crimeCategoryArray; }
        else { return propertyCategoryArray; }
    }

    public String[] getFilters(String newValue) {
        Set<String> filterArray = new LinkedHashSet<>();
        switch (newValue) {
            case "Category" -> filterArray = pixels.getCrimeCategories();
            case "Group" -> filterArray = pixels.getCrimeGroups();
            case "Type" -> filterArray = pixels.getCrimeTypes();
            case "Ward" -> filterArray = pixels.getWards();
            case "Neighbourhood" -> filterArray = pixels.getNeighborhoods();
            case null, default -> filterArray.add("None");
        }
        return filterArray.toArray(new String[0]);
    }

    public String[] getAssessmentClass(String newValue) {
        if (newValue.equals("Property")) {
            Set<String> assessmentClasses = pixels.getAssessmentClasses();
            assessmentClasses.add("None");
            return assessmentClasses.toArray(new String[0]);
        }
        return new String[]{"None"};
    }

    public void setAll(String mapType, String categoryOrGroup, String filter, String assessment) {
    //TODO Crime overlay generation
        /*
        String mapType = "crime";

        String categoryOrGroup = "category";
        String assessment = "";

        for (String current : pixels.getCrimeCategories()) {
            drawImage(mapType, categoryOrGroup, current, assessment);
        }

        categoryOrGroup = "group";
        for (String current : pixels.getCrimeGroups()) {
            drawImage(mapType, categoryOrGroup, current, assessment);
        }

        drawImage(mapType, "", "", assessment);
        */

        //TODO Property overlay generation
        /*
        String mapType = "property";

        String categoryOrGroup = "ward";

        for (String ward : pixels.getWards()) {
            for (String assessment : pixels.getAssessmentClasses()) {
                drawImage(mapType, categoryOrGroup, ward, assessment);
            }
        }

        categoryOrGroup = "neighbourhood";
        for (String current : pixels.getNeighborhoods()) {
            drawImage(mapType, categoryOrGroup, current, assessment);
        }

        drawImage(mapType, "", "", assessment);
        */

        /*
        //TODO Specific overlay generation
        Scanner input = new Scanner(System.in);

        System.out.println("[crime, property]\nMap type?");
        mapType = input.nextLine();

        if (mapType.equals("crime")) {
            System.out.println("[category, group, type]\nCrime specifics? (Enter to skip)");
            categoryOrGroup = input.nextLine();

            if (!categoryOrGroup.isEmpty()) {
                if (categoryOrGroup.equals("category")) {
                    System.out.println(pixels.getCrimeCategories());
                }
                else if (categoryOrGroup.equals("group")) {
                    System.out.println(pixels.getCrimeGroups());
                }
                else {
                    System.out.println(pixels.getCrimeTypes());
                }
                System.out.println("Which? (case sensitive)");
                filter = input.nextLine();
            }
        }
        else { // mapType.equals("property")
            System.out.println("[ward, neighbourhood]\nWant specific? (Enter to skip)");
            categoryOrGroup = input.nextLine();

            filter = "";
            if (!categoryOrGroup.isEmpty()) {
                if (categoryOrGroup.equals("ward")) {
                    System.out.println(pixels.getWards());
                }
                else {
                    System.out.println(pixels.getNeighborhoods());
                }
                System.out.println("Which? (case sensitive) (Enter to skip)");
                filter = input.nextLine();
            }

            System.out.println(pixels.getAssessmentClasses() + "\nWhich assessment class? (case sensitive) (Enter to skip)");
            assessment = input.nextLine();
        }
        drawImage();
        */
    }

    /**
     * Generates and saves a heat map overlay based on current filter settings
     * @return Path to the generated image file, or null if generation failed
     */
    public String drawImage() {
        Map<String, Double> pixelValues = getPixelValues();

        if (!pixelValues.isEmpty()) {
            Map<String, Color> gradientMap = gradientMap(pixelValues);

            // Create a fresh image
            Graphics2D g2d = img.createGraphics();

            // Clear the image completely
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, width, height);
            g2d.setComposite(AlphaComposite.SrcOver);

            // Only draw the colored dots, leaving the rest transparent
            for (Map.Entry<String, Color> current : gradientMap.entrySet()) {
                g2d.setColor(current.getValue());
                String[] coordinate = current.getKey().split(",");
                int x = Integer.parseInt(coordinate[0]);
                int y = Integer.parseInt(coordinate[1]);
                g2d.fillRect(x, y, 5, 5); // Draw each data point as a 5x5 pixel rectangle
            }

            g2d.dispose();

            //TODO Remember to remove this
            // Safe filename cause shit won't find the image so just force every extension check
            String safeFilter = filter.replace('/', '_').replace('\\', '_')
                    .replace(':', '_').replace('*', '_')
                    .replace('?', '_').replace('"', '_')
                    .replace('<', '_').replace('>', '_')
                    .replace('|', '_');

            String fileName = mapType + "_" + categoryOrGroup + "_" + safeFilter + "_" + assessment + ".png";
            String outputDir = GenerateKeyCSV.getOutputDir();

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
        } else {
            System.out.println("Empty: " + mapType + "_" + categoryOrGroup + "_" + filter + "_" + assessment);
            return null;
        }
    }


    private Map<String, Double> getPixelValues() {
        Map<String, Double> pixelValues = new HashMap<>();
        double count;

        if (mapType.equals("Crime")) {
            for (Map.Entry<String, CalculatePixelValue.CrimePixelData> entry : pixels.getCrimePixels().entrySet()) {
                CalculatePixelValue.CrimePixelData crimeData = entry.getValue();

                count = switch (categoryOrGroup) {
                    case "Category" -> crimeData.getCategoryCount(filter);
                    case "Group" -> crimeData.getGroupCount(filter);
                    case "Type" -> crimeData.getGroupTypeCount(filter);
                    default -> crimeData.getCount();
                };
                if (count > 0) pixelValues.put(entry.getKey(), count);
            }
        }
        else { // mapType.equals("property")
            for (Map.Entry<String, CalculatePixelValue.PropertyPixelData> entry : pixels.getPropertyPixels().entrySet()) {
                CalculatePixelValue.PropertyPixelData propertyValues = entry.getValue();

                Map<String, Integer> propertyMap = null;
                Map<String, Integer> assessmentMap = null;

                if (!categoryOrGroup.equals("None")) {
                    if (categoryOrGroup.equals("Ward")) { propertyMap = propertyValues.getWardCount(); }
                    else { propertyMap = propertyValues.getNeighborhoodCount(); }
                }

                if (!assessment.equals("None")) { assessmentMap = propertyValues.getAssessmentClassCount(); }

                if ((propertyMap == null || propertyMap.containsKey(filter)) &&
                        (assessmentMap == null || assessmentMap.containsKey(assessment))) {
                    count = propertyValues.getAverageValue();
                    pixelValues.put(entry.getKey(), count);
                }
            }
        }
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
}