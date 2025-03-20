package ca.macewan.thebatmap.utils.models;

public class CrimeLocation {
    private double latitude;
    private double longitude;

    public CrimeLocation(double x, double y) {
        mercatorToLatLon(x, y);
    }

    public double getLatitude() { return this.latitude; }

    public double getLongitude() { return this.longitude; }

    public void mercatorToLatLon(double x, double y) {
        double RADIUS = 6378137.0;
        this.latitude = (180.0 / Math.PI) * (2.0 * Math.atan(Math.exp(y / RADIUS)) - Math.PI / 2.0);
        this.longitude = (x / RADIUS) * (180.0 / Math.PI);
    }

    @Override
    public String toString() { return "(" + this.latitude + ", " + this.longitude + ")"; }
}
