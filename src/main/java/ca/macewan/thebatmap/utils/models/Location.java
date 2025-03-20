package ca.macewan.thebatmap.utils.models;

public class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return this.latitude; }

    public double getLongitude() { return this.longitude; }

    @Override
    public String toString() {
        return "(" + this.latitude + ", " + this.longitude + ")";
    }
}
