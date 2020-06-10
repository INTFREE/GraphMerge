package knowledgeGraph.ga;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.wordSim.WordEmbedding;

import java.util.Arrays;
import java.util.HashSet;

public class VertexSimilarity {
    public static int[][] dp = new int[1500][1500];
    public static HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "the", "an", "of", "A", "The", "on", "with"));

    public static void main(String argv[]) {
        WordEmbedding wordEmbedding = new WordEmbedding();
        wordEmbedding.setEmbedding();
        System.out.println("finish reading");
        String A = "hyoglossus";
        String B = "hyoglossus muscle";
        String[] arrayA = A.split(" ");
        String[] arrayB = B.split(" ");
        double[] a = new double[200];
        for (int i = 0; i < arrayA.length; i++) {
            if (stopWords.contains(arrayA[i])) {
                continue;
            }
            double[] temp = wordEmbedding.getWordEmbedding(arrayA[i]);
            if (temp.length != 200) {
                System.out.println("similarity erro" + arrayA[i]);
                continue;
            }
            for (int j = 0; j < 200; j++) {
                a[j] += temp[j];
            }
        }
        double[] b = new double[200];
        for (int i = 0; i < arrayB.length; i++) {
            if (stopWords.contains(arrayB[i])) {
                continue;
            }
            double[] temp = wordEmbedding.getWordEmbedding(arrayB[i]);
            for (int j = 0; j < 200; j++) {
                b[j] += temp[j];
            }
        }
        double multiple_ans = 0.0;
        double multiple_a = 0.0, multiple_b = 0.0;
        for (int i = 0; i < 200; i++) {
            multiple_a += a[i] * a[i];
            multiple_b += b[i] * b[i];
            multiple_ans += a[i] * b[i];
        }
        System.out.println(multiple_ans + "\t" + multiple_a + "\t" + multiple_b);
        System.out.println(multiple_ans / (Math.sqrt(multiple_a) * Math.sqrt(multiple_b)));

    }

    public static double calcSimilarity(Vertex v1, Vertex v2) {
//        double wordSimilarity = WordSimilarityForCh.simWord(value1, value2);
//        if (Double.doubleToLongBits(wordSimilarity) == Double.doubleToLongBits(0)) {
//            wordSimilarity = VertexSimilarity.getEditDistance(value1, value2);
//        }
        double wordEditDistance = getEditDistance(v1.getValue(), v2.getValue());
        double embeddingDistance = getEmbeddingSimilarity(v1, v2);
        return (wordEditDistance + embeddingDistance) / 2;
//        wordSimilarity = CoreSynonymDictionary.similarity(v1.getValue(), v2.getValue());
//        System.out.println(v1.getValue() + " " + v2.getValue() + " " + wordSimilarity);
//        return wordSimilarity;
    }

    public static double getEmbeddingSimilarity(Vertex v1, Vertex v2) {
        String A = v1.getValue(), B = v2.getValue();
        String[] arrayA = A.split(" ");
        String[] arrayB = B.split(" ");
        double[] a = new double[200];
        for (int i = 0; i < arrayA.length; i++) {
            if (stopWords.contains(arrayA[i])) {
                continue;
            }
            double[] temp = ExperimentMain.wordEmbedding.getWordEmbedding(arrayA[i]);
            if (temp.length != 200) {
                System.out.println("similarity erro" + arrayA[i]);
                continue;
            }
            for (int j = 0; j < 200; j++) {
                a[j] += temp[j];
            }
        }
        double[] b = new double[200];
        for (int i = 0; i < arrayB.length; i++) {
            if (stopWords.contains(arrayB[i])) {
                continue;
            }
            double[] temp = ExperimentMain.wordEmbedding.getWordEmbedding(arrayB[i]);
            for (int j = 0; j < 200; j++) {
                b[j] += temp[j];
            }
        }
        double multiple_ans = 0.0;
        double multiple_a = 0.0, multiple_b = 0.0;
        for (int i = 0; i < 200; i++) {
            multiple_a += a[i] * a[i];
            multiple_b += b[i] * b[i];
            multiple_ans += a[i] * b[i];
        }
        if (multiple_a == 0.0 || multiple_b == 0.0) {
            return 0;
        }
        double ans = multiple_ans / (Math.sqrt(multiple_a) * Math.sqrt(multiple_b));
        return ans;
    }


    public static double getEditDistance(String A, String B) {
        if (A.equals(B)) {
            return 1.0;
        }
        //dp[i][j]表示源串A位置i到目标串B位置j处最低需要操作的次数
        dp[0][0] = 0;
        for (int i = 1; i <= A.length(); i++)
            dp[i][0] = i;
        for (int j = 1; j <= B.length(); j++)
            dp[0][j] = j;
        for (int i = 1; i <= A.length(); i++) {
            for (int j = 1; j <= B.length(); j++) {
                if (A.charAt(i - 1) == B.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else {
                    dp[i][j] = Math.min(dp[i - 1][j] + 1,
                            Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + 1));
                }
            }
        }
        return 1 - ((double) dp[A.length()][B.length()] / Math.max(A.length(), B.length()));
    }
}
