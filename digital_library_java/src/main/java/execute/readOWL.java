package execute;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;
import static org.semanticweb.owlapi.search.Searcher.annotationObjects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import execute.readDataFromExcel;
import model.Data;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class readOWL {
    public static final IRI bookIRI = IRI.create("http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL");

    //create OWL Manager
    public static OWLOntologyManager create() {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        PriorityCollection<OWLOntologyIRIMapper> iriMappers = m.getIRIMappers();
        iriMappers.add(new AutoIRIMapper(new File("materializedOntologies"), true));
        return m;
    }

    // print Properties of specified class
    public static void printProperties(OWLOntology o, OWLReasoner reasoner, OWLClass cls, OWLDataFactory df) {
        for (OWLObjectPropertyExpression prop : o.getObjectPropertiesInSignature()) {

            OWLClassExpression restriction = df.getOWLObjectSomeValuesFrom(prop, df.getOWLThing());
            OWLClassExpression intersection = df.getOWLObjectIntersectionOf(cls, df.getOWLObjectComplementOf(
                    restriction));
            boolean sat = !reasoner.isSatisfiable(intersection);
            if (sat) {
                System.out.println("Instances of " + cls +
                        " necessarily have the property " + prop);
            }
        }
    }

    //Add individuals for Book
    public static void createBookIndividual(OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException {

        //Step1.Call class Book using Prefix manager
        String baseBook = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
        PrefixManager pmBook = new DefaultPrefixManager(baseBook);
        OWLClass book = df.getOWLClass(":Book", pmBook);

        //Step2.Load data from Excel File
        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();


        String bookNum = "book";

        //Step3.Get data related to class book.
        for (Data data : list) {
            String title = data.getTitle();
            String bID = data.getbID().replace(".0", "");
            int qty = Integer.parseInt(data.getQty().replace(".0", ""));
            String type = data.getType();
            int year = Integer.parseInt(data.getYear().replace(".0", ""));
            int IdBook = Integer.parseInt(bID);
            String bookNumR = bookNum + bID; //the name of book-individual

            //Step4.Create Individuals belong to class Book
            OWLNamedIndividual bookAddNew = df.getOWLNamedIndividual(bookNumR, pmBook);
            //Create Axiom to declare that the new individuals are instances of class Book
            OWLClassAssertionAxiom clAxiom = df.getOWLClassAssertionAxiom(book, bookAddNew);

            //Step5.Call the Data Properties declared in the OWL file
            OWLDataProperty hasBookId = df.getOWLDataProperty(":bookId", pmBook);
            OWLDataProperty hasBookTitle = df.getOWLDataProperty(":bookTitle", pmBook);
            OWLDataProperty hasBookQty = df.getOWLDataProperty(":bookQty", pmBook);
            OWLDataProperty hasBookType = df.getOWLDataProperty(":bookType", pmBook);
            OWLDataProperty hasBookYear = df.getOWLDataProperty(":bookYear", pmBook);
            //Create Axioms to add the values into compatible data properties.
            OWLDataPropertyAssertionAxiom dataAxiom1 =
                    df.getOWLDataPropertyAssertionAxiom(hasBookId, bookAddNew, IdBook);
            OWLDataPropertyAssertionAxiom dataAxiom2 =
                    df.getOWLDataPropertyAssertionAxiom(hasBookTitle, bookAddNew, title);
            OWLDataPropertyAssertionAxiom dataAxiom3 =
                    df.getOWLDataPropertyAssertionAxiom(hasBookQty, bookAddNew, qty);
            OWLDataPropertyAssertionAxiom dataAxiom4 =
                    df.getOWLDataPropertyAssertionAxiom(hasBookType, bookAddNew, type);
            OWLDataPropertyAssertionAxiom dataAxiom5 =
                    df.getOWLDataPropertyAssertionAxiom(hasBookYear, bookAddNew, year);

            //Step6.Add axiom into the ontology
            m.addAxiom(o, clAxiom);
            m.addAxiom(o, dataAxiom1);
            m.addAxiom(o, dataAxiom2);
            m.addAxiom(o, dataAxiom3);
            m.addAxiom(o, dataAxiom4);
            m.addAxiom(o, dataAxiom5);
        }

        //Save the ontology
        m.saveOntology(o, new OWLXMLDocumentFormat());
    }

    //Add individuals for Author
    public static void createAuthorIndividual(OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException {
        String baseAuthor = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
        PrefixManager pmAuthor = new DefaultPrefixManager(baseAuthor);
        OWLClass author = df.getOWLClass(":Author", pmAuthor);

        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();
        String auNum = "author";
        for (Data data : list) {
            String bID = data.getbID().replace(".0", "");
            String authorName = data.getAuthor();
            int authorID = Integer.parseInt(bID);
            String authorNum = auNum + bID;
            OWLNamedIndividual authorAddNew = df.getOWLNamedIndividual(authorNum, pmAuthor);
            OWLClassAssertionAxiom clAxiom = df.getOWLClassAssertionAxiom(author, authorAddNew);

            OWLDataProperty hasAuthorId = df.getOWLDataProperty(":authorId", pmAuthor);
            OWLDataProperty hasAuthorName = df.getOWLDataProperty(":authorName", pmAuthor);
            OWLDataPropertyAssertionAxiom dataAuthorAxiom =
                    df.getOWLDataPropertyAssertionAxiom(hasAuthorName, authorAddNew, authorName);

            OWLDataPropertyAssertionAxiom dataAuthorAxiom2 =
                    df.getOWLDataPropertyAssertionAxiom(hasAuthorId, authorAddNew, authorID);
            m.addAxiom(o, clAxiom);
            m.addAxiom(o, dataAuthorAxiom);
            m.addAxiom(o, dataAuthorAxiom2);
        }
        m.saveOntology(o, new OWLXMLDocumentFormat());
    }

    //Add individuals for Collection
		/*public static void createCollectionIndividual (OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException{
			String baseCollect = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
			PrefixManager pmCollect = new DefaultPrefixManager(baseCollect);
			OWLClass collect = df.getOWLClass(":Collection",pmCollect);
			
			readDataFromExcel readData = new readDataFromExcel();
			List<Data> list = readData.executeReadExcel();
			String coNum="collection";
			for (Data data: list){
				String bID = data.getbID().replace(".0", "");
				String coName = data.getCollection();
				if (coName)
					String collectNum = coNum+bID;
					OWLNamedIndividual coAddNew = df.getOWLNamedIndividual(collectNum,pmCollect);
					OWLClassAssertionAxiom clAxiom = df.getOWLClassAssertionAxiom(collect, coAddNew);
					
					OWLDataProperty hasCoName = df.getOWLDataProperty(":collectionName",pmCollect);
					OWLDataPropertyAssertionAxiom dataCoAxiom = 
							df.getOWLDataPropertyAssertionAxiom(hasCoName, coAddNew, coName);
					
					m.addAxiom(o,clAxiom);
					m.addAxiom(o, dataCoAxiom);
			}
			m.saveOntology(o, new OWLXMLDocumentFormat());
		}*/

    //Add individuals for Collection
    public static void createPublisherIndividual(OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException {
        String basePub = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
        PrefixManager pmPub = new DefaultPrefixManager(basePub);
        OWLClass publisher = df.getOWLClass(":Publisher", pmPub);

        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();
        String pubNum = "publisher";
        for (Data data : list) {
            String bID = data.getbID().replace(".0", "");
            String pubName = data.getPublisher();
            int pubID = Integer.parseInt(bID);
            String pubNumR = pubNum + bID;
            OWLNamedIndividual pubAddNew = df.getOWLNamedIndividual(pubNumR, pmPub);
            OWLClassAssertionAxiom clAxiom = df.getOWLClassAssertionAxiom(publisher, pubAddNew);

            OWLDataProperty hasPubId = df.getOWLDataProperty(":publisherId", pmPub);
            OWLDataProperty hasPubName = df.getOWLDataProperty(":publisherName", pmPub);
            OWLDataPropertyAssertionAxiom dataCoAxiom =
                    df.getOWLDataPropertyAssertionAxiom(hasPubName, pubAddNew, pubName);

            OWLDataPropertyAssertionAxiom dataCoAxiom2 =
                    df.getOWLDataPropertyAssertionAxiom(hasPubId, pubAddNew, pubID);
            m.addAxiom(o, clAxiom);
            m.addAxiom(o, dataCoAxiom);
            m.addAxiom(o, dataCoAxiom2);
        }
        m.saveOntology(o, new OWLXMLDocumentFormat());
    }

    public static void createCollectionIndividual(OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException {
        String cobase = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
        PrefixManager coPub = new DefaultPrefixManager(cobase);
        OWLClass collection = df.getOWLClass(":Collection", coPub);
        //Get Collection Data from excel
        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();

        String collect1 = "CoAquaticResourcesManagement";
        String collect2 = "CoBiomedicalEngineering";
        String collect3 = "CoBiotechnology";
        String collect4 = "CoBusinessAdministration";
        String collect5 = "CoChemicalApplications";

        String collect6 = "CoCivilEngineering";
        String collect7 = "CoComputerScienceAndEngineering";
        String collect8 = "CoElectronicAndTelecommunicationEngineering";
        String collect9 = "CoEntertainment";
        String collect10 = "CoFinance";
        String collect11 = "CoFoodTechnology";
        String collect12 = "CoForeignLanguage";
        String collect13 = "CoHospitalityManagement";
        String collect14 = "CoIndustrialSystemsEngineering";

        String collect15 = "CoLiteratureAndArt";
        String collect16 = "CoMathematics";
        String collect17 = "CoNaturalScience";
        String collect18 = "CoOtherAreas";
        String collect19 = "CoPhysics";
        String collect20 = "CoPsychology";
        String collect21 = "CoSocialSciences";
		
	
		
			/*OWLNamedIndividual CollectionAddIndi1 = df.getOWLNamedIndividual(collect21,coPub);
			OWLClassAssertionAxiom addCollectAxiom1 = df.getOWLClassAssertionAxiom(collection, CollectionAddIndi1);
			m.addAxiom(o,addCollectAxiom1);*/

		/*for(Data data : list){
			String collectionName = data.getCollection();
			
			OWLDataProperty hasCollectionName = df.getOWLDataProperty(":collectionName", coPub);
			OWLDataProperty hasCollectionId = df.getOWLDataProperty(":collectionId",coPub);
			OWLDataPropertyAssertionAxiom dataCoAxiom = 
					df.getOWLDataPropertyAssertionAxiom(hasPubName, pubAddNew, pubName);
			
			OWLDataPropertyAssertionAxiom dataCoAxiom2 = 
					df.getOWLDataPropertyAssertionAxiom(hasPubId, pubAddNew, pubID);
		}*/
        m.saveOntology(o, new OWLXMLDocumentFormat());
    }

    @SuppressWarnings("deprecation")
    public static void addObjectProperty(OWLOntologyManager m, OWLDataFactory df, OWLOntology o) throws OWLOntologyStorageException, IOException {

        //Step1. Create Prefix Manager to call Object Property
        String base = "http://www.semanticweb.org/haipham/ontologies/2017/4/bookOWL#";
        PrefixManager pm = new DefaultPrefixManager(base);

        readDataFromExcel readData = new readDataFromExcel();
        List<Data> list = readData.executeReadExcel();

        //Step2. Get the object property declared in the ontology
        OWLObjectProperty hasAuthor = df.getOWLObjectProperty(":hasAuthor", pm);
        OWLObjectProperty hasCollection = df.getOWLObjectProperty(":hasCollection", pm);
        OWLObjectProperty hasPublisher = df.getOWLObjectProperty(":hasPublisher", pm);
        OWLObjectProperty isAuthorOf = df.getOWLObjectProperty(":isAuthorOf", pm);
        OWLObjectProperty isCollectionOf = df.getOWLObjectProperty(":isCollectionOf", pm);
        OWLObjectProperty isPublisherOf = df.getOWLObjectProperty(":isPublisherOf", pm);

        for (Data data : list) {
            String bId = data.getbID().replace(".0", "");
            String temp = data.getCollection();


            //Step3.Get the Individuals in the ontology
            OWLIndividual bookIndi = df.getOWLNamedIndividual(":book" + bId, pm);
            OWLIndividual authorIndi = df.getOWLNamedIndividual(":author" + bId, pm);
            OWLIndividual publisherIndi = df.getOWLNamedIndividual(":publisher" + bId, pm);

            //Step4.Declare the relations between two individuals
            OWLObjectPropertyAssertionAxiom hasAuthorAxiom =
                    df.getOWLObjectPropertyAssertionAxiom(hasAuthor, bookIndi, authorIndi);
            //Individuals of book hasAuthor individuals of author
            OWLObjectPropertyAssertionAxiom hasPublisherAxiom =
                    df.getOWLObjectPropertyAssertionAxiom(hasPublisher, bookIndi, publisherIndi);
            //Individuals of book hasPublisher individuals of publisher
            OWLObjectPropertyAssertionAxiom isAuthorOfAxiom =
                    df.getOWLObjectPropertyAssertionAxiom(isAuthorOf, authorIndi, bookIndi);
            //Individuals of author isAuthorOf individuals of book

            OWLObjectPropertyAssertionAxiom isPublisherOfAxiom =
                    df.getOWLObjectPropertyAssertionAxiom(isPublisherOf, publisherIndi, bookIndi);
            //Individuals of publisher isPublisherOf individuals of book

            //Create Axiom for relations between books and collection
            String collectionName = "";
            // Collection 1
            if (temp.equals("Aquatic Resources Management")) {
                collectionName = "CoAquaticResourcesManagement";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 2
            } else if (temp.equals("Biomedical Engineering")) {
                collectionName = "CoBiomedicalEngineering";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 3
            } else if (temp.equals("Biotechnology")) {
                collectionName = "CoBiotechnology";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 4
            } else if (temp.equals("Business Administration")) {
                collectionName = "CoBusinessAdministration";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 5
            } else if (temp.equals("Chemical Applications")) {
                collectionName = "CoChemicalApplications";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 6
            } else if (temp.equals("Civil Engineering")) {
                collectionName = "CoCivilEngineering";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 7
            } else if (temp.equals("Computer Science And Engineering")) {
                collectionName = "CoComputerScienceAndEngineering";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 8
            } else if (temp.equals("Electronic And Telecommunication Engineering")) {
                collectionName = "CoElectronicAndTelecommunicationEngineering";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 9
            } else if (temp.equals("Entertainment")) {
                collectionName = "CoEntertainment";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 10
            } else if (temp.equals("Finance")) {
                collectionName = "CoFinance";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 11
            } else if (temp.equals("Food Technology")) {
                collectionName = "CoFoodTechnology";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 12
            } else if (temp.equals("Foreign Language")) {
                collectionName = "CoForeignLanguage";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 13
            } else if (temp.equals("Hospitality Management")) {
                collectionName = "CoHospitalityManagement";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 14
            } else if (temp.equals("Industrial Systems Engineering")) {
                collectionName = "CoIndustrialSystemsEngineering";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 15
            } else if (temp.equals("Literature And Art")) {
                collectionName = "CoLiteratureAndArt";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 16
            } else if (temp.equals("Mathematics")) {
                collectionName = "CoMathematics";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 17
            } else if (temp.equals("Natural Science")) {
                collectionName = "CoNaturalScience";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 18
            } else if (temp.equals("Other Areas")) {
                collectionName = "CoOtherAreas";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 19
            } else if (temp.equals("Physics")) {
                collectionName = "CoPhysics";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 20
            } else if (temp.equals("Psychology")) {
                collectionName = "CoPsychology";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
                // Collection 21
            } else if (temp.equals("Social Sciences")) {
                collectionName = "CoSocialSciences";
                OWLIndividual collectionIndi = df.getOWLNamedIndividual(collectionName, pm);
                OWLObjectPropertyAssertionAxiom hasCollectionAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);
                OWLObjectPropertyAssertionAxiom isCollectionOfAxiom =
                        df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
                //Add axiom to ontology
                AddAxiom addHasCollection = new AddAxiom(o, hasCollectionAxiom);
                m.applyChange(addHasCollection);
                AddAxiom addIsCollectionOf = new AddAxiom(o, isCollectionOfAxiom);
                m.applyChange(addIsCollectionOf);
            }
			
			/*OWLObjectPropertyAssertionAxiom hasCollectionAxiom = 
					df.getOWLObjectPropertyAssertionAxiom(hasCollection, bookIndi, collectionIndi);*/
            //Individuals of book hasCollection individuals of collection
//			OWLObjectPropertyAssertionAxiom isCollectionOfAxiom = 
//					df.getOWLObjectPropertyAssertionAxiom(isCollectionOf, collectionIndi, bookIndi);
            //Individuals of collection isCollectionOf individuals of book
            //

            //Step5. Add axioms to the ontology
            AddAxiom addHasAuthor = new AddAxiom(o, hasAuthorAxiom);
            m.applyChange(addHasAuthor);
            AddAxiom addHasPublisher = new AddAxiom(o, hasPublisherAxiom);
            m.applyChange(addHasPublisher);

            AddAxiom addIsAuthorOf = new AddAxiom(o, isAuthorOfAxiom);
            m.applyChange(addIsAuthorOf);
            AddAxiom addIsPublisherOf = new AddAxiom(o, isPublisherOfAxiom);
            m.applyChange(addIsPublisherOf);

        }
        m.saveOntology(o, new OWLXMLDocumentFormat());
    }

    public static void main(String[] args) throws Exception {
        //create OWL Manager
        OWLOntologyManager m = create();
        //create Data Factory for reasoner
        OWLDataFactory df = m.getOWLDataFactory();
        //create Reasoner Factory
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        //Create input File
        File inputFile = new File("src/main/resources/bookOWL.owl");
        //Load ontology
        OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);
		/*for (OWLClass cls: o.getClassesInSignature()){
			
	    	System.out.println(cls.getIRI());
	    }*/

        Set<OWLLogicalAxiom> axiomSet = o.getLogicalAxioms();
        Iterator<OWLLogicalAxiom> iteratorAxiom = axiomSet.iterator();

        Set<OWLClass> classes;
        Set<OWLObjectProperty> prop;
        Set<OWLDataProperty> dataProp;
        Set<OWLNamedIndividual> individuals;

        classes = o.getClassesInSignature();
        prop = o.getObjectPropertiesInSignature();
        dataProp = o.getDataPropertiesInSignature();
        individuals = o.getIndividualsInSignature();
        //configurator = new OWLAPIOntologyConfigurator(this);            

        System.out.println("Classes");
        System.out.println("--------------------------------");
        for (OWLClass cls : classes) {
            System.out.println("+: " + cls.getIRI().getShortForm());

            System.out.println(" \tObject Property Domain");
            for (OWLObjectPropertyDomainAxiom op : o.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
                if (op.getDomain().equals(cls)) {
                    for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                        System.out.println("\t\t +: " + oop.getIRI().getShortForm());
                    }
                    System.out.println("\t\t +: " + op.getProperty().getNamedProperty().getIRI().getShortForm());
                }
            }
        }


        createBookIndividual(m, df, o);
        System.out.println("1.Add individuals for book successfully");

        createAuthorIndividual(m, df, o);
        System.out.println("2.Add individuals for author successfully");
		
		/*createCollectionIndividual(m, df, o);
		System.out.println("3.Add individuals for collection successfully");*/

        createPublisherIndividual(m, df, o);
        System.out.println("3.Add individuals for publisher successfully");


        addObjectProperty(m, df, o);
        System.out.println("4.Add Object Property between individuals successfully");

        int z = o.getIndividualsInSignature().size();
        System.out.println("Number of individuals: " + z);
        //Remove the ontology from the manager
        m.removeOntology(o);
    }
}
