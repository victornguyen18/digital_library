package execute;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Multimap;

import execute.readDataFromExcel;
import model.Data;
import model.allData;
import model.authorData;
import model.bookData;
import model.collectionData;
import model.publisherData;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;
import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.swing.Renderer;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class reasonerOWL {

    private static final String BASE_URL = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL";
    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static void testNewOnto() throws OWLOntologyCreationException, IOException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        File inputFile = new File("src/main/resources/bookOWL.owl");
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");
        // Get list from excel File
        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();
        OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author3130", pm);
        OWLNamedIndividual selectedPub = df.getOWLNamedIndividual(":publisher3130", pm);
        OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book3130", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        OWLObjectProperty hasAuthor = df.getOWLObjectProperty(":hasAuthor", pm);
        OWLObjectProperty hasPublisher = df.getOWLObjectProperty(":hasPublisher", pm);
        OWLObjectProperty isPublisherOf = df.getOWLObjectProperty(":isPublisherOf", pm);
        OWLObjectProperty isCollectionOf = df.getOWLObjectProperty(":isCollectionOf", pm);
        OWLObjectProperty isAuthorOf = df.getOWLObjectProperty(":isAuthorOf", pm);
        for (OWLNamedIndividual pubOfBook : rs.getObjectPropertyValues(selectedBook, hasPublisher).getFlattened()) {
            String NamePub = renderer.render(pubOfBook);
            System.out.println("Name of publisher : " + NamePub);
        }
        for (OWLNamedIndividual booksPub : rs.getObjectPropertyValues(selectedPub, isPublisherOf).getFlattened()) {
            System.out.println("books of pub : " + renderer.render(booksPub));
        }

    }

    public static List<allData> getDataPropertyValue() throws IOException, OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        File inputFile = new File("/Users/victornguyen/Sites/BookDigital/src/main/resources/bookOWL.owl");
        // Load ontology
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);

        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner reasoner = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");
        // Get list from excel File
        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();

        // Create objects list of books, authors, publishers, collection
        List<bookData> listBookData = new ArrayList<bookData>();
        List<authorData> listAuthorData = new ArrayList<authorData>();
        List<publisherData> listPublisherData = new ArrayList<publisherData>();
        List<collectionData> listCollectionData = new ArrayList<collectionData>();
        List<allData> listAllData = new ArrayList<allData>();

        for (Data data : list) {
            // Create objects
            bookData bd = new bookData();
            authorData ad = new authorData();
            publisherData pd = new publisherData();
            collectionData cd = new collectionData();
            allData allData = new allData();
            // Get ID in excel File
            String bId = data.getbID().replace(".0", "");

            // Get Data Books

            OWLNamedIndividual selectedBook = df.getOWLNamedIndividual(":book" + bId, pm);

            OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
            OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
            OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
            OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
            OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);

            for (OWLLiteral bookId : reasoner.getDataPropertyValues(selectedBook, hasbookId)) {
                for (OWLLiteral bookTitle : reasoner.getDataPropertyValues(selectedBook, hasbookTitle)) {
                    for (OWLLiteral bookType : reasoner.getDataPropertyValues(selectedBook, hasbookType)) {
                        for (OWLLiteral bookYear : reasoner.getDataPropertyValues(selectedBook, hasbookYear)) {
                            for (OWLLiteral bookQty : reasoner.getDataPropertyValues(selectedBook, hasbookQty)) {
                                bd.setbID(bookId.getLiteral());
                                bd.setTitle(bookTitle.getLiteral());
                                bd.setType(bookType.getLiteral());
                                bd.setYear(bookYear.getLiteral());
                                bd.setQty(bookQty.getLiteral());

                                listBookData.add(bd);

                                allData.setbData(bd);

                            }
                        }
                    }
                }
            }
            // Get Data Authors
            OWLNamedIndividual selectedAuthor = df.getOWLNamedIndividual(":author" + bId, pm);
            OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
            OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
            for (OWLLiteral authorId : reasoner.getDataPropertyValues(selectedAuthor, hasAuthorId)) {
                for (OWLLiteral authorName : reasoner.getDataPropertyValues(selectedAuthor, hasAuthorName)) {
                    ad.setbID(authorId.getLiteral());
                    ad.setAuthor(authorName.getLiteral());
                    allData.setaData(ad);
                }
            }

            // Get Data Publisher
            OWLNamedIndividual selectedPublisher = df.getOWLNamedIndividual(":publisher" + bId, pm);
            OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
            OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
            for (OWLLiteral publisherId : reasoner.getDataPropertyValues(selectedPublisher, haspublisherId)) {
                for (OWLLiteral publisherName : reasoner.getDataPropertyValues(selectedPublisher, haspublisherName)) {
                    pd.setbID(publisherId.getLiteral());
                    pd.setPublisherName(publisherName.getLiteral());
                    allData.setpData(pd);
                    /* System.out.println("pub "+publisherName.getLiteral()); */
                }
            }
            // Get Data Collection
            String collectStart = ":Co";
            String coFromExcel = data.getCollection();

            OWLNamedIndividual selectedCollection1 = df
                    .getOWLNamedIndividual(collectStart + "AquaticResourcesManagement", pm);
            OWLNamedIndividual selectedCollection2 = df.getOWLNamedIndividual(collectStart + "BiomedicalEngineering",
                    pm);
            OWLNamedIndividual selectedCollection3 = df.getOWLNamedIndividual(collectStart + "Biotechnology", pm);
            OWLNamedIndividual selectedCollection4 = df.getOWLNamedIndividual(collectStart + "BusinessAdministration",
                    pm);
            OWLNamedIndividual selectedCollection5 = df.getOWLNamedIndividual(collectStart + "ChemicalApplications",
                    pm);
            OWLNamedIndividual selectedCollection6 = df.getOWLNamedIndividual(collectStart + "CivilEngineering", pm);
            OWLNamedIndividual selectedCollection7 = df
                    .getOWLNamedIndividual(collectStart + "ComputerScienceAndEngineering", pm);
            OWLNamedIndividual selectedCollection8 = df
                    .getOWLNamedIndividual(collectStart + "ElectronicAndTelecommunicationEngineering", pm);
            OWLNamedIndividual selectedCollection9 = df.getOWLNamedIndividual(collectStart + "Entertainment", pm);
            OWLNamedIndividual selectedCollection10 = df.getOWLNamedIndividual(collectStart + "Finance", pm);
            OWLNamedIndividual selectedCollection11 = df.getOWLNamedIndividual(collectStart + "FoodTechnology", pm);
            OWLNamedIndividual selectedCollection12 = df.getOWLNamedIndividual(collectStart + "ForeignLanguage", pm);
            OWLNamedIndividual selectedCollection13 = df.getOWLNamedIndividual(collectStart + "HospitalityManagement",
                    pm);
            OWLNamedIndividual selectedCollection14 = df
                    .getOWLNamedIndividual(collectStart + "IndustrialSystemsEngineering", pm);
            OWLNamedIndividual selectedCollection15 = df.getOWLNamedIndividual(collectStart + "LiteratureAndArt", pm);
            OWLNamedIndividual selectedCollection16 = df.getOWLNamedIndividual(collectStart + "Mathematics", pm);
            OWLNamedIndividual selectedCollection17 = df.getOWLNamedIndividual(collectStart + "NaturalScience", pm);
            OWLNamedIndividual selectedCollection18 = df.getOWLNamedIndividual(collectStart + "OtherAreas", pm);
            OWLNamedIndividual selectedCollection19 = df.getOWLNamedIndividual(collectStart + "Physics", pm);
            OWLNamedIndividual selectedCollection20 = df.getOWLNamedIndividual(collectStart + "Psychology", pm);
            OWLNamedIndividual selectedCollection21 = df.getOWLNamedIndividual(collectStart + "SocialSciences", pm);
            OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);
            OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);

            /* Collection 1 */
            if (coFromExcel.equals("Aquatic Resources Management")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection1, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection1,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 2 */
            } else if (coFromExcel.equals("Biomedical Engineering")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection2, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection2,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 3 */
            } else if (coFromExcel.equals("Biotechnology")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection3, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection3,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 4 */
            } else if (coFromExcel.equals("Business Administration")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection4, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection4,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 5 */
            } else if (coFromExcel.equals("Chemical Applications")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection5, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection5,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 6 */
            } else if (coFromExcel.equals("Civil Engineering")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection6, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection6,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 7 */
            } else if (coFromExcel.equals("Computer Science And Engineering")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection7, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection7,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 8 */
            } else if (coFromExcel.equals("Electronic And Telecommunication Engineering")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection8, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection8,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 9 */
            } else if (coFromExcel.equals("Entertainment")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection9, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection9,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 10 */
            } else if (coFromExcel.equals("Finance")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection10, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection10,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 11 */
            } else if (coFromExcel.equals("Food Technology")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection11, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection11,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 12 */
            } else if (coFromExcel.equals("Foreign Language")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection12, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection12,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 13 */
            } else if (coFromExcel.equals("Hospitality Management")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection13, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection13,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 14 */
            } else if (coFromExcel.equals("Industrial Systems Engineering")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection14, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection14,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 15 */
            } else if (coFromExcel.equals("Literature And Art")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection15, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection15,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 16 */
            } else if (coFromExcel.equals("Mathematics")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection16, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection16,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 17 */
            } else if (coFromExcel.equals("Natural Science")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection17, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection17,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 18 */
            } else if (coFromExcel.equals("Other Areas")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection18, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection18,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 19 */
            } else if (coFromExcel.equals("Physics")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection19, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection19,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 20 */
            } else if (coFromExcel.equals("Psychology")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection20, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection20,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
                /* Collection 21 */
            } else if (coFromExcel.equals("Social Sciences")) {
                for (OWLLiteral collectId : reasoner.getDataPropertyValues(selectedCollection21, hascollectionId)) {
                    for (OWLLiteral collectName : reasoner.getDataPropertyValues(selectedCollection21,
                            hascollectionName)) {
                        cd.setcId(collectId.getLiteral());
                        cd.setCollectName(collectName.getLiteral());
                        listCollectionData.add(cd);

                        allData.setcData(cd);
                    }
                }
            }
            // Add all 4 lists of authors, books, collections, publishers to listAllData

            listAllData.add(allData);
        }
        System.out.println("Succsessfully Load OWL Reasoner");

        return listAllData;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, IOException {

        /* List<allData> getList = getDataPropertyValue(); */

        /* List<authorData> authorList = getAuthorValue(); */

        /*
         * for(bookData list : bookDataList){ String title = list.getTitle(); String
         * bookID = list.getbID(); System.out.println(bookID);
         * System.out.println(title); }
         */
        FileOutputStream outputStream = new FileOutputStream(
                "/Users/victornguyen/Sites/BookDigital/allAuthorValue.txt");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        testNewOnto();

        /*
         * for(allData list : getList){ String bookId = list.getbData().getbID(); String
         * bookTitle = list.getbData().getTitle(); String bookType =
         * list.getbData().getType(); String bookYear = list.getbData().getYear();
         * String bookQty = list.getbData().getQty();
         */

        /* String authorId = list.getaData().getbID(); */
        /*
         * String authorName = list.getaData().getAuthor(); String publisherName =
         * list.getpData().getPublisherName();
         */
        /*
         * String collectionName = list.getcData().getCollectName();
         * System.out.println(bookId+collectionName); bufferedWriter.write(bookId);
         * bufferedWriter.newLine(); bufferedWriter.write(bookTitle);
         * bufferedWriter.newLine(); bufferedWriter.write(bookType);
         * bufferedWriter.newLine(); bufferedWriter.write(bookYear);
         * bufferedWriter.newLine(); bufferedWriter.write(bookQty);
         * bufferedWriter.newLine(); bufferedWriter.write(authorId);
         * bufferedWriter.newLine(); bufferedWriter.write(authorName);
         * bufferedWriter.newLine(); bufferedWriter.write(publisherName);
         * bufferedWriter.newLine(); bufferedWriter.write(collectionName);
         * bufferedWriter.newLine();
         * bufferedWriter.write("----------------------------");
         * bufferedWriter.newLine();
         *
         * }System.out.println("Write File Successfully");
         */

        bufferedWriter.close();
    }
}
