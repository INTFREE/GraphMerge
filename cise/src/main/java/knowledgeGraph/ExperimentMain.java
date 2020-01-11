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
    public static HashMap<String, String> ans;

    public static void main(String argv[]) throws IOException {
        ans = new HashMap<>();
        int data_size = 5; // 数据集大小
        boolean mergeAttr = true; // 是否对Entity的Name进行了
        boolean withOutRelation = false; // 图中是否包含relation节点
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
        readAns(data_size);
        System.out.println("ans size : " + ans.size());
        long startTime, endTime;

        ExperimentFileImporter fileImporter = new ExperimentFileImporter();

//        FileImporter2 importer = new FileImporter2(data_size, mergeAttr, withOutRelation);
        Graph graph1 = fileImporter.readGraph(1);
        Graph graph2 = fileImporter.readGraph(2);
//
//        System.out.println("read finished");
//        graph1.print();
//        graph2.print();
//        ArrayList<Graph> graphArrayList = new ArrayList<>();
//        graphArrayList.add(graph1);
//        graphArrayList.add(graph2);
//
//        HashSet<Graph> graphHashSet = new HashSet<>();
//        graphHashSet.add(graph1);
//        graphHashSet.add(graph2);
//
//        startTime = System.currentTimeMillis();
//        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
//        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo, true);
//
//        mergedGraghInfo.generateMergeGraphByMatch2();
//        endTime = System.currentTimeMillis();
//        System.out.println("merge time:" + (endTime - startTime));
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
//        System.out.println("entropy : " + etr);
//        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
    }

    public static void readAns(int dataSize) {
        try {
            // read vertex file
            String vertexFileName = System.getProperty("user.dir") + "/src/entropy_calc/data_" + dataSize + "/" + "entity_";
            File vertexFile1 = new File(vertexFileName + "1");
            File vertexFile2 = new File(vertexFileName + "2");

            InputStream inputStream1 = new FileInputStream(vertexFile1);
            Reader reader1 = new InputStreamReader(inputStream1);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
            String line1;

            InputStream inputStream2 = new FileInputStream(vertexFile2);
            Reader reader2 = new InputStreamReader(inputStream2);
            BufferedReader bufferedReader2 = new BufferedReader(reader2);
            String line2;
            while ((line1 = bufferedReader1.readLine()) != null) {
                line2 = bufferedReader2.readLine();
                String vertexName1 = line1.split("\\|")[1];
                String vertexName2 = line2.split("\\|")[1];

                if (vertexName1.equalsIgnoreCase("__null__")) {
                    vertexName1 = "";
                }
                ans.put(vertexName1, vertexName2);
            }
            bufferedReader1.close();
            bufferedReader2.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("entity")) {
                String key = "", value = "";
                for (Vertex vertex : mergedVertex.getVertexSet()) {
                    if (vertex.getGraph().getUserName().equalsIgnoreCase("1")) {
                        key = vertex.getValue();
                    } else {
                        value = vertex.getValue();
                    }
                }
                if (value.equalsIgnoreCase(ans.get(key))) {
                    correctNum += 1;
                }
            }
        }
        return (double) correctNum / ans.size();
    }
}
