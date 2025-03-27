package ca.macewan.thebatmap.utils.general;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class DrawOverlay {
    private static final CalculatePixelValue pixels = new CalculatePixelValue();
    private static final Set<String> crimePixels = pixels.getCrimePixels().keySet();
    private static final Set<String> propertyPixels = pixels.getPropertyPixels().keySet();

    // Create the new image needed
    private static final BufferedImage img = new BufferedImage(
            CoordinateToPixel.getMapWidth()+1, CoordinateToPixel.getMapHeight()+1, BufferedImage.TYPE_INT_RGB);

    public static void main(String[] args) {
        drawImage(0);
        drawImage(1);
    }

    public static void drawImage(int mode) {
        Set<String> data;
        String type;
        if (mode == 0) {
            data = crimePixels;
            type = "crime";
        }
        else {
            data = propertyPixels;
            type = "property";
        }

        for (String current : data) {
            String[] coordinate = current.split(",");
            int x = Integer.parseInt(coordinate[0]);
            int y = Integer.parseInt(coordinate[1]);
            img.setRGB(x, y, Color.WHITE.getRGB());
        }

        File outputfile = new File(GenerateKeyCSV.getOutputDir() + type + "Pixels.jpg");

        try { ImageIO.write(img, "jpg", outputfile); }
        catch (IOException _) { }
    }
}