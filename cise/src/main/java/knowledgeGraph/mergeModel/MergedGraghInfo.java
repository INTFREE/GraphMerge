package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.GraphsInfo;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.util.UtilFunction;
import org.neo4j.register.Register;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.omg.Messaging.SyncScopeHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergedGraghInfo {
    private GraphsInfo graphsInfo;
    private MergedGraph mergedGraph;
    public boolean isChanged = true;

    private Map<String, Set<MergedVertex>> typeToVertexSetMap;
    /**
     * 被融合图中的节点到融合图中节点的映射，即：被融合图中的某个节点属于融合图中的哪个节点
     */
    private Map<Vertex, MergedVertex> vertexToMergedVertexMap;

    /**
     * 被融合图中的边到融合图中边的映射，即：被融合图中的某个边属于融合图中的哪个边
     */
    private Map<Edge, MergedEdge> edgeToMergedEdgeMap;

    /**
     * 融合图的熵
     */
    double entropy = -1;

    /**
     * 构造函数，接收一组待融合图信息作为参数
     *
     * @param graphsInfo 待融合图信息
     */
    public MergedGraghInfo(GraphsInfo graphsInfo) {
        this.graphsInfo = graphsInfo;
        this.mergedGraph = new MergedGraph();
        typeToVertexSetMap = new HashMap<>();
        vertexToMergedVertexMap = new HashMap<>();
        edgeToMergedEdgeMap = new HashMap<>();
    }

    public double getEntropy() {
        return entropy;
    }

    public void setMergedGraph(MergedGraph mergedGraph) {
        this.mergedGraph = mergedGraph;
    }

    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public Set<MergedVertex> getMergedVertexByType(String type) {
        if (typeToVertexSetMap.containsKey(type)) {
            return typeToVertexSetMap.get(type);
        }
        return new HashSet<>();
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public void setTypeToVertexSetMap(Map<String, Set<MergedVertex>> typeToVertexSetMap) {
        this.typeToVertexSetMap = typeToVertexSetMap;
    }

    public GraphsInfo getGraphsInfo() {
        return graphsInfo;
    }

    public Set<String> getAllTypes() {
        return this.typeToVertexSetMap.keySet();
    }


    public void generateInitialMergeGraph() {
        this.mergedGraph = new MergedGraph();
        Set<MergedVertex> mergedVertexSet = new HashSet<>();
        // 随机生成初始融合图
        for (String type : this.graphsInfo.getVertexTypeSet()) {
            if (type.equalsIgnoreCase("Value")) {
                continue;
            }
            System.out.println(type);
            Set<MergedVertex> tmpMergedVertexSet = new HashSet<>();
            Set<Vertex> vertexSet = this.graphsInfo.getTypeToVertexSetMap().get(type);
            for (Vertex vertex : vertexSet) {
                MergedVertex mergedVertex = new MergedVertex(type);
                HashSet<MergedVertex> tmp = new HashSet<>();
                tmp.add(mergedVertex);
                // 来自同一图的点不能融合在同一节点
                for (MergedVertex mergedVertex1 : tmpMergedVertexSet) {
                    boolean flag = true;
                    for (Vertex vertex1 : mergedVertex1.getVertexSet()) {
                        if (vertex1.getGraph().equals(vertex.getGraph())) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        tmp.add(mergedVertex1);
                    }
                }
                // 随机挑选一个节点融合
                MergedVertex randomChose = new UtilFunction.CollectionUtil<MergedVertex>()
                        .pickRandom(tmp);
                randomChose.getVertexSet().add(vertex);
                vertex.setMergedVertex(randomChose);
                if (!tmpMergedVertexSet.contains(randomChose)) {
                    tmpMergedVertexSet.add(randomChose);
                }
            }
            mergedVertexSet.addAll(tmpMergedVertexSet);
        }
        // 对于每个value节点，单独生成一个融合节点
        Set<Vertex> vertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Value");
        for (Vertex vertex : vertexSet) {
            MergedVertex mergedVertex = new MergedVertex("Value");
            mergedVertex.getVertexSet().add(vertex);
            vertex.setMergedVertex(mergedVertex);
            mergedVertexSet.add(mergedVertex);
        }

        Set<Graph> graphSet = this.graphsInfo.getGraphSet();
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        for (Graph graph : graphSet) {
            for (Edge edge : graph.edgeSet()) {
                boolean flag = false;
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    MergedVertex source = mergedEdge.getSource();
                    MergedVertex target = mergedEdge.getTarget();
                    if (source.containsVertex(edge.getSource()) && target.containsVertex(edge.getTarget())) {
                        mergedEdge.addEdge(edge);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    MergedVertex source = null;
                    MergedVertex target = null;
                    for (MergedVertex mergedVertex : mergedVertexSet) {
                        if (mergedVertex.containsVertex(edge.getSource())) {
                            source = mergedVertex;
                        }
                        if (mergedVertex.containsVertex(edge.getTarget())) {
                            target = mergedVertex;
                        }
                    }
                    if (source == null || target == null) {
                        System.out.println("edge has no source or target");
                    } else {
                        MergedEdge mergedEdge = new MergedEdge(source, target, edge.getRoleName());
                        mergedEdge.addEdge(edge);
                        mergedEdgeSet.add(mergedEdge);
                    }
                }
            }
        }
        for (MergedVertex mergedVertex : mergedVertexSet) {
            this.mergedGraph.addVertex(mergedVertex);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            this.mergedGraph.addEdge(mergedEdge.getSource(), mergedEdge.getTarget(), mergedEdge);
        }
        for (MergedVertex mergedVertex : mergedVertexSet) {
            mergedVertex.setMergedGraph(this.mergedGraph);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            mergedEdge.setMergedGraph(this.mergedGraph);
        }
    }

}
