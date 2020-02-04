package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Bigraph;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BasicEntropyCalculator implements EntropyCalculator {

    boolean opt = false;
    boolean calcValue = true;
    boolean detailed = false;
    HashSet<MergedVertex> unusualMergedVertexSet = new HashSet<>();


    public BasicEntropyCalculator() {
    }

    public BasicEntropyCalculator(boolean opt, boolean calcValue, boolean detailed) {
        this.opt = opt;
        this.calcValue = calcValue;
        this.detailed = detailed;
    }

    @Override
    public double calculateEntropy(MergedGraghInfo mergedGraphInfo) {

        int[] count = {0, 0, 0, 0, 0, 0};
        double[] partEntropy = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        int unusuals = 0;
        double threshold = 0;
        System.out.println("enter entropy calculate");
        //int DLT = mergedGraphInfo.getGraphsInfo().getGraphNum();
        int DLT = 2;
        // 初始化熵
        double finalEntropy = 0.0;
        double maxVertexEntropy = 0.0;
        // 融合图的边数
        int edgeNum = mergedGraphInfo.getMergedGraph().edgeSet().size();
        double edgeEntropy = 0.0;
        Set<MergedVertex> mergedVertexSet = mergedGraphInfo.getMergedGraph().vertexSet();
        HashMap<MergedVertex, Double> mergedVertexEntropy = new HashMap<>();
        for (MergedVertex mergedVertex : mergedVertexSet) {
            if (opt && calcValue && mergedVertex.getVertexSet().size() <= 1) continue; // 只有一个值节点时，熵值为0;
            //if (opt && mergedVertex.getType() == "Relation") continue; // Relaiton节点没有熵值
            if (opt && !calcValue && mergedVertex.getType() == "Value") continue; // 不计算Value的入熵

            //Set<Graph> graphSetInMV = new HashSet<>();
            List<Graph> graphListInMV = new ArrayList<>();
            for (Vertex vertex : mergedVertex.getVertexSet()) {
                graphListInMV.add(vertex.getGraph());
            }

            double currentEntropy = 0.0;
            double tmpEntropy = 0.0;
            HashMap<String, List<MergedEdge>> inEdgeTypeHash = new HashMap<>();
            HashMap<String, List<MergedEdge>> outEdgeTypeHash = new HashMap<>();

            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
                String roleName = mergedEdge.getRoleName();
                if (!inEdgeTypeHash.containsKey(roleName)) {
                    inEdgeTypeHash.put(roleName, new ArrayList<>());
                }
                inEdgeTypeHash.get(roleName).add(mergedEdge);
            }

            for (String inType : inEdgeTypeHash.keySet()) {
                if (opt && !calcValue && mergedVertex.getVertexSet().size() <= 1) tmpEntropy = 1;
                else
                    tmpEntropy = calculateEdgeEntropyForVertex(graphListInMV, inEdgeTypeHash.get(inType), inType, EdgeType.IN);
                if (detailed && tmpEntropy != 0) {
                    int pos = 0;
                    if (inType.substring(0, 4).equals("name")) pos += 0;
                    else if (inType.substring(0, 4).equals("attr")) pos += 2;
                    else if (inType.substring(0, 3).equals("rel")) pos += 4;
                    else System.out.println("Error1: " + inType + (inType.substring(0, 4) == "name"));

                    if (inType.substring(inType.length() - 6, inType.length()).equals("source")) pos += 0;
                    else if (inType.substring(inType.length() - 6, inType.length()).equals("target")) pos += 1;
                    else System.out.println("Error2:" + inType.substring(inType.length() - 6, inType.length()));

                    count[pos]++;
                    partEntropy[pos] += tmpEntropy;
                }
                currentEntropy += tmpEntropy;
            }

            for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex)) {
                String roleName = mergedEdge.getRoleName();
                if (outEdgeTypeHash.get(roleName) == null) outEdgeTypeHash.put(roleName, new ArrayList<>());
                outEdgeTypeHash.get(roleName).add(mergedEdge);
            }

            for (String outType : outEdgeTypeHash.keySet()) {
                if (opt && !calcValue && mergedVertex.getVertexSet().size() <= 1) tmpEntropy = 1;
                else
                    tmpEntropy = calculateEdgeEntropyForVertex(graphListInMV, outEdgeTypeHash.get(outType), outType, EdgeType.OUT);
                if (detailed && tmpEntropy != 0) {
                    int pos = 0;
                    if (outType.substring(0, 4).equals("name")) pos += 0;
                    else if (outType.substring(0, 4).equals("attr")) pos += 2;
                    else if (outType.substring(0, 3).equals("rel")) pos += 4;
                    else System.out.println("Error3: " + outType + (outType.substring(0, 4) == "name"));

                    if (outType.substring(outType.length() - 6, outType.length()).equals("source")) pos += 0;
                    else if (outType.substring(outType.length() - 6, outType.length()).equals("target")) pos += 1;
                    else System.out.println("Error4" + outType);

                    count[pos]++;
                    partEntropy[pos] += tmpEntropy;
                }
                currentEntropy += tmpEntropy;
            }
