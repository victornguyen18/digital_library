package model;

public class bookData {

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public bookData(String title, String bID, String year, String type,
                    String qty) {

        this.title = title;
        this.bID = bID;
        this.year = year;
        this.type = type;
        this.qty = qty;
    }

    public bookData() {

    }

    private String title;
    private String bID;
    private String year;
    private String type;
    private String qty;
}
