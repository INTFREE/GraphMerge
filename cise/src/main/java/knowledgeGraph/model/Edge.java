package knowledgeGraph.model;

public class Edge {
    public Integer id;
    public RelationVertex source;
    public NodeVertex target;
    public String roleName;

    public Edge(Integer id, RelationVertex source, NodeVertex target, String roleName) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "source id " + this.source.id.toString() + " target id " + this.target.id.toString() + " roleName " + this.roleName;
    }
}
