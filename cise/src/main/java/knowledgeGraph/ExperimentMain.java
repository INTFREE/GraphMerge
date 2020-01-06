package knowledgeGraph;

import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.BasicPlanExecutor;
import knowledgeGraph.ga.MergeMigratePlanner;
import knowledgeGraph.ga.SimlarityMigratePlanner;
import knowledgeGraph.io.FileImporter2;
import knowledgeGraph.mergeModel.MergedGraghInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExperimentMain {
    public static void main(String argv[]) throws IOException {
        int data_size = 5; // 数据集大小
        boolean mergeAttr = true; // 是否对Entity的Name进行了
        boolean withOutRelation = false; // 图中是否包含relation节点
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计

        long startTime, endTime;


        FileImporter2 importer = new FileImporter2(data_size, mergeAttr, withOutRelation);
        Graph graph1 = importer.readGraph(1, 1);
        Graph graph2 = importer.readGraph(2, 1);

        System.out.println("read finished");

        ArrayList<Graph> graphArrayList = new ArrayList<>();
        graphArrayList.add(graph1);
        graphArrayList.add(graph2);

        HashSet<Graph> graphHashSet = new HashSet<>();
        graphHashSet.add(graph1);
        graphHashSet.add(graph2);
        PrintGraph(graph1);
        PrintGraph(graph2);

        startTime = System.currentTimeMillis();
        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);

        mergedGraghInfo.generateMergeGraphByMatch2();
        endTime = System.currentTimeMillis();
        System.out.println("merge time:" + (endTime - startTime));

        startTime = System.currentTimeMillis();
        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        endTime = System.currentTimeMillis();
        System.out.println("entropy calculating time:" + (endTime - startTime));

        System.out.println("entropy : " + etr);
        System.out.println(">>> migrate info");
        MergeMigratePlanner mergeMigratePlanner = new MergeMigratePlanner();
        MigratePlan migratePlan = mergeMigratePlanner.getVertexMigratePlan(mergedGraghInfo);
        System.out.println(migratePlan.getPlanArrayList().size());
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        planExecutor.ExecutePlan(migratePlan);
        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println("entropy : " + etr);
    }

    public static void PrintGraph(Graph graph) {
        System.out.println(">>>>>> Graph Info");
        System.out.println(graph.getUserName());
        System.out.println("Vertex size " + graph.vertexSet().size());
        System.out.println("Edge size " + graph.edgeSet().size());
        HashMap<String, HashSet<Vertex>> typeToVertex = new HashMap<>();
        for (Vertex vertex : graph.vertexSet()) {
            if (!typeToVertex.containsKey(vertex.getType())) {
                typeToVertex.put(vertex.getType(), new HashSet<>());
            }
            typeToVertex.get(vertex.getType()).add(vertex);
        }
        for (String type : typeToVertex.keySet()) {
            System.out.println(type + " size : " + typeToVertex.get(type).size());
        }
    }
}
