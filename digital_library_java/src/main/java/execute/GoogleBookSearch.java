package execute;

import similarity.Cosine;

import java.awt.print.Book;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import javax.print.Doc;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import model.GoogleBook;

public class GoogleBookSearch {

    public static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    public static final String remove = "- Ấn bản khác ";

    //get html
    public Document getSearchResults(String searchTerm, int num) throws IOException {
        String searchURL = GOOGLE_SEARCH_URL + "?q=" + searchTerm + "&num=" + num + "&tbm=bks";
        Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
        return doc;
    }

    ;
	
	public List<String> getTitles(Document doc) {
        Elements results = doc.select("h3.r > a");
        ArrayList<String> titles = new ArrayList<String>();
        for (Element result : results) {
            String title = result.text();
            titles.add(title);
        }
        return titles;
    }

    public List<String> getAuthors(Document doc) {
        Elements results = doc.select("div.s");
        ArrayList<String> authors = new ArrayList<String>();
        for (Element result : results) {
            String linkText = result.text();
            String text = linkText.substring(6, linkText.indexOf("-"));
            String author = text.replace("google.com", "");
            authors.add(author);
        }
        return authors;
    }

    public List<String> getYears(Document doc) {
        Elements results = doc.select("div.s");
        ArrayList<String> years = new ArrayList<String>();
        for (Element result : results) {
            String linkText = result.text();
            Pattern pattern = Pattern.compile("\\-(.*?)\\-");
            Matcher matcher = pattern.matcher(linkText);
            if (matcher.find()) {
                years.add(matcher.group(1).trim());
            }
        }
        return years;
    }

    public List<String> getDescriptions(Document doc) {
        Elements results = doc.select("div.s");
        ArrayList<String> descriptions = new ArrayList<String>();
        for (Element result : results) {
            String linkText = result.text();
            String des = linkText.substring(linkText.lastIndexOf("-"));
            String description;
            if (des.contains(remove)) {
                description = des.replace(remove, "");
            } else {
                description = des.replace("-", "");
            }
            descriptions.add(description);
        }
        return descriptions;
    }

    public List<GoogleBook> getBook(String searchTerm, int num) throws IOException {

        Cosine cosine = new Cosine();
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        GoogleBookSearch merge = new GoogleBookSearch();

        Document doc = merge.getSearchResults(searchTerm, num);
        List<String> titles = merge.getTitles(doc);
        List<String> authors = merge.getAuthors(doc);
        List<String> years = merge.getYears(doc);
        List<String> descriptions = merge.getDescriptions(doc);
        for (int i = 0; i < num; i++) {
            GoogleBook bookModel = new GoogleBook();
            String bookTitle = titles.get(i);
            double numCosine = cosine.similarity(searchTerm.toLowerCase(), bookTitle.toLowerCase());
            bookModel.setTitle(titles.get(i));
            bookModel.setAuthors(authors.get(i));
            bookModel.setYear(years.get(i));
            bookModel.setDescription(descriptions.get(i));
            bookModel.setNumCosine(numCosine);
            books.add(bookModel);
        }
        return books;

    }

//	 public List<BookModel> sort(List<BookModel> books){
//		Collections.sort(books, new BookModel.CompValue());;
//		return books;
//		 
//	 }
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        GoogleBookSearch merge = new GoogleBookSearch();
        int num = 50;
//		Document doc=merge.getSearchResults("technology", 10);
//		merge.getBook("technology", 10);
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        books.addAll(merge.getBook("Abstract data types in Java", num));
        Collections.sort(books, new GoogleBook.CompValue());
//		Cosine cosine = new Cosine();
//		double numCosin=cosine.similarity("Expanding Access to Science and Technology", "technology");
//		System.out.println(numCosin);
        for (int i = 0; i < num; i++) {
            System.out.println("Num " + i + " id: " + books.get(i).getId());
            System.out.println("Num " + i + " Title: " + books.get(i).getTitle());
            System.out.println("Num " + i + " Author: " + books.get(i).getAuthors());
            System.out.println("Num " + i + " Year: " + books.get(i).getYear());
            System.out.println("Num " + i + " Descriptions: " + books.get(i).getDescription());
            System.out.println(" Cosine num:" + books.get(i).getNumCosine());
            System.out.println("----------");
        }
        for (GoogleBook book : books) {
            System.out.println(" Id: " + book.getId());
            System.out.println(" Title: " + book.getTitle());
            System.out.println(" Author: " + book.getAuthors());
            System.out.println(" Year: " + book.getYear());
            System.out.println(" Descriptions: " + book.getDescription());
            System.out.println(" Cosine num:" + book.getNumCosine());
            System.out.println("----------");
        }
//		System.out.println(doc);
//		List<String> titles=merge.getTitles(doc);

//		System.out.println(titles);
//		List<String> authors=merge.getAuthors(doc);
//		for(int i=0;i<authors.size();i++) {
//			System.out.println("Num "+i+" Author: "+authors.get(i));
//		}
//		
//		List<String> years=merge.getYears(doc);
//		for(int i=0;i<years.size();i++) {
//			System.out.println("Num "+i+" Author: "+years.get(i));
//		}
//		List<String> descriptions=merge.getDescriptions(doc);
//		for(int i=0;i<descriptions.size();i++) {
//			System.out.println("Num "+i+" Author: "+descriptions.get(i));
//		}
    }

}
