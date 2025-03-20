package ca.macewan.thebatmap.utils.general;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Utility class for managing file paths and locations
 */
public class FileUtils {
    // Base path for sample data
    private static final String SAMPLE_DATA_DIR = "/ca/macewan/thebatmap/sample-data/";

    // File paths as resource names
    public static final String PROPERTY_DATA_PATH = SAMPLE_DATA_DIR + "property_data.csv";
    public static final String CRIME_DATA_PATH = SAMPLE_DATA_DIR + "crime_stats.csv";

    /**
     * Gets an input stream for a resource
     *
     * @param resourceName Name of the resource
     * @return InputStream for the resource
     */
    public static InputStream getResourceAsStream(String resourceName) {
        return Objects.requireNonNull(FileUtils.class.getResourceAsStream(resourceName),
                "Resource not found: " + resourceName);
    }

    /**
     * Creates a temporary file from a resource and returns its path
     *
     * @param resourceName Name of the resource
     * @return Path to the temporary file
     * @throws IOException If an I/O error occurs
     */
    public static Path getResourceAsPath(String resourceName) throws IOException {
        String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        Path tempFile = Files.createTempFile("thebatmap-", fileName);

        try (InputStream in = getResourceAsStream(resourceName)) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Make sure the file is deleted on exit
        tempFile.toFile().deleteOnExit();

        return tempFile;
    }
}