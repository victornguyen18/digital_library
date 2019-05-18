package execute;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import similarity.Cosine;

public class GoogleBookSearchJava {
	public static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
	public static final String remove="- Ấn bản khác ";
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		String sLeft="google.com";
		Scanner scanner= new Scanner(System.in);
		System.out.println("Enter search term");
		String searchTerm=scanner.nextLine();
		System.out.println("Enter number of results: ");
		int num=scanner.nextInt();
		scanner.close();
		String 	searchURL=GOOGLE_SEARCH_URL+"?q="+searchTerm+"&num="+num+"&tbm=bks";
		Document doc=Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
		Elements results1=doc.select("h3.r > a");
		Elements results2=doc.select("div.s");
		
		
		for (Element result : results1) {
//			System.out.println(result);
			String linkHref=result.attr("href");
			String linkText = result.text();
			
			//****
			//get book Title
			System.out.println("Title:"+ linkText);
			System.out.println("URL::" + linkHref.substring(6, linkHref.indexOf("&")));
			
		}
//		System.out.println(results1);
//		System.out.println(results2);
		
		for (Element result : results2) {
//			System.out.println(result);
//			String linkHref=result.attr("a");
			String linkText = result.text();
//			System.out.println(linkText);
			//get author
			String text=linkText.substring(6,linkText.indexOf("-"));
//			System.out.println(text);
			//remove google.com to get author
			String author=text.replace("google.com", "");
			System.out.println("Author:" +author);
			
			//get year using pattern
			Pattern pattern= Pattern.compile("\\-(.*?)\\-");
			Matcher matcher= pattern.matcher(linkText);
			if (matcher.find()) {
			    System.out.println("Year"+matcher.group(1));
			}
//			
			// get description
			String des=linkText.substring(linkText.lastIndexOf("-"));
			String description;
			if(des.contains(remove)) {
				description=	des.replace(remove,"");
				
			} else {
				description=	des.replace("-","");
				
			}
			System.out.println("Description:"+description);
			
			
			
//			System.out.println("Text::" + linkText );
//			System.out.println("Author::" + linkText);//.substring(6,linkText.indexOf("-")) );
//			System.out.println("Author::" + linkText.substring(6,linkText.indexOf("-")) );
			
		}
		
	}
}
