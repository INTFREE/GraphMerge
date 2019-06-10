package knowledgeGraph.baseModel;

public class Edge {
    private Integer id;
    private Vertex source;
    private Vertex target;
    private String roleName;
    private Graph graph;

    public Edge(Integer id, Vertex source, Vertex target, String roleName) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.roleName = roleName;
        this.graph = null;
    }

    @Override
    public String toString() {
        return "source id " + this.source.getId().toString() + " target id " + this.target.getId().toString() + " roleName " + this.roleName;
    }

    public Graph getGraph() {
        return graph;
    }

    public Integer getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setSource(Vertex source) {
        this.source = source;
    }

    public void setTarget(Vertex target) {
        this.target = target;
    }
}
