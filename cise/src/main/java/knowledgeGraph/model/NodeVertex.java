package knowledgeGraph.model;

public class NodeVertex {
    public Integer id;
    public String type;
    public String value;
    public Integer modelId;

    public NodeVertex(Integer id, String type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.modelId = -1;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    @Override
    public String toString() {
        return "id " + this.id.toString() + " type " + type + " value " + value + " modelId " + this.modelId.toString();
    }
}
