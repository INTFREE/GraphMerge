package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;
import java.util.HashSet;

public class Bigraph extends SimpleWeightedGraph<Vertex, DefaultWeightedEdge> {
    public Bigraph() {
        super(DefaultWeightedEdge.class);
    }
}
