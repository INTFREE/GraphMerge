package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultDirectedGraph;

public class Graph extends DefaultDirectedGraph<Vertex, Edge> {
    private String userName;

    public Graph(String userName) {
        super(Edge.class);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}