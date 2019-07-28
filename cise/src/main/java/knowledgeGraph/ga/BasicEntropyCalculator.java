package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicEntropyCalculator implements EntropyCalculator {
    @Override
    public double calculateEntropy(MergedGraghInfo mergedGraphInfo) {
        System.out.println("enter entropy calculate");
        int DLT = mergedGraphInfo.getGraphsInfo().getGraphNum();
        // 初始化熵
        double finalEntropy = 0.0;
        double maxVertexEntropy = 0.0;
        // 融合图的边数
        int edgeNum = mergedGraphInfo.getMergedGraph().edgeSet().size();
        double edgeEntropy = 0.0;
        Set<MergedVertex> mergedVertexSet = mergedGraphInfo.getMergedGraph().vertexSet();
        for (MergedVertex mergedVertex : mergedVertexSet) {
            Set<Graph> graphSetInMV = new HashSet<>();
            for (Vertex vertex : mergedVertex.getVertexSet()) {
                graphSetInMV.add(vertex.getGraph());
            }

            double currentEntropy = 0.0;
            Set<String> inEdgeTypeSet = new HashSet<>();
            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
                inEdgeTypeSet.add(mergedEdge.getRoleName());
            }

            Set<String> outEdgeTypeSet = new HashSet<>();
            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex)) {
                outEdgeTypeSet.add(mergedEdge.getRoleName());
            }

            for (String inType : inEdgeTypeSet) {
                currentEntropy += calculateEdgeEntropyForVertex(mergedGraphInfo, mergedVertex, graphSetInMV, inType, EdgeType.IN);
            }
            for (String outType : outEdgeTypeSet) {
                currentEntropy += calculateEdgeEntropyForVertex(mergedGraphInfo, mergedVertex, graphSetInMV, outType, EdgeType.OUT);
            }
            edgeEntropy += currentEntropy;
