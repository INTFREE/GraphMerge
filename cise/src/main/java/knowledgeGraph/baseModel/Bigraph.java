package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Bigraph extends SimpleWeightedGraph<Vertex, DefaultWeightedEdge> {
    public Bigraph() {
        super(DefaultWeightedEdge.class);
    }
}
