package execute;

import similarity.Cosine;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import execute.readDataFromExcel;
import execute.reasonerOWL;

import org.apache.http.impl.client.AIMDBackoffManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import objects.Cooccurrence;
import objects.Vocabulary;
import utils.Methods;
import utils.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jblas.DoubleMatrix;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import model.Data;
import model.allData;
import model.authorData;
import model.bookData;
import model.collectionData;
import model.cosineResultData;
import model.publisherData;
import model.searchData;
import model.sortData;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Collections;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class searchMethod {

    private static final String BASE_URL = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL";
    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static Set<String> getGlovewords(String input) {
        String file = "src/main/resources/test.txt";
        Options options = new Options();
        options.debug = true;
        Vocabulary vocab = GloVe.build_vocabulary(file, options);
        options.window_size = 3;
        List<Cooccurrence> c = GloVe.build_cooccurrence(vocab, file, options);
        options.iterations = 10;
        options.vector_size = 10;
        options.debug = true;
        DoubleMatrix W = GloVe.train(vocab, c, options);
        Set<String> similarset = new LinkedHashSet<String>();
        List<String> similars = Methods.most_similar(W, vocab, input, 10);
        for (String similar : similars) {
            System.out.println(similar);
            similarset.add(similar);
        }
        return similarset;

    }

    public static Set<String> getWordNet(String input) {
        JSONParser parser = new JSONParser();
        Set<String> synset = new LinkedHashSet<String>();
        try {
            Object obj = parser.parse(
                    new FileReader("src/main/resources/testJson.json"));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            JSONArray msg = (JSONArray) jsonObject.get(input);
            if (msg != null) {
                Iterator<String> iterator = msg.iterator();
                while (iterator.hasNext()) {
                    String keySet = iterator.next();
                    synset.add(keySet);
                }
            } else {
                System.out.println("not exist");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return synset;
    }

    public static List<allData> searchOnlyCosine(String searchInput, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {
        // 2.Get the list of book ontology data
        reasonerOWL reasoner = new reasonerOWL();
        List<allData> ontologyList = reasoner.getDataPropertyValue();
        // 3.Get Cosine and give an example of search input
        Cosine cosine = new Cosine();

        /*----------------------------------------------------*/
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        List<allData> listChosenData = new ArrayList<allData>();
        Set<String> setfinalBookId = new LinkedHashSet<String>();
        /*---------------------------------------------------------------*/
        /* 1.Start Get ID of cosine similarity books */

        for (allData allList : ontologyList) {
            String bookTitle = allList.getbData().getTitle();
            String bookId = allList.getbData().getbID();
            String cosinId = "";
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + bookId, pm);
            if (cosine.similarity(bookTitle.toLowerCase(), searchInput.toLowerCase()) > 0.5) {
                for (OWLLiteral bookTitleLit : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    String titleFromOnto = bookTitleLit.getLiteral();
                    if (titleFromOnto.equals(bookTitle)) {
                        OWLNamedIndividual cosinBooks = selectedBook;
                        /* System.out.println("cosin Books :" + cosinBooks); */
                        for (OWLLiteral cosinIdLit : rs.getDataPropertyValues(cosinBooks, hasbookId)) {
                            cosinId = cosinIdLit.getLiteral();
                            setfinalBookId.add(cosinId);
                        }
                    }
                }
            }
        }

        // 1.End
        /*---------------------------------------------------------------*/
        for (String finalId : setfinalBookId) {
            /* System.out.println(finalId); */
            bookData bd = new bookData();
            authorData ad = new authorData();
            publisherData pd = new publisherData();
            collectionData cd = new collectionData();
            allData allData = new allData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + finalId, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + finalId, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + finalId, pm);
            for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                bd.setbID(bookId.getLiteral());
                                bd.setTitle(bookTitle.getLiteral());
                                bd.setType(bookType.getLiteral());
                                bd.setYear(bookYear.getLiteral());
                                bd.setQty(bookQty.getLiteral());
                                allData.setbData(bd);
                            }
                        }
                    }
                }
            }
            for (OWLLiteral authorId : rs.getDataPropertyValues(selectedAuthor, hasAuthorId)) {
                for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                    ad.setbID(authorId.getLiteral());
                    ad.setAuthor(authorName.getLiteral());
                    allData.setaData(ad);
                }
            }
            for (OWLLiteral publisherId : rs.getDataPropertyValues(selectedPublisher, haspublisherId)) {
                for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                    pd.setbID(publisherId.getLiteral());
                    pd.setPublisherName(publisherName.getLiteral());
                    allData.setpData(pd);
                }
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectId : rs.getDataPropertyValues(finalChosenCo, hascollectionId)) {
                    for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        allData.setcData(cd);
                    }
                }
            }
            /* End of Collection */
            listChosenData.add(allData);
        }
        /* 7.End */
        return listChosenData;
    }

    public static List<allData> searchByPub(String searchInput, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {
        // 2.Get the list of book ontology data
        reasonerOWL reasoner = new reasonerOWL();
        List<allData> ontologyList = reasoner.getDataPropertyValue();
        // 3.Get Cosine and give an example of search input
        Cosine cosine = new Cosine();

        /*----------------------------------------------------*/
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        OWLObjectProperty hasAuthor = df.getOWLObjectProperty(":hasAuthor", pm);
        OWLObjectProperty hasPublisher = df.getOWLObjectProperty(":hasPublisher", pm);
        OWLObjectProperty isPublisherOf = df.getOWLObjectProperty(":isPublisherOf", pm);
        OWLObjectProperty isCollectionOf = df.getOWLObjectProperty(":isCollectionOf", pm);
        OWLObjectProperty isAuthorOf = df.getOWLObjectProperty(":isAuthorOf", pm);
        /**/
        List<allData> listChosenData = new ArrayList<allData>();
        Set<String> setfinalBookId = new LinkedHashSet<String>();
        /*---------------------------------------------------------------*/
        /* 1.Start Get ID of cosine similarity books */

        Set<String> setId = new LinkedHashSet<String>();
        for (allData allList : ontologyList) {
            String bookTitle = allList.getbData().getTitle();
            String bookId = allList.getbData().getbID();
            String cosinId = "";
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + bookId, pm);
            if (cosine.similarity(bookTitle.toLowerCase(), searchInput.toLowerCase()) > 0.5) {
                for (OWLLiteral bookTitleLit : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    String titleFromOnto = bookTitleLit.getLiteral();
                    if (titleFromOnto.equals(bookTitle)) {
                        OWLNamedIndividual cosinBooks = selectedBook;
                        /* System.out.println("cosin Books :" + cosinBooks); */
                        for (OWLLiteral cosinIdLit : rs.getDataPropertyValues(cosinBooks, hasbookId)) {
                            cosinId = cosinIdLit.getLiteral();
                            setId.add(cosinId);
                            setfinalBookId.add(cosinId);
                        }
                    }
                }
            }
        }
        // 1.End
        /*---------------------------------------------------------------*/

        /* 2.Start get the publishers of cosine results */
        /* System.out.println("1.Get Cosine ID"); */
        Set<String> setPub = new LinkedHashSet<String>();
        Set<String> setCosinAuthor = new LinkedHashSet<String>();
        for (String cosinBid : setId) {
            /* System.out.println(cosinBid); */
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + cosinBid, pm);
            for (OWLNamedIndividual PubOfBooks : rs.getObjectPropertyValues(selectedBook, hasPublisher)
                    .getFlattened()) {
                String selectedPub = renderer.render(PubOfBooks);
                setPub.add(selectedPub);
            }
            for (OWLNamedIndividual authorOfCosin : rs.getObjectPropertyValues(selectedBook, hasAuthor)
                    .getFlattened()) {
                String cosinAuthor = renderer.render(authorOfCosin);
                setCosinAuthor.add(cosinAuthor);
            }
        }
        // 2.End
        /*---------------------------------------------------------------*/
        /* 3.Get all book of the author */
        Set<String> setBookOfAu = new LinkedHashSet<String>();
        for (String auSet : setCosinAuthor) {
            OWLNamedIndividual selectedAu = df.getOWLNamedIndividual(":" + auSet, pm);
            for (OWLNamedIndividual BooksOfAu : rs.getObjectPropertyValues(selectedAu, isAuthorOf).getFlattened()) {
                String selectedBook = renderer.render(BooksOfAu);
                setBookOfAu.add(selectedBook);
            }
        }
        /* 3.End */
        /*---------------------------------------------------------------*/
        /* 4.Get all publishers of the above book */
        Set<String> setPubOf = new LinkedHashSet<String>();
        for (String bookOfAu : setBookOfAu) {
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":" + bookOfAu, pm);
            for (OWLNamedIndividual pubOfBook : rs.getObjectPropertyValues(selectedBook, hasPublisher).getFlattened()) {
                String selectedPub = renderer.render(pubOfBook);
                setPubOf.add(selectedPub);
            }
        }
        /* 4.End */
        /*---------------------------------------------------------------*/
        /* 5.Get final set of pub */
        Set<String> setFinalPub = new LinkedHashSet<String>();
        for (String pubCosine : setPub) {
            for (String pubSet : setPubOf) {
                /* System.out.println("-------------"+cosinAu); */
                if (pubSet.equals(pubCosine)) {
                    setFinalPub.add(pubSet);
                }
            }
        }
        /* 5.End */
        /*---------------------------------------------------------------*/
        /* 6.Get final set of book ID */
        for (String finalPub : setFinalPub) {
            for (String cosinBid : setId) {
                String newId = finalPub.substring(9);
                if (!newId.equals(cosinBid)) {
                    setfinalBookId.add(newId);
                }
            }
        }
        /* 6.End *//*---------------------------------------------------------------*/
        // *7.Final Get data property values
        System.out.println("Final book ID : ");
        for (String finalId : setfinalBookId) {
            /* System.out.println(finalId); */
            bookData bd = new bookData();
            authorData ad = new authorData();
            publisherData pd = new publisherData();
            collectionData cd = new collectionData();
            allData allData = new allData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + finalId, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + finalId, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + finalId, pm);
            for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                bd.setbID(bookId.getLiteral());
                                bd.setTitle(bookTitle.getLiteral());
                                bd.setType(bookType.getLiteral());
                                bd.setYear(bookYear.getLiteral());
                                bd.setQty(bookQty.getLiteral());
                                allData.setbData(bd);
                            }
                        }
                    }
                }
            }
            for (OWLLiteral authorId : rs.getDataPropertyValues(selectedAuthor, hasAuthorId)) {
                for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                    ad.setbID(authorId.getLiteral());
                    ad.setAuthor(authorName.getLiteral());
                    allData.setaData(ad);
                }
            }
            for (OWLLiteral publisherId : rs.getDataPropertyValues(selectedPublisher, haspublisherId)) {
                for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                    pd.setbID(publisherId.getLiteral());
                    pd.setPublisherName(publisherName.getLiteral());
                    allData.setpData(pd);
                }
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectId : rs.getDataPropertyValues(finalChosenCo, hascollectionId)) {
                    for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        allData.setcData(cd);
                    }
                }
            }
            /* End of Collection */
            listChosenData.add(allData);
        }
        /* 7.End */
        return listChosenData;
    }

    public static List<allData> searchByCollect(String searchInput, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {

        // 2.Get the list of book ontology data
        reasonerOWL reasoner = new reasonerOWL();
        List<allData> ontologyList = reasoner.getDataPropertyValue();
        // 3.Get Cosine and give an example of search input
        Cosine cosine = new Cosine();

        /*----------------------------------------------------*/
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /*----------------------------------------------------*/
        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        OWLObjectProperty hasAuthor = df.getOWLObjectProperty(":hasAuthor", pm);
        OWLObjectProperty hasPublisher = df.getOWLObjectProperty(":hasPublisher", pm);
        OWLObjectProperty isPublisherOf = df.getOWLObjectProperty(":isPublisherOf", pm);
        OWLObjectProperty isCollectionOf = df.getOWLObjectProperty(":isCollectionOf", pm);
        OWLObjectProperty isAuthorOf = df.getOWLObjectProperty(":isAuthorOf", pm);
        /**/
        List<allData> listChosenData = new ArrayList<allData>();
        Set<String> setfinalBookId = new LinkedHashSet<String>();
        /*---------------------------------------------------------------*/
        /* 1.Start Get ID of cosine similarity books */
        Set<String> setId = new LinkedHashSet<String>();
        for (allData allList : ontologyList) {
            String bookTitle = allList.getbData().getTitle();
            String bookId = allList.getbData().getbID();
            String cosinId = "";
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + bookId, pm);
            if (cosine.similarity(bookTitle.toLowerCase(), searchInput.toLowerCase()) > 0.5) {
                for (OWLLiteral bookTitleLit : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    String titleFromOnto = bookTitleLit.getLiteral();
                    if (titleFromOnto.equals(bookTitle)) {
                        OWLNamedIndividual cosinBooks = selectedBook;
                        /* System.out.println("cosin Books :" + cosinBooks); */
                        for (OWLLiteral cosinIdLit : rs.getDataPropertyValues(cosinBooks, hasbookId)) {
                            cosinId = cosinIdLit.getLiteral();
                            setId.add(cosinId);
                            setfinalBookId.add(cosinId);
                        }
                    }
                }
            }
        }
        // 1.End
        /*---------------------------------------------------------------*/
        /* 2.Start Get the collection of the similarity books */
        /* System.out.println("1.Get Cosine ID"); */
        Set<String> setCo = new LinkedHashSet<String>();
        Set<String> setCosinAuthor = new LinkedHashSet<String>();
        for (String cosinBid : setId) {
            /* System.out.println(cosinBid); */
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + cosinBid, pm);
            for (OWLNamedIndividual CoOfBooks : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String selectedCollection = renderer.render(CoOfBooks);
                setCo.add(selectedCollection);
            }
            for (OWLNamedIndividual authorOfCosin : rs.getObjectPropertyValues(selectedBook, hasAuthor)
                    .getFlattened()) {
                String cosinAuthor = renderer.render(authorOfCosin);
                setCosinAuthor.add(cosinAuthor);
            }
        }
        // 2.
        /*---------------------------------------------------------------*/
        /* 3.Start Get all books of the collection */
        /* System.out.println("2.Set of collection : "); */
        Set<String> bookOfCo = new LinkedHashSet<String>();
        for (String collect : setCo) {
            /* System.out.println(collect); */
            OWLNamedIndividual selectedCo = df.getOWLNamedIndividual(":" + collect, pm);
            for (OWLNamedIndividual BooksOfCo : rs.getObjectPropertyValues(selectedCo, isCollectionOf).getFlattened()) {
                String selectedBook = renderer.render(BooksOfCo);
                bookOfCo.add(selectedBook);
            }
        }
        /* 3.End */
        /*---------------------------------------------------------------*/
        /* 4.Start */
        /* System.out.println("3.Book of the collection : "); */
        Set<String> authorOfBook = new LinkedHashSet<String>();
        for (String bookFromCo : bookOfCo) {
            /* System.out.println(bookFromCo); */
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":" + bookFromCo, pm);
            for (OWLNamedIndividual AuthorOfBook : rs.getObjectPropertyValues(selectedBook, hasAuthor).getFlattened()) {
                String authorFromCo = renderer.render(AuthorOfBook);
                authorOfBook.add(authorFromCo);
            }
        }
        /* 4.End */
        /*---------------------------------------------------------------*/
        /* 5.Get final set of author */
        /* System.out.println("4.Set of Author : "); */
        Set<String> setFinalAuthor = new LinkedHashSet<String>();
        for (String authorSet : authorOfBook) {
            for (String cosinAu : setCosinAuthor) {
                /* System.out.println("-------------"+cosinAu); */
                if (authorSet.equals(cosinAu)) {
                    setFinalAuthor.add(authorSet);
                }
            }
        }
        /* 5.End */
        /*---------------------------------------------------------------*/
        /* 6.Get final set of book ID */
        /* System.out.println("5.Final Au : "); */
        for (String finalAu : setFinalAuthor) {
            for (String cosinBid : setId) {
                String newId = finalAu.substring(6);
                if (!newId.equals(cosinBid)) {
                    setfinalBookId.add(newId);
                }
            }
        }
        /* 6.End */
        /*---------------------------------------------------------------*/
        // *7.Final Get data property values
        /* System.out.println("Final book ID : "); */
        for (String finalId : setfinalBookId) {
            /* System.out.println(finalId); */
            bookData bd = new bookData();
            authorData ad = new authorData();
            publisherData pd = new publisherData();
            collectionData cd = new collectionData();
            allData allData = new allData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + finalId, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + finalId, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + finalId, pm);
            for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                bd.setbID(bookId.getLiteral());
                                bd.setTitle(bookTitle.getLiteral());
                                bd.setType(bookType.getLiteral());
                                bd.setYear(bookYear.getLiteral());
                                bd.setQty(bookQty.getLiteral());
                                allData.setbData(bd);
                            }
                        }
                    }
                }
            }
            for (OWLLiteral authorId : rs.getDataPropertyValues(selectedAuthor, hasAuthorId)) {
                for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                    ad.setbID(authorId.getLiteral());
                    ad.setAuthor(authorName.getLiteral());
                    allData.setaData(ad);
                }
            }
            for (OWLLiteral publisherId : rs.getDataPropertyValues(selectedPublisher, haspublisherId)) {
                for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                    pd.setbID(publisherId.getLiteral());
                    pd.setPublisherName(publisherName.getLiteral());
                    allData.setpData(pd);
                }
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectId : rs.getDataPropertyValues(finalChosenCo, hascollectionId)) {
                    for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        allData.setcData(cd);
                    }
                }
            }
            /* End of Collection */
            listChosenData.add(allData);
        }
        /* 7.End */
        return listChosenData;
    }

    /*
     * SearchGloVe process: 1.Get input 2. Split into single word 3. Get synset of
     * words 4. Search depend on cosine similarity 5. Sort
     */
    public static List<sortData> searchGloVe(String input, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {
        Cosine cosine = new Cosine();
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);

        String[] words = input.split(" ");
        List<allData> getList = new ArrayList<allData>();
        Set<String> wordGloVe = new LinkedHashSet<String>();
        for (String listWord : words) {
            Set<String> similarset = getGlovewords(listWord);
            for (String word : similarset) {
                wordGloVe.add(word);
            }
        }

        wordGloVe.add(input);
        Set<String> setId = new LinkedHashSet<String>();

//		for (String getSynset : wordNetSet) {
//			System.out.println(getSynset);
//			getList = searchOnlyCosine(getSynset,m,o);
//			for (allData finalData : getList) {
//				String Id = finalData.getbData().getbID();
//				setId.add(Id);
//			}
//		}
        for (String getSimilar : wordGloVe) {
            System.out.println(getSimilar);
            getList = searchOnlyCosine(getSimilar, m, o);
            for (allData finalData : getList) {
                String Id = finalData.getbData().getbID();
                setId.add(Id);
            }
        }
        List<sortData> sortList = new ArrayList<sortData>();
        for (String id : setId) {
            sortData sd = new sortData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + id, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + id, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + id, pm);
            for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                String nameBookTitle = bookTitle.getLiteral();
                                double numCosin = cosine.similarity(nameBookTitle.toLowerCase(), input.toLowerCase());
                                sd.setTitle(nameBookTitle);
                                sd.setCosineRate(numCosin);
                                sd.setbID(bookId.getLiteral());
                                sd.setQty(bookQty.getLiteral());
                                sd.setYear(bookYear.getLiteral());
                            }
                        }
                    }
                }
            }

            for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                sd.setAuthor(authorName.getLiteral());
            }

            for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                sd.setPublisher(publisherName.getLiteral());
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                    sd.setCollection(collectName.getLiteral());
                }
            }
            /* End of Collection */
            sortList.add(sd);

        }
        return sortList;
    }

    public static List<sortData> searchOption1(String input, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {
        Cosine cosine = new Cosine();
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        String[] words = input.split(" ");
        List<allData> getList = new ArrayList<allData>();
        Set<String> wordNetSet = new LinkedHashSet<String>();

        for (String listWord : words) {
            Set<String> synset = getWordNet(listWord);
            for (String word : synset) {
                wordNetSet.add(word);
            }
        }
        wordNetSet.add(input);
        Set<String> setId = new LinkedHashSet<String>();
        for (String getSynset : wordNetSet) {
            System.out.println(getSynset);
            getList = searchOnlyCosine(getSynset, m, o);
            for (allData finalData : getList) {
                String Id = finalData.getbData().getbID();
                setId.add(Id);
            }
        }
        System.out.println("final Results : ");
        List<sortData> sortList = new ArrayList<sortData>();
        for (String id : setId) {
            System.out.println(id);
            sortData sd = new sortData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + id, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + id, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + id, pm);
            for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                String nameBookTitle = bookTitle.getLiteral();
                                double numCosin = cosine.similarity(nameBookTitle.toLowerCase(), input.toLowerCase());
                                sd.setTitle(nameBookTitle);
                                sd.setCosineRate(numCosin);
                                sd.setbID(bookId.getLiteral());
                                sd.setQty(bookQty.getLiteral());
                                sd.setYear(bookYear.getLiteral());
                            }
                        }
                    }
                }
            }

            for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                sd.setAuthor(authorName.getLiteral());
            }

            for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                sd.setPublisher(publisherName.getLiteral());
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                    sd.setCollection(collectName.getLiteral());
                }
            }
            /* End of Collection */
            sortList.add(sd);

        }
        return sortList;
    }

    public static List<sortData> searchOption2(String input, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {
        Cosine cosine = new Cosine();
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /*----------------------------------------------------*/
        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        String[] words = input.split(" ");
        List<allData> getList = new ArrayList<allData>();
        Set<String> wordNetSet = new LinkedHashSet<String>();

        for (String listWord : words) {
            Set<String> synset = getWordNet(listWord);
            for (String word : synset) {
                wordNetSet.add(word);
            }
        }
        wordNetSet.add(input);
        Set<String> setId = new LinkedHashSet<String>();
        for (String getSynset : wordNetSet) {
            System.out.println(getSynset);
            getList = searchByCollect(getSynset, m, o);
            for (allData finalData : getList) {
                String Id = finalData.getbData().getbID();
                setId.add(Id);
            }
        }
        System.out.println("Option2 -- final Results  : ");
        List<sortData> sortList = new ArrayList<sortData>();
        for (String id : setId) {
            sortData sd = new sortData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + id, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + id, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + id, pm);
            for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                String nameBookTitle = bookTitle.getLiteral();
                                double numCosin = cosine.similarity(nameBookTitle.toLowerCase(), input.toLowerCase());
                                sd.setTitle(nameBookTitle);
                                sd.setCosineRate(numCosin);
                                sd.setbID(bookId.getLiteral());
                                sd.setQty(bookQty.getLiteral());
                                sd.setYear(bookYear.getLiteral());
                            }
                        }
                    }
                }
            }

            for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                sd.setAuthor(authorName.getLiteral());
            }

            for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                sd.setPublisher(publisherName.getLiteral());
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                    sd.setCollection(collectName.getLiteral());
                }
            }
            /* End of Collection */
            sortList.add(sd);

        }
        return sortList;
    }

    public static List<sortData> searchOption3(String input, OWLOntologyManager m, OWLOntology o)
            throws OWLOntologyCreationException, IOException {

        Cosine cosine = new Cosine();
        // Step1. Create Pellet reasoner
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");

        /*----------------------------------------------------*/
        /* Create Data Properties */
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);
        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        String[] words = input.split(" ");
        List<allData> getList = new ArrayList<allData>();
        Set<String> wordNetSet = new LinkedHashSet<String>();

        for (String listWord : words) {
            Set<String> synset = getWordNet(listWord);
            for (String word : synset) {
                wordNetSet.add(word);
            }
        }
        wordNetSet.add(input);
        Set<String> setId = new LinkedHashSet<String>();
        for (String getSynset : wordNetSet) {
            System.out.println(getSynset);
            getList = searchByPub(getSynset, m, o);
            for (allData finalData : getList) {
                String Id = finalData.getbData().getbID();
                setId.add(Id);
            }
        }
        System.out.println("Option3 -- final Results : ");
        List<sortData> sortList = new ArrayList<sortData>();
        for (String id : setId) {
            sortData sd = new sortData();
            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + id, pm);
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + id, pm);
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + id, pm);
            for (OWLLiteral bookTitle : rs.getDataPropertyValues(selectedBook, hasbookTitle)) {
                for (OWLLiteral bookId : rs.getDataPropertyValues(selectedBook, hasbookId)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(selectedBook, hasbookQty)) {
                                String nameBookTitle = bookTitle.getLiteral();
                                double numCosin = cosine.similarity(nameBookTitle.toLowerCase(), input.toLowerCase());
                                sd.setTitle(nameBookTitle);
                                sd.setCosineRate(numCosin);
                                sd.setbID(bookId.getLiteral());
                                sd.setQty(bookQty.getLiteral());
                                sd.setYear(bookYear.getLiteral());
                            }
                        }
                    }
                }
            }

            for (OWLLiteral authorName : rs.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                sd.setAuthor(authorName.getLiteral());
            }

            for (OWLLiteral publisherName : rs.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                sd.setPublisher(publisherName.getLiteral());
            }
            for (OWLNamedIndividual finalCoIndi : rs.getObjectPropertyValues(selectedBook, hasCollection)
                    .getFlattened()) {
                String nameFinalCoIndi = renderer.render(finalCoIndi);
                OWLNamedIndividual finalChosenCo = df.getOWLNamedIndividual(":" + nameFinalCoIndi, pm);
                for (OWLLiteral collectName : rs.getDataPropertyValues(finalChosenCo, hascollectionName)) {
                    sd.setCollection(collectName.getLiteral());
                }
            }
            /* End of Collection */
            sortList.add(sd);

        }
        return sortList;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException {
        /* List<allData> getlist = searchOption1("introduction to computing"); */

        String input = "Java";
        System.out.println("final Results");
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        File inputFile = new File("src/main/resources/bookOWL.owl");
        // Load ontology
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);
        List<sortData> listOp3 = searchGloVe(input, m, o);
        Collections.sort(listOp3, new sortData.CompValue());
        int num = 0;
        for (sortData sortData : listOp3) {
            num = num + 1;
            System.out.println("Num of books :" + num);
            String id = sortData.getbID();
            double cosine = sortData.getCosineRate();
            String title = sortData.getTitle();
            System.out.println("Id : " + id);
            System.out.println("Title : " + "'" + title + "' with Cosine Index: " + cosine);
        }

    }
}
