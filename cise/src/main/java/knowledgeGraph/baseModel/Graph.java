package knowledgeGraph.baseModel;

import java.util.Set;

public class Graph {
    private Set<Edge> edgeSet;
    private Set<Vertex> vertexSet;
    private String userName;

    public Graph(Set<Edge> edgeSet, Set<Vertex> vertexSet, String userName) {
        this.edgeSet = edgeSet;
        this.vertexSet = vertexSet;
        this.userName = userName;

    }

    @Override
    public String toString() {
        return "graph " + userName + " edge num " + this.edgeSet.size() + " vertex num " + this.vertexSet.size();
    }

    public String getUserName() {
        return userName;
    }

    public Set<Edge> getEdgeSet() {
        return edgeSet;
    }

    public Set<Vertex> getVertexSet() {
        return vertexSet;
    }
}
