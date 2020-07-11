package knowledgeGraph;

import knowledgeGraph.io.*;
import knowledgeGraph.mergeModel.*;
import knowledgeGraph.wordSim.WordEmbedding;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.*;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.BasicPlanExecutor;
import knowledgeGraph.ga.BigraphMatchPlanner;
import knowledgeGraph.ga.SimilarityMigratePlanner;
import knowledgeGraph.util.RelationMap;
import knowledgeGraph.wordSim.RelatedWord;

import java.io.*;
import java.util.*;

public class ExperimentMain {
    public static HashMap<Integer, Integer> ans;
    public static HashSet<MergedVertex> wrongMergedVertexSet;
    public static HashSet<MergedVertex> correctMergedVertexSet;
    public static int max_lenth = 0;
    public static RelatedWord relatedWord;
    public static String exp_dir;
    public static HashMap<Integer, String> exp_files;
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
        exp_files = new HashMap<>();
    }

    public static void main(String args[]) throws IOException, ParseException {
        init();

        MergedGraghInfo mergedGraghInfo;
        Options options = new Options();
        Option dirName = Option.builder("d")
                .required(true).hasArgs().argName("数据目录")
                .desc("输入数据所在文件夹").build();
        options.addOption(dirName);
        Option fileName = Option.builder("f").hasArgs()
                .required(true).numberOfArgs(2).argName("图文件")
                .desc("图文件，至少需要两个").build();
        options.addOption(fileName);
        Option round = Option.builder("r").hasArgs()
                .required(true).argName("迭代轮数")
                .desc("迭代算法迭代轮数").build();
        options.addOption(round);
        Option percent = Option.builder("p").hasArgs()
                .required(true).argName("迭代比例")
                .desc("迭代算法每轮迭代比例").build();
        options.addOption(percent);
        Option type = Option.builder("t").hasArgs()
                .required(true).argName("数据类型")
                .desc("数据类型").build();
        options.addOption(type);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String dir = cmd.getOptionValue("d");
        System.out.println("dirName: " + dir);
        exp_dir = dir;

        String[] files = cmd.getOptionValues("f");
        for (int i = 1; i <= files.length; i++) {
            exp_files.put(i, files[i - 1]);
        }

        Integer round_num = Integer.parseInt(cmd.getOptionValue("r"));

        double rate = Double.parseDouble(cmd.getOptionValue("p"));

        int index = exp_files.get(1).indexOf(".");
        fileType = cmd.getOptionValue("t");
        mergedGraghInfo = firstStep(fileType);

        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计

        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator(opt, calcValue, detailed);
        SimilarityMigratePlanner similarityMigratePlanner = new SimilarityMigratePlanner(mergedGraghInfo, rate);
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        for (int i = 1; i <= round_num; i++) {
            step(i, basicEntropyCalculator, similarityMigratePlanner, planExecutor, mergedGraghInfo);
        }

    }

    public static MergedGraghInfo readData() throws IOException {
        readAns();

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
        return mergedGraghInfo;
    }

    public static void step(int num, BasicEntropyCalculator basicEntropyCalculator, SimilarityMigratePlanner similarityMigratePlanner, BasicPlanExecutor planExecutor, MergedGraghInfo mergedGraghInfo) throws IOException {
        long startTime, endTime;
        String round = "Round" + num;
        File dir = new File(round);
        if (!dir.exists()) {
            dir.mkdir();
        }
        startTime = System.currentTimeMillis();
        MigratePlan migratePlan = similarityMigratePlanner.getVertexMigratePlan();
        endTime = System.currentTimeMillis();
        System.out.println(round + "plan time " + (endTime - startTime));
        System.out.println(round + " >>> migrate size " + migratePlan.getPlanArrayList().size());
        saveMigratePlan(round, migratePlan);
        planExecutor.ExecutePlan(migratePlan, true, true);
        double etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        HashSet<Integer> migrateId = new HashSet<>();
        for (Plan plan : migratePlan.getPlanArrayList()) {
            migrateId.add(plan.getTarget().getId());
        }
        System.out.println("After Migrate entropy : " + mergedGraghInfo.calculateEntropy(migrateId));
        mergedGraghInfo.saveEntropy(round + "/Entropy");
        mergedGraghInfo.saveDetailEntropy(round + "/DetailEntropy");
        System.out.println(round + "entropy : " + etr);
        System.out.println(round + "hit one : " + calcuteHitOne(mergedGraghInfo));
        mergedGraghInfo.getMergedGraph().saveToFile(round + "/MergedGraph");
        writeWrongSet(mergedGraghInfo, round + "/WrongVertex");
        writeCorrectSet(mergedGraghInfo, round + "/CorrectVertex");
        writeWrongSetName(mergedGraghInfo, round + "/WrongVertexName");
    }

    public static void mergeByAns(String fileType) throws IOException {
        String round = "RoundAns";
        System.out.println(round);
        File dir = new File(round);
        if (!dir.exists()) {
            dir.mkdir();
        }
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计
        // 大规模实验数据
        Graph graph1, graph2;
        BasicImporter importer;
        if (fileType.equalsIgnoreCase("owl")) {
            importer = new OWLImporter(exp_dir);
        } else if (fileType.equalsIgnoreCase("rdf")) {
            importer = new RDFImporter(exp_dir);
        } else {
            importer = new ExperimentFileImporter(exp_dir);
        }
        graph1 = importer.readGraph(1, exp_files.get(1));
        graph2 = importer.readGraph(2, exp_files.get(2));
        importer.readAns();
        saveAns();

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

        if (fileType.equalsIgnoreCase("normal")) {
            migrateRelation(mergedGraghInfo.getMergedGraph());
        }
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
        HashMap<Integer, Vertex> idToVertex = new HashMap<>();
        for (Graph graph : graphArrayList) {
            for (Vertex vertex : graph.vertexSet()) {
                if (vertex.getType().equalsIgnoreCase("entity")) {
                    idToVertex.put(vertex.getId(), vertex);
                }
            }
        }

        MigratePlan migratePlan = new MigratePlan();
        for (Integer sourceId : ans.keySet()) {
            Vertex source = idToVertex.get(sourceId);
            Vertex target = idToVertex.get(ans.get(sourceId));
            migratePlan.addPlan(new Plan(source, source.getMergedVertex(), target.getMergedVertex()));
        }
        System.out.println(">>> migrate info");
        System.out.println(migratePlan.getPlanArrayList().size());

        System.out.println("execute plan ");
        startTime = System.currentTimeMillis();
//        执行迁移方案
        BasicPlanExecutor planExecutor = new BasicPlanExecutor(mergedGraghInfo);
        planExecutor.ExecutePlan(migratePlan, true, true);
        endTime = System.currentTimeMillis();
        System.out.println("execute time:" + (endTime - startTime));
        mergedGraghInfo.getMergedGraph().saveToFile(round + "/MergedGraph");

//        计算迁移后熵值
        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println("new entropy : " + etr);
        mergedGraghInfo.saveEntropy(round + "/Entropy");
        mergedGraghInfo.saveDetailEntropy(round + "/DetailEntropy");
        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
        writeWrongSet(mergedGraghInfo, round + "/WrongVertex");
        writeCorrectSet(mergedGraghInfo, round + "/CorrectVertex");
        writeWrongSetName(mergedGraghInfo, round + "/WrongVertexName");
    }

    public static MergedGraghInfo firstStep(String fileType) throws IOException {
        String round = "Round0";
        System.out.println(round);
        File dir = new File(round);
        if (!dir.exists()) {
            dir.mkdir();
        }
        boolean opt = true; // 简化运算
        boolean calcValue = true; // 是否计算Value节点的入熵
        boolean detailed = false; // 是否对细分信息进行统计

        // TODO: 修改为多图
        Graph graph1, graph2;
        BasicImporter importer;
        if (fileType.equalsIgnoreCase("owl")) {
            importer = new OWLImporter(exp_dir);
        } else if (fileType.equalsIgnoreCase("rdf")) {
            importer = new RDFImporter(exp_dir);
        } else {
            importer = new ExperimentFileImporter(exp_dir);
        }
        graph1 = importer.readGraph(1, exp_files.get(1));
        graph2 = importer.readGraph(2, exp_files.get(2));
        importer.readAns();

        checkGraph(graph1);
        checkGraph(graph2);

        saveAns();

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
        if (fileType.equalsIgnoreCase("normal")) {
            migrateRelation(mergedGraghInfo.getMergedGraph());
        }
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
        mergedGraghInfo.getMergedGraph().saveToFile(round + "/MergedGraph");

//        计算迁移后熵值
        etr = basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
        System.out.println("new entropy : " + etr);
        mergedGraghInfo.saveEntropy(round + "/Entropy");
        mergedGraghInfo.saveDetailEntropy(round + "/DetailEntropy");
        System.out.println("hit one : " + calcuteHitOne(mergedGraghInfo));
        writeWrongSet(mergedGraghInfo, round + "/WrongVertex");
        writeCorrectSet(mergedGraghInfo, round + "/CorrectVertex");
        writeWrongSetName(mergedGraghInfo, round + "/WrongVertexName");
        saveOneNode(mergedGraghInfo, round + "/OneNode");
        return mergedGraghInfo;
    }

    public static double calcuteHitOne(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        int correctNum = 0;
        int wrongNum = 0;
        HashSet<MergedVertex> tempwrongMergedVertexSet = new HashSet<>();
        HashSet<MergedVertex> tempCorrectMergedVertexSet = new HashSet<>();
        System.out.println("total mergedVertex size : " + mergedGraghInfo.getMergedGraph().vertexSet().size());
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
        System.out.println("precision : " + (double) (correctNum / (correctNum + wrongNum)));
        return (double) correctNum / ans.size();
    }

    public static void writeWrongSet(MergedGraghInfo mergedGraghInfo, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (MergedVertex mergedVertex : wrongMergedVertexSet) {
                writer.write(mergedVertex.getId() + "\t" + mergedGraghInfo.getMergedVertexIndexInEntropy(mergedVertex) + "\n");
                if (fileType.equalsIgnoreCase("rdf")) {
                    for (Vertex vertex : mergedVertex.getVertexSet()) {
                        writer.write(vertex.getGraph().getUserName() + "\t" + vertex.getId());
                        for (String type : vertex.getRdfType()) {
                            writer.write("\t" + type);
                        }
                        writer.write("\n");
                    }
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("write file error" + e.toString());
        }
    }

    public static void writeCorrectSet(MergedGraghInfo mergedGraghInfo, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (MergedVertex mergedVertex : correctMergedVertexSet) {
                writer.write(mergedVertex.getId() + "\t" + mergedGraghInfo.getMergedVertexIndexInEntropy(mergedVertex) + "\n");
                if (fileType.equalsIgnoreCase("rdf")) {
                    for (Vertex vertex : mergedVertex.getVertexSet()) {
                        writer.write(vertex.getGraph().getUserName() + "\t" + vertex.getId());
                        for (String type : vertex.getRdfType()) {
                            writer.write("\t" + type);
                        }
                        writer.write("\n");
                    }
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("write file error" + e.toString());
        }
    }

    public static void writeWrongSetName(MergedGraghInfo mergedGraghInfo, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (MergedVertex mergedVertex : wrongMergedVertexSet) {
                writer.write(mergedVertex.getId() + "\t" + mergedGraghInfo.getMergedVertexIndexInEntropy(mergedVertex) + "\n");
                for (Vertex vertex : mergedVertex.getVertexSet()) {
                    writer.write(vertex.getValue() + "\t");
                }
                writer.write("\n");
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("write file error" + e.toString());
        }
    }

    public static void saveOneNode(MergedGraghInfo mergedGraghInfo, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (MergedVertex mergedVertex : mergedGraghInfo.getMergedGraph().vertexSet()) {
                if (mergedVertex.getType().equalsIgnoreCase("entity") && mergedVertex.getVertexSet().size() == 1) {
                    writer.write(mergedVertex.toString() + "\n");
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("write file error" + e.toString());
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

    public static void saveMigratePlan(String Round, MigratePlan migratePlan) throws IOException {
        File file = new File(Round + "/MigratePlan");
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(migratePlan.getPlanArrayList().size() + "\n");
        for (Plan plan : migratePlan.getPlanArrayList()) {
            writer.write(plan.getVertex().getId() + "\t" + plan.getSource().getId() + "\t" + plan.getTarget().getId() + "\n");
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
        System.out.println("check graph " + graph.getUserName());
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
        for (MergedVertex vertex : graph.vertexSet()) {
            if (vertex.getType().equalsIgnoreCase("relation")) {
                Set<MergedEdge> edgeSet = graph.outgoingEdgesOf(vertex);
                if (edgeSet.size() != 2) {
                    System.out.println("wrong merged data");
                    System.out.println(vertex.getId() + "\t" + edgeSet.size());
                }
            }
            if (vertex.getType().equalsIgnoreCase("entity") || vertex.getType().equalsIgnoreCase("relation")) {
                if (vertex.getVertexSet().size() == 2) {
                    System.out.println("wrong init vertex size " + vertex.getVertexSet().size());
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
        System.out.println("migrate relation");
        RelationMap relationMap = new RelationMap();
        relationMap.setRelationMap();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("relation")) {
                if (mergedVertex.getVertexSet().size() == 1) {
                    Vertex vertex = mergedVertex.getVertexSet().iterator().next();
                    String newValue = relationMap.getRelationMap(vertex.getValue());
                    if (!vertex.getValue().equalsIgnoreCase(newValue)) {
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
