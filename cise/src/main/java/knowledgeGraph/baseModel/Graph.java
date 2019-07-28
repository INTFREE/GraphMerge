package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.HashSet;

public class Graph extends DefaultDirectedGraph<Vertex, Edge> {
    private String userName;
    private HashMap<Vertex, HashSet<Vertex>> relationToVertex;

    public Graph(String userName) {
        super(Edge.class);
        this.userName = userName;
        this.relationToVertex = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public HashMap<Vertex, HashSet<Vertex>> getRelationToVertex() {
        return relationToVertex;
    }

    public void setRelationToVertex(HashMap<Vertex, HashSet<Vertex>> relationToVertex) {
        this.relationToVertex = relationToVertex;
    }

}