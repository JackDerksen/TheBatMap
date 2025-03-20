package ca.macewan.thebatmap.utils.models;

public class Location {
    private double latitude;
    private double longitude;
    private String pointLocation;

    public Location(double latitude, double longitude, String pointLocation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.pointLocation = pointLocation;
    }

    public double getLatitude() { return this.latitude; }

    public double getLongitude() { return this.longitude; }

    public String getPointLocation() { return pointLocation; }

    @Override
    public String toString() {
        return "(" + this.latitude + ", " + this.longitude + ")";
    }
}
