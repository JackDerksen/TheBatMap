package ca.macewan.thebatmap.utils.parsers;

import ca.macewan.thebatmap.utils.general.FileUtils;
import ca.macewan.thebatmap.utils.models.PropertyData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for property assessment data
 */
public class ParseProperties {

    /**
     * Parses property assessment data from the default file location
     *
     * @return List of PropertyData objects
     * @throws IOException If an I/O error occurs
     */
    public static List<PropertyData> parsePropertyData() throws IOException {
        return parsePropertyData(FileUtils.getResourceAsPath(FileUtils.PROPERTY_DATA_PATH));
    }

    /**
     * Parses property assessment data from a specific file
     *
     * @param filePath Path to the CSV file containing property data
     * @return List of PropertyData objects
     * @throws IOException If an I/O error occurs
     */
    public static List<PropertyData> parsePropertyData(Path filePath) throws IOException {
        List<Map<String, String>> csvData = ParseCSV.readAsMaps(filePath);
        List<PropertyData> propertyDataList = new ArrayList<>();

        for (Map<String, String> row : csvData) {
            PropertyData property = new PropertyData();

            // Map CSV fields to PropertyData object based on the actual CSV structure
            property.setAccountNumber(row.get("Account Number"));
            property.setAddress(row.get("House Number"), row.get("Street Name"));
            property.setNeighbourhood(row.get("Neighbourhood ID"), row.get("Neighbourhood"), row.get("Ward"));
            property.setLocation(parseDouble(row.get("Latitude")), parseDouble(row.get("Longitude")), row.get("Point Location"));
            property.setAssessment(
                    parseDouble(row.get("Assessed Value")),
                    parseInt(row.get("Assessment Class % 1")),
                    parseInt(row.get("Assessment Class % 2")),
                    parseInt(row.get("Assessment Class % 3")),
                    row.get("Assessment Class 1"),
                    row.get("Assessment Class 2"),
                    row.get("Assessment Class 3")
            );

            propertyDataList.add(property);
        }

        return propertyDataList;
    }

    /**
     * Safely parses a String to double
     */
    private static double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Safely parses a String to int
     */
    private static int parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}