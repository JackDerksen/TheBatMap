package ca.macewan.thebatmap.utils.general;

import ca.macewan.thebatmap.utils.models.CrimeData;
import ca.macewan.thebatmap.utils.models.PropertyData;
import ca.macewan.thebatmap.utils.parsers.ParseCrime;
import ca.macewan.thebatmap.utils.parsers.ParseProperties;

import java.io.IOException;
import java.util.*;

/**
 * Utility class for calculating pixel intensity values based on crime and property data
 */
public class CalculatePixelValue {

    // Map dimensions
    private static final int MAP_WIDTH = 1812;
    private static final int MAP_HEIGHT = 1850;

    // Data storage
    private Map<String, CrimePixelData> crimePixels = new HashMap<>();
    private Map<String, PropertyPixelData> propertyPixels = new HashMap<>();

    public Map<String, CrimePixelData> getCrimePixels() {
        return crimePixels;
    }

    public Map<String, PropertyPixelData> getPropertyPixels() {
        return propertyPixels;
    }

    /**
     * Inner class to store crime data for a specific pixel
     */
    public static class CrimePixelData {
        private int count = 0;
        private Map<String, Integer> categoryCount = new HashMap<>();
        private Map<String, Integer> groupCount = new HashMap<>();
        private Map<String, Integer> groupTypeCount = new HashMap<>();

        public void addCrime(CrimeData crime) {
            count++;

            // Count by category
            String category = crime.getOccurrenceCategory();
            if (category != null) {
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            }

            // Count by group
            String group = crime.getOccurrenceGroup();
            if (group != null) {
                groupCount.put(group, groupCount.getOrDefault(group, 0) + 1);
            }

            // Count by group type
            String groupType = crime.getOccurrenceTypeGroup();
            if (groupType != null) {
                groupTypeCount.put(groupType, groupTypeCount.getOrDefault(groupType, 0) + 1);
            }
        }

        public int getCount() {
            return count;
        }

        public int getCategoryCount(String category) {
            return categoryCount.getOrDefault(category, 0);
        }

        public int getGroupCount(String group) {
            return groupCount.getOrDefault(group, 0);
        }

        public int getGroupTypeCount(String groupType) {
            return groupTypeCount.getOrDefault(groupType, 0);
        }

        public Map<String, Integer> getCategoryCount() {
            return categoryCount;
        }

        public Map<String, Integer> getGroupCount() {
            return groupCount;
        }

        public Map<String, Integer> getGroupTypeCount() {
            return groupTypeCount;
        }
    }

    /**
     * Inner class to store property data for a specific pixel
     */
    public static class PropertyPixelData {
        private int count = 0;
        private double totalValue = 0;
        private Map<String, Integer> wardCount = new HashMap<>();
        private Map<String, Integer> neighborhoodCount = new HashMap<>();
        private Map<String, Integer> assessmentClassCount = new HashMap<>();

        public void addProperty(PropertyData property) {
            count++;
            totalValue += property.getAssessment().getAssessedValue();

            // Count by ward
            String ward = property.getNeighbourhood().getWard();
            if (ward != null && !ward.isEmpty()) {
                wardCount.put(ward, wardCount.getOrDefault(ward, 0) + 1);
            }

            // Count by neighborhood
            String neighborhood = property.getNeighbourhood().getNeighbourhood();
            if (neighborhood != null && !neighborhood.isEmpty()) {
                neighborhoodCount.put(neighborhood, neighborhoodCount.getOrDefault(neighborhood, 0) + 1);
            }

            // Count by assessment class
            String assessmentClass = property.getAssessment().getAssessment1().split(" ")[0];
            if (assessmentClass != null && !assessmentClass.isEmpty()) {
                assessmentClassCount.put(assessmentClass, assessmentClassCount.getOrDefault(assessmentClass, 0) + 1);
            }
        }

        public int getCount() {
            return count;
        }

        public double getAverageValue() {
            return count > 0 ? totalValue / count : 0;
        }

        public double getTotalValue() {
            return totalValue;
        }

        public Map<String, Integer> getWardCount() {
            return wardCount;
        }

        public Map<String, Integer> getNeighborhoodCount() {
            return neighborhoodCount;
        }

        public Map<String, Integer> getAssessmentClassCount() {
            return assessmentClassCount;
        }
    }

    /**
     * Loads property and crime data and processes it into pixel maps
     * @throws IOException If an I/O error occurs
     */
    public void loadData() throws IOException {
        loadPropertyData();
        loadCrimeData();
    }