//            // 设置二部图相关参数
//            if (mergedGraphInfo.isBiGraph()) {
//                Bigraph biGraph = mergedGraphInfo.getBiGraph();
//                List<Vertex> vertexList = new ArrayList<>(mergedVertex.getVertexSet());
//                biGraph.addVertex(vertexList.get(0));
//                biGraph.addVertex(vertexList.get(1));
//                biGraph.setEdgeWeight(biGraph.addEdge(vertexList.get(0), vertexList.get(1)), currentEntropy);
//            }

            if (currentEntropy > threshold) {
//                unusuals++;
                if (mergedVertex.getType() == "Entity") {
                    int intersection = 0;
                    for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
                        if (mergedEdge.getEdgeSet().size() == 2) {
                            intersection++;
                        }
                    }
                    if (intersection <= 1) {
                        unusuals++;
                        unusualMergedVertexSet.add(mergedVertex);
                        System.out.println("Unusual Vertex " + mergedVertex.getVertexSet().iterator().next().getValue() + ": " + currentEntropy);
                    }
                }
            }
            // Question
            mergedVertexEntropy.put(mergedVertex, currentEntropy);
            edgeEntropy += currentEntropy;
            finalEntropy += currentEntropy * edgeNum;

        }
        mergedGraphInfo.setMergedVertexToEntropy(mergedVertexEntropy);
        if (detailed) {
            for (int i = 0; i < 6; i++)
                System.out.println("Entropy Part " + i + ": " + count[i] + ", " + partEntropy[i]);
        }
        System.out.println("Unusual Count " + unusuals);
        System.out.println("edge entropy " + edgeEntropy);
        System.out.println("final entropy " + finalEntropy);
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
                                                 List<Graph> graphInThisMV,
                                                 String type,
                                                 EdgeType edgeInOrOut) {

        /**
         * 每个被融合图引用的融合边集合
         */
        Map<Graph, List<MergedEdge>> graphToReferencedMergedEdgeSetMap = new HashMap<>();

        Set<MergedEdge> targetMergedEdgeSet = new HashSet<>();

        if (edgeInOrOut.equals(EdgeType.IN)) {
            targetMergedEdgeSet = mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex);

        } else if (edgeInOrOut.equals(EdgeType.OUT)) {
            targetMergedEdgeSet = mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex);
        }

        // 对于目标集合中的每个融合边，
        for (MergedEdge me : targetMergedEdgeSet) {
            // 找到me的类型与type是否一致
            if (me.getRoleName() != type) continue;
            // 找到其包含的被融合边集合
            Set<Edge> edgeSet = me.getEdgeSet();
            // 找到这些被融合边来自的被融合图的集合
            List<Graph> referencedGraphSet = new ArrayList<>();
            for (Edge edge : edgeSet) {
                referencedGraphSet.add(edge.getGraph());
            }
            // 对于找到的每个被融合图
            for (Graph graph : referencedGraphSet) {
                // 将当前的融合边添加到它引用的融合边集合中
                if (!graphToReferencedMergedEdgeSetMap.containsKey(graph)) {
                    graphToReferencedMergedEdgeSetMap.put(graph, new ArrayList<>());
                }
                graphToReferencedMergedEdgeSetMap.get(graph).add(me);
            }
        }

        // 逆置这个映射，对于每个“融合边的组合”，计算引用了这个组合的被融合图的集合
        Map<List<MergedEdge>, List<Graph>> mergedEdgeSetToReferenceGraphSetMap = new HashMap<>();

        List<MergedEdge> emptyEdgeSet = new ArrayList<>();

        for (Graph graph : graphInThisMV) {
            List<MergedEdge> mergedEdges;
            mergedEdges = graphToReferencedMergedEdgeSetMap.getOrDefault(graph, emptyEdgeSet);
            if (!mergedEdgeSetToReferenceGraphSetMap.containsKey(mergedEdges)) {
                mergedEdgeSetToReferenceGraphSetMap.put(mergedEdges, new ArrayList<>());
            }
            mergedEdgeSetToReferenceGraphSetMap.get(mergedEdges).add(graph);

        }