//            if (mergedVertex.getType().equals("Entity")) {
//                currentEntropy += 5 * calculateVertexContentEntropy(mergedGraphInfo, graphSetInMV, mergedVertex);
//                if (currentEntropy > maxVertexEntropy){
//                    mergedGraphInfo.getMergedGraph().setMostEntropyMergedVertex(mergedVertex);
//                }
//            }
//            if(currentEntropy > maxVertexEntropy){
//                mergedGraphInfo.getMergedGraph().setMostEntropyMergedVertex(mergedVertex);
//            }
//            double delta = Math.log(DLT) / Math.log(2);
//            delta = delta <= 2.0 ? 2.0 : delta;
//            finalEntropy += currentEntropy * (delta - Math.pow(delta, (double) graphSetInMV.size() / DLT)) * edgeNum;
            finalEntropy += currentEntropy * edgeNum;

        }
        System.out.println("edge entropy " + edgeEntropy);
        System.out.println("final entropy" + finalEntropy);
        return finalEntropy;
    }

    /**
     * 计算融合节点自身的熵，这个熵不考虑节点相关的边，只计算来自融合节点自身内容的不一致
     *
     * @return
     */
    private double calculateVertexContentEntropy(MergedGraghInfo mergedGraphInfo, Set<Graph> graphInThisMv, MergedVertex mergedVertex) {

        int graphNum = graphInThisMv.size();

        double entropy = 0.0;

        for (Vertex vX : mergedVertex.getVertexSet()) {
            double pX = 1.0 / graphNum;
            double rstSumPySxy = 0.0;
            for (Vertex vY : mergedVertex.getVertexSet()) {
                double pY = 1.0 / graphNum;
                double sXY = VertexSimilarity.calcSimilarity(vX, vY); // 需要确定节点相似度算法，如果是0.0会报Infinity
                rstSumPySxy += pY * sXY;
            }
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }

        return Math.abs(entropy);
    }

    private double calculateEdgeEntropyForVertex(MergedGraghInfo mergedGraphInfo,
                                                 MergedVertex mergedVertex,
                                                 Set<Graph> graphInThisMV,
                                                 String type,
                                                 EdgeType edgeInOrOut) {

        /**
         * 每个被融合图引用的融合边集合
         */
        Map<Graph, Set<MergedEdge>> graphToReferencedMergedEdgeSetMap = new HashMap<>();

        Set<MergedEdge> targetMergedEdgeSet = new HashSet<>();

        if (edgeInOrOut.equals(EdgeType.IN)) {
            targetMergedEdgeSet = mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex);

        } else if (edgeInOrOut.equals(EdgeType.OUT)) {
            targetMergedEdgeSet = mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex);
        }

        // 对于目标集合中的每个融合边，
        for (MergedEdge me : targetMergedEdgeSet) {
            // 找到其包含的被融合边集合
            Set<Edge> edgeSet = me.getEdgeSet();
            // 找到这些被融合边来自的被融合图的集合
            Set<Graph> referencedGraphSet = new HashSet<>();
            for (Edge edge : edgeSet) {
                referencedGraphSet.add(edge.getGraph());
            }
            // 对于找到的每个被融合图
            for (Graph graph : referencedGraphSet) {
                // 将当前的融合边添加到它引用的融合边集合中
                if (!graphToReferencedMergedEdgeSetMap.containsKey(graph)) {
                    graphToReferencedMergedEdgeSetMap.put(graph, new HashSet<>());
                }
                graphToReferencedMergedEdgeSetMap.get(graph).add(me);
            }
        }

        // 逆置这个映射，对于每个“融合边的组合”，计算引用了这个组合的被融合图的集合
        Map<Set<MergedEdge>, Set<Graph>> mergedEdgeSetToReferenceGraphSetMap = new HashMap<>();

        Set<MergedEdge> emptyEdgeSet = new HashSet<>();

        for (Graph graph : graphInThisMV) {
            Set<MergedEdge> mergedEdges;
            mergedEdges = graphToReferencedMergedEdgeSetMap.getOrDefault(graph, emptyEdgeSet);
            if (!mergedEdgeSetToReferenceGraphSetMap.containsKey(mergedEdges)) {
                mergedEdgeSetToReferenceGraphSetMap.put(mergedEdges, new HashSet<>());
            }
            mergedEdgeSetToReferenceGraphSetMap.get(mergedEdges).add(graph);

        }

        int graphNum = graphInThisMV.size();


        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        Set<Map.Entry<Set<MergedEdge>, Set<Graph>>> allEdgeComb = mergedEdgeSetToReferenceGraphSetMap.entrySet();
        for (Map.Entry<Set<MergedEdge>, Set<Graph>> edgeCombinationX : allEdgeComb) {
            Set<Graph> usersX = edgeCombinationX.getValue();
            double pX = ((double) usersX.size()) / graphNum;

            double rstSumPySxy = 0.0;

            for (Map.Entry<Set<MergedEdge>, Set<Graph>> edgeCombinationY : allEdgeComb) {
                Set<Graph> usersY = edgeCombinationY.getValue();
                double pY = ((double) usersY.size()) / graphNum;
                double similarity;

                // 如果两个融合边组合是一样的，则相似度为1
                if (edgeCombinationX.equals(edgeCombinationY)) {
                    similarity = 1.0;
                }
                // 如果其中一方为空，则相似度为0
                else if (edgeCombinationX.getKey().isEmpty() || edgeCombinationY.getKey().isEmpty()) {
                    similarity = 0;
                }

                // 否则，二者相似度等于 交集大小/并集大小
                else {
                    // 计算X和Y相似度时需要的交集和并集
                    Set<MergedEdge> intersection = new HashSet<>(edgeCombinationX.getKey());
                    intersection.retainAll(edgeCombinationY.getKey());
                    Set<MergedEdge> union = new HashSet<>(edgeCombinationX.getKey());
                    union.addAll(edgeCombinationY.getKey());
                    similarity = ((double) intersection.size()) / union.size();
                }
                rstSumPySxy += pY * similarity;
            }
            if (rstSumPySxy == 0) {
                System.out.println("here");
            }
            // 将log换位2为底
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }


        return Math.abs(entropy);
    }
}