    /**
     * Loads and processes property data
     * @throws IOException If an I/O error occurs
     */
    public void loadPropertyData() throws IOException {
        propertyPixels.clear();

        List<PropertyData> properties = ParseProperties.parsePropertyData();
        System.out.println("Processing " + properties.size() + " properties...");

        for (PropertyData property : properties) {
            if (property.getLocation() == null) {
                continue;
            }

            double lat = property.getLocation().getLatitude();
            double lon = property.getLocation().getLongitude();

            // Skip properties outside map bounds
            if (!CoordinateToPixel.isInBounds(lat, lon)) {
                continue;
            }

            int[] pixelCoords = CoordinateToPixel.geoToPixel(lat, lon);
            String key = pixelCoords[0] + "," + pixelCoords[1];

            // Get or create pixel data
            PropertyPixelData pixelData = propertyPixels.getOrDefault(key, new PropertyPixelData());
            pixelData.addProperty(property);
            propertyPixels.put(key, pixelData);
        }

        System.out.println("Processed properties into " + propertyPixels.size() + " unique pixels");
    }

    /**
     * Loads and processes crime data
     * @throws IOException If an I/O error occurs
     */
    public void loadCrimeData() throws IOException {
        crimePixels.clear();

        List<CrimeData> crimes = ParseCrime.parseCrimeData();
        System.out.println("Processing " + crimes.size() + " crimes...");

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
            String key = pixelCoords[0] + "," + pixelCoords[1];

            // Get or create pixel data
            CrimePixelData pixelData = crimePixels.getOrDefault(key, new CrimePixelData());
            pixelData.addCrime(crime);
            crimePixels.put(key, pixelData);
        }

