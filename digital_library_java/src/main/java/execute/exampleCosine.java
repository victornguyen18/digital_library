package execute;

import java.util.ArrayList;
import java.util.Map;

import similarity.Cosine;

import java.util.*;

public class exampleCosine {

    public static void main(String[] args) {
        Cosine cosine = new Cosine();
        String s1 = "Book related to New York";
        Map<String, Integer> profiles1 = cosine.getProfile(s1.toLowerCase());
        System.out.println(profiles1);
        String s2 = "My other string...";
        List<String> list = new ArrayList<String>();
        list.add("New York hermit akon sting");
        list.add("Decoy plays poker at New York");
        list.add("New York pellet book");
        list.add("New York Book");
        // Let's work with sequences of 2 characters...

        int i = 0;
        for (String getlist : list) {
            i = i + 1;
            Map<String, Integer> profile = cosine.getProfile(getlist.toLowerCase());
            System.out.println("Profile" + i + " : " + profile);
            System.out.println(cosine.similarity(s1.toLowerCase(), getlist.toLowerCase()));
        }
    }

}