//        int graphNum = graphInThisMV.size(); //此时缺省值相当于非相似的值
        int graphNum = graphToReferencedMergedEdgeSetMap.size(); //此时若缺省则不计入运算

        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        Set<Map.Entry<List<MergedEdge>, List<Graph>>> allEdgeComb = mergedEdgeSetToReferenceGraphSetMap.entrySet();
        for (Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationX : allEdgeComb) {
            List<Graph> usersX = edgeCombinationX.getValue();
            double pX = ((double) usersX.size()) / graphNum;

            double rstSumPySxy = 0.0;

            for (Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationY : allEdgeComb) {
                List<Graph> usersY = edgeCombinationY.getValue();
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

    public static int ld(String s, String t) {
        int d[][];
        int sLen = s.length();
        int tLen = t.length();
        int si;
        int ti;
        char ch1;
        char ch2;
        int cost;
        if (sLen == 0) {
            return tLen;
        }
        if (tLen == 0) {
            return sLen;
        }
        d = new int[sLen + 1][tLen + 1];
        for (si = 0; si <= sLen; si++) {
            d[si][0] = si;
        }
        for (ti = 0; ti <= tLen; ti++) {
            d[0][ti] = ti;
        }
        for (si = 1; si <= sLen; si++) {
            ch1 = s.charAt(si - 1);
            for (ti = 1; ti <= tLen; ti++) {
                ch2 = t.charAt(ti - 1);
                if (ch1 == ch2) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                d[si][ti] = Math.min(Math.min(d[si - 1][ti] + 1, d[si][ti - 1] + 1), d[si - 1][ti - 1] + cost);
            }
        }
        return d[sLen][tLen];
    }

    private double getSimilarity(String src, String tar) {
        int ld = ld(src, tar);
        return 1 - (double) ld / Math.max(src.length(), tar.length());
    }

    public HashSet<MergedVertex> getUnusualMergedVertexSet() {
        return this.unusualMergedVertexSet;
    }

    private double calculateEdgeEntropyForVertex(List<Graph> graphInThisMV,
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
        //int graphNum = graphInThisMV.size(); //此时缺省值相当于非相似的值
        int graphNum = graphToReferencedMergedEdgeSetMap.size(); //此时若缺省则不计入运算
        // For test
        // int graphNum = 2;
        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        Set<Map.Entry<List<MergedEdge>, List<Graph>>> allEdgeComb = mergedEdgeSetToReferenceGraphSetMap.entrySet();
        for (Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationX : allEdgeComb) {
            List<Graph> usersX = edgeCombinationX.getValue();
            double pX = ((double) usersX.size()) / graphNum;

            double rstSumPySxy = 0.0;

            for (Map.Entry<List<MergedEdge>, List<Graph>> edgeCombinationY : allEdgeComb) {
                List<Graph> usersY = edgeCombinationY.getValue();
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

                // 如果是两个值节点，计算值的相似度（编辑距离）
                else if (edgeCombinationX.getKey().size() == 1 && edgeCombinationY.getKey().size() == 1 && edgeInOrOut.equals(EdgeType.OUT)) {//OUT指向多个值节点
                    MergedVertex mv1 = edgeCombinationX.getKey().iterator().next().getTarget();
                    MergedVertex mv2 = edgeCombinationY.getKey().iterator().next().getTarget();

                    if (mv1.getType() == "Value" && mv2.getType() == "Value") {
                        String s1 = mv1.getVertexSet().iterator().next().getValue();
                        String s2 = mv2.getVertexSet().iterator().next().getValue();
                        similarity = getSimilarity(s1, s2);

                        //System.out.println("Similarity: " + s1 + "-" + s2 + "-" + similarity);
                    } else {
                        // 两个无值的节点，所以相似度只能是0
                        similarity = 0;
                    }

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