        System.out.println("Processed crimes into " + crimePixels.size() + " unique pixels");
    }

    /**
     * Gets the crime intensity value for a specific pixel
     * @param x X coordinate
     * @param y Y coordinate
     * @return Crime intensity value (0-1)
     */
    public double getCrimeIntensity(int x, int y) {
        return getCrimeIntensity(x, y, null, null);
    }

    /**
     * Gets the crime intensity value for a specific pixel with optional filters
     * @param x X coordinate
     * @param y Y coordinate
     * @param category Optional category filter (null for all categories)
     * @param group Optional group filter (null for all groups)
     * @return Crime intensity value (0-1)
     */
    public double getCrimeIntensity(int x, int y, String category, String group) {
        if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) {
            return 0.0;
        }

        String key = x + "," + y;
        CrimePixelData pixelData = crimePixels.get(key);

        if (pixelData == null) {
            return 0.0;
        }

        // If no filters, return based on total count
        if (category == null && group == null) {
            return normalizeCount(pixelData.getCount());
        }

        // Filter by category if specified
        if (category != null && group == null) {
            return normalizeCount(pixelData.getCategoryCount(category));
        }

        // Filter by group if specified
        if (category == null && group != null) {
            return normalizeCount(pixelData.getGroupCount(group));
        }

        // Filter by both category and group
        int categoryCount = pixelData.getCategoryCount(category);
        int groupCount = pixelData.getGroupCount(group);

        // Return minimum of both filters (more restrictive)
        return normalizeCount(Math.min(categoryCount, groupCount));
    }

    /**
     * Gets the property value intensity for a specific pixel
     * @param x X coordinate
     * @param y Y coordinate
     * @return Property value intensity (0-1)
     */
    public double getPropertyIntensity(int x, int y) {
        return getPropertyIntensity(x, y, null, null);
    }

    /**
     * Gets the property value intensity for a specific pixel with optional filters
     * @param x X coordinate
     * @param y Y coordinate
     * @param neighborhood Optional neighborhood filter (null for all neighborhoods)
     * @param assessmentClass Optional assessment class filter (null for all classes)
     * @return Property value intensity (0-1)
     */
    public double getPropertyIntensity(int x, int y, String neighborhood, String assessmentClass) {
        if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) {
            return 0.0;
        }

        String key = x + "," + y;
        PropertyPixelData pixelData = propertyPixels.get(key);

        if (pixelData == null) {
            return 0.0;
        }

        // Apply filters based on parameters
        boolean includePixel = true;

        if (neighborhood != null) {
            int count = pixelData.getNeighborhoodCount().getOrDefault(neighborhood, 0);
            includePixel = includePixel && (count > 0);
        }

        if (assessmentClass != null) {
            int count = pixelData.getAssessmentClassCount().getOrDefault(assessmentClass, 0);
            includePixel = includePixel && (count > 0);
        }

        if (!includePixel) {
            return 0.0;
        }

        // Return normalized average value
        return normalizeValue(pixelData.getAverageValue());
    }

    /**
     * Gets the correlation value for a specific pixel
     * Values close to 1 indicate high property values and low crime
     * Values close to -1 indicate low property values and high crime
     * Values close to 0 indicate mixed or neutral areas
     * @param x X coordinate
     * @param y Y coordinate
     * @return Correlation value (-1 to 1)
     */
    public double getCorrelationValue(int x, int y) {
        if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) {
            return 0.0;
        }

        double propertyIntensity = getPropertyIntensity(x, y);
        double crimeIntensity = getCrimeIntensity(x, y);

        // If both values are 0, return 0 (no data)
        if (propertyIntensity == 0.0 && crimeIntensity == 0.0) {
            return 0.0;
        }

        // Calculate correlation value
        // 1 = high property values, low crime
        // -1 = low property values, high crime
        return propertyIntensity - crimeIntensity;
    }

    /**
     * Normalizes a crime count to an intensity value between 0 and 1
     * @param count Crime count
     * @return Normalized intensity (0-1)
     */
    public static double normalizeCount(double count) {
        // Will need to adjust this value based on the data
        final int MAX_EXPECTED_COUNT = 50;

        return Math.min(1.0, count / (double) MAX_EXPECTED_COUNT);
    }

    /**
     * Normalizes a property value to an intensity value between 0 and 1
     * @param value Property value
     * @return Normalized intensity (0-1)
     */
    public static double normalizeValue(double value) {
        // Will need to adjust these thresholds based on the data
        final double MIN_EXPECTED_VALUE = 100000.0;
        final double MAX_EXPECTED_VALUE = 1500000.0;

        if (value < MIN_EXPECTED_VALUE) {
            return 0.0;
        }

        if (value > MAX_EXPECTED_VALUE) {
            return 1.0;
        }

        return (value - MIN_EXPECTED_VALUE) / (MAX_EXPECTED_VALUE - MIN_EXPECTED_VALUE);
    }

    /**
     * Gets the heat value for a pixel using a specific calculation mode
     * @param x X coordinate
     * @param y Y coordinate
     * @param mode The calculation mode (0 = crime, 1 = property, 2 = correlation)
     * @return Heat value appropriate for the selected mode
     */
    public double getHeatValue(int x, int y, int mode) {
        switch (mode) {
            case 0: // Crime mode
                return getCrimeIntensity(x, y);
            case 1: // Property mode
                return getPropertyIntensity(x, y);
            case 2: // Correlation mode
                return getCorrelationValue(x, y);
            default:
                return 0.0;
        }
    }

    /**
     * Gets the maximum crime count across all pixels
     * @return Maximum crime count
     */
    public int getMaxCrimeCount() {
        int max = 0;
        for (CrimePixelData data : crimePixels.values()) {
            max = Math.max(max, data.getCount());
        }
        return max;
    }

    /**
     * Gets the maximum property value across all pixels
     * @return Maximum property value
     */
    public double getMaxPropertyValue() {
        double max = 0.0;
        for (PropertyPixelData data : propertyPixels.values()) {
            max = Math.max(max, data.getAverageValue());
        }
        return max;
    }

    /**
     * Gets the average property value across all pixels
     * @return Average property value
     */
    public double getAveragePropertyValue() {
        if (propertyPixels.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (PropertyPixelData data : propertyPixels.values()) {
            sum += data.getAverageValue();
        }
        return sum / propertyPixels.size();
    }

    /**
     * Gets the average crime count across all pixels
     * @return Average crime count
     */
    public double getAverageCrimeCount() {
        if (crimePixels.isEmpty()) {
            return 0.0;
        }

        int sum = 0;
        for (CrimePixelData data : crimePixels.values()) {
            sum += data.getCount();
        }
        return (double) sum / crimePixels.size();
    }

    /**
     * Gets all unique crime categories
     * @return Set of crime categories
     */
    public Set<String> getCrimeCategories() {
        Set<String> categories = new HashSet<>();
        for (CrimePixelData data : crimePixels.values()) {
            categories.addAll(data.getCategoryCount().keySet());
        }
        return categories;
    }

    /**
     * Gets all unique crime groups
     * @return Set of crime groups
     */
    public Set<String> getCrimeGroups() {
        Set<String> groups = new HashSet<>();
        for (CrimePixelData data : crimePixels.values()) {
            groups.addAll(data.getGroupCount().keySet());
        }
        return groups;
    }

    /**
     * Gets all unique crime types
     * @return Set of crime types
     */
    public Set<String> getCrimeTypes() {
        Set<String> types = new HashSet<>();
        for (CrimePixelData data : crimePixels.values()) {
            types.addAll(data.getGroupTypeCount().keySet());
        }
        return types;
    }

    /**
     * Gets all unique neighborhoods
     * @return Set of neighborhoods
     */
    public Set<String> getNeighborhoods() {
        Set<String> neighborhoods = new HashSet<>();
        for (PropertyPixelData data : propertyPixels.values()) {
            neighborhoods.addAll(data.getNeighborhoodCount().keySet());
        }
        return neighborhoods;
    }

    /**
     * Gets all unique assessment classes
     * @return Set of assessment classes
     */
    public Set<String> getAssessmentClasses() {
        Set<String> classes = new HashSet<>();
        for (PropertyPixelData data : propertyPixels.values()) {
            classes.addAll(data.getAssessmentClassCount().keySet());
        }
        return classes;
    }

    public Set<String> getWards() {
        Set<String> wards = new HashSet<>();
        for (PropertyPixelData data : propertyPixels.values()) {
            wards.addAll(data.getWardCount().keySet());
        }
        return wards;
    }
}