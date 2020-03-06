package knowledgeGraph;

import javafx.util.Pair;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.GraphsInfo;
import knowledgeGraph.baseModel.MigratePlan;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.BasicPlanExecutor;
import knowledgeGraph.ga.BigraphMatchPlanner;
import knowledgeGraph.ga.SimilarityMigratePlanner;
import knowledgeGraph.io.ExperimentFileImporter;
import knowledgeGraph.io.FileImporter2;
import knowledgeGraph.io.GraphFileImporter;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ExperimentMain {
    public static HashMap<Integer, Integer> ans;
    public static HashSet<MergedVertex> wrongMergedVertexSet;
    public static int max_lenth = 0;

    public static void main(String argv[]) throws IOException {
//        firstStep();
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
        ans = new HashMap<>();
        wrongMergedVertexSet = new HashSet<>();
        readAns();

        long startTime, endTime;
        GraphFileImporter importer = new GraphFileImporter();
        Pair<MergedGraph, ArrayList<Graph>> graphInfo = importer.readGraphFile(2);
        for (Graph graph : graphInfo.getValue()) {
            graph.print();
        }
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphInfo.getKey());

        System.out.println("finish mergeGraph read");
////
//        startTime = System.currentTimeMillis();
        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        endTime = System.currentTimeMillis();
//        System.out.println("entropy calculating time:" + (endTime - startTime));
//        System.out.println("entropy : " + etr);
//        calcuteHitOne(mergedGraghInfo);
////        mergedGraghInfo.saveEntropy();
//
//        SimilarityMigratePlanner similarityMigratePlanner = new SimilarityMigratePlanner(mergedGraghInfo);
//        MigratePlan migratePlan = similarityMigratePlanner.getVertexMigratePlan();
////        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
////        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();
////
////        System.out.println(">>> migrate info");
////        System.out.println(migratePlan.getPlanArrayList().size());
//        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
//        planExecutor.ExecutePlan(migratePlan);
////        mergedGraghInfo.getMergedGraph().saveToFile();
//        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        System.out.println("entropy : " + etr);
//        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
    }

    public static void firstStep() {
        // 小规模实验数据
//        int data_size = 5; // 数据集大小
//        boolean mergeAttr = true; // 是否对Entity的Name进行了
//        boolean withOutRelation = false; // 图中是否包含relation节点
//        boolean opt = true; // 简化运算
//        boolean calcValue = true; // 是否计算Value节点的入熵
//        boolean detailed = false; // 是否对细分信息进行统计
//        FileImporter2 fileImporter = new FileImporter2(data_size, mergeAttr, withOutRelation);
//        Graph graph1 = fileImporter.readGraph(1, 1);
//        Graph graph2 = fileImporter.readGraph(2, 1);
//        fileImporter.readAns(data_size);

        // 大规模实验数据
        ExperimentFileImporter fileImporter = new ExperimentFileImporter();
        Graph graph1 = fileImporter.readGraph(1);
        Graph graph2 = fileImporter.readGraph(2);
//        fileImporter.readAns();
//      // 保存数据
//        saveAns();
        graph1.print();
        graph2.print();

        ArrayList<Graph> graphArrayList = new ArrayList<>();
        graphArrayList.add(graph1);
        graphArrayList.add(graph2);

        HashSet<Graph> graphHashSet = new HashSet<>();
        graphHashSet.add(graph1);
        graphHashSet.add(graph2);
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo, true);
//
        mergedGraghInfo.generateMergeGraphByMatch2();
        endTime = System.currentTimeMillis();
        System.out.println("merge time:" + (endTime - startTime));
////
//        graph1.saveToFile();
//        graph2.saveToFile();
//        startTime = System.currentTimeMillis();
        // 计算熵值
//        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
//        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        endTime = System.currentTimeMillis();
//        System.out.println("entropy calculating time:" + (endTime - startTime));
//        System.out.println("entropy : " + etr);
        // 生成迁移方案
        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();
//        System.out.println(">>> migrate info");
//        System.out.println(migratePlan.getPlanArrayList().size());
        // 执行迁移方案
//        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
//        planExecutor.ExecutePlan(migratePlan);
//        mergedGraghInfo.getMergedGraph().saveToFile();
        // 计算熵值
//        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//        HashSet<MergedVertex> unusualSet = basicEntropyCalculator.getUnusualMergedVertexSet();
//        System.out.println("entropy : " + etr);
//        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo, unusualSet));
    }

    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        HashSet<MergedVertex> tempwrongMergedVertexSet = new HashSet<>();
        HashSet<Integer> wrongIds = new HashSet<>();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("entity")) {
                Integer key = -1, value = -1;
                if (mergedVertex.getVertexSet().size() == 2) {
                    Iterator<Vertex> iterator = mergedVertex.getVertexSet().iterator();
                    key = iterator.next().getId();
                    value = iterator.next().getId();
                    boolean flag = false;
                    if (ans.containsKey(key)) {
                        if (ans.get(key).intValue() == value) {
                            flag = true;
                        }
                    } else if (ans.containsKey(value)) {
                        if (ans.get(value).intValue() == key) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        correctNum += 1;
                    } else {
                        tempwrongMergedVertexSet.add(mergedVertex);
                    }
                } else {
                    wrongIds.add(mergedVertex.getVertexSet().iterator().next().getId());

                }
            }
        }
        System.out.println("wrong set size is : " + tempwrongMergedVertexSet.size());
        wrongMergedVertexSet = tempwrongMergedVertexSet;
        return (double) correctNum / ans.size();
    }

    public static void saveAns() throws IOException {
        File file = new File("AnsFile");
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        for (Integer key : ans.keySet()) {
            writer.write(key + "\t" + ans.get(key) + "\n");
        }
        writer.close();
        os.close();
    }

    public static void readAns() {
        try {
            // read vertex file
            String vertexFileName = "AnsFile";
            File vertexFile1 = new File(vertexFileName);

            InputStream inputStream1 = new FileInputStream(vertexFile1);
            Reader reader1 = new InputStreamReader(inputStream1);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
            String line1;

            while ((line1 = bufferedReader1.readLine()) != null) {
                String vertexName1 = line1.split("\t")[0];
                String vertexName2 = line1.split("\t")[1];
                ans.put(Integer.parseInt(vertexName1), Integer.parseInt(vertexName2));
            }
            System.out.println("finish ans read. size is : " + ExperimentMain.ans.size());
            bufferedReader1.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }
}
