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

    public static void main(String[] args) {
        try {
            pixels.loadData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        //TODO Specific overlay generation
        Scanner input = new Scanner(System.in);

        System.out.println("[crime, property]\nMap type?");
        String mapType = input.nextLine();

        if (mapType.equals("crime")) {
            System.out.println("[category, group]\nCrime specifics? (Enter to skip)");
            String categoryOrGroup = input.nextLine();

            String filter = "";
            if (!categoryOrGroup.isEmpty()) {
                if (categoryOrGroup.equals("category")) {
                    System.out.println(pixels.getCrimeCategories());
                }
                else {
                    System.out.println(pixels.getCrimeGroups());
                }
                System.out.println("Which? (case sensitive)");
                filter = input.nextLine();
            }

            drawImage(mapType, categoryOrGroup, filter, "");
        }
        else { // mapType.equals("property")
            System.out.println("[ward, neighbourhood]\nWant specific? (Enter to skip)");
            String wardOrNeighbour = input.nextLine();

            String filter = "";
            if (!wardOrNeighbour.isEmpty()) {
                if (wardOrNeighbour.equals("ward")) {
                    System.out.println(pixels.getWards());
                }
                else {
                    System.out.println(pixels.getNeighborhoods());
                }
                System.out.println("Which? (case sensitive) (Enter to skip)");
                filter = input.nextLine();
            }

            System.out.println(pixels.getAssessmentClasses() + "\nWhich assessment class? (case sensitive) (Enter to skip)");
            String assessment = input.nextLine();

            drawImage(mapType, wardOrNeighbour, filter, assessment);
        }
    }


    public static void drawImage(String mapType, String categoryOrGroup, String filter, String assessment) {
        Map<String, Double> pixelValues = getPixelValues(mapType, categoryOrGroup, filter, assessment);

        if (!pixelValues.isEmpty()) {
            Map<String, Color> gradientMap = gradientMap(pixelValues);

            Graphics2D g2d = img.createGraphics();

            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, width, height);

            g2d.setComposite(AlphaComposite.Src);

            for (Map.Entry<String, Color> current : gradientMap.entrySet()) {
                g2d.setColor(current.getValue());
                String[] coordinate = current.getKey().split(",");
                int x = Integer.parseInt(coordinate[0]);
                int y = Integer.parseInt(coordinate[1]);
                g2d.fillRect(x, y, 5, 5); //TODO Change pixel size
            }

            g2d.dispose();

            filter = filter.replace('/', '_');

            File outputfile = new File(GenerateKeyCSV.getOutputDir() + mapType + "_" +
                    categoryOrGroup + "_" + filter + "_" + assessment + ".png");

            try {
                ImageIO.write(img, "png", outputfile);
                System.out.println("Image created at " + outputfile);
            } catch (IOException _) {
            }
        }
        else System.out.println("Empty: " + mapType + "_" + categoryOrGroup + "_" + filter + "_" + assessment);
    }

    private static Map<String, Double> getPixelValues(String mapType, String categoryOrGroup, String filter, String assessment) {
        Map<String, Double> pixelValues = new HashMap<>();
        double count;

        if (mapType.equals("crime")) {
            for (Map.Entry<String, CalculatePixelValue.CrimePixelData> entry : pixels.getCrimePixels().entrySet()) {
                CalculatePixelValue.CrimePixelData crimeData = entry.getValue();

                if (categoryOrGroup.equals("category")) {
                    count = crimeData.getCategoryCount(filter);
                }
                else if (categoryOrGroup.equals("group")) {
                    count = crimeData.getGroupCount(filter);
                }
                else {
                    count = crimeData.getCount();
                }

                if (count > 0) pixelValues.put(entry.getKey(), count);
            }
        }
        else { // mapType.equals("property")
            for (Map.Entry<String, CalculatePixelValue.PropertyPixelData> entry : pixels.getPropertyPixels().entrySet()) {
                CalculatePixelValue.PropertyPixelData propertyValues = entry.getValue();

                Map<String, Integer> propertyMap = null;
                Map<String, Integer> assessmentMap = null;

                if (!categoryOrGroup.isEmpty()) {
                    if (categoryOrGroup.equals("ward")) {
                        propertyMap = propertyValues.getWardCount();
                    }
                    else {
                        propertyMap = propertyValues.getNeighborhoodCount();
                    }
                }

                if (!assessment.isEmpty()) {
                    assessmentMap = propertyValues.getAssessmentClassCount();
                }

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

        /*
        System.out.println("Outliers:");
        for (double num : sortedData) {
            if (num < lowerBound || num > upperBound) {
                System.out.println(num);
            }
        }
        */

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