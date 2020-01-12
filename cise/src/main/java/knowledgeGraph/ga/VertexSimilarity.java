package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.wordSim.WordEmbedding;
import knowledgeGraph.wordSim.WordSimilarityForCh;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;

public class VertexSimilarity {
    public static int[][] dp = new int[1500][1500];

    public static double calcSimilarity(Vertex v1, Vertex v2) {
//        double wordSimilarity = WordSimilarityForCh.simWord(value1, value2);
//        if (Double.doubleToLongBits(wordSimilarity) == Double.doubleToLongBits(0)) {
//            wordSimilarity = VertexSimilarity.getEditDistance(value1, value2);
//        }
        double wordSimilarity = getEditDistance(v1.getValue(), v2.getValue());
        return wordSimilarity;
//        wordSimilarity = CoreSynonymDictionary.similarity(v1.getValue(), v2.getValue());
//        System.out.println(v1.getValue() + " " + v2.getValue() + " " + wordSimilarity);
//        return wordSimilarity;
    }

    public static double getEditDistance(String A, String B) {
        if (A.equals(B)) {
            return 0.0;
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
        return dp[A.length()][B.length()];
    }
}
