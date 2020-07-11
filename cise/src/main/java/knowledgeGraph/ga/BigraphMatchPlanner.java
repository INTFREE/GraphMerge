package knowledgeGraph.ga;

import knowledgeGraph.TripleMain;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.mergeModel.MigratePlanner;
import knowledgeGraph.wordSim.RelatedWord;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BigraphMatchPlanner implements MigratePlanner {
    // TODO:修改为多图，主要是修改生成二部图匹配那里
    private Graph graph1;
    private Graph graph2;
    private HashSet<Vertex> entityVertexSet1;
    private HashSet<Vertex> entityVertexSet2;
    private MergedGraghInfo mergedGraghInfo;
    private MigratePlan migratePlan;
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
                if (sameValueMap.containsKey(vertex.getValue())) {
                    entityVertexSet1.add(vertex);
                } else {
                    sameValueMap.put(vertex.getValue(), vertex);
                }
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

        for (Vertex vertex : entityVertexSet1) {
            bigraph.addVertex(vertex);
        }
        for (Vertex vertex : entityVertexSet2) {
            bigraph.addVertex(vertex);
        }
        // TODO: 修改为多图
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
                if (TripleMain.relatedWord.getRelatedWord().containsKey(temp_word)) {
                    relatedWords = TripleMain.relatedWord.getRelatedWord().get(temp_word);
                    for (String relatedWord : relatedWords) {
                        if (keyWordToVertex2.containsKey(relatedWord)) {
                            relatedVertex.addAll(keyWordToVertex2.get(relatedWord));
                        }
                    }
                }
            }
            for (Vertex vertex1 : relatedVertex) {
                if (entityVertexSet2.contains(vertex1)) {
                    double sim = VertexSimilarity.calcSimilarity(vertex, vertex1);
                    bigraph.setEdgeWeight(bigraph.addEdge(vertex, vertex1), sim);
                }
            }
        }

        System.out.println("bigraph vertex size : " + bigraph.vertexSet().size());
        System.out.println("bigraph edge size : " + bigraph.edgeSet().size());
        return bigraph;
    }


}
