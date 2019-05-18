package execute;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class testWordnet {


    public static void configureJWordNet() {
        // WARNING: This still does not work in Java 5!!!
        try {
            // initialize JWNL (this must be done before JWNL can be used)
            // See the JWordnet documentation for details on the properties file
            JWNL.initialize(new FileInputStream("/Users/victornguyen/Sites/BookDigital/properties.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static List<String> getInput(String input) throws JWNLException {
        String snore1 = "to";
        String snore3 = "at";
        String snore5 = "by";
        String snore2 = "on";
        String snore4 = "in";
        String snore6 = "for";
        String snore7 = "since";
        String snore8 = "ago";
        String snore9 = "till";
        String snore10 = "until";
        String snore11 = "before";
        String snore12 = "past";
        String snore13 = "next";
        String snore14 = "beside";
        String snore15 = "below";
        String snore16 = "over";
        String snore17 = "above";
        String snore18 = "across";
        String snore19 = "through";
        String snore20 = "into";
        String snore21 = "toward";
        String snore22 = "from";
        String snore23 = "onto";
        String snore24 = "off";
        String snore25 = "of";
        String snore26 = "out";
        String snore27 = "about";
        String snore28 = "under";
        String snore29 = "around";
        String snore30 = "among";

        String snore31 = "after";
        String snore32 = "against";
        String snore33 = "along";
        String snore34 = "amid";
        String snore35 = "anti";
        String snore36 = "among";
        String snore37 = "as";
        String snore38 = "behind";
        String snore39 = "beneath";

        String snore40 = "besides";
        String snore41 = "between";
        String snore42 = "beyond";
        String snore43 = "but";
        String snore44 = "concerning";
        String snore45 = "considering";
        String snore46 = "despite";
        String snore47 = "down";
        String snore48 = "during";

        String snore49 = "except";
        String snore50 = "excepting";
        String snore51 = "excluding";
        String snore52 = "following";
        String snore53 = "inside";
        String snore54 = "like";
        String snore55 = "minus";
        String snore56 = "near";
        String snore57 = "opposite";

        String snore58 = "outside";
        String snore59 = "per";
        String snore60 = "plus";
        String snore61 = "regarding";
        String snore62 = "round";
        String snore63 = "save";
        String snore64 = "than";
        String snore65 = "towards";
        String snore66 = "underneath";

        String snore67 = "unlike";
        String snore68 = "up";
        String snore69 = "upon";
        String snore70 = "versus";
        String snore71 = "via";
        String snore72 = "with";
        String snore73 = "within";
        String snore74 = "without";
        configureJWordNet();
        List<String> list = new ArrayList<String>();
        Dictionary dictionary = Dictionary.getInstance();
        try {
            String[] keyword = input.replaceAll("[^a-zA-Z]", " ").split(" ");
            /*replaceAll(" [^a-zA-Z] "," ").toLowerCase()*/
            for (String key : keyword) {
                if (key.equals(snore1) || key.equals(snore2) || key.equals(snore3) ||
                        key.equals(snore4) || key.equals(snore5) || key.equals(snore6) ||
                        key.equals(snore7) || key.equals(snore8) || key.equals(snore9) ||
                        key.equals(snore10) || key.equals(snore11) || key.equals(snore12) ||
                        key.equals(snore13) || key.equals(snore14) || key.equals(snore15) ||
                        key.equals(snore16) || key.equals(snore17) || key.equals(snore18) ||
                        key.equals(snore19) || key.equals(snore20) || key.equals(snore21) ||
                        key.equals(snore22) || key.equals(snore23) || key.equals(snore24) ||
                        key.equals(snore25) || key.equals(snore26) || key.equals(snore27) ||
                        key.equals(snore28) || key.equals(snore29) || key.equals(snore30)
                        ||
                        key.equals(snore31) || key.equals(snore32) || key.equals(snore33) ||
                        key.equals(snore34) || key.equals(snore35) || key.equals(snore36) ||
                        key.equals(snore37) || key.equals(snore38) || key.equals(snore39) ||
                        key.equals(snore40) || key.equals(snore41) || key.equals(snore42) ||
                        key.equals(snore43) || key.equals(snore44) || key.equals(snore45) ||
                        key.equals(snore46) || key.equals(snore47) || key.equals(snore48)
                        ||
                        key.equals(snore49) || key.equals(snore50) || key.equals(snore51) ||
                        key.equals(snore52) || key.equals(snore53) || key.equals(snore54) ||
                        key.equals(snore55) || key.equals(snore56) || key.equals(snore57) ||
                        key.equals(snore58) || key.equals(snore59) || key.equals(snore60) ||
                        key.equals(snore61) || key.equals(snore62) || key.equals(snore63) ||
                        key.equals(snore64) || key.equals(snore65) || key.equals(snore66)
                        ||
                        key.equals(snore67) || key.equals(snore68) || key.equals(snore69) ||
                        key.equals(snore70) || key.equals(snore71) || key.equals(snore72) ||
                        key.equals(snore73) || key.equals(snore74)) {
                    System.out.println("ingnore: " + key);
                    continue;
                }
                if (key.endsWith("s") && !key.equals("bus") && !key.equals("business") && !key.equals("class")
                        && !key.equals("analysis") && !key.equals("thesis") && !key.equals("crisis") && !key.equals("virus")
                        && !key.equals("cactus")) {
                    key = key.substring(0, (key.length() - 1));
                }
                System.out.println("Senses of the word '" + key + "' :");
                int num = 0;

                IndexWord word = dictionary.lookupIndexWord(POS.NOUN, key);
                Synset[] senses = word.getSenses();
                for (Synset synset : senses) {
                    Word[] words = synset.getWords();
                    for (Word word2 : words) {
                        String result = word2.getLemma().toString();
                        list.add(result);
                    }

                }
            }
        } catch (NullPointerException e) {
            System.out.println("The system didn't understand your input");
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args)
            throws JWNLException {
        List<String> list = getInput("management");
        Set<String> s = new LinkedHashSet<String>(list);

        System.out.println("Results : ");
        for (String re : s) {
            System.out.println(re);
        }
    }
}
