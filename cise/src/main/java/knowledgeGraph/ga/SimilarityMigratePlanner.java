package knowledgeGraph.ga;


import knowledgeGraph.TripleMain;
import org.apache.commons.lang3.tuple.*;
import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.*;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.List;

public class SimilarityMigratePlanner implements MigratePlanner {
    private MergedGraghInfo mergedGraghInfo;
    private String[] allwords;
    String regex = "^[(,!:]+";
    String regexEnd = "[),!:]+$";
    private HashMap<String, HashSet<Vertex>> keywordToVertex;
    double rate;


    public SimilarityMigratePlanner(MergedGraghInfo mergedGraghInfo, double rate) {
        keywordToVertex = new HashMap<>();
        this.mergedGraghInfo = mergedGraghInfo;
        System.out.println("similarity size " + rate);
        this.rate = rate;
    }

    @Override
    public MigratePlan getVertexMigratePlan() {
        System.out.println(">>>>>>>Sim get Migrate Plan");
        MigratePlan migratePlan = new MigratePlan();
        keywordToVertex.clear();
        MergedGraph mergedGraph = this.mergedGraghInfo.getMergedGraph();
        BasicEntropyCalculator entropyCalculator = new BasicEntropyCalculator(this.mergedGraghInfo);
        BasicPlanExecutor executor = new BasicPlanExecutor(this.mergedGraghInfo);
        // 如果还存在单一节点，首先处理单独节点
        // TODO:修改为多图
        HashMap<String, HashSet<Vertex>> bigraphVerties = new HashMap<>();
        bigraphVerties.put("1", new HashSet<>());
        bigraphVerties.put("2", new HashSet<>());
        int twoNodeSize = 0;
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getType().equalsIgnoreCase("entity")) {
                if (mergedVertex.getVertexSet().size() == 1) {
                    Vertex vertex = mergedVertex.getVertexSet().iterator().next();
                    bigraphVerties.get(vertex.getGraph().getUserName()).add(vertex);
                } else if (mergedVertex.getVertexSet().size() == 2) {
                    twoNodeSize += 1;
                }
            }
        }
        System.out.println("one node graph_1 size " + bigraphVerties.get("1").size());
        System.out.println("one node graph_2 size " + bigraphVerties.get("2").size());
        // 计算相似度
        // 迁移5%且熵值小于某个阈值
        double size = twoNodeSize * rate;
        int new_size = (int) size;
        List<HashMap.Entry<MergedVertex, Double>> mergedVertexArrayList = mergedGraghInfo.getMergedVertexToEntropy().subList(0, new_size);
        System.out.println("entropy list size " + mergedVertexArrayList.size());
        double beforeEntropy = 0.0;
        for (HashMap.Entry<MergedVertex, Double> entry : mergedVertexArrayList) {
            MergedVertex mergedVertex = entry.getKey();
            beforeEntropy += entry.getValue();
            if (!mergedVertex.getType().equalsIgnoreCase("entity") || mergedVertex.getVertexSet().size() != 2) {
                continue;
            }
            Iterator<Vertex> vertexIterator = mergedVertex.getVertexSet().iterator();
            Vertex migrateVertex = vertexIterator.next();
            Vertex remainVertex = vertexIterator.next();
            MergedVertex mergedVertex1 = new MergedVertex("entity");
            mergedVertex1.setMergedGraph(mergedGraph);
            mergedGraph.addVertex(mergedVertex1);
            migratePlan.addPlan(new Plan(migrateVertex, mergedVertex, mergedVertex1));
            bigraphVerties.get(migrateVertex.getGraph().getUserName()).add(migrateVertex);
            bigraphVerties.get(remainVertex.getGraph().getUserName()).add(remainVertex);
        }
        System.out.println("BeforeEntropy : " + beforeEntropy);
        System.out.println("migrate two2one plan size : " + migratePlan.getPlanArrayList().size());
        executor.ExecutePlan(migratePlan, true, true);
        migratePlan.clear();
        System.out.println("graph_1 size " + bigraphVerties.get("1").size());
        System.out.println("graph_2 size " + bigraphVerties.get("2").size());
        long start_time = System.currentTimeMillis();
        calRelatedVertex(bigraphVerties.get("2"));
        long end_time = System.currentTimeMillis();
        System.out.println("calculate time " + (end_time - start_time));
        migratePlan = getBigraphPlan(bigraphVerties, entropyCalculator, executor);
        long new_end_time = System.currentTimeMillis();
        System.out.println("generate time :" + (new_end_time - end_time));
        return migratePlan;
    }

    public MigratePlan getBigraphPlan(HashMap<String, HashSet<Vertex>> oneNodeVerties, BasicEntropyCalculator entropyCalculator, BasicPlanExecutor executor) {
        MigratePlan migratePlan = new MigratePlan();
        Iterator<String> iterator = oneNodeVerties.keySet().iterator();
        String key1 = iterator.next();
        String key2 = iterator.next();
        System.out.println("still exist bigraph");
        System.out.println("graph" + key1 + " " + oneNodeVerties.get(key1).size());
        System.out.println("graph" + key2 + " " + oneNodeVerties.get(key2).size());
        Bigraph bigraph = new Bigraph();
        HashSet<Vertex> entity1 = oneNodeVerties.get(key1);
        HashSet<Vertex> entity2 = oneNodeVerties.get(key2);
        for (Vertex vertex : entity1) {
            bigraph.addVertex(vertex);
        }
        for (Vertex vertex : entity2) {
            bigraph.addVertex(vertex);
        }
        HashSet<Vertex> relatedVertex;
        long startTime = System.currentTimeMillis();
        long endTime;
        int count = 0;
        for (Vertex vertex1 : oneNodeVerties.get(key1)) {
            count += 1;
            relatedVertex = getRelatedVertex(vertex1);
            for (Vertex vertex2 : relatedVertex) {
                migratePlan.clear();
                MergedVertex source = vertex2.getMergedVertex();
                migratePlan.addPlan(new Plan(vertex2, source, vertex1.getMergedVertex()));
                executor.ExecutePlan(migratePlan, false, false);
                double similarity = 1 - entropyCalculator.calculateVertexEntropy(vertex1.getMergedVertex());
                bigraph.setEdgeWeight(bigraph.addEdge(vertex1, vertex2), similarity);
                migratePlan.clear();
                migratePlan.addPlan(new Plan(vertex2, vertex1.getMergedVertex(), source));
                executor.ExecutePlan(migratePlan, false, false);
            }
            if (count % 200 == 0) {
                endTime = System.currentTimeMillis();
                System.out.println("calculate : " + count + " " + (endTime - startTime));
                startTime = endTime;
            }
        }
        System.out.println("bigraph size" + bigraph.vertexSet().size() + "\t" + bigraph.edgeSet().size());
        System.out.println("entity1 size " + entity1.size());
        System.out.println("entity2 size " + entity2.size());
//        saveBigraph(bigraph, entity1, entity2);
        MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
                = new MaximumWeightBipartiteMatching<>(bigraph, entity1, entity2);
        MatchingAlgorithm.Matching<Vertex, DefaultWeightedEdge> matching = bipartiteMatching.getMatching();
        System.out.println("matching size " + matching.getEdges().size());
        for (DefaultWeightedEdge edge : matching.getEdges()) {
            migratePlan.addPlan(new Plan(bigraph.getEdgeSource(edge), bigraph.getEdgeSource(edge).getMergedVertex(), bigraph.getEdgeTarget(edge).getMergedVertex()));
        }

        System.out.println("plan size " + migratePlan.getPlanArrayList().size());
        return migratePlan;
    }

    private HashSet<Vertex> getRelatedVertex(Vertex vertex) {
        String[] relatedWords;
        HashSet<Vertex> relatedVertex = new HashSet<>();
        allwords = vertex.getValue().split(" ");
        for (String word : allwords) {
            String temp_word = word.replaceAll(regex, "").replaceAll(regexEnd, "");
            if (keywordToVertex.containsKey(temp_word)) {
                relatedVertex.addAll(keywordToVertex.get(temp_word));
            }
            if (TripleMain.relatedWord.getRelatedWord().containsKey(temp_word)) {
                relatedWords = TripleMain.relatedWord.getRelatedWord().get(temp_word);
                for (String temp : relatedWords) {
                    if (keywordToVertex.containsKey(temp)) {
                        relatedVertex.addAll(keywordToVertex.get(temp));
                    }
                }
            }
        }
        return relatedVertex;
    }

    private void calRelatedVertex(HashSet<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            if (!vertex.getType().equalsIgnoreCase("entity")) {
                continue;
            }
            allwords = vertex.getValue().split(" ");
            for (String word : allwords) {
                String keyword = word.replaceAll(regex, "").replaceAll(regexEnd, "");
                if (!keywordToVertex.containsKey(keyword)) {
                    keywordToVertex.put(keyword, new HashSet<>());
                }
                keywordToVertex.get(keyword).add(vertex);
            }
        }
        System.out.println("keywordToVertex : " + keywordToVertex.size());
    }

    // 去除源节点
    private MergedVertex getTargetMergedVertex(MergedGraph mergedGraph,
                                               MergedVertex baseMergedVertex,
                                               Set<MergedVertex> baseMergedVertexSet,
                                               Set<MergedVertex> mergedVertices,
                                               EdgeType edgeInOrOut) {
        HashMap<MergedVertex, Double> vertexDifference = new HashMap<>();
        for (MergedVertex mergedVertex : baseMergedVertexSet) {
            if (mergedVertex.equals(baseMergedVertex))
                continue;
            Set<MergedVertex> connectedVertexSet = new HashSet<>();
            if (edgeInOrOut.equals((EdgeType.IN))) {
                Set<MergedEdge> mergedEdgeSet = mergedGraph.incomingEdgesOf(mergedVertex);
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    connectedVertexSet.add(mergedEdge.getSource());
                }
            } else {
                Set<MergedEdge> mergedEdgeSet = mergedGraph.outgoingEdgesOf(mergedVertex);
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    connectedVertexSet.add(mergedEdge.getTarget());
                }
            }
            vertexDifference.put(mergedVertex, getSimilarity(connectedVertexSet, mergedVertices));
        }
        System.out.println("GetTarget");
        double res = -1;
        MergedVertex mostLikeMergedVertex = null;
        // TODO: 都是0需要处理
        for (MergedVertex mergedVertex : vertexDifference.keySet()) {
            double tmp = vertexDifference.get(mergedVertex);
            if (tmp > res) {
                res = tmp;
                mostLikeMergedVertex = mergedVertex;
            }
        }
        System.out.println(mostLikeMergedVertex.getId());
        return mostLikeMergedVertex;
    }

    private Pair<Vertex, Set<MergedVertex>> getMostDifferentVertex(MergedGraph mergedGraph, MergedVertex mergedVertex, EdgeType edgeInOrOut) {
        HashMap<Vertex, Set<MergedVertex>> vertexToMergedVertex = new HashMap<>();
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        if (edgeInOrOut.equals(EdgeType.IN)) {
            mergedEdgeSet = mergedGraph.incomingEdgesOf(mergedVertex);
        } else if (edgeInOrOut.equals(EdgeType.OUT)) {
            mergedEdgeSet = mergedGraph.outgoingEdgesOf(mergedVertex);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                Vertex tmpVertex;
                if (edgeInOrOut.equals(EdgeType.IN)) {
                    tmpVertex = edge.getTarget();
                } else {
                    tmpVertex = edge.getSource();
                }
                if (!vertexToMergedVertex.containsKey(tmpVertex)) {
                    vertexToMergedVertex.put(tmpVertex, new HashSet<>());
                }
                MergedVertex tmpMergedVertex;
                if (edgeInOrOut.equals(EdgeType.IN)) {
                    tmpMergedVertex = mergedEdge.getSource();
                } else {
                    tmpMergedVertex = mergedEdge.getTarget();
                }
                vertexToMergedVertex.get(tmpVertex).add(tmpMergedVertex);
            }
        }
        HashMap<Vertex, Double> vertexDifference = new HashMap<>();
        for (Vertex vertex : vertexToMergedVertex.keySet()) {
            vertexDifference.put(vertex, 0.0);
        }
        for (Vertex vertex : vertexToMergedVertex.keySet()) {
            Set<MergedVertex> mergedVertices = vertexToMergedVertex.get(vertex);
            for (Vertex vertex1 : vertexToMergedVertex.keySet()) {
                if (vertex1.equals(vertex)) {
                    continue;
                }
                Set<MergedVertex> tmpSet = vertexToMergedVertex.get(vertex1);
                double similarity = getSimilarity(mergedVertices, tmpSet);
                vertexDifference.put(vertex, vertexDifference.get(vertex) + similarity);
                vertexDifference.put(vertex1, vertexDifference.get(vertex1) + similarity);
            }
        }
        //TODO: 如果不同度都是0需要跳过
        double res = Double.MAX_VALUE;
        Vertex mostDifferentVertex = null;
        for (Vertex vertex : vertexDifference.keySet()) {
            double tmp = vertexDifference.get(vertex);
            if (tmp < res) {
                res = tmp;
                mostDifferentVertex = vertex;
            }
        }
        Set<MergedVertex> mergedVertices = vertexToMergedVertex.get(mostDifferentVertex);
        return new ImmutablePair<>(mostDifferentVertex, mergedVertices);
    }

    private double getSimilarity(Set<MergedVertex> baseSet, Set<MergedVertex> givenSet) {
        Set<MergedVertex> result = new HashSet<>();
        result.addAll(baseSet);
        result.retainAll(givenSet);
        int intersection = result.size();
        result.clear();
        result.addAll(baseSet);
        result.addAll(givenSet);
        return intersection / result.size();
    }

    private void saveBigraph(Bigraph bigraph, HashSet<Vertex> vertices1, HashSet<Vertex> vertices2) {
        try {
            File file = new File("BigraphFile");
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("bigraph vertex size " + bigraph.vertexSet().size() + "\n");
            writer.write("bigraph edge size " + bigraph.edgeSet().size() + "\n");
            writer.write("entity information" + "\n");
            for (Vertex vertex : vertices1) {
                writer.write(vertex.getGraph().getUserName() + " " + vertex.getId() + " " + vertex.getValue() + "\n");
            }
            for (Vertex vertex : vertices2) {
                writer.write(vertex.getGraph().getUserName() + " " + vertex.getId() + " " + vertex.getValue() + "\n");
            }

            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save Bigraph error " + e);
        }
    }

    private void saveMigratePlan(MigratePlan migratePlan) {
        try {
            File file = new File("MigratePlan");
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("migrate size " + migratePlan.getPlanArrayList().size() + "\n");
            for (Plan plan : migratePlan.getPlanArrayList()) {
                writer.write(plan.getVertex().getId() + "\t" + plan.getVertex().getValue() + "\n");
                writer.write(plan.getSource().getId() + "\t" + plan.getTarget().getId() + "\n");
                writer.write("target vertex value " + plan.getTarget().getVertexSet().iterator().next().getValue());
            }

            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save MigratePlan error " + e);
        }
    }

}
