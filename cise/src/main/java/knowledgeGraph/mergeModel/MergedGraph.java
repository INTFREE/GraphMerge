package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Vertex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.HashMap;
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
        System.out.println("merged edge " + this.edgeSet().size());
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

    public void saveToFile() throws IOException {
        File file = new File("MergedGraph");
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(this.vertexSet().size() + "\n");
        writer.write(this.edgeSet().size() + "\n");
        for (MergedVertex mergedVertex : this.vertexSet()) {
            writer.write(serializeMergedVertex(mergedVertex) + "\n");
        }
        for (MergedEdge mergedEdge : this.edgeSet()) {
            writer.write(serializeMergedEdge(mergedEdge) + "\n");
        }
        writer.close();
        os.close();
    }

    public String serializeMergedVertex(MergedVertex mergedVertex) {
        String res = "";
        res += mergedVertex.getId().toString() + "\t" + mergedVertex.getType() + "\n";
        StringBuilder builder = new StringBuilder();
        for (Vertex vertex : mergedVertex.getVertexSet()) {
            builder.append(vertex.getGraph().getUserName() + "\t");
            builder.append(vertex.getId().toString() + "|");
        }
        res += builder.toString();
        return res;
    }

    public String serializeMergedEdge(MergedEdge mergedEdge) {
        String res = "";
        res += mergedEdge.getSource().getId() + "\t" + mergedEdge.getTarget().getId() + "\t" + mergedEdge.getRoleName() + "\n";
        StringBuilder builder = new StringBuilder();
        for (Edge edge : mergedEdge.getEdgeSet()) {
            builder.append(edge.getGraph().getUserName() + "\t");
            builder.append(edge.getId() + "|");
        }
        res += builder.toString();
        return res;
    }

}
