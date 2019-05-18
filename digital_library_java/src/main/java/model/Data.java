package model;

public class Data {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Data(String title, String author, String bID, String year,
                String type, String qty, String collection, String publisher) {

        this.title = title;
        this.author = author;
        this.bID = bID;
        this.year = year;
        this.type = type;
        this.qty = qty;
        this.collection = collection;
        this.publisher = publisher;
    }

    private String title;
    private String author;
    private String bID;
    private String year;
    private String type;
    private String qty;
    private String collection;
    private String publisher;


    public Data() {

    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s - %s - %s - %s - %s - %s", title, author, bID, year, collection, publisher, type, qty);
    }


}
