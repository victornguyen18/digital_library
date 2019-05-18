package model;

import java.awt.print.Book;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class BookMerge {
    static final AtomicInteger count = new AtomicInteger(0);
    private int id = count.getAndIncrement();
    private String title;
    private String authors;
    private String year;
    private String source;
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

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public double getNumCosine() {
        return numCosine;
    }

    public void setNumCosine(double numCosine) {
        this.numCosine = numCosine;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public BookMerge(String title, String authors, String year, String source, double numCosine) {
        super();
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.source = source;
        this.numCosine = numCosine;
    }

    public BookMerge() {

    }


    @Override
    public String toString() {
        return "BookMerge [id=" + id + ", title=" + title + ", authors=" + authors + ", year=" + year + ", source="
                + source + ", numCosine=" + numCosine + "]";
    }


    public static class CompValue implements Comparator<BookMerge> {
        @Override
        public int compare(BookMerge book1, BookMerge book2) {
            if (book1.numCosine == book2.numCosine)
                return 0;
            else if (book1.numCosine > book2.numCosine)
                return -1;
            else return 1;
        }
    }

    @Override
    public int hashCode() {

        long temp;
        temp = Double.doubleToLongBits(numCosine);
        return title.hashCode() ^ authors.hashCode() ^ (int) (temp ^ (temp >>> 32)) ^ year.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BookMerge)) {
            return false;
        }
        BookMerge book = (BookMerge) obj;
        return book.year.equals(year) && book.authors.equals(authors) && book.title.equals(title);
    }


}
