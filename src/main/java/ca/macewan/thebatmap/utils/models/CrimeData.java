package ca.macewan.thebatmap.utils.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing crime statistics data
 */
public class CrimeData {
    private String occurrenceCategory;
    private String occurrenceGroup;
    private String occurrenceTypeGroup;
    private String intersection;
    private int objectId;
    private LocalDate dateReported;
    private CrimeLocation location;

    // Getters and setters
    public String getOccurrenceCategory() {
        return occurrenceCategory;
    }

    public void setOccurrenceCategory(String occurrenceCategory) {
        this.occurrenceCategory = occurrenceCategory;
    }

    public String getOccurrenceGroup() {
        return occurrenceGroup;
    }

    public void setOccurrenceGroup(String occurrenceGroup) {
        this.occurrenceGroup = occurrenceGroup;
    }

    public String getOccurrenceTypeGroup() {
        return occurrenceTypeGroup;
    }

    public void setOccurrenceTypeGroup(String occurrenceTypeGroup) {
        this.occurrenceTypeGroup = occurrenceTypeGroup;
    }

    public String getIntersection() {
        return intersection;
    }

    public void setIntersection(String intersection) {
        this.intersection = intersection;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public LocalDate getDateReported() {
        return dateReported;
    }

    public void setDateReported(LocalDate dateReported) {
        this.dateReported = dateReported;
    }

    public void setLocation(double x, double y) { this.location = new CrimeLocation(x, y); }

    public CrimeLocation getLocation() { return this.location; }

    @Override
    public String toString() {
        return "CrimeData{" +
                "occurrenceCategory='" + occurrenceCategory + '\'' +
                ", occurrenceGroup='" + occurrenceGroup + '\'' +
                ", occurrenceTypeGroup='" + occurrenceTypeGroup + '\'' +
                ", intersection='" + intersection + '\'' +
                ", dateReported=" + dateReported +
                '}';
    }
}