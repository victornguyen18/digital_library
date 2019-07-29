package execute;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import similarity.Cosine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.*;

import model.GoogleBook;

public class GoogleBookSearch {

    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    private static final String remove = "- Ấn bản khác ";
    private static final String remove1 = "-Xem trước";

    //get html
    public Document getSearchResults(String searchTerm, int num, int start) {
        //Get page
        String searchURL = GOOGLE_SEARCH_URL + "?q=" + searchTerm + "&start=" + start + "&tbm=bks&num=" + num;
        try {
            Document doc = Jsoup.connect(searchURL)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
//                    .userAgent("Mozilla/5.0")
                    .maxBodySize(0)
                    .get();
//            Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").maxBodySize(0)
//                    .timeout(600000).get();
            Date date = new Date();
            System.out.println(date);
//            String fileName = "/Users/victornguyen/Desktop/test" + date + ".html";
//            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//            writer.write(doc.toString());
            System.out.println(searchURL);
            return doc;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
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
            String text = "";
            if (linkText.indexOf('-') != -1 && linkText.indexOf('-') > 6) {
                text = linkText.substring(6, linkText.indexOf('-'));
            } else {
                text = "Cannot find author";
            }
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
            String year = "";
            if (matcher.find()) {
                year = matcher.group(1).trim();
            } else {
                year = "None";
            }
            years.add(year);
        }
        return years;
    }

    public List<String> getDescriptions(Document doc) {
        Elements results = doc.select("div.s");
        ArrayList<String> descriptions = new ArrayList<String>();
        for (Element result : results) {
            String linkText = result.text();
            String des = "";
            if (linkText.lastIndexOf('-') != -1) {
                des = linkText.substring(linkText.lastIndexOf('-'));
            } else {
                des = "Cannot find descriptions";
            }
//            String des = linkText.substring(linkText.lastIndexOf("-"));
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

    public List<GoogleBook> getBook(String searchTerm, int num, int start) throws IOException {
        Cosine cosine = new Cosine();
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        GoogleBookSearch merge = new GoogleBookSearch();

        Document doc = merge.getSearchResults(searchTerm, num, start);
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

    public String getBookJson(String searchTerm, int num, int start) throws IOException {
        JSONArray bookJsons = new JSONArray();
        Cosine cosine = new Cosine();
        GoogleBookSearch merge = new GoogleBookSearch();
        Document doc = merge.getSearchResults(searchTerm.replace(' ', '+'), num, start);
        List<String> titles = merge.getTitles(doc);
        List<String> authors = merge.getAuthors(doc);
        List<String> years = merge.getYears(doc);
        List<String> descriptions = merge.getDescriptions(doc);
        List<String> images = merge.getImages(doc);
        for (int i = 0; i < num; i++) {
            JSONObject tmpJson = new JSONObject();
            String bookTitle = titles.get(i);
            tmpJson.put("title", bookTitle);
            tmpJson.put("author", authors.get(i));
            tmpJson.put("year", years.get(i));
            tmpJson.put("des", descriptions.get(i));
            tmpJson.put("image", images.get(i));
            double numCosine = cosine.similarity(searchTerm.toLowerCase(), bookTitle.toLowerCase());
            tmpJson.put("num_cosine", numCosine);
            bookJsons.add(tmpJson.toString());
        }
        return bookJsons.toString();
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        GoogleBookSearch merge = new GoogleBookSearch();
        int page = 0;
        int num = 50;
        int start = page * num;
        String jsonBooks = merge.getBookJson("technology", num, start);
//        String jsonBooks = merge.getBookJson("Abstract data types in Java", num, start);
        System.out.println(jsonBooks);
    }

}
