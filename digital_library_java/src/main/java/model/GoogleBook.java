package model;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;

public class GoogleBook {

    //auto increment using AtomicInteger
    static final AtomicInteger count = new AtomicInteger(0);
    private int id = count.getAndIncrement();
    private String title;
    private String authors;
    private String year;
    private String description;
    private double numCosine;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public double getNumCosine() {
        return numCosine;
    }

    public void setNumCosine(double numCosine) {
        this.numCosine = numCosine;
    }


    public GoogleBook(int id, String title, String authors, String year, String description, double numCosine) {
        super();
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.description = description;
        this.numCosine = numCosine;
    }

    public GoogleBook() {

    }

    @Override
    public String toString() {
        return "BookModel [id=" + id + ", title=" + title + ", authors=" + authors + ", year=" + year + ", description="
                + description + ", numCosine=" + numCosine + "]";
    }

    public static class CompValue implements Comparator<GoogleBook> {
        @Override
        public int compare(GoogleBook book1, GoogleBook book2) {
            if (book1.numCosine == book2.numCosine)
                return 0;
            else if (book1.numCosine > book2.numCosine)
                return -1;
            else return 1;
        }
    }


}
