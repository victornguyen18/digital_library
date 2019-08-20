package execute;

import similarity.Cosine;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.google.common.collect.Multimap;

import execute.readDataFromExcel;
import execute.reasonerOWL;

import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;
import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

import javax.swing.Renderer;

public class resoneCollectionList {
    private static final String BASE_URL = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL";
    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static List<allData> getByCollection(String temp) throws OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        File inputFile = new File("src/main/resources/bookOWL.owl");
        //Load ontology
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);


        reasonerOWL reasoner = new reasonerOWL();
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner rs = reasonerFactory.createReasoner(o, new SimpleConfiguration());
        OWLDataFactory df = m.getOWLDataFactory();

        PrefixDocumentFormat pm = m.getOntologyFormat(o).asPrefixOWLOntologyFormat();
        pm.setDefaultPrefix(BASE_URL + "#");
        OWLDataProperty hascollectionName = df.getOWLDataProperty(":collectionName", pm);
        OWLDataProperty hascollectionId = df.getOWLDataProperty(":collectionId", pm);

        OWLObjectProperty isCollectionOf = df.getOWLObjectProperty(":isCollectionOf", pm);
        String collectStart = ":Co";
        OWLNamedIndividual selectedCollection1 = df.getOWLNamedIndividual(collectStart + "AquaticResourcesManagement", pm);
        OWLNamedIndividual selectedCollection2 = df.getOWLNamedIndividual(collectStart + "BiomedicalEngineering", pm);
        OWLNamedIndividual selectedCollection3 = df.getOWLNamedIndividual(collectStart + "Biotechnology", pm);
        OWLNamedIndividual selectedCollection4 = df.getOWLNamedIndividual(collectStart + "BusinessAdministration", pm);
        OWLNamedIndividual selectedCollection5 = df.getOWLNamedIndividual(collectStart + "ChemicalApplications", pm);
        OWLNamedIndividual selectedCollection6 = df.getOWLNamedIndividual(collectStart + "CivilEngineering", pm);
        OWLNamedIndividual selectedCollection7 = df.getOWLNamedIndividual(collectStart + "ComputerScienceAndEngineering", pm);
        OWLNamedIndividual selectedCollection8 = df.getOWLNamedIndividual(collectStart + "ElectronicAndTelecommunicationEngineering", pm);
        OWLNamedIndividual selectedCollection9 = df.getOWLNamedIndividual(collectStart + "Entertainment", pm);
        OWLNamedIndividual selectedCollection10 = df.getOWLNamedIndividual(collectStart + "Finance", pm);
        OWLNamedIndividual selectedCollection11 = df.getOWLNamedIndividual(collectStart + "FoodTechnology", pm);
        OWLNamedIndividual selectedCollection12 = df.getOWLNamedIndividual(collectStart + "ForeignLanguage", pm);
        OWLNamedIndividual selectedCollection13 = df.getOWLNamedIndividual(collectStart + "HospitalityManagement", pm);
        OWLNamedIndividual selectedCollection14 = df.getOWLNamedIndividual(collectStart + "IndustrialSystemsEngineering", pm);
        OWLNamedIndividual selectedCollection15 = df.getOWLNamedIndividual(collectStart + "LiteratureAndArt", pm);
        OWLNamedIndividual selectedCollection16 = df.getOWLNamedIndividual(collectStart + "Mathematics", pm);
        OWLNamedIndividual selectedCollection17 = df.getOWLNamedIndividual(collectStart + "NaturalScience", pm);
        OWLNamedIndividual selectedCollection18 = df.getOWLNamedIndividual(collectStart + "OtherAreas", pm);
        OWLNamedIndividual selectedCollection19 = df.getOWLNamedIndividual(collectStart + "Physics", pm);
        OWLNamedIndividual selectedCollection20 = df.getOWLNamedIndividual(collectStart + "Psychology", pm);
        OWLNamedIndividual selectedCollection21 = df.getOWLNamedIndividual(collectStart + "SocialSciences", pm);
        OWLDataProperty hasbookId = df.getOWLDataProperty(":bookId", pm);
        OWLDataProperty hasbookTitle = df.getOWLDataProperty(":bookTitle", pm);
        OWLDataProperty hasbookType = df.getOWLDataProperty(":bookType", pm);
        OWLDataProperty hasbookYear = df.getOWLDataProperty(":bookYear", pm);
        OWLDataProperty hasbookQty = df.getOWLDataProperty(":bookQty", pm);

        OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pm);
        OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pm);
        OWLDataProperty haspublisherId = df.getOWLDataProperty(":publisherId", pm);
        OWLDataProperty haspublisherName = df.getOWLDataProperty(":publisherName", pm);
        OWLNamedIndividual chosenCollection = null;
        //input name of collection
        if (temp.equals("Aquatic Resources Management")) {
            chosenCollection = selectedCollection1;
        } else if (temp.equals("Biomedical Engineering")) {
            chosenCollection = selectedCollection2;
        } else if (temp.equals("Biotechnology")) {
            chosenCollection = selectedCollection3;
        } else if (temp.equals("Business Administration")) {
            chosenCollection = selectedCollection4;
        } else if (temp.equals("Chemical Applications")) {
            chosenCollection = selectedCollection5;
        } else if (temp.equals("Civil Engineering")) {
            chosenCollection = selectedCollection6;
        } else if (temp.equals("Computer Science And Engineering")) {
            chosenCollection = selectedCollection7;
        } else if (temp.equals("Electronic And Telecommunication Engineering")) {
            chosenCollection = selectedCollection8;
        } else if (temp.equals("Entertainment")) {
            chosenCollection = selectedCollection9;
        } else if (temp.equals("Finance")) {
            chosenCollection = selectedCollection10;
        } else if (temp.equals("Food Technology")) {
            chosenCollection = selectedCollection11;
        } else if (temp.equals("Foreign Language")) {
            chosenCollection = selectedCollection12;
        } else if (temp.equals("Hospitality Management")) {
            chosenCollection = selectedCollection13;
        } else if (temp.equals("Industrial Systems Engineering")) {
            chosenCollection = selectedCollection14;
        } else if (temp.equals("Literature And Art")) {
            chosenCollection = selectedCollection15;
        } else if (temp.equals("Mathematics")) {
            chosenCollection = selectedCollection16;
        } else if (temp.equals("Natural Science")) {
            chosenCollection = selectedCollection17;
        } else if (temp.equals("Other Areas")) {
            chosenCollection = selectedCollection18;
        } else if (temp.equals("Physics")) {
            chosenCollection = selectedCollection19;
        } else if (temp.equals("Psychology")) {
            chosenCollection = selectedCollection20;
        } else if (temp.equals("Social Sciences")) {
            chosenCollection = selectedCollection21;
        }

        /*Create data model*/

        List<allData> listChosenData = new ArrayList<allData>();
        List<bookData> listOfChosenBook = new ArrayList<bookData>();



        /*--------------------------*/
        for (OWLNamedIndividual bookInTheCollection : rs.getObjectPropertyValues(chosenCollection, isCollectionOf).getFlattened()) {
            /*Get Collection*/
            bookData bd = new bookData();
            authorData ad = new authorData();
            publisherData pd = new publisherData();
            collectionData cd = new collectionData();
            allData allData = new allData();
            for (OWLLiteral collectId : rs.getDataPropertyValues(chosenCollection, hascollectionId)) {
                for (OWLLiteral collectName : rs.getDataPropertyValues(chosenCollection, hascollectionName)) {
                    cd.setcId(collectId.getLiteral());
                    cd.setCollectName(collectName.getLiteral());
                    allData.setcData(cd);
                }
            }

            String nameBook = renderer.render(bookInTheCollection);

            //1.Start: Get datavalue of Book in the collection
            OWLNamedIndividual chosenBook = df.getOWLNamedIndividual(":" + nameBook, pm);

            for (OWLLiteral bookId : rs.getDataPropertyValues(chosenBook, hasbookId)) {
                for (OWLLiteral bookTitle : rs.getDataPropertyValues(chosenBook, hasbookTitle)) {
                    for (OWLLiteral bookType : rs.getDataPropertyValues(chosenBook, hasbookType)) {
                        for (OWLLiteral bookYear : rs.getDataPropertyValues(chosenBook, hasbookYear)) {
                            for (OWLLiteral bookQty : rs.getDataPropertyValues(chosenBook, hasbookQty)) {
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
            } /*End of OWLLiteral Book*/
            listOfChosenBook.add(bd);

            String bId = bd.getbID();
            OWLNamedIndividual chosenAuthor = df.getOWLNamedIndividual(":author" + bId, pm);
            for (OWLLiteral authorId : rs.getDataPropertyValues(chosenAuthor, hasAuthorId)) {
                for (OWLLiteral authorName : rs.getDataPropertyValues(chosenAuthor, hasAuthorName)) {
                    ad.setbID(authorId.getLiteral());
                    ad.setAuthor(authorName.getLiteral());
                    allData.setaData(ad);
                }
            }
            OWLNamedIndividual chosenPublisher = df.getOWLNamedIndividual(":publisher" + bId, pm);
            for (OWLLiteral publisherId : rs.getDataPropertyValues(chosenPublisher, haspublisherId)) {
                for (OWLLiteral publisherName : rs.getDataPropertyValues(chosenPublisher, haspublisherName)) {
                    pd.setbID(publisherId.getLiteral());
                    pd.setPublisherName(publisherName.getLiteral());
                    allData.setpData(pd);
                }
            }
            listChosenData.add(allData);
        }/*End of The chosen Collection*/
        return listChosenData;
    }

    public static void main(String[] args) throws OWLOntologyCreationException {
        String temp = "Computer Science And Engineering";

        List<allData> listOfChosenBook = getByCollection(temp);
        for (allData list : listOfChosenBook) {
            System.out.println(list.getcData().getCollectName());
            System.out.println(list.getaData().getAuthor());
            System.out.println(list.getbData().getTitle());
        }
    }
}
