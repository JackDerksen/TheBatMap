package ca.macewan.thebatmap.utils.models;

public class Assessment {
    private final double assessedValue;
    private final int assessmentClass1Percent;
    private final int assessmentClass2Percent;
    private final int assessmentClass3Percent;
    private final String assessmentClass1;
    private final String assessmentClass2;
    private final String assessmentClass3;

    public Assessment(double assessedValue, int assessmentClass1Percent, int assessmentClass2Percent, int assessmentClass3Percent,
                      String assessmentClass1, String assessmentClass2, String assessmentClass3) {
        this.assessedValue = assessedValue;
        this.assessmentClass1Percent = assessmentClass1Percent;
        this.assessmentClass2Percent = assessmentClass2Percent;
        this.assessmentClass3Percent = assessmentClass3Percent;
        this.assessmentClass1 = assessmentClass1;
        this.assessmentClass2 = assessmentClass2;
        this.assessmentClass3 = assessmentClass3;
    }

    public double getAssessedValue() { return this.assessedValue; }

    public String getAssessment1() { return this.assessmentClass1 + " " + this.assessmentClass1Percent + "%"; }

    public String getAssessment2() { return this.assessmentClass2 + " " + this.assessmentClass2Percent + "%"; }

    public String getAssessment3() { return this.assessmentClass3 + " " + this.assessmentClass3Percent + "%"; }

    @Override
    public String toString() {
        StringBuilder assessmentCombined = new StringBuilder();

        assessmentCombined.append("[");
        if (assessmentClass1 != null && !assessmentClass1.isEmpty()) {
            assessmentCombined.append(assessmentClass1).append(" ").append(assessmentClass1Percent).append("%");
        }
        if (assessmentClass2 != null && !assessmentClass2.isEmpty()) {
            assessmentCombined.append(", ").append(assessmentClass2).append(" ").append(assessmentClass2Percent).append("%");
        }
        if (assessmentClass3 != null && !assessmentClass3.isEmpty()) {
            assessmentCombined.append(", ").append(assessmentClass3).append(" ").append(assessmentClass3Percent).append("%");
        }
        assessmentCombined.append("]");

        String assessment = assessmentCombined.toString().trim();

        return String.format("%,f0", this.assessedValue) +
                "\nAssessment Class: " + assessment;
    }
}
