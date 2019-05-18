package execute;

import java.io.IOException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {
    public static final String SEARCH_URL = "http://opac2.vnulib.edu.vn/F?func=find-b&local_base=IUL&con_lng=eng";

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter search term");
        String searchTerm = scanner.nextLine();
        System.out.println("Enter number of results: ");
        int num = scanner.nextInt();
        scanner.close();
//		String searchUrl=SEARCH_URL+"&num"+num+"&request="+searchTerm;
        String searchUrl = SEARCH_URL + "&request=" + searchTerm;
        Document doc = Jsoup.connect(searchUrl).userAgent("Mozilla/5.0").get();
        Elements results = doc.select("tr");
        Elements contents = doc.getElementsByAttributeStarting("valign");
//		System.out.println(content);
        for (Element content : contents) {

//			System.out.println(result);
//			System.out.println("-------");
//			String linkHref=result.attr("td.td1");
            String linkText = content.text();
            System.out.println("Result :" + linkText + "\n");
        }
    }

}
