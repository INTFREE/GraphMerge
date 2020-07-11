package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Vertex;
import org.jgrapht.graph.DirectedPseudograph;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class MergedGraph extends DirectedPseudograph<MergedVertex, MergedEdge> {
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

    public void saveToFile(String fileName) throws IOException {
        File file = new File(fileName);
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

    public void saveMergedVertex(MergedVertex mergedVertex) {
        try {
            File file = new File("MergedVertex");
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("MergedVertex info : \n");
            writer.write(serializeMergedVertex(mergedVertex) + "\n");
            writer.write("MergedEdge info : \n");
            for (MergedEdge mergedEdge : incomingEdgesOf(mergedVertex)) {
                writer.write(serializeMergedEdge(mergedEdge) + "\n");
                MergedVertex source = mergedEdge.getSource();
                writer.write("other related mergedVertex \n");
                for (MergedEdge relationEdge : outgoingEdgesOf(source)) {
                    if (mergedEdge.equals(relationEdge)) {
                        continue;
                    }
                    writer.write(serializeMergedVertex(relationEdge.getTarget()));
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save MergedVertex error " + e);
        }
    }
    public void print(){
        System.out.println(">>>>>>MergedGraph Info");
        System.out.println("Vertex size " + this.vertexSet().size());
        System.out.println("Edge size " + this.edgeSet().size());
    }

}
