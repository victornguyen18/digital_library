package model;

public class cosineResultData {

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public double getResultCosine() {
        return resultCosine;
    }

    public void setResultCosine(double resultCosine) {
        this.resultCosine = resultCosine;
    }

    public cosineResultData(String resultName, double resultCosine) {

        this.resultName = resultName;
        this.resultCosine = resultCosine;
    }

    public cosineResultData() {

    }

    private String resultName;
    private double resultCosine;
}
