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
    private static final String OUTPUT_DIR = "src/main/resources/ca/macewan/thebatmap/key-data/";
    public static final String KEY_PROPERTY_DATA_PATH = OUTPUT_DIR + "key_property_data.csv";
    public static final String KEY_CRIME_DATA_PATH = OUTPUT_DIR + "key_crime_data.csv";

    public static String getOutputDir() { return OUTPUT_DIR; }

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
            writer.write("id,value,latitude,longitude,x,y,neighborhood,ward,address");
            writer.newLine();

            // Write data rows
            int id = 1;
            for (PropertyData property : properties) {
                if (property.getLocation() == null) {
                    continue; // Skip properties without location data
                }

                double lat = property.getLocation().getLatitude();
                double lon = property.getLocation().getLongitude();

                // Skip properties outside map bounds
                if (!CoordinateToPixel.isInBounds(lat, lon)) {
                    continue;
                }

                int[] pixelCoords = CoordinateToPixel.geoToPixel(lat, lon);

                // Get the full address as a string
                String address = property.getAddress() != null ? property.getAddress().toString() : "";

                writer.write(String.format("%d,%.2f,%.6f,%.6f,%d,%d,%s,%s,%s",
                        id++,
                        property.getAssessment().getAssessedValue(),
                        lat,
                        lon,
                        pixelCoords[0],
                        pixelCoords[1],
                        escapeCSV(property.getNeighbourhood().getNeighbourhood()),
                        escapeCSV(property.getNeighbourhood().getWard()),
                        escapeCSV(address)
                ));
                writer.newLine();
            }
        }

        System.out.println("Generated property data CSV at: " + outputPath.toAbsolutePath());
        return outputPath;
    }

    /**
     * Generates a CSV file with simplified crime data categories
     * tailored for homebuyer-focused heatmap visualization
     *
     * @throws IOException If an I/O error occurs
     */
    public static void generateKeyCrimeData() throws IOException {
        // Ensure output directory exists
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // Get crime data
        List<CrimeData> crimes = ParseCrime.parseCrimeData();

        // Write to CSV
        Path outputPath = Paths.get(KEY_CRIME_DATA_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            // Write header with simplified categories
            writer.write("id,simplified_category,original_category,type,latitude,longitude,x,y,intersection");
            writer.newLine();

            // Stats counters
            int violentCount = 0;
            int propertyCount = 0;
            int disorderCount = 0;
            int miscCount = 0;
            int skippedCount = 0;

            // Write data rows
            int id = 1;
            for (CrimeData crime : crimes) {
                if (crime.getLocation() == null) {
                    skippedCount++;
                    continue;
                }

                double lat = crime.getLocation().getLatitude();
                double lon = crime.getLocation().getLongitude();

                // Skip crimes outside map bounds
                if (!CoordinateToPixel.isInBounds(lat, lon)) {
                    skippedCount++;
                    continue;
                }

                int[] pixelCoords = CoordinateToPixel.geoToPixel(lat, lon);

                // Get original values (with null checks)
                String originalCategory = crime.getOccurrenceCategory() != null ? crime.getOccurrenceCategory() : "";
                String group = crime.getOccurrenceGroup() != null ? crime.getOccurrenceGroup() : "";
                String type = crime.getOccurrenceTypeGroup() != null ? crime.getOccurrenceTypeGroup() : "";
                String intersection = crime.getIntersection() != null ? crime.getIntersection() : "";

                // Classify into simplified categories
                String simplifiedCategory = classifyCrime(originalCategory, group, type);

                // Update counters
                switch (simplifiedCategory) {
                    case "Violent Crime":
                        violentCount++;
                        break;
                    case "Property Crime":
                        propertyCount++;
                        break;
                    case "Public Disorder":
                        disorderCount++;
                        break;
                    case "Misc":
                        miscCount++;
                        break;
                }

                writer.write(String.format("%d,%s,%s,%s,%.6f,%.6f,%d,%d,%s",
                        id++,
                        escapeCSV(simplifiedCategory),
                        escapeCSV(originalCategory),
                        escapeCSV(type),
                        lat,
                        lon,
                        pixelCoords[0],
                        pixelCoords[1],
                        escapeCSV(intersection)
                ));
                writer.newLine();
            }

            System.out.println("Crime classification stats:");
            System.out.println("- Violent Crimes: " + violentCount);
            System.out.println("- Property Crimes: " + propertyCount);
            System.out.println("- Public Disorder: " + disorderCount);
            System.out.println("- Misc: " + miscCount);
            System.out.println("- Skipped: " + skippedCount + " (missing or out-of-bounds location)");
        }

        System.out.println("Generated simplified crime data CSV at: " + outputPath.toAbsolutePath());
    }

    /**
     * Classifies crimes into simplified categories for homebuyer-focused visualization
     *
     * @param category Original occurrence category
     * @param group Original occurrence group
     * @param type Original occurrence type
     * @return Simplified category (Violent Crime, Property Crime, Public Disorder, or Misc)
     */
    private static String classifyCrime(String category, String group, String type) {
        // Default to "Misc" if any values are null
        if (category == null || group == null || type == null) {
            return "Misc";
        }

        // 1. Violent Crimes
        if (category.equals("Violent")) {
            return "Violent Crime";
        }

        if (category.equals("Weapons")) {
            return "Violent Crime";
        }

        if (group.equals("Personal Violence")) {
            return "Violent Crime";
        }

        // Specific robbery cases (even if categorized as Property in original data)
        if (type.contains("Robbery")) {
            return "Violent Crime";
        }

        // 2. Property Crimes
        if (group.equals("Property")) {
            // Check for specific property crime types
            if (type.contains("Break and Enter") ||
                    type.contains("Theft") ||
                    type.contains("Arson") ||
                    type.contains("Property Damage")) {
                return "Property Crime";
            }
        }

        // Vehicle-related property crimes
        if (type.contains("Motor Vehicle")) {
            return "Property Crime";
        }

        // 3. Public Disorder
        if (category.equals("Disorder")) {
            // Exclude certain disorder types
            if (!type.contains("Fraud")) {
                return "Public Disorder";
            }
        }

        if (group.equals("General Disorder")) {
            return "Public Disorder";
        }

        if (type.equals("Trespassing") ||
                type.equals("Suspicious Person") ||
                type.equals("Disturbance") || type.equals("Drugs") ||
                type.equals("Intoxicated Person")) {
            return "Public Disorder";
        }

        // 4. Default to "Misc" for everything else
        return "Misc";
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
     * Checks if the CSV files exist
     * @return true if both files exist, false otherwise
     */
    public static boolean keyFilesExist() {
        Path propertyPath = Paths.get(KEY_PROPERTY_DATA_PATH);
        Path crimePath = Paths.get(KEY_CRIME_DATA_PATH);
        return Files.exists(propertyPath) && Files.exists(crimePath);
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