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

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jblas.DoubleMatrix;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Search {
    private static final String BASE_URL = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL";
    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static Set<String> getGlovewords(String input) {
        String file = "/Users/victornguyen/Sites/BookDigital/src/main/resources/test.txt";
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
            } /* End of Collection */
            listChosenData.add(allData);
        }
        /* 7.End */
        return listChosenData;
    }

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
//		Set<String> wordNetSet = new LinkedHashSet<String>();
        Set<String> wordGloVe = new LinkedHashSet<String>();
//		for (String listWord : words) {
//			Set<String> synset = getWordNet(listWord);
//			for (String word : synset) {
//				wordNetSet.add(word);
//			}
//		}
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
            } /* End of Collection */
            sortList.add(sd);

        }
        return sortList;
    }


    public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException {
        /* List<allData> getlist = searchOption1("introduction to computing"); */

        String input = "Java";
        System.out.println("final Results");
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        File inputFile = new File("/Users/victornguyen/Sites/BookDigital/src/main/resources/bookOWL.owl");
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
            System.out.println("Title : " + "'" + title + "' with Cosine Index: " + cosine);
        }

    }

}
