package knowledgeGraph.ga;


import javafx.util.Pair;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.MigratePlan;
import knowledgeGraph.baseModel.Plan;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;

import java.util.*;

public class SimlarityMigratePlanner implements MigratePlanner {
    @Override
    public MigratePlan getVertexMigratePlan(MergedGraghInfo mergedGraghInfo) {
        System.out.println(">>>>>>> get Migrate Plan");
        MigratePlan migratePlan = new MigratePlan();
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        // 计算相似度
        // 迁移10%且熵值小于某个阈值
        List<HashMap.Entry<MergedVertex, Double>> mergedVertexArrayList = mergedGraghInfo.getMergedVertexToEntropy();
        for (HashMap.Entry<MergedVertex, Double> entry : mergedVertexArrayList) {
            System.out.println("Source " + entry.getKey().getVertexSet().size());
            EdgeType type = EdgeType.IN;
            System.out.println("type " + entry.getKey().getType());
            if (entry.getKey().getType().equalsIgnoreCase("relation")) {
                type = EdgeType.OUT;
            }
            Pair<Vertex, Set<MergedVertex>> vertexSetPair = getMostDifferentVertex(mergedGraph, entry.getKey(), type);
            Set<MergedVertex> sameTypeMergedVertexSet = mergedGraghInfo.getMergedVertexByType(vertexSetPair.getKey().getType());
            System.out.println("111 " + sameTypeMergedVertexSet.size());
            MergedVertex mutateTarget = getTargetMergedVertex(mergedGraph, entry.getKey(), sameTypeMergedVertexSet, vertexSetPair.getValue(), type);
            migratePlan.addPlan(new Plan(vertexSetPair.getKey(), entry.getKey(), mutateTarget));
            break;
        }
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
}
