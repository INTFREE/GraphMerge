package knowledgeGraph;

import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.GAProcess;
import knowledgeGraph.io.FileImporter;
import knowledgeGraph.io.GraphImporter;
import knowledgeGraph.io.Importer;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;

import java.util.HashMap;
import java.util.HashSet;


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
        System.out.println("read finished");
        HashMap<String, HashSet<Vertex>> mergeVertexToVertexSet = importer.readMatch(1);
        HashSet<Graph> graphHashSet = new HashSet<>();
        graphHashSet.add(graph1);
        graphHashSet.add(graph2);

        long startTime = System.currentTimeMillis();
        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);
        mergedGraghInfo.generateMergeGraphByMatch(mergeVertexToVertexSet);
        long endTime = System.currentTimeMillis();
        System.out.println("time");
        System.out.println(endTime - startTime);

        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator();
        basicEntropyCalculator.calculateEntropy(mergedGraghInfo);

        Bigraph bigraph = mergedGraghInfo.getBiGraph();
        MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
                = new MaximumWeightBipartiteMatching<>(bigraph, graph1.vertexSet(), graph2.vertexSet());
        Matching<Vertex, DefaultWeightedEdge> matchings = bipartiteMatching.getMatching();
        System.out.println(matchings.getEdges().size());

    }


}
