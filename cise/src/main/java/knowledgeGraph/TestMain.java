package knowledgeGraph;

import javafx.util.Pair;
import knowledgeGraph.baseModel.MigratePlan;
import knowledgeGraph.baseModel.Plan;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.ga.BasicPlanExecutor;
import knowledgeGraph.ga.SimilarityMigratePlanner;
import knowledgeGraph.io.TestFileImporter;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class TestMain {
    static HashMap<String, Vertex> nameToVertex;

    public static void main(String argv[]) throws IOException {
        HashSet<Pair<String, HashSet<String>>> test = new HashSet<>();
        HashSet<String> value = new HashSet<>();
        value.add("test");
        test.add(new Pair<>("test", value));
        test.add(new Pair<>("test", value));
        System.out.println(test);


//        TestFileImporter testFileImporter = new TestFileImporter(4);
//        MergedGraph mergedGraph;
//        Pair<HashMap<String, Vertex>, MergedGraph> readInfo = testFileImporter.readGraph();
//        HashMap<String, Vertex> vertexHashMap = readInfo.getKey();
//        nameToVertex = vertexHashMap;
//        mergedGraph = readInfo.getValue();
//        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(mergedGraph);
//        HashMap<MergedVertex, Double> mergedVertexHashMap = new HashMap<>();
//        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
//            mergedVertexHashMap.put(mergedVertex, Double.valueOf(mergedVertex.getVertexSet().size()));
//        }
//
//        mergedGraghInfo.setMergedVertexToEntropy(mergedVertexHashMap);
//        SimilarityMigratePlanner planner = new SimilarityMigratePlanner(mergedGraghInfo);
//        MigratePlan migratePlan = planner.getVertexMigratePlan();
//        System.out.println(">>>>>plan list");
//        for (Plan plan : migratePlan.getPlanArrayList()) {
//            System.out.println(plan.getVertex());
//            for (String name : vertexHashMap.keySet()) {
//                if (vertexHashMap.get(name).equals(plan.getVertex())) {
//                    System.out.println("name: " + name);
//                }
//            }
//            System.out.println("Source: " + plan.getSource().getVertexSet().size());
//            for (Vertex vertex : plan.getSource().getVertexSet()) {
//                for (String name : vertexHashMap.keySet()) {
//                    if (vertexHashMap.get(name).equals(vertex)) {
//                        System.out.println("name: " + name);
//                    }
//                }
//            }
//            System.out.println("Target: " + plan.getTarget().getVertexSet().size());
//            for (Vertex vertex : plan.getTarget().getVertexSet()) {
//                for (String name : vertexHashMap.keySet()) {
//                    if (vertexHashMap.get(name).equals(vertex)) {
//                        System.out.println("name: " + name);
//                    }
//                }
//            }
//        }
//        BasicPlanExecutor executor = new BasicPlanExecutor(mergedGraghInfo);
//        executor.ExecutePlan(migratePlan);
//        System.out.println(mergedGraghInfo.getMergedGraph());

    }
}
