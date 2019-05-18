package model;

public class publisherData {


    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getbID() {
        return bID;
    }

    public void setbID(String bID) {
        this.bID = bID;
    }

    public publisherData(String publisherName, String bID) {

        this.publisherName = publisherName;
        this.bID = bID;
    }

    public publisherData() {

    }

    private String publisherName;
    private String bID;
}
