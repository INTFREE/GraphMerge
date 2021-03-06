package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;


public class BasicEntropyCalculator implements EntropyCalculator {

    MergedGraghInfo mergedGraghInfo;


    public BasicEntropyCalculator(MergedGraghInfo mergedGraghInfo) {
        this.mergedGraghInfo = mergedGraghInfo;
    }

    public BasicEntropyCalculator(boolean opt, boolean calcValue, boolean detailed) {
    }

    @Override
    public double calculateEntropy(MergedGraghInfo mergedGraphInfo) {
        mergedGraghInfo = mergedGraphInfo;
        System.out.println("enter entropy calculate");
        //int DLT = mergedGraphInfo.getGraphsInfo().getGraphNum();
        int DLT = 2;
        // 初始化熵
        double finalEntropy = 0.0;
        // 融合图的边数
        int edgeNum = mergedGraphInfo.getMergedGraph().edgeSet().size();
        double edgeEntropy = 0.0;
        Set<MergedVertex> mergedVertexSet = mergedGraphInfo.getMergedGraph().vertexSet();
        HashMap<MergedVertex, Double> mergedVertexEntropy = new HashMap<>();
        int countEntity = 0;
        long startTime = System.currentTimeMillis();
        for (MergedVertex mergedVertex : mergedVertexSet) {
            if (!mergedVertex.getType().equalsIgnoreCase("entity")) {
                continue;
            }
            countEntity += 1;
            List<Graph> graphListInMV = new ArrayList<>();
            for (Vertex vertex : mergedVertex.getVertexSet()) {
                graphListInMV.add(vertex.getGraph());
            }
            double currentEntropy = 0.0;
            double tmpEntropy = 0.0;
            int count_type = 1;
            Pair<Double, Integer> res;
            HashMap<String, List<MergedEdge>> inEdgeTypeHash = new HashMap<>();
            HashMap<String, List<MergedEdge>> outEdgeTypeHash = new HashMap<>();
            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
                if (mergedEdge.getEdgeSet().size() == 0) {
                    continue;
                }
                String roleName = mergedEdge.getRoleName();
                if (!inEdgeTypeHash.containsKey(roleName)) {
                    inEdgeTypeHash.put(roleName, new ArrayList<>());
                }
                inEdgeTypeHash.get(roleName).add(mergedEdge);
            }
            for (String inType : inEdgeTypeHash.keySet()) {
                res = calculateEdgeEntropyForVertex(graphListInMV, inEdgeTypeHash.get(inType), inType, EdgeType.IN);
                tmpEntropy = res.getKey();
                if (res.getValue() == 2) {
                    count_type += 1;
                }
                currentEntropy += tmpEntropy;
            }

            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex)) {
                if (mergedEdge.getEdgeSet().size() == 0) {
                    continue;
                }
                String roleName = mergedEdge.getRoleName();
                if (!outEdgeTypeHash.containsKey(roleName)) {
                    outEdgeTypeHash.put(roleName, new ArrayList<>());
                }
                outEdgeTypeHash.get(roleName).add(mergedEdge);
            }

            for (String outType : outEdgeTypeHash.keySet()) {
                res = calculateEdgeEntropyForVertex(graphListInMV, outEdgeTypeHash.get(outType), outType, EdgeType.OUT);
                tmpEntropy = res.getKey();
                if (res.getValue() == 2) {
                    count_type += 1;
                }
                currentEntropy += tmpEntropy;
            }
            currentEntropy = currentEntropy / count_type;
            mergedVertexEntropy.put(mergedVertex, currentEntropy);
            edgeEntropy += currentEntropy;
            finalEntropy += currentEntropy * edgeNum;
            if (countEntity % 1000 == 0) {
                long endTime = System.currentTimeMillis();
                System.out.println("caculate size " + countEntity + "\t" + (endTime - startTime));
                startTime = endTime;
            }
        }
        mergedGraphInfo.setMergedVertexToEntropy(mergedVertexEntropy);
        System.out.println("edge entropy " + edgeEntropy);
        System.out.println("final entropy " + finalEntropy);
        return finalEntropy;
    }

    public double calculateVertexEntropy(MergedVertex mergedVertex) {
        if (this.mergedGraghInfo == null) {
            System.out.println("no mergedGraphInfo");
            return 0.0;
        }
        List<Graph> graphListInMV = new ArrayList<>();
        for (Vertex vertex : mergedVertex.getVertexSet()) {
            graphListInMV.add(vertex.getGraph());
        }
        double currentEntropy = 0.0;
        double tmpEntropy = 0.0;
        int count_type = 1;
        Pair<Double, Integer> res;
        HashMap<String, List<MergedEdge>> inEdgeTypeHash = new HashMap<>();
        HashMap<String, List<MergedEdge>> outEdgeTypeHash = new HashMap<>();
        for (MergedEdge mergedEdge : this.mergedGraghInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
            if (mergedEdge.getEdgeSet().size() == 0) {
                continue;
            }
            String roleName = mergedEdge.getRoleName();
            if (!inEdgeTypeHash.containsKey(roleName)) {
                inEdgeTypeHash.put(roleName, new ArrayList<>());
            }
            inEdgeTypeHash.get(roleName).add(mergedEdge);
        }
        for (String inType : inEdgeTypeHash.keySet()) {
            res = calculateEdgeEntropyForVertex(graphListInMV, inEdgeTypeHash.get(inType), inType, EdgeType.IN);
            tmpEntropy = res.getKey();
            if (res.getValue() == 2) {
                count_type += 1;
            }
            currentEntropy += tmpEntropy;
        }

        for (MergedEdge mergedEdge : this.mergedGraghInfo.getMergedGraph().outgoingEdgesOf(mergedVertex)) {
            if (mergedEdge.getEdgeSet().size() == 0) {
                continue;
            }
            String roleName = mergedEdge.getRoleName();
            if (!outEdgeTypeHash.containsKey(roleName)) {
                outEdgeTypeHash.put(roleName, new ArrayList<>());
            }
            outEdgeTypeHash.get(roleName).add(mergedEdge);
        }

        for (String outType : outEdgeTypeHash.keySet()) {
            res = calculateEdgeEntropyForVertex(graphListInMV, outEdgeTypeHash.get(outType), outType, EdgeType.OUT);
            tmpEntropy = res.getKey();
            if (res.getValue() == 2) {
                count_type += 1;
            }
            currentEntropy += tmpEntropy;
        }
        return currentEntropy / count_type;
    }



    private Pair<Double, Integer> calculateEdgeEntropyForVertex(List<Graph> graphInThisMV,
                                                                List<MergedEdge> targetMergedEdgeSet,
                                                                String type,
                                                                EdgeType edgeInOrOut) {

        /**
         * 每个被融合图引用的融合边集合
         */
        Map<String, List<MergedEdge>> graphToReferencedMergedEdgeSetMap = new HashMap<>();
        // 对于目标集合中的每个融合边，
        for (MergedEdge me : targetMergedEdgeSet) {
            // 找到其包含的被融合边集合
            Set<Edge> edgeSet = me.getEdgeSet();
            // 找到这些被融合边来自的被融合图的集合
            for (Edge edge : edgeSet) {
                Graph graph = edge.getGraph(); //Merged Edge中不存在来自于同一个图的多条边
                String graphId = graph.getUserName();
                if (!graphToReferencedMergedEdgeSetMap.containsKey(graphId)) {
                    graphToReferencedMergedEdgeSetMap.put(graphId, new ArrayList<>());
                }
                graphToReferencedMergedEdgeSetMap.get(graphId).add(me);
            }
        }
        // 逆置这个映射，对于每个“融合边的组合”，计算引用了这个组合的被融合图的集合
        Map<List<MergedEdge>, List<Graph>> mergedEdgeSetToReferenceGraphSetMap = new HashMap<>();

        List<MergedEdge> emptyEdgeSet = new ArrayList<>();

        for (Graph graph : graphInThisMV) {
            List<MergedEdge> mergedEdges;
            mergedEdges = graphToReferencedMergedEdgeSetMap.getOrDefault(graph.getUserName(), emptyEdgeSet);
            if (!mergedEdgeSetToReferenceGraphSetMap.containsKey(mergedEdges)) {
                mergedEdgeSetToReferenceGraphSetMap.put(mergedEdges, new ArrayList<>());
            }
            mergedEdgeSetToReferenceGraphSetMap.get(mergedEdges).add(graph);

        }
        /*
         TODO:
            改为 int graphNum = graphInThisMV.size(); 这样缺省的情况存在的熵值，缺省值会判定为一个和之前都不相似的值.
            2部图, 如果缺省的话理想情况下应该是1不是0.5
            我看了下代码好像现在可能会算出来0.5
         Done.
        */
//        int graphNum = graphInThisMV.size(); //此时缺省值相当于非相似的值
        int graphNum = graphToReferencedMergedEdgeSetMap.size(); //此时若缺省则不计入运算
        // For test
        // int graphNum = 2;
        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        ArrayList<Map.Entry<List<MergedEdge>, List<Graph>>> allEdgeComb = new ArrayList<>(mergedEdgeSetToReferenceGraphSetMap.entrySet());
        int lenth = allEdgeComb.size();
        ArrayList<VertexContext> contextArrayList = new ArrayList<>(lenth);
        Integer id = 0;
        for (Map.Entry<List<MergedEdge>, List<Graph>> entry : allEdgeComb) {
            contextArrayList.add(id, genertateContext(entry.getKey(), id));
            id += 1;
        }
        for (int i = 0; i < lenth; i++) {
            Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationX = allEdgeComb.get(i);
            List<Graph> usersX = edgeCombinationX.getValue();
            double pX = ((double) usersX.size()) / graphNum;
            double rstSumPySxy = 0.0;
            for (int j = 0; j < lenth; j++) {
                Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationY = allEdgeComb.get(j);
                List<Graph> usersY = edgeCombinationY.getValue();
                double pY = ((double) usersY.size()) / graphNum;
                double similarity;
                // 如果两个融合边组合是一样的，则相似度为1
                if (i == j) {
                    similarity = 1.0;
                } else if (edgeCombinationX.equals(edgeCombinationY)) {
                    similarity = 1.0;
                }
                // 如果其中一方为空，则相似度为0
                else if (edgeCombinationX.getKey().isEmpty() || edgeCombinationY.getKey().isEmpty()) {
                    similarity = 0;
                }
               /*
                 FIXME:
                    因为当前节点通过relation节点连接, 算熵的时候需要按照relation异侧的节点进行计算
                    需要更新s(x,y)的计算方法：
                        原mv1，mv2其实图中的relation节点，所以需要找到relation节点连接的目标节点
                    举例：
                        对于二元关系的计算
                            rel(name)
                                - - E(张三)
                                - name - val(张三)
                            计算E(张三)的熵值，当前mv取到的是rel(name)节点，实际上mv需要取到异侧节点val(张三)。
                            计算方法不变。
                        对于多元关系的计算
                            r(出演)
                                - 电影 - E(大话西游)
                                - 演员 - E(周星驰）
                                - 角色 - E(至尊宝)
                            计算E(大话西游)的熵值，当前实际上计算的是r(出演)，我们需要计算的是- 演员 - E(周星驰），- 角色 - E(至尊宝)
                    TODO:
                        考虑2点：
                            a. 是否考虑多元关系
                            b. 时候考虑连接一组val集合(最后一个else的情况，当前是如果连接一组的话就不考虑只的相似性了)
                        方案1：不考虑a、b，需要更新第一个else if中的mv计算方式
                        方案2：仅考虑b. 需要采用方案1，且需要更新最后一个else，同样需要计算考虑mv的值相似性，且不需要做最大匹配。
                        方案3: 仅考虑a. 需要采用方案1，且将一个多元关系拆分为多个二元关系
                        方案4: 仅考虑a, 需要采用方案1, 且将多元关系作为一个集合（类似else中的做法）
                        方案5: 考虑a和b，采用方案2+方案3/4

                        建议采用方案1或者方案3
                */

                // 如果是两个值节点，计算值的相似度（编辑距离）
                // FIXME: edgeCombinationX.getKey().size() == 1 && edgeCombinationY.getKey().size() == 1 这个不太懂，是说如果只有一条出边吧？非集合状态
                else {
                    similarity = contextArrayList.get(i).getSimilarity(contextArrayList.get(j));
                }
                rstSumPySxy += pY * similarity;
            }
            // 将log换位2为底
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }
        return new ImmutablePair<>(Math.abs(entropy), graphNum);
    }

    public VertexContext genertateContext(List<MergedEdge> mergedEdgeList, Integer id) {
        VertexContext vertexContext = new VertexContext(id);
        for (MergedEdge mergedEdge : mergedEdgeList) {
            HashMap<String, MergedVertex> mergedVertexHashMap = new HashMap<>();
            MergedVertex relationVertex = mergedEdge.getSource();
            Set<MergedEdge> outgoingMergedEdges = mergedGraghInfo.getMergedGraph().outgoingEdgesOf(relationVertex);
            for (MergedEdge relationMergedEdge : outgoingMergedEdges) {
                if (relationMergedEdge.equals(mergedEdge)) {
                    continue;
                }
                mergedVertexHashMap.put(relationMergedEdge.getRoleName(), relationMergedEdge.getTarget());
            }
            vertexContext.addContext(mergedVertexHashMap);
        }
        return vertexContext;
    }

    public void printEdgeSet(List<MergedEdge> mergedEdgeList) {
        String info = "";
        for (MergedEdge mergedEdge : mergedEdgeList) {
            info += mergedEdge.getRoleName() + mergedEdge.getEdgeSet().iterator().next().getId() + "\t";
        }
        System.out.println(info);
    }
}
