package knowledgeGraph.ga;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.TripleMain;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.wordSim.WordEmbedding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class VertexSimilarity {
    public static int[][] dp = new int[1505][1505];
    public static HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "the", "an", "of", "A", "The", "on", "with"));
    public static String[] arrayA;
    public static String[] arrayB;
    static double[] a = new double[200];
    static double[] b = new double[200];
    static double[] tempEmbedding = new double[200];

    public static void main(String argv[]) {
        double[] temp = new double[200];
        for (int i = 0; i < 200; i++) {
            System.out.println(temp[i]);
        }
        temp[1] = 1;
        for (int i = 0; i < 200; i++) {
            System.out.println(temp[i]);
        }
    }

    public static double calcSimilarity(Vertex v1, Vertex v2) {
        double wordEditDistance = getEditDistance(v1.getValue(), v2.getValue());
        double embeddingDistance = getEmbeddingSimilarity(v1, v2);
        return (wordEditDistance + embeddingDistance) / 2;
    }

    public static double getEmbeddingSimilarity(Vertex v1, Vertex v2) {
        String A = v1.getValue(), B = v2.getValue();
        arrayA = A.split(" ");
        arrayB = B.split(" ");
        for (int i = 0; i < 200; i++) {
            a[i] = b[i] = 0.0;
        }
        for (int i = 0; i < arrayA.length; i++) {
            if (stopWords.contains(arrayA[i])) {
                continue;
            }
            tempEmbedding = TripleMain.wordEmbedding.getWordEmbedding(arrayA[i]);
            for (int j = 0; j < 200; j++) {
                a[j] += tempEmbedding[j];
            }
        }
        for (int i = 0; i < arrayB.length; i++) {
            if (stopWords.contains(arrayB[i])) {
                continue;
            }
            tempEmbedding = TripleMain.wordEmbedding.getWordEmbedding(arrayB[i]);
            for (int j = 0; j < 200; j++) {
                b[j] += tempEmbedding[j];
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
        if (A.length() > 1500) {
            A = A.substring(1500);
        }
        if (B.length() > 1500) {
            B = B.substring(1500);
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
