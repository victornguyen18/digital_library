package model;

public class collectionData {

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getCollectName() {
        return collectName;
    }

    public void setCollectName(String collectName) {
        this.collectName = collectName;
    }

    public collectionData(String cId, String collectName) {
        this.cId = cId;
        this.collectName = collectName;
    }

    public collectionData() {

    }

    private String cId;
    private String collectName;
}
