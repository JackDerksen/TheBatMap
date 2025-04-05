package ca.macewan.thebatmap.utils.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing CSV files with support for various formats
 * and special handling for values containing commas within quotes or parentheses.
 */
public class ParseCSV {

    /**
     * Reads CSV data into a list of maps where keys are column names
     *
     * @param filePath Path to the CSV file
     * @return List of maps, each representing a row with column name as key
     * @throws IOException If an I/O error occurs
     */
    public static List<Map<String, String>> readAsMaps(Path filePath) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result; // Empty file
            }

            String[] headers = parseCSVLine(headerLine);

            // Process each data line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = parseCSVLine(line);
                Map<String, String> row = new HashMap<>();

                // Map each column to its header
                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    row.put(headers[i], values[i]);
                }

                result.add(row);
            }
        }

        return result;
    }

    /**
     * Parses a single CSV line, handling commas within quotes and parentheses
     *
     * @param line CSV line to parse
     * @return Array of field values
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;
        boolean insideParens = false;

        for (char c : line.toCharArray()) {
            if (c == '"' && !insideParens) {
                insideQuotes = !insideQuotes;
                currentField.append(c);
            } else if (c == '(' && !insideQuotes) {
                insideParens = true;
                currentField.append(c);
            } else if (c == ')' && insideParens) {
                insideParens = false;
                currentField.append(c);
            } else if (c == ',' && !insideQuotes && !insideParens) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }
}