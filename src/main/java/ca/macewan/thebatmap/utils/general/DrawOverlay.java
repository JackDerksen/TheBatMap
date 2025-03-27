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
    private static final int width = CoordinateToPixel.getMapWidth() + 1;
    private static final int height = CoordinateToPixel.getMapHeight() + 1;

    // Create the new image needed
    private static final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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

        Graphics2D g2d = img.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);

        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.RED);

        for (String current : data) {
            String[] coordinate = current.split(",");
            int x = Integer.parseInt(coordinate[0]);
            int y = Integer.parseInt(coordinate[1]);
            g2d.fillRect(x, y, 1, 1);
        }

        g2d.dispose();

        File outputfile = new File(GenerateKeyCSV.getOutputDir() + type + "Pixels.png");

        try { ImageIO.write(img, "png", outputfile); }
        catch (IOException _) { }
    }
}