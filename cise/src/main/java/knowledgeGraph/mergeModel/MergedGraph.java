package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Vertex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.HashSet;
import java.util.Set;

public class MergedGraph extends DefaultDirectedGraph<MergedVertex, MergedEdge> {
    private MergedVertex mostEntropyMergedVertex;

    public MergedGraph() {
        super(MergedEdge.class);
    }

    public void setMostEntropyMergedVertex(MergedVertex mostEntropyMergedVertex) {
        this.mostEntropyMergedVertex = mostEntropyMergedVertex;
    }

    @Override
    public String toString() {
        System.out.println("MergedGraph Info");
        System.out.println("merged vertex " + this.vertexSet().size());

        for (MergedVertex mergedVertex : this.vertexSet()) {
            System.out.println(mergedVertex.getVertexSet().size());
        }

        System.out.println("merged edge " + this.edgeSet().size());
        for (MergedEdge mergedEdge : this.edgeSet()) {
            System.out.println(mergedEdge.getEdgeSet().size());
        }
        return "";
    }

    public MergedVertex getMostEntropyMergedVertex() {
        return mostEntropyMergedVertex;
    }

    public void mutateMergedGraph(MergedVertex targetMergedVertex, Vertex sourceVertex) {
        System.out.println(targetMergedVertex.toString());
        MergedVertex source = sourceVertex.getMergedVertex();
        Set<MergedEdge> changedMergedEdge = new HashSet<>();
        if (source.getType().equalsIgnoreCase("entity")) {
            changedMergedEdge = this.incomingEdgesOf(source);
        } else {
            changedMergedEdge = this.outgoingEdgesOf(source);
        }
        for (MergedEdge mergedEdge : changedMergedEdge) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                if (edge.getSource() == sourceVertex) {
                    MergedEdge newMergedEdge = new MergedEdge(targetMergedVertex, mergedEdge.getTarget(), edge.getRoleName());
                    newMergedEdge.addEdge(edge);
                    this.addEdge(newMergedEdge.getSource(), newMergedEdge.getTarget(), newMergedEdge);
                    mergedEdge.getEdgeSet().remove(edge);
                } else if (edge.getTarget() == sourceVertex) {
                    MergedEdge newMergedEdge = new MergedEdge(mergedEdge.getSource(), targetMergedVertex, edge.getRoleName());
                    newMergedEdge.addEdge(edge);
                    this.addEdge(newMergedEdge.getSource(), newMergedEdge.getTarget(), newMergedEdge);
                    mergedEdge.getEdgeSet().remove(edge);
                }
            }
            if (mergedEdge.getEdgeSet().isEmpty()) {
                this.removeEdge(mergedEdge);
            }
        }
        source.removeVertex(sourceVertex);
        targetMergedVertex.addVertex(sourceVertex);
    }

}
