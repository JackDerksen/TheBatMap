package ca.macewan.thebatmap.utils.models;

/**
 * Model class representing property assessment data
 */
public class PropertyData {
    private String accountNumber;
    private Address address;
    private Neighbourhood neighbourhood;
    private double assessedValue;
    private double latitude;
    private double longitude;
    private String pointLocation;
    private int assessmentClass1Percent;
    private int assessmentClass2Percent;
    private int assessmentClass3Percent;
    private String assessmentClass1;
    private String assessmentClass2;
    private String assessmentClass3;

    // Getters and setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAddress(String suite, String houseNum, String street, String garage) {
        this.address = new Address(suite,  houseNum, street, garage);
    }

    public Address getAddress() { return this.address; }

    public void setNeighbourhood(String neighbourhoodId, String neighbourhood, String ward) {
        this.neighbourhood = new Neighbourhood(neighbourhoodId, neighbourhood, ward);
    }

    public Neighbourhood getNeighbourhood() { return this.neighbourhood; }

    public double getAssessedValue() {
        return assessedValue;
    }

    public void setAssessedValue(double assessedValue) {
        this.assessedValue = assessedValue;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPointLocation() {
        return pointLocation;
    }

    public void setPointLocation(String pointLocation) {
        this.pointLocation = pointLocation;
    }

    public int getAssessmentClass1Percent() {
        return assessmentClass1Percent;
    }

    public void setAssessmentClass1Percent(int assessmentClass1Percent) {
        this.assessmentClass1Percent = assessmentClass1Percent;
    }

    public int getAssessmentClass2Percent() {
        return assessmentClass2Percent;
    }

    public void setAssessmentClass2Percent(int assessmentClass2Percent) {
        this.assessmentClass2Percent = assessmentClass2Percent;
    }

    public int getAssessmentClass3Percent() {
        return assessmentClass3Percent;
    }

    public void setAssessmentClass3Percent(int assessmentClass3Percent) {
        this.assessmentClass3Percent = assessmentClass3Percent;
    }

    public String getAssessmentClass1() {
        return assessmentClass1;
    }

    public void setAssessmentClass1(String assessmentClass1) {
        this.assessmentClass1 = assessmentClass1;
    }

    public String getAssessmentClass2() {
        return assessmentClass2;
    }

    public void setAssessmentClass2(String assessmentClass2) {
        this.assessmentClass2 = assessmentClass2;
    }

    public String getAssessmentClass3() {
        return assessmentClass3;
    }

    public void setAssessmentClass3(String assessmentClass3) {
        this.assessmentClass3 = assessmentClass3;
    }

    @Override
    public String toString() {
        return "PropertyData{" +
                "accountNumber='" + accountNumber + '\'' +
                ", address='" + address + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", assessedValue=" + assessedValue +
                '}';
    }
}