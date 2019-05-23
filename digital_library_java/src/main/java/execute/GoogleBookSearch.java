package execute;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import similarity.Cosine;

import java.util.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.*;

import model.GoogleBook;

public class GoogleBookSearch {

    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    private static final String remove = "- Ấn bản khác ";

    //get html
    public Document getSearchResults(String searchTerm, int page) throws IOException {
        //Get page
        int start = 10 * page;
        String searchURL = GOOGLE_SEARCH_URL + "?q=" + searchTerm + "&start=" + start + "&tbm=bks&num=10";
        Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
        System.out.println(searchURL);
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

    public List<String> getImages(Document doc) {
        Elements results = doc.select("img");
        ArrayList<String> images = new ArrayList<String>();
        for (Element result : results) {
            String image = result.attr("src");
            if (image.indexOf('&') != -1) {
                image = image.substring(0, image.indexOf('&')) + "&printsec=frontcover&img=1";
            }
            images.add(image);
        }
        return images;
    }

    public List<GoogleBook> getBook(String searchTerm, int page) throws IOException {
        int num = page * 10;
        Cosine cosine = new Cosine();
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        GoogleBookSearch merge = new GoogleBookSearch();

        Document doc = merge.getSearchResults(searchTerm, page);
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

    public String getBookJson(String searchTerm, int page) throws IOException {
        int num = 10;
        JsonArray bookJsons = new JsonArray();
        Cosine cosine = new Cosine();
        GoogleBookSearch merge = new GoogleBookSearch();
        Document doc = merge.getSearchResults(searchTerm.replace(' ', '+'), page);
        List<String> titles = merge.getTitles(doc);
        List<String> authors = merge.getAuthors(doc);
        List<String> years = merge.getYears(doc);
        List<String> descriptions = merge.getDescriptions(doc);
        List<String> images = merge.getImages(doc);
        for (int i = 0; i < num; i++) {
            JSONObject tmpJson = new JSONObject();
            String bookTitle = titles.get(i);
//            String json_tmp = "";
//            json_tmp += "'title':'" + titles.get(i) + "',";
            tmpJson.put("title", titles.get(i));
//            json_tmp += "'author':'" + authors.get(i) + "',";
            tmpJson.put("author", authors.get(i));
//            json_tmp += "'year':'" + years.get(i) + "',";
            tmpJson.put("year", years.get(i));
//            json_tmp += "'descriptions':'" + descriptions.get(i) + "',";
            tmpJson.put("des", descriptions.get(i));
//            json_tmp += "'image':'" + images.get(i) + "',";
            tmpJson.put("image", images.get(i));
            double numCosine = cosine.similarity(searchTerm.toLowerCase(), bookTitle.toLowerCase());
//            json_tmp += "'num_cosine':'" + numCosine + "'";
            tmpJson.put("num_cosine", numCosine);
            System.out.println(tmpJson.toJSONString());
            bookJsons.add(tmpJson.toString());
        }
        return bookJsons.toString();
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        GoogleBookSearch merge = new GoogleBookSearch();
        int page = 0;
        int num = page * 10;
//		Document doc=merge.getSearchResults("technology", 10);
//		merge.getBook("technology", 10);
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        String jsonBooks = merge.getBookJson("Abstract data types in Java", page);
        System.out.println(jsonBooks);
//		Cosine cosine = new Cosine();
//		double numCosin=cosine.similarity("Expanding Access to Science and Technology", "technology");
//		System.out.println(numCosin);
//        for (int i = 0; i < num; i++) {
//            System.out.println("Num " + i + " id: " + books.get(i).getId());
//            System.out.println("Num " + i + " Title: " + books.get(i).getTitle());
//            System.out.println("Num " + i + " Author: " + books.get(i).getAuthors());
//            System.out.println("Num " + i + " Year: " + books.get(i).getYear());
//            System.out.println("Num " + i + " Descriptions: " + books.get(i).getDescription());
//            System.out.println(" Cosine num:" + books.get(i).getNumCosine());
//            System.out.println("----------");
//        }
//        for (GoogleBook book : books) {
//            System.out.println(" Id: " + book.getId());
//            System.out.println(" Title: " + book.getTitle());
//            System.out.println(" Author: " + book.getAuthors());
//            System.out.println(" Year: " + book.getYear());
//            System.out.println(" Descriptions: " + book.getDescription());
//            System.out.println(" Cosine num:" + book.getNumCosine());
//            System.out.println("----------");
//        }
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
