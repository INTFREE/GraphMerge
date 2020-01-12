package knowledgeGraph.ga;

import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.mergeModel.MigratePlanner;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;

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
        System.out.println(this.entityVertexSet1.size());
        System.out.println(this.entityVertexSet2.size());

        KuhnMunkresMinimalWeightBipartitePerfectMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
                = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(bigraph, entityVertexSet1, entityVertexSet2);
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
        for (Vertex vertex : entityVertexSet1) {
            bigraph.addVertex(vertex);
            for (Vertex vertex1 : entityVertexSet2) {
                bigraph.addVertex(vertex1);
                DefaultWeightedEdge edge = bigraph.addEdge(vertex, vertex1);
                bigraph.setEdgeWeight(edge, VertexSimilarity.calcSimilarity(vertex, vertex1));
            }
        }

        System.out.println("bigraph vertex size : " + bigraph.vertexSet().size());
        System.out.println("bigraph edge size : " + bigraph.edgeSet().size());
        return bigraph;
    }
}
