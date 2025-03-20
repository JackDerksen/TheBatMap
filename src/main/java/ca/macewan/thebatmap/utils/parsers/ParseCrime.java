package ca.macewan.thebatmap.utils.parsers;

import ca.macewan.thebatmap.utils.general.FileUtils;
import ca.macewan.thebatmap.utils.models.CrimeData;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for crime statistics data
 */
public class ParseCrime {

    // Date formatter for the format used in the CSV file (yyyy/MM/dd)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * Parses crime statistics data from the default file location
     *
     * @return List of CrimeData objects
     * @throws IOException If an I/O error occurs
     */
    public static List<CrimeData> parseCrimeData() throws IOException {
        return parseCrimeData(FileUtils.getResourceAsPath(FileUtils.CRIME_DATA_PATH));
    }

    /**
     * Parses crime statistics data from a specific file
     *
     * @param filePath Path to the CSV file containing crime data
     * @return List of CrimeData objects
     * @throws IOException If an I/O error occurs
     */
    public static List<CrimeData> parseCrimeData(Path filePath) throws IOException {
        List<Map<String, String>> csvData = ParseCSV.readAsMaps(filePath);
        List<CrimeData> crimeDataList = new ArrayList<>();

        for (Map<String, String> row : csvData) {
            CrimeData crime = new CrimeData();

            // Map CSV fields to CrimeData object
            crime.setOccurrenceCategory(row.get("Occurrence_Category"));
            crime.setOccurrenceGroup(row.get("Occurrence_Group"));
            crime.setOccurrenceTypeGroup(row.get("Occurrence_Type_Group"));
            crime.setIntersection(row.get("Intersection"));
            crime.setObjectId(parseInt(row.get("OBJECTID")));
            crime.setDateReported(parseDate(row.get("Date Reported")));
            crime.setLocation(parseDouble(row.get("x")), parseDouble(row.get("y")));

            crimeDataList.add(crime);
        }

        return crimeDataList;
    }

    /**
     * Safely parses a date string to LocalDate
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Safely parses a String to double
     */
    private static double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
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