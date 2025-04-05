package ca.macewan.thebatmap.utils.general;

/**
 * Utility class for converting between geographic coordinates (latitude/longitude)
 * and pixel coordinates on the Edmonton map image.
 */
public class CoordinateToPixel {
    // Map image dimensions (pixels)
    private static final int MAP_WIDTH = 1812;
    private static final int MAP_HEIGHT = 1850;

    // Lat/lon boundaries of the map image
    private static final double TOP_LEFT_LAT = 53.652716;
    private static final double TOP_LEFT_LON = -113.698940;
    private static final double BOTTOM_RIGHT_LAT = 53.420792;
    private static final double BOTTOM_RIGHT_LON = -113.317686;

    public static int getMapWidth() {
        return MAP_WIDTH;
    }

    public static int getMapHeight() {
        return MAP_HEIGHT;
    }

    /**
     * Converts latitude and longitude to pixel X coordinate on the map image
     * @param longitude The longitude value to convert
     * @return The X coordinate (pixel) on the map image
     */
    public static int longitudeToX(double longitude) {
        return (int) Math.round(MAP_WIDTH *
                (longitude - TOP_LEFT_LON) / (BOTTOM_RIGHT_LON - TOP_LEFT_LON));
    }

    /**
     * Converts latitude and longitude to pixel Y coordinate on the map image
     * @param latitude The latitude value to convert
     * @return The Y coordinate (pixel) on the map image
     */
    public static int latitudeToY(double latitude) {
        return (int) Math.round(MAP_HEIGHT *
                (TOP_LEFT_LAT - latitude) / (TOP_LEFT_LAT - BOTTOM_RIGHT_LAT));
    }

    /**
     * Converts from pixel X coordinate to longitude
     * @param x The X coordinate (pixel) on the map image
     * @return The longitude value
     */
    public static double xToLongitude(int x) {
        return TOP_LEFT_LON + (x * (BOTTOM_RIGHT_LON - TOP_LEFT_LON) / MAP_WIDTH);
    }

    /**
     * Converts from pixel Y coordinate to latitude
     * @param y The Y coordinate (pixel) on the map image
     * @return The latitude value
     */
    public static double yToLatitude(int y) {
        return TOP_LEFT_LAT - (y * (TOP_LEFT_LAT - BOTTOM_RIGHT_LAT) / MAP_HEIGHT);
    }

    /**
     * Converts latitude and longitude to pixel coordinates
     * @param latitude The latitude value
     * @param longitude The longitude value
     * @return int array with [x, y] pixel coordinates
     */
    public static int[] geoToPixel(double latitude, double longitude) {
        int x = longitudeToX(longitude);
        int y = latitudeToY(latitude);
        return new int[] {x, y};
    }

    /**
     * Checks if the provided coordinates are within the bounds of the map
     * @param latitude The latitude to check
     * @param longitude The longitude to check
     * @return true if coordinates are within map bounds, false otherwise
     */
    public static boolean outOfBounds(double latitude, double longitude) {
        return !(latitude <= TOP_LEFT_LAT) ||
                !(latitude >= BOTTOM_RIGHT_LAT) ||
                !(longitude >= TOP_LEFT_LON) ||
                !(longitude <= BOTTOM_RIGHT_LON);
    }
}