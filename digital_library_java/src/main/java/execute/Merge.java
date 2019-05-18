package execute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import model.BookMerge;
import model.GoogleBook;
import model.sortData;
import execute.*;

public class Merge {
    public ArrayList<BookMerge> mergeBook(String searchterm) throws IOException, OWLOntologyCreationException {

        Merge merge = new Merge();
        ArrayList<BookMerge> mergebooks = new ArrayList<BookMerge>();
        //get google book
        GoogleBookSearch search = new GoogleBookSearch();
        ArrayList<GoogleBook> books = new ArrayList<GoogleBook>();
        books.addAll(search.getBook(searchterm, 10));
        Collections.sort(books, new GoogleBook.CompValue());

        //get IU book
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        File inputFile = new File("/Users/victornguyen/Sites/BookDigital/src/main/resources/bookOWL.owl");
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);
        searchMethod searchMethod = new searchMethod();
        List<sortData> listOp = searchMethod.searchGloVe(searchterm, m, o);
//		List<sortData> listOp = searchMethod.searchOption1(searchterm, m, o);
        Collections.sort(listOp, new sortData.CompValue());

        // merge 2 list of books
        mergebooks = merge.mergeBook1(listOp, books);


        //use hash code to remove duplicate
        Set<BookMerge> uniqueBook = new HashSet<BookMerge>(mergebooks);
        mergebooks.clear();
        mergebooks.addAll(uniqueBook);

        return mergebooks;

    }

    public ArrayList<BookMerge> mergeBook1(List<sortData> listOp, ArrayList<GoogleBook> books) {
        ArrayList<BookMerge> mergebooks = new ArrayList<BookMerge>();
        for (int i = 0; i < books.size(); i++) {
            BookMerge book = new BookMerge();
            book.setTitle(books.get(i).getTitle());
            book.setAuthors(books.get(i).getAuthors());
            book.setYear(books.get(i).getYear());
            book.setNumCosine(books.get(i).getNumCosine());
            book.setSource("Google book");
            mergebooks.add(book);
        }

        for (int j = 0; j < listOp.size(); j++) {
            BookMerge bookModel = new BookMerge();
            bookModel.setTitle(listOp.get(j).getTitle());
            bookModel.setAuthors(listOp.get(j).getAuthor());
            bookModel.setYear(listOp.get(j).getYear());
            bookModel.setNumCosine(listOp.get(j).getCosineRate());
            bookModel.setSource("IU library");
            mergebooks.add(bookModel);
        }

        return mergebooks;
    }


    //	public boolean isDulicate(sortData sortData, GoogleBook googleBook) {
//		if(sortData.getYear().equals(googleBook.getYear())) 
//			if(sortData.getAuthor().equals(googleBook.getAuthors()))
//				if(sortData.getTitle().equals(googleBook.getTitle()))
//				{
//					return true;
//				}			
//		return false;
//				
//	}
//	
    public static void main(String[] args) throws IOException, OWLOntologyCreationException {

        Merge merge = new Merge();
        ArrayList<BookMerge> books = new ArrayList<BookMerge>();

        books.addAll(merge.mergeBook("circuit analysis"));
        Collections.sort(books, new BookMerge.CompValue());


        double sumCosine = 0;
        for (int i = 0; i < books.size(); i++) {
            System.out.println(" id: " + i);
            System.out.println(" Title: " + books.get(i).getTitle());
            System.out.println(" Author: " + books.get(i).getAuthors());
            System.out.println(" Year: " + books.get(i).getYear());
            System.out.println(" Cosine num:" + books.get(i).getNumCosine());
            System.out.println(" Source:" + books.get(i).getSource());
            System.out.println("----------");
            sumCosine = sumCosine + books.get(i).getNumCosine();
        }
        double avrCosine = sumCosine / books.size();
        System.out.println("avr cosine:" + avrCosine);
    }

}
