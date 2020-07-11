package knowledgeGraph.baseModel;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

// TODO:修改二部图结构
public class Bigraph extends SimpleWeightedGraph<Vertex, DefaultWeightedEdge> {
    public Bigraph() {
        super(DefaultWeightedEdge.class);
    }
}
