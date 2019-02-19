package knowledgeGraph.baseModel;

public class Vertex {
    private Integer id;
    private String type;   // type分为entity, value, relation
    private String value;
    private Integer modelId;
    private Graph graph;

    public Vertex(Integer id, String type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.modelId = -1;
        this.graph = null;
    }

    public Vertex(Integer id, String type) {
        this.id = id;
        this.type = type;
        this.value = "";
        this.modelId = -1;
        this.graph = null;
    }

    @Override
    public String toString() {
        return "graph" + this.graph.getUserName() + this.id.toString() + " " + this.type + " " + this.value + " " + this.modelId.toString();
    }

    public Graph getGraph() {
        return graph;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

