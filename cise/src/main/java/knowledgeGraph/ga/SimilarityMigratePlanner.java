package knowledgeGraph.ga;


import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.*;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.*;

public class SimilarityMigratePlanner implements MigratePlanner {
    private MergedGraghInfo mergedGraghInfo;

    public SimilarityMigratePlanner(MergedGraghInfo mergedGraghInfo) {
        this.mergedGraghInfo = mergedGraghInfo;
    }

    @Override
    public MigratePlan getVertexMigratePlan() {
        System.out.println(">>>>>>>Sim get Migrate Plan");
        MigratePlan migratePlan = new MigratePlan();
        MergedGraph mergedGraph = this.mergedGraghInfo.getMergedGraph();

        // 如果还存在单一节点，首先处理单独节点
        HashMap<String, HashSet<Pair<Vertex, VertexContext>>> oneNodeVerties = new HashMap<>();

        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getVertexSet().size() == 1 && mergedVertex.getType().equalsIgnoreCase("entity")) {
                Vertex vertex = mergedVertex.getVertexSet().iterator().next();
                if (!oneNodeVerties.containsKey(vertex.getGraph().getUserName())) {
                    oneNodeVerties.put(vertex.getGraph().getUserName(), new HashSet<>());
                }
                oneNodeVerties.get(vertex.getGraph().getUserName()).add(new Pair<>(vertex, mergedGraph.getVertexContext(vertex)));
            }
        }

        // 如果还存在二部图
        if (oneNodeVerties.keySet().size() == 2) {
            return getBigraphPlan(oneNodeVerties);
        }
        // 计算相似度
        // 迁移10%且熵值小于某个阈值
        List<HashMap.Entry<MergedVertex, Double>> mergedVertexArrayList = mergedGraghInfo.getMergedVertexToEntropy();
        for (HashMap.Entry<MergedVertex, Double> entry : mergedVertexArrayList) {
            if (!entry.getKey().getType().equalsIgnoreCase("entity")) {
                continue;
            }
            System.out.println("Source " + entry.getKey().getVertexSet().size());
            EdgeType type = EdgeType.IN;
            System.out.println("type " + entry.getKey().getType());
            if (entry.getKey().getType().equalsIgnoreCase("relation")) {
                type = EdgeType.OUT;
            }
            Pair<Vertex, Set<MergedVertex>> vertexSetPair = getMostDifferentVertex(mergedGraph, entry.getKey(), type);
            Set<MergedVertex> sameTypeMergedVertexSet = mergedGraghInfo.getMergedVertexByType(vertexSetPair.getKey().getType());
            MergedVertex mutateTarget = getTargetMergedVertex(mergedGraph, entry.getKey(), sameTypeMergedVertexSet, vertexSetPair.getValue(), type);
            System.out.println("Target " + mutateTarget.getVertexSet().size());
            migratePlan.addPlan(new Plan(vertexSetPair.getKey(), entry.getKey(), mutateTarget));
            break;
        }
        return migratePlan;
    }

    public MigratePlan getBigraphPlan(HashMap<String, HashSet<Pair<Vertex, VertexContext>>> oneNodeVerties) {
        MigratePlan migratePlan = new MigratePlan();
        Iterator<String> iterator = oneNodeVerties.keySet().iterator();
        String key1 = iterator.next();
        String key2 = iterator.next();
        System.out.println("still exist bigraph");
        System.out.println("graph" + key1 + " " + oneNodeVerties.get(key1).size());
        System.out.println("graph" + key2 + " " + oneNodeVerties.get(key2).size());
        Bigraph bigraph = new Bigraph();
        HashSet<Vertex> entity1 = new HashSet<>();
        HashSet<Vertex> entity2 = new HashSet<>();
        for (Pair<Vertex, VertexContext> vertex1 : oneNodeVerties.get(key1)) {
            bigraph.addVertex(vertex1.getKey());
            entity1.add(vertex1.getKey());
            for (Pair<Vertex, VertexContext> vertex2 : oneNodeVerties.get(key2)) {
                bigraph.addVertex(vertex2.getKey());
                entity2.add(vertex2.getKey());
                Double similarity = VertexSimilarity.calcSimilarity(vertex1.getKey(), vertex2.getKey());
                bigraph.setEdgeWeight(bigraph.addEdge(vertex1.getKey(), vertex2.getKey()), similarity);
            }
        }
        System.out.println("bigraph size" + bigraph.vertexSet().size() + " " + bigraph.vertexSet().size());
        System.out.println("entity1 size " + entity1.size());
        System.out.println("entity2 size " + entity2.size());
        saveBigraph(bigraph, entity1, entity2);
        MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
                = new MaximumWeightBipartiteMatching<>(bigraph, entity1, entity2);
        MatchingAlgorithm.Matching<Vertex, DefaultWeightedEdge> matching = bipartiteMatching.getMatching();
        System.out.println("matching size " + matching.getEdges().size());
        for (DefaultWeightedEdge edge : matching.getEdges()) {
            migratePlan.addPlan(new Plan(bigraph.getEdgeSource(edge), bigraph.getEdgeSource(edge).getMergedVertex(), bigraph.getEdgeTarget(edge).getMergedVertex()));
        }
        System.out.println("plan size " + migratePlan.getPlanArrayList().size());
        return migratePlan;
    }

    // 去除源节点
    private MergedVertex getTargetMergedVertex(MergedGraph mergedGraph,
                                               MergedVertex baseMergedVertex,
                                               Set<MergedVertex> baseMergedVertexSet,
                                               Set<MergedVertex> mergedVertices,
                                               EdgeType edgeInOrOut) {
        HashMap<MergedVertex, Double> vertexDifference = new HashMap<>();
        for (MergedVertex mergedVertex : baseMergedVertexSet) {
            if (mergedVertex.equals(baseMergedVertex))
                continue;
            Set<MergedVertex> connectedVertexSet = new HashSet<>();
            if (edgeInOrOut.equals((EdgeType.IN))) {
                Set<MergedEdge> mergedEdgeSet = mergedGraph.incomingEdgesOf(mergedVertex);
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    connectedVertexSet.add(mergedEdge.getSource());
                }
            } else {
                Set<MergedEdge> mergedEdgeSet = mergedGraph.outgoingEdgesOf(mergedVertex);
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    connectedVertexSet.add(mergedEdge.getTarget());
                }
            }
            vertexDifference.put(mergedVertex, getSimilarity(connectedVertexSet, mergedVertices));
        }
        System.out.println("GetTarget");
        double res = -1;
        MergedVertex mostLikeMergedVertex = null;
        // TODO: 都是0需要处理
        for (MergedVertex mergedVertex : vertexDifference.keySet()) {
            double tmp = vertexDifference.get(mergedVertex);
            if (tmp > res) {
                res = tmp;
                mostLikeMergedVertex = mergedVertex;
            }
        }
        System.out.println(mostLikeMergedVertex.getId());
        return mostLikeMergedVertex;
    }

    private Pair<Vertex, Set<MergedVertex>> getMostDifferentVertex(MergedGraph mergedGraph, MergedVertex mergedVertex, EdgeType edgeInOrOut) {
        HashMap<Vertex, Set<MergedVertex>> vertexToMergedVertex = new HashMap<>();
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        if (edgeInOrOut.equals(EdgeType.IN)) {
            mergedEdgeSet = mergedGraph.incomingEdgesOf(mergedVertex);
        } else if (edgeInOrOut.equals(EdgeType.OUT)) {
            mergedEdgeSet = mergedGraph.outgoingEdgesOf(mergedVertex);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                Vertex tmpVertex;
                if (edgeInOrOut.equals(EdgeType.IN)) {
                    tmpVertex = edge.getTarget();
                } else {
                    tmpVertex = edge.getSource();
                }
                if (!vertexToMergedVertex.containsKey(tmpVertex)) {
                    vertexToMergedVertex.put(tmpVertex, new HashSet<>());
                }
                MergedVertex tmpMergedVertex;
                if (edgeInOrOut.equals(EdgeType.IN)) {
                    tmpMergedVertex = mergedEdge.getSource();
                } else {
                    tmpMergedVertex = mergedEdge.getTarget();
                }
                vertexToMergedVertex.get(tmpVertex).add(tmpMergedVertex);
            }
        }
        HashMap<Vertex, Double> vertexDifference = new HashMap<>();
        for (Vertex vertex : vertexToMergedVertex.keySet()) {
            vertexDifference.put(vertex, 0.0);
        }
        for (Vertex vertex : vertexToMergedVertex.keySet()) {
            Set<MergedVertex> mergedVertices = vertexToMergedVertex.get(vertex);
            for (Vertex vertex1 : vertexToMergedVertex.keySet()) {
                if (vertex1.equals(vertex)) {
                    continue;
                }
                Set<MergedVertex> tmpSet = vertexToMergedVertex.get(vertex1);
                double similarity = getSimilarity(mergedVertices, tmpSet);
                vertexDifference.put(vertex, vertexDifference.get(vertex) + similarity);
                vertexDifference.put(vertex1, vertexDifference.get(vertex1) + similarity);
            }
        }
        //TODO: 如果不同度都是0需要跳过
        double res = Double.MAX_VALUE;
        Vertex mostDifferentVertex = null;
        for (Vertex vertex : vertexDifference.keySet()) {
            double tmp = vertexDifference.get(vertex);
            if (tmp < res) {
                res = tmp;
                mostDifferentVertex = vertex;
            }
        }
        Set<MergedVertex> mergedVertices = vertexToMergedVertex.get(mostDifferentVertex);
        return new Pair<>(mostDifferentVertex, mergedVertices);
    }

    private double getSimilarity(Set<MergedVertex> baseSet, Set<MergedVertex> givenSet) {
        Set<MergedVertex> result = new HashSet<>();
        result.addAll(baseSet);
        result.retainAll(givenSet);
        int intersection = result.size();
        result.clear();
        result.addAll(baseSet);
        result.addAll(givenSet);
        return intersection / result.size();
    }

    private void saveBigraph(Bigraph bigraph, HashSet<Vertex> vertices1, HashSet<Vertex> vertices2) {
        try {
            File file = new File("BigraphFile");
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("bigraph vertex size " + bigraph.vertexSet().size() + "\n");
            writer.write("bigraph edge size " + bigraph.edgeSet().size() + "\n");
            writer.write("entity information" + "\n");
            for (Vertex vertex : vertices1) {
                writer.write(vertex.getGraph().getUserName() + " " + vertex.getId() + " " + vertex.getValue() + "\n");
            }
            for (Vertex vertex : vertices2) {
                writer.write(vertex.getGraph().getUserName() + " " + vertex.getId() + " " + vertex.getValue() + "\n");
            }

            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save Bigraph error " + e);
        }
    }

    private void saveMigratePlan(MigratePlan migratePlan) {
        try {
            File file = new File("MigratePlan");
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("migrate size " + migratePlan.getPlanArrayList().size() + "\n");
            for (Plan plan : migratePlan.getPlanArrayList()) {
                writer.write(plan.getVertex().getId() + "\t" + plan.getVertex().getValue() + "\n");
                writer.write(plan.getSource().getId() + "\t" + plan.getTarget().getId() + "\n");
                writer.write("target vertex value " + plan.getTarget().getVertexSet().iterator().next().getValue());
            }

            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save MigratePlan error " + e);
        }
    }

}