package ca.macewan.thebatmap.utils.models;

public class Neighbourhood {
    private final String neighbourhoodId;
    private final String neighbourhood;
    private final String ward;

    public Neighbourhood(String neighbourhoodId, String neighbourhood, String ward) {
        this.neighbourhoodId = neighbourhoodId;
        this.neighbourhood = neighbourhood;
        this.ward = ward;
    }

    public String getNeighbourhoodId() { return this.neighbourhoodId; }

    public String getNeighbourhood() { return this.neighbourhood; }

    public String getWard() { return this.ward; }

    @Override
    public String toString() {
        return this.neighbourhood + " (" + this.ward + ")";
    }
}
