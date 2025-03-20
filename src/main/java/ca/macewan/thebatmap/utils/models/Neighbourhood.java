package ca.macewan.thebatmap.utils.models;

public class Neighbourhood {
    private final String neighbourhood;
    private final String ward;

    public Neighbourhood(String neighbourhood, String ward) {
        this.neighbourhood = neighbourhood;
        this.ward = ward;
    }

    public String getNeighbourhood() { return this.neighbourhood; }

    public String getWard() { return this.ward; }

    @Override
    public String toString() {
        return this.neighbourhood + " (" + this.ward + ")";
    }
}
