package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.GraphsInfo;
import knowledgeGraph.baseModel.Vertex;
import org.neo4j.register.Register;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergedGraghInfo {
    private GraphsInfo graphsInfo;
    private MergedGraph mergedGraph;

    public boolean isChanged = true;

    private Map<Integer, Set<MergedVertex>> typeToVertexSetMap;
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

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public void setTypeToVertexSetMap(Map<Integer, Set<MergedVertex>> typeToVertexSetMap) {
        this.typeToVertexSetMap = typeToVertexSetMap;
    }

    public GraphsInfo getGraphsInfo() {
        return graphsInfo;
    }

    public void generateInitialMergeGraph() {
        this.mergedGraph = new MergedGraph();
        Set<MergedVertex> mergedVertexSet = new HashSet<>();
        for (Integer type : this.graphsInfo.getVertexTypeSet()) {
            Set<Vertex> vertexSet = this.graphsInfo.getTypeToVertexSetMap().get(type);
            Vertex tempVertex = vertexSet.iterator().next();
            MergedVertex mergedVertex = new MergedVertex(vertexSet, tempVertex.getType(), type);
            mergedVertexSet.add(mergedVertex);
        }
        Set<Graph> graphSet = this.graphsInfo.getGraphSet();
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        for (Graph graph : graphSet) {
            for (Edge edge : graph.getEdgeSet()) {
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
        this.mergedGraph.setMergedVertexSet(mergedVertexSet);
        this.mergedGraph.setMergedEdgeSet(mergedEdgeSet);
    }
}
