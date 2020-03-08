package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;

import java.util.*;


public class BasicEntropyCalculator implements EntropyCalculator {

    boolean opt = false;
    boolean calcValue = true;
    boolean detailed = false;
    HashSet<MergedVertex> unusualMergedVertexSet = new HashSet<>();
    MergedGraghInfo mergedGraghInfo;


    public BasicEntropyCalculator() {
    }

    public BasicEntropyCalculator(boolean opt, boolean calcValue, boolean detailed) {
        this.opt = opt;
        this.calcValue = calcValue;
        this.detailed = detailed;
    }

    @Override
    public double calculateEntropy(MergedGraghInfo mergedGraphInfo) {
        mergedGraghInfo = mergedGraphInfo;
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
            /*
             FIXME: 该if存在几个问题
              1、只用于处理2图？判断的是两图是否融合
              2、只处理entity的熵值？按照文章的话其实entity/relation/value都要算的
              Q: 感觉现在的计算方式relation应该不会有出边熵，value节点可能有边熵，但是上下文好像没意义？感觉只需要计算entity
              3、当前如果2图中节点未融合，则节点的熵值为0.
             目标: 为了保证节点能够尽可能的融合，可以有两种处理方式
                1. 未融合的节点熵值为0，但是需要计算值节点的入熵
                2. 未融合计算熵值（按照出入边和其他出入边均不相同进行计算），不再计算值节点的熵值
              我看了下当前的算法是按照目标1处理的，在两图里面可能问题不大，只需要把未融合的节点做下一轮融合就行了
             TODO:
                1. 删除 !mergedVertex.getType().equalsIgnoreCase("entity")
                Done
                2. mergedVertex.getVertexSet().size() != 2 改为 mergedVertex.getVertexSet().size() < 2
                Done
            */
            if (mergedVertex.getVertexSet().size() < 2) {
                continue;
            }
            // For test
//            if (mergedVertex.getId() != 1) {
//                continue;
//            }
            if (!mergedVertex.getType().equalsIgnoreCase("entity")) {
                continue;
            }
            /*
             FIXME:
              1. mergedVertex.getVertexSet().size()是不是和FIXME 1的问题重复了
              2. calcValue只是用于判断是否需要计算值节点的入熵的吧？这里是不是不需要判断
             TODO:
              建议删除此if
              Done
            */

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
                if (!outEdgeTypeHash.containsKey(roleName)) {
                    outEdgeTypeHash.put(roleName, new ArrayList<>());
                }
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
                System.out.println("intype " + outType + " " + tmpEntropy);
            }

//            if (currentEntropy > threshold) {
////                unusuals++;
//                if (mergedVertex.getType() == "Entity") {
//                    int intersection = 0;
//                    for (MergedEdge mergedEdge : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex)) {
//                        if (mergedEdge.getEdgeSet().size() == 2) {
//                            intersection++;
//                        }
//                    }
//                    if (intersection <= 1) {
//                        unusuals++;
//                        unusualMergedVertexSet.add(mergedVertex);
//                        System.out.println("Unusual Vertex " + mergedVertex.getVertexSet().iterator().next().getValue() + ": " + currentEntropy);
//                    }
//                }
//            }
            // Question
            mergedVertexEntropy.put(mergedVertex, currentEntropy);
            edgeEntropy += currentEntropy;
            finalEntropy += currentEntropy * edgeNum;
            System.out.println("entropy " + currentEntropy);

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
        /*
         TODO:
            改为 int graphNum = graphInThisMV.size(); 这样缺省的情况存在的熵值，缺省值会判定为一个和之前都不相似的值.
            2部图, 如果缺省的话理想情况下应该是1不是0.5
            我看了下代码好像现在可能会算出来0.5
         Done.
        */
        int graphNum = graphInThisMV.size(); //此时缺省值相当于非相似的值
//        int graphNum = graphToReferencedMergedEdgeSetMap.size(); //此时若缺省则不计入运算
        // For test
        // int graphNum = 2;
        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        Set<Map.Entry<List<MergedEdge>, List<Graph>>> allEdgeComb = mergedEdgeSetToReferenceGraphSetMap.entrySet();
        HashMap<Map.Entry<List<MergedEdge>, List<Graph>>, VertexContext> entryVertexContextHashMap = new HashMap<>();
        Integer id = 1;
        for (Map.Entry<List<MergedEdge>, List<Graph>> entry : allEdgeComb) {
            entryVertexContextHashMap.put(entry, genertateContext(entry.getKey(), id));
            id += 1;
        }
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
                    similarity = entryVertexContextHashMap.get(edgeCombinationX).getSimilarity(entryVertexContextHashMap.get(edgeCombinationY));
                }
                rstSumPySxy += pY * similarity;
            }
            // 将log换位2为底
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }

        return Math.abs(entropy);
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
