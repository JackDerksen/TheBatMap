package ca.macewan.thebatmap.utils.models;

public class Address {
    private final String houseNum;
    private final String street;

    public Address(String houseNum, String street) {
        this.houseNum = houseNum;
        this.street = street;
    }

    public String getHouseNum() { return this.houseNum; }

    public String getStreet() { return this.street; }

    @Override
    public String toString() {
        StringBuilder address = new StringBuilder();
        if (houseNum != null && !houseNum.isEmpty()) {
            address.append(houseNum).append(" ");
        }
        if (street != null && !street.isEmpty()) {
            address.append(street);
        }
        return address.toString().trim();
    }
}
