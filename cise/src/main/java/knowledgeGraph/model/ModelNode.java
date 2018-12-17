package knowledgeGraph.model;

public class ModelNode {
    public Integer id;
    public String value;
    public String tag;

    public ModelNode(Integer id, String value, String tag) {
        this.id = id;
        this.value = value;
        this.tag = tag;
    }
}
