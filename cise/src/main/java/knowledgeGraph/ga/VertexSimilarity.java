package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.util.wordSim.WordSimilarityForCh;
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
        if(value1 == null || value2 == null){
            System.out.println("value error");
            return 0.0;
        }
        return WordSimilarityForCh.simWord(value1, value2);
    }
}
