package knowledgeGraph;

import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.BasicPlanExecutor;
import knowledgeGraph.ga.BigraphMatchPlanner;
import knowledgeGraph.ga.SimilarityMigratePlanner;
import knowledgeGraph.io.*;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.wordSim.RelatedWord;
import knowledgeGraph.wordSim.WordEmbedding;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.base.Sys;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TripleMain {
    public static HashMap<Integer, Integer> ans;
    public static HashSet<MergedVertex> wrongMergedVertexSet;
    public static HashSet<MergedVertex> correctMergedVertexSet;
    public static RelatedWord relatedWord;
    public static String expDir;
    public static WordEmbedding wordEmbedding;
    public static String fileType;

    public static void init() {
        ans = new HashMap<>();
        wrongMergedVertexSet = new HashSet<>();
        correctMergedVertexSet = new HashSet<>();
        relatedWord = new RelatedWord();
        relatedWord.setRelatedWord();
        wordEmbedding = new WordEmbedding();
        wordEmbedding.setEmbedding();
    }

    public static void main(String args[]) throws IOException, ParseException {
        init();

        MergedGraghInfo mergedGraghInfo;
        Options options = new Options();
        Option dirName = Option.builder("d")
                .required(true).hasArgs().argName("数据目录")
                .desc("输入数据所在文件夹").build();
        options.addOption(dirName);
        Option round = Option.builder("r").hasArgs()
                .required(true).argName("迭代轮数")
                .desc("迭代算法迭代轮数").build();
        options.addOption(round);
        Option percent = Option.builder("p").hasArgs()
                .required(true).argName("迭代比例")
                .desc("迭代算法每轮迭代比例").build();
        options.addOption(percent);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String dir = cmd.getOptionValue("d");
        System.out.println("dirName: " + dir);
        expDir = dir;

        Integer roundNum = Integer.parseInt(cmd.getOptionValue("r"));

        double rate = Double.parseDouble(cmd.getOptionValue("p"));

        File baseDir = new File(expDir);
        File[] dirs = baseDir.listFiles();
        String baseGraphDir = "";
        String delGraphDir = "";
        if (dirs == null || dirs.length == 0) {
            System.out.println("base dir is empty");
            return;
        } else {
            // del_edge level
            for (File firstDir : dirs) {
                if (firstDir.getName().startsWith("testdot")) {
                    baseGraphDir = expDir + "/" + firstDir.getName();
                    System.out.println("base graph dir : " + baseGraphDir);
                }
            }
            if (baseGraphDir.equalsIgnoreCase("")) {
                System.out.println("no base graph dir");
                return;
            }
            for (File firstDir : dirs) {
                if (!firstDir.getName().startsWith("testdot")) {
                    File[] firstDirs = firstDir.listFiles();
                    // 0.1 level
                    for (File secondDir : firstDirs) {
                        File[] secondDirs = secondDir.listFiles();
                        for (File thirdDir : secondDirs) {
                            delGraphDir = expDir + "/" + firstDir.getName() + "/" + secondDir.getName() + "/" + thirdDir.getName();
                            System.out.println("the other graph dir :" + delGraphDir);
                            mergedGraghInfo = firstStep(baseGraphDir, delGraphDir);
                            boolean opt = true; // 简化运算
                            boolean calcValue = true; // 是否计算Value节点的入熵
                            boolean detailed = false; // 是否对细分信息进行统计

                            BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
                            SimilarityMigratePlanner similarityMigratePlanner = new SimilarityMigratePlanner(mergedGraghInfo, rate);
                            BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
                            for (int i = 1; i <= roundNum; i++) {
                                step(i, basicEntropyCalculator, similarityMigratePlanner, planExecutor, mergedGraghInfo);
                            }
                        }
                    }
                }
            }
        }

    }


    public static void step(int num, BasicEntropyCalculator basicEntropyCalculator, SimilarityMigratePlanner similarityMigratePlanner, BasicPlanExecutor planExecutor, MergedGraghInfo mergedGraghInfo) throws IOException {
        String round = "Round" + num;
        MigratePlan migratePlan = similarityMigratePlanner.getVertexMigratePlan();
        planExecutor.ExecutePlan(migratePlan, true, true);
        basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println(round + "hit one : " + calcuteHitOne(mergedGraghInfo));
    }

    public static MergedGraghInfo firstStep(String baseGraphDir, String delGraphDir) throws IOException {
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计

        Graph graph1, graph2;
        BasicImporter importer = new TripleFileImporter(expDir);
        graph1 = importer.readGraph(1, baseGraphDir);
        graph2 = importer.readGraph(2, delGraphDir);
        importer.readAns();

        graph1.print();
        graph2.print();

        ArrayList<Graph> graphArrayList = new ArrayList<>();
        graphArrayList.add(graph1);
        graphArrayList.add(graph2);

        HashSet<Graph> graphHashSet = new HashSet<>();
        graphHashSet.add(graph1);
        graphHashSet.add(graph2);

        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo, true);
        mergedGraghInfo.generateMergeGraphByMatch2();

        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        basicEntropyCalculator.calculateEntropy(mergedGraghInfo);

        // 生成迁移方案
        BigraphMatchPlanner bigraphMatchPlanner = new BigraphMatchPlanner(graph1, graph2, mergedGraghInfo);
        MigratePlan migratePlan = bigraphMatchPlanner.getVertexMigratePlan();

        //  执行迁移方案
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        planExecutor.ExecutePlan(migratePlan, true, true);

        System.out.println("Round 0 hit one : " + calcuteHitOne(mergedGraghInfo));
        return mergedGraghInfo;
    }

    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        int wrongNum = 0;
        HashSet<MergedVertex> tempwrongMergedVertexSet = new HashSet<>();
        HashSet<MergedVertex> tempCorrectMergedVertexSet = new HashSet<>();

        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("entity")) {
                Integer key = -1, value = -1;
                if (mergedVertex.getVertexSet().size() == 2) {
                    Iterator<Vertex> iterator = mergedVertex.getVertexSet().iterator();
                    key = iterator.next().getId();
                    value = iterator.next().getId();
                    boolean flag = false;
                    if (!(ans.containsKey(key) || ans.containsKey(value))) {
                        continue;
                    }
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
                        tempCorrectMergedVertexSet.add(mergedVertex);
                    } else {
                        tempwrongMergedVertexSet.add(mergedVertex);
                        wrongNum += 1;
                    }
                }
            }
        }
        System.out.println("wrong set size is : " + tempwrongMergedVertexSet.size());

        wrongMergedVertexSet = tempwrongMergedVertexSet;
        correctMergedVertexSet = tempCorrectMergedVertexSet;

        System.out.println("correct Num : " + correctNum);
        System.out.println("wrong Num : " + wrongNum);
        System.out.println("precision : " + ((double) correctNum / (correctNum + wrongNum)));
        return (double) correctNum / ans.size();
    }

}
