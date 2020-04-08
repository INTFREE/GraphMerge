package knowledgeGraph;

import org.apache.commons.lang3.tuple.*;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.io.GraphFileImporter;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;

import java.io.IOException;
import java.util.ArrayList;

public class tool {
    public static void main(String argv[]) throws IOException {
        GraphFileImporter importer = new GraphFileImporter();

        Pair<MergedGraph, ArrayList<Graph>> graphInfo = importer.readGraphFile(2);
        for (Graph graph : graphInfo.getValue()) {
            graph.print();
        }

        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphInfo.getKey());
        System.out.println("finish mergeGraph read");
    }
}
