package model;

import java.util.Comparator;

public class sortData {

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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
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

    public double getCosineRate() {
        return cosineRate;
    }

    public void setCosineRate(double cosineRate) {
        this.cosineRate = cosineRate;
    }

    public sortData(String title, String author, String publisher,
                    String collection, String bID, String year, String type,
                    String qty, double cosineRate) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.collection = collection;
        this.bID = bID;
        this.year = year;
        this.type = type;
        this.qty = qty;
        this.cosineRate = cosineRate;
    }

    private String title;

    public sortData() {

    }

    private String author;
    private String publisher;
    private String collection;
    private String bID;
    private String year;
    private String type;
    private String qty;
    private double cosineRate;

    public static class CompValue implements Comparator<sortData> {
        @Override
        public int compare(sortData arg0, sortData arg1) {
            if (arg0.cosineRate > arg1.cosineRate) return -1;
            if (arg0.cosineRate < arg1.cosineRate) return 1;
            return 0;
        }
    }


}
