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
    Set<Integer> vertexTypeSet;

    /**
     * 存放所有待融合图的边类型
     */
    Set<String> edgeTypeSet;

    /**
     * 节点类型到对应节点集合的映射
     */
    private Map<Integer, Set<Vertex>> typeToVertexSetMap;

    /**
     * 构造函数
     */
    public GraphsInfo(Collection<Graph> graphs) {

        graphSet = new HashSet<>(graphs);

        vertexTypeSet = new HashSet<>();
        edgeTypeSet = new HashSet<>();
        typeToVertexSetMap = new HashMap<>();

        // 更新上面那些集合的信息
        for (Graph g : graphSet) {
            for (Vertex v : g.getVertexSet()) {
                vertexTypeSet.add(v.getId());
                if (!typeToVertexSetMap.containsKey(v.getId())) {
                    typeToVertexSetMap.put(v.getId(), new HashSet<>());
                }
                typeToVertexSetMap.get(v.getId()).add(v);
            }
            for (Edge e : g.getEdgeSet()) {
                edgeTypeSet.add(e.getRoleName());
            }
        }
    }

    public Map<Integer, Set<Vertex>> getTypeToVertexSetMap() {
        return typeToVertexSetMap;
    }

    public Set<Graph> getGraphSet() {
        return graphSet;
    }

    public Set<Integer> getVertexTypeSet() {
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

    public void setTypeToVertexSetMap(Map<Integer, Set<Vertex>> typeToVertexSetMap) {
        this.typeToVertexSetMap = typeToVertexSetMap;
    }

    public void setVertexTypeSet(Set<Integer> vertexTypeSet) {
        this.vertexTypeSet = vertexTypeSet;
    }

    public int getGraphNum() {
        return this.graphSet.size();
    }
}
