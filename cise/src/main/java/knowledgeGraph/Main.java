package knowledgeGraph;

import knowledgeGraph.ga.GAProcess;
import knowledgeGraph.io.FileImporter;
import knowledgeGraph.io.GraphImporter;
import knowledgeGraph.io.Importer;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;

import java.util.ArrayList;
import java.util.Set;


public class Main {
    public static void main(String argv[]) {
//        Importer importer = new Importer();
//        ArrayList<String> userNameList = importer.getUserNameList();
//        GraphImporter graphImporter = new GraphImporter();
//        ArrayList<Graph> graphArrayList = new ArrayList<>();
//        for (String userName : userNameList) {
//            Graph graph = graphImporter.readGraph(importer, "大话西游-电影人物关系图谱", userName);
//            graphArrayList.add(graph);
//        }
//        System.out.println(graphArrayList.size());
//        importer.finishImport();
//
//        GAProcess ga = new GAProcess(1, 0.2, 1000, 200, graphArrayList);
//        ga.quiet = true;
//        ga.parallel = false;
//        ga.Run();
        FileImporter importer = new FileImporter();

        Graph graph1 = importer.readGraph(1);
        Graph graph2 = importer.readGraph(2);

    }


}
