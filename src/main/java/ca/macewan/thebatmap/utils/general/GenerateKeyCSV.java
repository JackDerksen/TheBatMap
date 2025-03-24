package ca.macewan.thebatmap.utils.general;

import ca.macewan.thebatmap.utils.models.CrimeData;
import ca.macewan.thebatmap.utils.models.PropertyData;
import ca.macewan.thebatmap.utils.parsers.ParseCrime;
import ca.macewan.thebatmap.utils.parsers.ParseProperties;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for generating CSV files with key data for heatmap visualization
 */
public class GenerateKeyCSV {
    private static final String OUTPUT_DIR = "data/";
    public static final String KEY_PROPERTY_DATA_PATH = OUTPUT_DIR + "keyPropertyData.csv";
    public static final String KEY_CRIME_DATA_PATH = OUTPUT_DIR + "keyCrimeData.csv";

    /**
     * Generates a CSV file with key property data (value and location)
     * @return Path to the generated CSV file
     * @throws IOException If an I/O error occurs
     */
    public static Path generateKeyPropertyData() throws IOException {
        // Ensure output directory exists
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // Get property data
        List<PropertyData> properties = ParseProperties.parsePropertyData();

        // Write to CSV
        Path outputPath = Paths.get(KEY_PROPERTY_DATA_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            // Write header
            writer.write("id,value,latitude,longitude,x,y,neighborhood,ward");
            writer.newLine();

            // Write data rows
            int id = 1;
            for (PropertyData property : properties) {
                double lat = property.getLocation().getLatitude();
                double lon = property.getLocation().getLongitude();

                // Skip properties outside map bounds
                if (!CoordinateToPixel.isInBounds(lat, lon)) {
                    continue;
                }

                int[] pixelCoords = CoordinateToPixel.geoToPixel(lat, lon);

                writer.write(String.format("%d,%.2f,%.6f,%.6f,%d,%d,%s,%s",
                        id++,
                        property.getAssessment().getAssessedValue(),
                        lat,
                        lon,
                        pixelCoords[0],
                        pixelCoords[1],
                        escapeCSV(property.getNeighbourhood().getNeighbourhood()),
                        escapeCSV(property.getNeighbourhood().getWard())
                ));
                writer.newLine();
            }
        }

        System.out.println("Generated property data CSV at: " + outputPath.toAbsolutePath());
        return outputPath;
    }

    /**
     * Generates a CSV file with key crime data (type and location)
     * @return Path to the generated CSV file
     * @throws IOException If an I/O error occurs
     */
    public static Path generateKeyCrimeData() throws IOException {
        // Ensure output directory exists
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // Get crime data
        List<CrimeData> crimes = ParseCrime.parseCrimeData();

        // Write to CSV
        Path outputPath = Paths.get(KEY_CRIME_DATA_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            // Write header
            writer.write("id,category,group,type,latitude,longitude,x,y");
            writer.newLine();

            // Write data rows
            int id = 1;
            for (CrimeData crime : crimes) {
                if (crime.getLocation() == null) {
                    continue;
                }

                double lat = crime.getLocation().getLatitude();
                double lon = crime.getLocation().getLongitude();

                // Skip crimes outside map bounds
                if (!CoordinateToPixel.isInBounds(lat, lon)) {
                    continue;
                }

                int[] pixelCoords = CoordinateToPixel.geoToPixel(lat, lon);

                writer.write(String.format("%d,%s,%s,%s,%.6f,%.6f,%d,%d",
                        id++,
                        escapeCSV(crime.getOccurrenceCategory()),
                        escapeCSV(crime.getOccurrenceGroup()),
                        escapeCSV(crime.getOccurrenceTypeGroup()),
                        lat,
                        lon,
                        pixelCoords[0],
                        pixelCoords[1]
                ));
                writer.newLine();
            }
        }

        System.out.println("Generated crime data CSV at: " + outputPath.toAbsolutePath());
        return outputPath;
    }

    /**
     * Helper method to escape special characters in CSV values
     * @param value The string to escape
     * @return Escaped string safe for CSV
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // Add quotes and escape existing quotes if value contains comma, quote or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Main method to generate both CSV files
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            generateKeyPropertyData();
            generateKeyCrimeData();
            System.out.println("CSV generation completed successfully!");
        } catch (IOException e) {
            System.err.println("Error generating CSV files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}