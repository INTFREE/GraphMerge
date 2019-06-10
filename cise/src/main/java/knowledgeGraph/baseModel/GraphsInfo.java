package knowledgeGraph.baseModel;

import java.util.*;

// 存放一组待融合图信息
public class GraphsInfo {
    /**
     * 存放所有待融合图
     */
    protected Set<Graph> graphSet;

    /**
     * 存放待融合图中所有节点类型
     */
    Set<String> vertexTypeSet;

    /**
     * 存放所有待融合图的边类型
     */
    Set<String> edgeTypeSet;

    /**
     * 节点类型到对应节点集合的映射
     */
    private Map<String, Set<Vertex>> typeToVertexSetMap;

    /**
     * 构造函数
     */
    public GraphsInfo(Collection<Graph> graphs) {

        graphSet = new HashSet<>(graphs);

        vertexTypeSet = new HashSet<>();
        edgeTypeSet = new HashSet<>();
        typeToVertexSetMap = new HashMap<>();

        // 更新上面那些集合的信息，TODO
        for (Graph g : graphSet) {
            for (Vertex v : g.vertexSet()) {
                vertexTypeSet.add(v.getType());
                if (!typeToVertexSetMap.containsKey(v.getType())) {
                    typeToVertexSetMap.put(v.getType(), new HashSet<>());
                }
                typeToVertexSetMap.get(v.getType()).add(v);
            }
            for (Edge e : g.edgeSet()) {
                edgeTypeSet.add(e.getRoleName());
            }
        }
    }

    public Map<String, Set<Vertex>> getTypeToVertexSetMap() {
        return typeToVertexSetMap;
    }

    public Set<Graph> getGraphSet() {
        return graphSet;
    }

    public Set<String> getVertexTypeSet() {
        return vertexTypeSet;
    }

    public Set<String> getEdgeTypeSet() {
        return edgeTypeSet;
    }

    public void setEdgeTypeSet(Set<String> edgeTypeSet) {
        this.edgeTypeSet = edgeTypeSet;
    }

    public void setGraphSet(Set<Graph> graphSet) {
        this.graphSet = graphSet;
    }

    public void setTypeToVertexSetMap(Map<String, Set<Vertex>> typeToVertexSetMap) {
        this.typeToVertexSetMap = typeToVertexSetMap;
    }

    public void setVertexTypeSet(Set<String> vertexTypeSet) {
        this.vertexTypeSet = vertexTypeSet;
    }

    public int getGraphNum() {
        return this.graphSet.size();
    }
}
