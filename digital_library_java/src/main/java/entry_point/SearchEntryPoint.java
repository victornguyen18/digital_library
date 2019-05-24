package entry_point;

import com.google.gson.Gson;
import py4j.GatewayServer;

import java.util.*;

import execute.*;
import model.*;
import objects.*;
import utils.*;

import org.json.simple.JSONObject;

import java.io.*;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.jblas.DoubleMatrix;

public class SearchEntryPoint {
    SearchEntryPoint() {
    }

    public String searchGoogleBook(String search, int num, int page) {
        JSONObject result = new JSONObject();
        try {
            int start = num * (page - 1);
            GoogleBookSearch merge = new GoogleBookSearch();
            result.put("status", 200);
            String data = merge.getBookJson(search, num, start);
            result.put("data", data);
            return result.toJSONString();
        } catch (Exception e) {
            result.put("status", 404);
            result.put("message", e.getMessage());
            System.out.println(e.getMessage());
            return result.toJSONString();
        }
    }

    public String searchOntology(String search, int num, int page) {
        JSONObject result = new JSONObject();
        Gson gson = new Gson();
        String data;
        try {
            OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            File inputFile = new File("src/main/resources/bookOWL.owl");
            // Load ontology
            OWLOntology o = m.loadOntologyFromOntologyDocument(inputFile);
            List<sortData> list = searchMethod.searchGloVe(search, m, o);
            Collections.sort(list, new sortData.CompValue());
            System.out.println("Running Glove");
            int start = num * (page - 1);
            List<sortData> listPage = list.subList(start, start + num);
            data = gson.toJson(listPage);
            result.put("status", 200);
            result.put("data", data);
            return result.toJSONString();
        } catch (Exception e) {
            result.put("status", 404);
            result.put("message", e.getMessage());
            System.out.println(e.getMessage());
            return result.toJSONString();
        }
    }


    public static void main(String[] args) {
        SearchEntryPoint application = new SearchEntryPoint();
        GatewayServer server = new GatewayServer(application);
        System.out.println("Open JavaGatewayServer: " + server.getPort());
        server.start();
    }
}
