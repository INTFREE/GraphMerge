package knowledgeGraph;

import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.*;
import knowledgeGraph.io.ExperimentFileImporter;
import knowledgeGraph.io.FileImporter2;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExperimentMain {
    public static HashMap<Integer, Integer> ans;

    public static void main(String argv[]) throws IOException {
        ans = new HashMap<>();
        int data_size = 5; // 数据集大小
        boolean mergeAttr = true; // 是否对Entity的Name进行了
        boolean withOutRelation = false; // 图中是否包含relation节点
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
        long startTime, endTime;

        ExperimentFileImporter fileImporter = new ExperimentFileImporter();

        //FileImporter2 fileImporter = new FileImporter2(data_size, mergeAttr, withOutRelation);
        Graph graph1 = fileImporter.readGraph(1);
        Graph graph2 = fileImporter.readGraph(2);
        fileImporter.readAns();
//
        System.out.println("read finished");
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

        startTime = System.currentTimeMillis();
        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        endTime = System.currentTimeMillis();
        System.out.println("entropy calculating time:" + (endTime - startTime));
        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();
        System.out.println("entropy : " + etr);
        System.out.println(">>> migrate info");
//        MergeMigratePlanner mergeMigratePlanner = new MergeMigratePlanner(mergedGraghInfo);
//        MigratePlan migratePlan = mergeMigratePlanner.getVertexMigratePlan();
        System.out.println(migratePlan.getPlanArrayList().size());
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        planExecutor.ExecutePlan(migratePlan);
        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println("entropy : " + etr);
        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
    }


    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
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
                }
            }
        }
        return (double) correctNum / ans.size();
    }
}
