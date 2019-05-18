package execute;

import objects.Cooccurrence;
import objects.Vocabulary;
import utils.Methods;
import utils.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jblas.DoubleMatrix;

public class Test_Glove {
    public static void main(String[] args) {

//		    String file = "/Study/Prethesis/GloVe-1.2/eval/question-data/gram8-plural.txt";
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
        int i = 0;
        List<String> similars = Methods.most_similar(W, vocab, "math", 10);
        for (String similar : similars) {
            i++;
            System.out.println("@" + similar + " " + i);
        }

    }
}
