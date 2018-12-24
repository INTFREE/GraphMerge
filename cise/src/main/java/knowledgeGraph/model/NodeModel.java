package knowledgeGraph.model;

public class NodeModel {
    public Integer id;
    public String value;
    public String tag;

    public NodeModel(Integer id, String value, String tag) {
        this.id = id;
        this.value = value;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "node_id " + this.id.toString() + " value " + this.value + " tag " + this.tag + "\n";
    }
}
