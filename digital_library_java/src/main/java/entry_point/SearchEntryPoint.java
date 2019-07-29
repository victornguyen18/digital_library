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
            System.out.println("Search term: " + search);
            System.out.println("No. item: " + num);
            System.out.println("No. page: " + page);
            int sizeList = list.size();
            int totalPage = (sizeList / num) + 1;
            int start = num * (page - 1);
            int finish = start + num;
            if (start < sizeList) {
                System.out.println("Running Glove");
                System.out.println("Size list: " + sizeList);
                if (sizeList < finish) {
                    finish = sizeList;
                }
                List<sortData> listPage = list.subList(start, finish);
                data = gson.toJson(listPage);
                result.put("status", 200);
                result.put("data", data);
                result.put("totalPage", totalPage);
                return result.toJSONString();
            } else {
                result.put("status", 404);
                result.put("message", "Out of page");
                return result.toJSONString();
            }
        } catch (Exception e) {
            result.put("status", 404);
            result.put("message", e.getMessage());
            System.out.println(e.getMessage());
            return result.toJSONString();
        }
    }


    public static void main(String[] args) {
        SearchEntryPoint application = new SearchEntryPoint();
        //Create new entry point JVM
        GatewayServer server = new GatewayServer(application);
        System.out.println("Open JavaGatewayServer: " + server.getPort());
        //Start the gateway
        server.start();
    }
}
