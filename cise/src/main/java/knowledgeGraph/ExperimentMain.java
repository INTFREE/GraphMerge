package knowledgeGraph;

import com.sun.scenario.effect.Merge;
import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
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
import knowledgeGraph.wordSim.RelatedWord;

import java.io.*;
import java.util.*;

public class ExperimentMain {
    public static HashMap<Integer, Integer> ans;
    public static HashSet<MergedVertex> wrongMergedVertexSet;
    public static int max_lenth = 0;
    public static RelatedWord relatedWord;

    public static void init() {
        ans = new HashMap<>();
        wrongMergedVertexSet = new HashSet<>();
        relatedWord = new RelatedWord();
        relatedWord.setRelatedWord();
        readAns();
    }

    public static void main(String argv[]) throws IOException {
        init();
//        firstStep();
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
//
        long startTime, endTime;
        GraphFileImporter importer = new GraphFileImporter();
        Pair<MergedGraph, ArrayList<Graph>> graphInfo = importer.readGraphFile(2);
        for (Graph graph : graphInfo.getValue()) {
            graph.print();
        }
        Iterator<Graph> graphIterator = graphInfo.getValue().iterator();
        Graph graph1 = graphIterator.next();
        Graph graph2 = graphIterator.next();
        System.out.println(graph1.getUserName() + "\t" + graph2.getUserName());

        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphInfo.getKey());
        System.out.println("finish mergeGraph read");

        migrateRelation(mergedGraghInfo.getMergedGraph());

        startTime = System.currentTimeMillis();
        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        endTime = System.currentTimeMillis();
        System.out.println("entropy calculating time:" + (endTime - startTime));
        System.out.println("before entropy : " + etr);
        mergedGraghInfo.saveEntropy("BeforeEntropy");
        System.out.println("before hit one : " + calcuteHitOne(mergedGraghInfo));
//        writeWrongSet(mergedGraghInfo, "BeforeWrongVertex");

        startTime = endTime;

