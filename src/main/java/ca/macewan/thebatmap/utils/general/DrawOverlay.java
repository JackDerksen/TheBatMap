package ca.macewan.thebatmap.utils.general;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
        drawImage(0);
        drawImage(1);
    }

    public static void drawImage(int mode) {
        String type;
        Map<String, Double> pixelValues;

        if (mode == 0) {
            type = "crime";
            pixelValues = getPixelValues(pixels.getCrimePixels());
        }
        else {
            type = "property";
            pixelValues = getPixelValues(pixels.getPropertyPixels());
        }

        Map<String, Color> gradientMap = gradientMap(pixelValues, mode);

        Graphics2D g2d = img.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);

        g2d.setComposite(AlphaComposite.Src);

        for (Map.Entry<String, Color> current: gradientMap.entrySet()) {
            g2d.setColor(current.getValue());
            String[] coordinate = current.getKey().split(",");
            int x = Integer.parseInt(coordinate[0]);
            int y = Integer.parseInt(coordinate[1]);
            g2d.fillRect(x, y, 1, 1);
        }

        g2d.dispose();

        File outputfile = new File(GenerateKeyCSV.getOutputDir() + type + "Pixels.png");

        try { ImageIO.write(img, "png", outputfile); }
        catch (IOException _) { }
    }

    /**
     * Normalizes neighbourhoods' mean values into RGB values from Blue (min) -> Green -> Red (max).
     *
     * @return Map of Coordinate as Key, Color as Value
     */
    public static Map<String, Color> gradientMap(Map<String, Double> pixelValues, int mode) {
        Map<String, Color> colorMap = new LinkedHashMap<>();
        double normalized;

        for (Map.Entry<String, Double> current : pixelValues.entrySet()) {
            if (mode == 0) {
                normalized = CalculatePixelValue.normalizeCount(current.getValue());
            }
            else {
                normalized = CalculatePixelValue.normalizeValue(current.getValue());
            }
            colorMap.put(current.getKey(), getGradientColor(normalized));
        }

        return colorMap;
    }

    private static <T> Map<String, Double> getPixelValues(Map<String, T> pixelData) {
        Map<String, Double> pixelValues = new HashMap<>();
        double value;

        for (Map.Entry<String, T> entry : pixelData.entrySet()) {
            if (entry.getValue() instanceof CalculatePixelValue.CrimePixelData) {
                value = ((CalculatePixelValue.CrimePixelData) entry.getValue()).getCount();
            }
            else {
                value = ((CalculatePixelValue.PropertyPixelData) entry.getValue()).getAverageValue();
            }

            pixelValues.put(entry.getKey(), value);
        }
        return pixelValues;
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
}