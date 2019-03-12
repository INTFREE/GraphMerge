package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.wordSim.WordSimilarityForCh;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

public class VertexSimilarity {
    public static double calcSimilarity(Vertex v1, Vertex v2) {
        Graph graph1 = v1.getGraph();
        String value1 = null;
        for (Edge edge : graph1.getEdgeSet()) {
            if (edge.getTarget().equals(v1)) {
                Vertex relationVertex = edge.getSource();
                boolean flag = false;
                for (Edge edge1 : graph1.getEdgeSet()) {
                    if (edge1.getSource().equals(relationVertex) && edge1.getTarget().getType().equals("Value")) {
                        value1 = edge1.getTarget().getValue();
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
        }
        Graph graph2 = v2.getGraph();
        String value2 = null;
        for (Edge edge : graph2.getEdgeSet()) {
            if (edge.getTarget().equals(v2)) {
                Vertex relationVertex = edge.getSource();
                boolean flag = false;
                for (Edge edge1 : graph2.getEdgeSet()) {
                    if (edge1.getSource().equals(relationVertex) && edge1.getTarget().getType().equals("Value")) {
                        value2 = edge1.getTarget().getValue();
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
        }
        if (value1 == null || value2 == null) {
            System.out.println("value error");
            return 0.0;
        }

        double wordSimilarity = WordSimilarityForCh.simWord(value1, value2);
        if (Double.doubleToLongBits(wordSimilarity) == Double.doubleToLongBits(0)) {
            wordSimilarity = VertexSimilarity.getEditDistance(value1, value2);
        }
        return wordSimilarity;
    }

    public static double getEditDistance(String A, String B) {
        if (A.equals(B)) {
            return 0.0;
        }
        //dp[i][j]表示源串A位置i到目标串B位置j处最低需要操作的次数
        int[][] dp = new int[A.length() + 1][B.length() + 1];
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
