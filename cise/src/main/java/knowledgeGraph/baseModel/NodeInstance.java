package knowledgeGraph.baseModel;

public class NodeInstance {
    public Integer id;
    public String value;
    public String type;

    public NodeInstance(Integer id, String value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " " + id.toString() + " value " + value;
    }
}
