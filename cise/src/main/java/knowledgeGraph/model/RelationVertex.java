package knowledgeGraph.model;

public class RelationVertex {
    public Integer id;

    public RelationVertex(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Relation id " + this.id.toString();
    }
}