        SimilarityMigratePlanner similarityMigratePlanner = new SimilarityMigratePlanner(mergedGraghInfo);
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        for (int i = 0; i < 1; i++){
            startTime = System.currentTimeMillis();
            MigratePlan migratePlan = similarityMigratePlanner.getVertexMigratePlan();
            endTime = System.currentTimeMillis();
            System.out.println("plan time " + (endTime - startTime));
            System.out.println(">>> migrate info");
            System.out.println(migratePlan.getPlanArrayList().size());
            planExecutor.ExecutePlan(migratePlan, true, true);
            mergedGraghInfo.getMergedGraph().print();
            mergedGraghInfo.saveEntropy("AfterEntropy");
            etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
            System.out.println("after entropy : " + etr);
            System.out.println("after hit one : " + calcuteHitOne(mergedGraghInfo));
        }

    }

    public static void firstStep() throws IOException {
        // 小规模实验数据
//        int data_size = 5; // 数据集大小
//        boolean mergeAttr = true; // 是否对Entity的Name进行了
//        boolean withOutRelation = false; // 图中是否包含relation节点
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
//        FileImporter2 fileImporter = new FileImporter2(data_size, mergeAttr, withOutRelation);
//        Graph graph1 = fileImporter.readGraph(1, 1);
//        Graph graph2 = fileImporter.readGraph(2, 1);
//        fileImporter.readAns(data_size);

        // 大规模实验数据
        ExperimentFileImporter fileImporter = new ExperimentFileImporter();
        Graph graph1 = fileImporter.readGraph(1);
        checkGraph(graph1);
        Graph graph2 = fileImporter.readGraph(2);
        checkGraph(graph2);
        ans = new HashMap<>();
        wrongMergedVertexSet = new HashSet<>();

        fileImporter.readAns();
        // 保存数据
        saveAns();
//        readAns();
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

        mergedGraghInfo.generateMergeGraphByMatch2();
        endTime = System.currentTimeMillis();
        System.out.println("merge time:" + (endTime - startTime));
        checkMergedGraph(mergedGraghInfo.getMergedGraph());

        System.out.println("save Graph file");
        graph1.saveToFile();
        graph2.saveToFile();


        startTime = System.currentTimeMillis();
        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        endTime = System.currentTimeMillis();
        System.out.println("entropy calculating time:" + (endTime - startTime));
        System.out.println("primitive entropy : " + etr);

        // 生成迁移方案
        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();
        System.out.println(">>> migrate info");
        System.out.println(migratePlan.getPlanArrayList().size());

        System.out.println("execute plan ");
        startTime = System.currentTimeMillis();
//        执行迁移方案
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        planExecutor.ExecutePlan(migratePlan, true, true);
        endTime = System.currentTimeMillis();
        System.out.println("execute time:" + (endTime - startTime));
        mergedGraghInfo.getMergedGraph().saveToFile();

//        计算迁移后熵值
        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println("new entropy : " + etr);
        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
    }

    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        HashSet<MergedVertex> tempwrongMergedVertexSet = new HashSet<>();
        HashSet<Integer> wrongIds = new HashSet<>();
        System.out.println("total mergedVertex size : " + mergedGraghInfo.getMergedGraph().vertexSet().size());
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

    public static void writeWrongSet(MergedGraghInfo mergedGraghInfo, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (MergedVertex mergedVertex : wrongMergedVertexSet) {
                writer.write(mergedVertex.getId() + "\t" + mergedGraghInfo.getMergedVertexIndexInEntropy(mergedVertex) + "\n");
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
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

    public static void checkGraph(Graph graph) {
        System.out.println("wrong data " + graph.getUserName());
        for (Vertex vertex : graph.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("relation")) {
                Set<Edge> edgeSet = graph.outgoingEdgesOf(vertex);
                if (edgeSet.size() != 2) {
                    System.out.println(vertex.getId() + "\t" + edgeSet.size());
                }
            }
        }
    }

    public static void checkMergedGraph(MergedGraph graph) {
        System.out.println("wrong merged data");
        for (MergedVertex vertex : graph.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("relation")) {
                Set<MergedEdge> edgeSet = graph.outgoingEdgesOf(vertex);
                if (edgeSet.size() != 2) {
                    System.out.println(vertex.getId() + "\t" + edgeSet.size());
                }
            }
        }
        System.out.println("edge data");
        for (MergedEdge edge : graph.edgeSet()) {
            if (edge.getEdgeSet().size() != 1) {
                System.out.println(edge.getRoleName() + "\t" + edge.getEdgeSet().size() + "\t" + edge.getEdgeSet());
            }
        }
    }

    public static void migrateRelation(MergedGraph mergedGraph) {
        HashMap<String, String> relationMap = new HashMap<>();
        try {
            for (int i = 1; i <= 2; i++) {
                String fileName = "relation_" + i + "_map";
                File vertexFile1 = new File(fileName);

                InputStream inputStream1 = new FileInputStream(vertexFile1);
                Reader reader1 = new InputStreamReader(inputStream1);
                BufferedReader bufferedReader1 = new BufferedReader(reader1);
                String line1;

                while ((line1 = bufferedReader1.readLine()) != null) {
                    String originName = line1.split("\t")[0];
                    String newName = line1.split("\t")[1];
                    relationMap.put(originName, newName);
                }
                bufferedReader1.close();
            }

        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("relation")) {
                if (mergedVertex.getVertexSet().size() == 1) {
                    Vertex vertex = mergedVertex.getVertexSet().iterator().next();
                    if (relationMap.containsKey(vertex.getValue())) {
                        String newValue = relationMap.get(vertex.getValue());
                        vertex.setValue(newValue);
                        Set<MergedEdge> relatedEdges = mergedGraph.outgoingEdgesOf(mergedVertex);
                        for (MergedEdge mergedEdge : relatedEdges) {
                            if (mergedEdge.getRoleName().endsWith("source")) {
                                mergedEdge.setRoleName(newValue + "-source");
                                for (Edge edge : mergedEdge.getEdgeSet()) {
                                    edge.setRoleName(newValue + "-source");
                                }
                            } else if (mergedEdge.getRoleName().endsWith("target")) {
                                mergedEdge.setRoleName(newValue + "-target");
                                for (Edge edge : mergedEdge.getEdgeSet()) {
                                    edge.setRoleName(newValue + "-target");
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
