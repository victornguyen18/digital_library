package entry_point;

import py4j.GatewayServer;

import java.util.*;

import execute.*;
import model.*;

import org.json.simple.JSONObject;

import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.jblas.DoubleMatrix;


public class SearchEntryPoint {
    SearchEntryPoint() {
    }

    public String searchGoogleBook(String search, int page) {
        try {
            GoogleBookSearch merge = new GoogleBookSearch();
            return merge.getBookJson(search, page);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String searchOntology(String search, int page) {
        return "";
    }


    public static void main(String[] args) {
        SearchEntryPoint application = new SearchEntryPoint();
        GatewayServer server = new GatewayServer(application);
        System.out.println("Open JavaGatewayServer: " + server.getPort());
        server.start();
    }
}
