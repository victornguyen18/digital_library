package model;

public class authorData {

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getbID() {
        return bID;
    }

    public void setbID(String bID) {
        this.bID = bID;
    }

    public authorData(String author, String bID) {

        this.author = author;
        this.bID = bID;
    }

    public authorData() {

    }

    private String author;
    private String bID;
}
