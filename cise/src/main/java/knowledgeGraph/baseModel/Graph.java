package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class Graph extends DefaultDirectedGraph<Vertex, Edge> {
    private String userName;
    private HashMap<Vertex, HashSet<Vertex>> relationToVertex;
    private HashMap<String, HashSet<Vertex>> keyWordToVertex;

    public Graph(String userName) {
        super(Edge.class);
        this.userName = userName;
        this.relationToVertex = new HashMap<>();
        this.keyWordToVertex = new HashMap<>();
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

    public void addKeyWord(String keyWord, Vertex vertex) {
        if (!keyWordToVertex.containsKey(keyWord)) {
            keyWordToVertex.put(keyWord, new HashSet<>());
        }
        keyWordToVertex.get(keyWord).add(vertex);
    }

    public HashMap<String, HashSet<Vertex>> getKeyWordToVertex() {
        return keyWordToVertex;
    }

    public void saveToFile() throws IOException {
        File file = new File("Graph_" + userName);
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(this.vertexSet().size() + "\n");
        writer.write(this.edgeSet().size() + "\n");
        for (Vertex vertex : this.vertexSet()) {
            writer.write(serializeVertex(vertex) + "\n");
        }
        for (Edge edge : this.edgeSet()) {
            writer.write(serializeEdge(edge) + "\n");
        }
        writer.close();
        os.close();
    }

    public String serializeVertex(Vertex vertex) {
        String res = "";
        res += vertex.getId().toString() + "\t" + vertex.getType() + "\t" + vertex.getValue();
        return res;
    }

    public String serializeEdge(Edge edge) {
        String res = "";
        res += edge.getId() + "\t" + edge.getSource().getId() + "\t" + edge.getTarget().getId() + "\t" + edge.getRoleName();
        return res;
    }
}