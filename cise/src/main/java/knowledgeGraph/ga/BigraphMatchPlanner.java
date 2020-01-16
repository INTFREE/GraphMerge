package knowledgeGraph.ga;

import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.mergeModel.MigratePlanner;
import knowledgeGraph.wordSim.RelatedWord;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BigraphMatchPlanner implements MigratePlanner {
    private Graph graph1;
    private Graph graph2;
    private HashSet<Vertex> entityVertexSet1;
    private HashSet<Vertex> entityVertexSet2;
    private MergedGraghInfo mergedGraghInfo;
    private MigratePlan migratePlan;
    private RelatedWord relatedWord;
    String regex = "^[(,!:]+";
    String regexEnd = "[),!:]+$";
    private HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "the", "an", "of", "A", "The", "on", "with"));


    public BigraphMatchPlanner(Graph graph1, Graph graph2, MergedGraghInfo mergedGraghInfo) {
        this.graph1 = graph1;
        this.graph2 = graph2;
        this.mergedGraghInfo = mergedGraghInfo;
        this.entityVertexSet1 = new HashSet<>();
        this.entityVertexSet2 = new HashSet<>();
        migratePlan = new MigratePlan();
        relatedWord = new RelatedWord();
    }

    @Override
    public MigratePlan getVertexMigratePlan() {
        if (!this.mergedGraghInfo.isBiGraph()) {
            System.out.println("This is not a bigraph, don't use this planner");
            return null;
        }
        Bigraph bigraph = this.generateBigraph(graph1, graph2);
        MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
                = new MaximumWeightBipartiteMatching<>(bigraph, entityVertexSet1, entityVertexSet2);
        MatchingAlgorithm.Matching<Vertex, DefaultWeightedEdge> matching = bipartiteMatching.getMatching();
        System.out.println("Bigraph match size :" + matching.getEdges().size());
        for (DefaultWeightedEdge edge : matching.getEdges()) {
            migratePlan.addPlan(new Plan(bigraph.getEdgeSource(edge), bigraph.getEdgeSource(edge).getMergedVertex(), bigraph.getEdgeTarget(edge).getMergedVertex()));
        }
        return migratePlan;
    }

    private Bigraph generateBigraph(Graph graph1, Graph graph2) {
        Bigraph bigraph = new Bigraph();
        // Find the same name vertex
        HashMap<String, Vertex> sameValueMap = new HashMap<>();
        for (Vertex vertex : graph1.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("entity")) {
                sameValueMap.put(vertex.getValue(), vertex);
            }
        }
        for (Vertex vertex : graph2.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("entity")) {
                if (sameValueMap.containsKey(vertex.getValue())) {
                    migratePlan.addPlan(new Plan(vertex, vertex.getMergedVertex(), sameValueMap.get(vertex.getValue()).getMergedVertex()));
                    sameValueMap.remove(vertex.getValue());
                } else {
                    entityVertexSet2.add(vertex);
                }
            }
        }
        System.out.println("Graph1 left vertex size : " + sameValueMap.size());
        System.out.println("Graph2 left vertex size :" + entityVertexSet2.size());
        entityVertexSet1.addAll(sameValueMap.values());
//        for (Vertex vertex : graph1.vertexSet()) {
//            if (vertex.getType().equalsIgnoreCase("entity")) {
//                entityVertexSet1.add(vertex);
//            }
//        }
//        for (Vertex vertex : graph2.vertexSet()) {
//            if (vertex.getType().equalsIgnoreCase("entity")) {
//                entityVertexSet2.add(vertex);
//            }
//        }

        for (Vertex vertex : entityVertexSet1) {
            bigraph.addVertex(vertex);
        }
        for (Vertex vertex : entityVertexSet2) {
            bigraph.addVertex(vertex);
        }
        HashMap<String, HashSet<Vertex>> keyWordToVertex1 = graph1.getKeyWordToVertex();
        HashMap<String, HashSet<Vertex>> keyWordToVertex2 = graph2.getKeyWordToVertex();
        String[] relatedWords;
        String[] allwords;
        HashSet<Vertex> relatedVertex = new HashSet<>();
        for (Vertex vertex : entityVertexSet1) {
            relatedVertex.clear();
            allwords = vertex.getValue().split(" ");
            for (String word : allwords) {
                String temp_word = word.replaceAll(regex, "").replaceAll(regexEnd, "");
                if (stopWords.contains(temp_word)) {
                    continue;
                }
                if (keyWordToVertex2.containsKey(temp_word)) {
                    relatedVertex.addAll(keyWordToVertex2.get(temp_word));
                }
                if (relatedWord.getRelatedWord().containsKey(temp_word)) {
                    relatedWords = relatedWord.getRelatedWord().get(temp_word);
                    for (String relatedWord : relatedWords) {
                        if (keyWordToVertex2.containsKey(relatedWord)) {
                            relatedVertex.addAll(keyWordToVertex2.get(relatedWord));
                        }
                    }
                }
            }
            for (Vertex vertex1 : relatedVertex) {
                if (entityVertexSet2.contains(vertex1)) {
                    bigraph.setEdgeWeight(bigraph.addEdge(vertex, vertex1), VertexSimilarity.calcSimilarity(vertex, vertex1));
                }
            }

        }
//        for (Vertex vertex : entityVertexSet1) {
//            for (Vertex vertex1 : entityVertexSet2) {
//                double similarity = VertexSimilarity.calcSimilarity(vertex, vertex1);
//                if (similarity > 0.5) {
//                    bigraph.setEdgeWeight(bigraph.addEdge(vertex, vertex1), VertexSimilarity.calcSimilarity(vertex, vertex1));
//                }
//            }
//        }

        System.out.println("bigraph vertex size : " + bigraph.vertexSet().size());
        System.out.println("bigraph edge size : " + bigraph.edgeSet().size());
        return bigraph;
    }
}
