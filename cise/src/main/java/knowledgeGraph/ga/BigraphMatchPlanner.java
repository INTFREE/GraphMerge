package knowledgeGraph.ga;

import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
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


    public BigraphMatchPlanner(Graph graph1, Graph graph2, MergedGraghInfo mergedGraghInfo) {
        this.graph1 = graph1;
        this.graph2 = graph2;
        this.mergedGraghInfo = mergedGraghInfo;
        this.entityVertexSet1 = new HashSet<>();
        this.entityVertexSet2 = new HashSet<>();
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
        MigratePlan migratePlan = new MigratePlan();
        for (DefaultWeightedEdge edge : matching.getEdges()) {
            migratePlan.addPlan(new Plan(bigraph.getEdgeSource(edge), bigraph.getEdgeSource(edge).getMergedVertex(), bigraph.getEdgeTarget(edge).getMergedVertex()));
        }
        return migratePlan;
    }

    private Bigraph generateBigraph(Graph graph1, Graph graph2) {
        Bigraph bigraph = new Bigraph();
        for (Vertex vertex : graph1.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("entity")) {
                entityVertexSet1.add(vertex);
                bigraph.addVertex(vertex);
            }
        }
        for (Vertex vertex : graph2.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("entity")) {
                entityVertexSet2.add(vertex);
                bigraph.addVertex(vertex);
            }
        }
        System.out.println("graph1 entity size :" + entityVertexSet1.size());
        System.out.println("graph2 entity size :" + entityVertexSet2.size());
        HashMap<Double, HashSet<DefaultWeightedEdge>> edgeToWeight = new HashMap<>();

        for (Vertex vertex : entityVertexSet1) {
            edgeToWeight.clear();
            for (Vertex vertex1 : entityVertexSet2) {
                double similarity = VertexSimilarity.calcSimilarity(vertex, vertex1);
                DefaultWeightedEdge edge = bigraph.addEdge(vertex, vertex1);
                System.out.println(vertex.getValue() + " " + vertex1.getValue() + " " + similarity);
                if (!edgeToWeight.containsKey(similarity)) {
                    edgeToWeight.put(similarity, new HashSet<>());
                }
                edgeToWeight.get(similarity).add(edge);
                bigraph.setEdgeWeight(edge, VertexSimilarity.calcSimilarity(vertex, vertex1));
            }
            if (edgeToWeight.containsKey(1.0)) {
                System.out.println("There is one edge");
                for (Map.Entry<Double, HashSet<DefaultWeightedEdge>> entry : edgeToWeight.entrySet()) {
                    if (entry.getKey().equals(1.0)) {
                        continue;
                    }
                    bigraph.removeAllEdges(entry.getValue());
                }
            }
        }
        System.out.println("bigraph vertex size : " + bigraph.vertexSet().size());
        System.out.println("bigraph edge size : " + bigraph.edgeSet().size());
        return bigraph;
    }
}
