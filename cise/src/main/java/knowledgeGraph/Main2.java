package knowledgeGraph;

import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.GAProcess;
import knowledgeGraph.io.FileImporter;
import knowledgeGraph.io.FileImporter2;

import knowledgeGraph.io.GraphImporter;
import knowledgeGraph.io.Importer;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Main2 {
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

//        FileImporter importer = new FileImporter();
//
//        Graph graph1 = importer.readGraph(1);
//        Graph graph2 = importer.readGraph(2);
//        System.out.println("read finished");
//        HashMap<String, HashSet<Vertex>> mergeVertexToVertexSet = importer.readMatch(1);
//        HashSet<Graph> graphHashSet = new HashSet<>();
//        graphHashSet.add(graph1);
//        graphHashSet.add(graph2);
//        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
//        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);
//        mergedGraghInfo.generateMergeGraphByMatch(mergeVertexToVertexSet);

        FileImporter2 importer = new FileImporter2();

        for (Integer i = 1; i <= 3; i++) {
            HashMap<String, Integer> Entity2Id = new HashMap<>();
            HashMap<String, Integer> Val2Id = new HashMap<>();
            HashMap<String, Integer> Rel2Id = new HashMap<>();
            HashMap<String, Integer> Edge2Id = new HashMap<>();

            Graph graph1 = importer.readGraph(1, i, Entity2Id, Val2Id, Rel2Id, Edge2Id);
//            System.out.println(Entity2Id.size());
//            System.out.println(Val2Id.size());
//            System.out.println(Rel2Id.size());
//            System.out.println(Edge2Id.size());

            Graph graph2 = importer.readGraph(2, i, Entity2Id, Val2Id, Rel2Id, Edge2Id);
//            System.out.println(Entity2Id.size());
//            System.out.println(Val2Id.size());
//            System.out.println(Rel2Id.size());
//            System.out.println(Edge2Id.size());
            System.out.println("read finished");

            ArrayList<Graph> graphArrayList = new ArrayList<>();
            graphArrayList.add(graph1);
            graphArrayList.add(graph2);

            HashSet<Graph> graphHashSet = new HashSet<>();
            graphHashSet.add(graph1);
            graphHashSet.add(graph2);
            long startTime = System.currentTimeMillis();
            GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
            MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);
            mergedGraghInfo.generateMergeGraphByMatch2();
            long endTime = System.currentTimeMillis();
            System.out.println("time");
            System.out.println(endTime - startTime);
            BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator();
            double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
            System.out.println(etr);
        }
    }


}
