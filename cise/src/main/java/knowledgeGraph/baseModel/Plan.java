package knowledgeGraph.baseModel;

import javafx.util.Pair;
import knowledgeGraph.mergeModel.MergedVertex;

public class Plan {
    private Vertex vertex;
    private MergedVertex source;
    private MergedVertex target;

    public Plan(Vertex vertex, MergedVertex source, MergedVertex target) {
        this.vertex = vertex;
        this.source = source;
        this.target = target;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public MergedVertex getSource() {
        return source;
    }

    public MergedVertex getTarget() {
        return target;
    }
}
