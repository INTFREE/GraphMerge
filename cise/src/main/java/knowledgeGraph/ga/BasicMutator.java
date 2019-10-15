package knowledgeGraph.ga;

import javafx.util.Pair;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;
import knowledgeGraph.util.UtilFunction.RandomUtil;
import knowledgeGraph.util.UtilFunction.CollectionUtil;

import java.util.*;


public class BasicMutator implements Mutator {

    @Override
    public boolean mutate(MergedGraghInfo inputMergedGraph) {
        MergedVertex mergedVertex = inputMergedGraph.getMergedGraph().getMostEntropyMergedVertex();
        MergedGraph mergedGraph = inputMergedGraph.getMergedGraph();
        EdgeType type = EdgeType.OUT;
        if (mergedVertex.getType().equalsIgnoreCase("entity")) {
            type = EdgeType.IN;
        }
        Pair<Vertex, Set<MergedVertex>> vertexSetPair = getMostDifferentVertex(mergedGraph, mergedVertex, type);
        Set<MergedVertex> mergedVertexSet = inputMergedGraph.getMergedVertexByType(vertexSetPair.getKey().getType());
        MergedVertex mutateTarget = getTargetMergedVertex(mergedGraph, mergedVertexSet, vertexSetPair.getValue(), type);
        mergedGraph.mutateMergedGraph(mutateTarget, vertexSetPair.getKey());
        return true;
    }

    private MergedVertex getTargetMergedVertex(MergedGraph mergedGraph, Set<MergedVertex> baseMergedVertexSet, Set<MergedVertex> mergedVertices, EdgeType edgeInOrOut) {
        HashMap<MergedVertex, Double> vertexDifference = new HashMap<>();
        for (MergedVertex mergedVertex : baseMergedVertexSet) {
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
        double res = Double.MAX_VALUE;
        MergedVertex minDifferentVertex = null;
        for (MergedVertex mergedVertex : vertexDifference.keySet()) {
            double tmp = vertexDifference.get(mergedVertex);
            if (tmp < res) {
                res = tmp;
                minDifferentVertex = mergedVertex;
            }
        }
        return minDifferentVertex;
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
        System.out.println(result.size());
        return intersection / result.size();
    }
}
