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

    public void print() {
        System.out.println(">>>>>> Graph Info");
        System.out.println(this.getUserName());
        System.out.println("Vertex size " + this.vertexSet().size());
        System.out.println("Edge size " + this.edgeSet().size());
        HashMap<String, HashSet<Vertex>> typeToVertex = new HashMap<>();
        for (Vertex vertex : this.vertexSet()) {
            if (!typeToVertex.containsKey(vertex.getType())) {
                typeToVertex.put(vertex.getType(), new HashSet<>());
            }
            typeToVertex.get(vertex.getType()).add(vertex);
        }
        for (String type : typeToVertex.keySet()) {
            System.out.println(type + " size : " + typeToVertex.get(type).size());
        }
    }

}