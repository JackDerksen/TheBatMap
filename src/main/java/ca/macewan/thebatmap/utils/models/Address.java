package ca.macewan.thebatmap.utils.models;

public class Address {
    private final String suite;
    private final String houseNum;
    private final String street;
    private final String garage;

    public Address(String suite, String houseNum, String street, String garage) {
        this.suite = suite;
        this.houseNum = houseNum;
        this.street = street;
        this.garage = garage;
    }

    public String getSuite() { return this.suite; }

    public String getHouseNum() { return this.houseNum; }

    public String getStreet() { return this.street; }

    public String getGarage() { return this.garage; }

    @Override
    public String toString() {
        StringBuilder address = new StringBuilder();
        if (suite != null && !suite.isEmpty()) {
            address.append(suite).append(" - ");
        }
        if (houseNum != null && !houseNum.isEmpty()) {
            address.append(houseNum).append(" ");
        }
        if (street != null && !street.isEmpty()) {
            address.append(street);
        }
        return address.toString().trim();
    }
}
