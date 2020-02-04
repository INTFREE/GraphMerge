package knowledgeGraph;

import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.*;
import knowledgeGraph.io.ExperimentFileImporter;
import knowledgeGraph.io.FileImporter2;
import knowledgeGraph.io.GraphFileImporter;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.*;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ExperimentMain {
    public static HashMap<Integer, Integer> ans;
    public static int max_lenth = 0;

    public static void main(String argv[]) throws IOException {

        ans = new HashMap<>();
        int data_size = 5; // 数据集大小
        boolean mergeAttr = true; // 是否对Entity的Name进行了
        boolean withOutRelation = false; // 图中是否包含relation节点
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
        long startTime, endTime;

//        ExperimentFileImporter fileImporter = new ExperimentFileImporter();
//        Graph graph1 = fileImporter.readGraph(1);
//        Graph graph2 = fileImporter.readGraph(2);
//        fileImporter.readAns();

        FileImporter2 fileImporter = new FileImporter2(data_size, mergeAttr, withOutRelation);
        Graph graph1 = fileImporter.readGraph(1, 1);
        Graph graph2 = fileImporter.readGraph(2, 1);
        fileImporter.readAns(data_size);

        graph1.print();
        graph2.print();

        ArrayList<Graph> graphArrayList = new ArrayList<>();
        graphArrayList.add(graph1);
        graphArrayList.add(graph2);

        HashSet<Graph> graphHashSet = new HashSet<>();
        graphHashSet.add(graph1);
        graphHashSet.add(graph2);

        startTime = System.currentTimeMillis();
        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo, true);

        mergedGraghInfo.generateMergeGraphByMatch2();
        endTime = System.currentTimeMillis();
        System.out.println("merge time:" + (endTime - startTime));

        graph1.saveToFile();
        graph2.saveToFile();
        mergedGraghInfo.getMergedGraph().saveToFile();

        GraphFileImporter importer = new GraphFileImporter();
        importer.readGraphFile(2);

//
//        startTime = System.currentTimeMillis();
//        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
//        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        endTime = System.currentTimeMillis();
//        System.out.println("entropy calculating time:" + (endTime - startTime));
//        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
//        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();
//        System.out.println("entropy : " + etr);
//        System.out.println(">>> migrate info");
////        MergeMigratePlanner mergeMigratePlanner = new MergeMigratePlanner(mergedGraghInfo);
////        MigratePlan migratePlan = mergeMigratePlanner.getVertexMigratePlan();
//        System.out.println(migratePlan.getPlanArrayList().size());
//        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
//        planExecutor.ExecutePlan(migratePlan);
//        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        HashSet<MergedVertex> unusualSet = basicEntropyCalculator.getUnusualMergedVertexSet();
//        System.out.println("entropy : " + etr);
//        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo, unusualSet));
    }


    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo, HashSet<MergedVertex> unusualSet) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        HashSet<MergedVertex> wrongMergedVertexSet = new HashSet<>();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("entity")) {
                Integer key = -1, value = -1;
                for (Vertex vertex : mergedVertex.getVertexSet()) {
                    if (vertex.getGraph().getUserName().equalsIgnoreCase("1")) {
                        key = vertex.getId();
                    } else {
                        value = vertex.getId();
                    }
                }
                if (value == ans.get(key)) {
                    correctNum += 1;
                } else {
                    wrongMergedVertexSet.add(mergedVertex);
                }
            }
        }
        System.out.println("wrong set size is : " + wrongMergedVertexSet.size());
        wrongMergedVertexSet.retainAll(unusualSet);
        System.out.println("unusual set size is : " + unusualSet.size());
        System.out.println("interaction size is : " + wrongMergedVertexSet.size());
        return (double) correctNum / ans.size();
    }
}
