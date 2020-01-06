package knowledgeGraph.baseModel;

import knowledgeGraph.mergeModel.MergedVertex;

import java.util.HashSet;
import java.util.Set;

public class Vertex {
    private Integer id;
    private String type;   // type分为entity, value, relation
    private String value;  // value节点的value属性即为值，entity节点为核心属性值节点的值
    private Integer modelId;
    private Graph graph;
    private MergedVertex mergedVertex;
    private HashSet<Vertex> relatedVertex;

    public Vertex(Integer id, String type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.modelId = -1;
        this.graph = null;
        this.mergedVertex = null;
        this.relatedVertex = new HashSet<>();
    }

    public Vertex(Integer id, String type) {
        this.id = id;
        this.type = type;
        this.value = "";
        this.modelId = -1;
        this.graph = null;
        this.relatedVertex = new HashSet<>();
    }

    @Override
    public String toString() {
        return "graph" + this.graph.getUserName() + " " + this.id.toString() + " " + this.type + " " + this.value + " " + this.modelId.toString();
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public Integer getModelId() {
        return modelId;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setMergedVertex(MergedVertex mergedVertex) {
        this.mergedVertex = mergedVertex;
    }

    public MergedVertex getMergedVertex() {
        return mergedVertex;
    }

    public void addRelatedVertex(Vertex vertex) {
        this.relatedVertex.add(vertex);
    }

    public HashSet<Vertex> getRelatedVertex() {
        return this.relatedVertex;
    }
}

