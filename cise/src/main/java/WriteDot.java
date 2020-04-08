import org.apache.commons.lang3.tuple.*;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.io.GraphFileImporter;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class WriteDot {
    static HashMap<String, String> typeToShape = new HashMap<>();

    public static void main(String argv[]) throws IOException {
        typeToShape.put("Entity", "record");
        typeToShape.put("Relation", "none");
        typeToShape.put("Value", "circle");
        GraphFileImporter importer = new GraphFileImporter();
        Pair<MergedGraph, ArrayList<Graph>> graphInfo = importer.readGraphFile(2);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphInfo.getKey());
        File file = new File("MergedGraph.dot");
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write("digraph MergedGraph { \n");
        for (MergedVertex mergedVertex : mergedGraghInfo.getMergedGraph().vertexSet()) {
            writer.write(constructVertex(mergedVertex) + "\n");
        }
        for (MergedEdge mergedEdge : mergedGraghInfo.getMergedGraph().edgeSet()) {
            writer.write(constructEdge(mergedEdge) + "\n");
        }
        writer.write("}");
        writer.close();
        os.close();
    }

    private static String constructVertex(MergedVertex mergedVertex) {
        String content = "";
        content += getVertexKey(mergedVertex) + "[shape=\"" + typeToShape.get(mergedVertex.getType()) + "\", label=\"";
        if (mergedVertex.getType().equalsIgnoreCase("entity")) {
            ArrayList<Vertex> vertices = new ArrayList<>(mergedVertex.getVertexSet());
            content += "<f0> " + vertices.get(0).getValue().replace("\"", "") + " " + vertices.get(0).getGraph().getUserName();
            for (int i = 1; i < vertices.size(); i++) {
                content += "| <f" + i + "> " + vertices.get(i).getValue().replace("\"", "") + " " + vertices.get(i).getGraph().getUserName();
            }
        } else {
            Vertex vertex = mergedVertex.getVertexSet().iterator().next();
            content += vertex.getValue().replace("\"", "");
        }
        content += "\"];";
        return content;
    }

    private static String constructEdge(MergedEdge mergedEdge) {
        String content = "";
        if (mergedEdge.getEdgeSet().size() == 2) {
            content += getVertexKey(mergedEdge.getSource()) + "->" + getVertexKey(mergedEdge.getTarget());
            content += "[label=\"" + mergedEdge.getEdgeSet().size() + "\"];";
        }

        return content;
    }

    public static String getVertexKey(MergedVertex mergedVertex) {
        return mergedVertex.getType() + mergedVertex.getId().toString();
    }
}
