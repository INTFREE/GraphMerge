package knowledgeGraph;

import knowledgeGraph.ga.BasicEntropyCalculator;
import knowledgeGraph.ga.GAProcess;
import knowledgeGraph.ga.SimlarityMigratePlanner;
import knowledgeGraph.io.FileImporter;
import knowledgeGraph.io.GraphImporter;
import knowledgeGraph.io.Importer;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;
import org.neo4j.register.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Main {
    public static void main(String argv[]) {
        Importer importer = new Importer();
        ArrayList<String> userNameList = new ArrayList<>();
        userNameList.add("jiangy@pku.edu.cn");
        userNameList.add("weiyh@pku.edu.cn");
        GraphImporter graphImporter = new GraphImporter();
        ArrayList<Graph> graphArrayList = new ArrayList<>();
        for (String userName : userNameList) {
            Graph graph = graphImporter.readGraph(importer, "大话西游-电影人物关系图谱", userName);
            System.out.println(">>>>>> Graph Info");
            System.out.println(graph.getUserName());
            System.out.println("Vertex size " + graph.vertexSet().size());
            System.out.println("Edge size " + graph.edgeSet().size());
            graphArrayList.add(graph);
            for (Vertex vertex : graph.vertexSet()) {
                if (vertex.getType().equalsIgnoreCase("entity")) {
                    System.out.println(vertex.getValue());
                }
            }
        }
        importer.finishImport();

        MergedGraph mergedGraph = new MergedGraph();
        initMergedGraph2(mergedGraph, graphArrayList);
        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(mergedGraph);

        System.out.println(">>>>>> entropy info");
        BasicEntropyCalculator calculator = new BasicEntropyCalculator();
        System.out.println(calculator.calculateEntropy(mergedGraghInfo));
        System.out.println(mergedGraghInfo.getMergedVertexToEntropy().size());
        for (Map.Entry<MergedVertex, Double> entry : mergedGraghInfo.getMergedVertexToEntropy()) {
            System.out.println(entry.getValue() + " " + entry.getKey().getId() + " " + entry.getKey().getType());
        }

        System.out.println(">>>>>> Planner Info");
        SimlarityMigratePlanner planner = new SimlarityMigratePlanner();
        MigratePlan migratePlan = planner.getVertexMigratePlan(mergedGraghInfo);
        for (Plan plan : migratePlan.getPlanArrayList()) {
            System.out.println(plan.getVertex().getValue());
            System.out.println(plan.getSource().getType());
            System.out.println(plan.getTarget().getType());
        }

    }

    public static void initMergedGraph(MergedGraph mergedGraph, ArrayList<Graph> graphArrayList) {
        HashMap<Integer, MergedVertex> idToMergedVertex = new HashMap<>();
        HashMap<Vertex, MergedVertex> vertexToMergedVertex = new HashMap<>();
        for (Graph graph : graphArrayList) {
            for (Vertex vertex : graph.vertexSet()) {
                if (vertex.getType().equalsIgnoreCase("value")) {
                    if (idToMergedVertex.containsKey(vertex.getId())) {
                        idToMergedVertex.get(vertex.getId()).addVertex(vertex);
                        vertexToMergedVertex.put(vertex, idToMergedVertex.get(vertex.getId()));
                    } else {
                        HashSet<Vertex> vertices = new HashSet<>();
                        vertices.add(vertex);
                        MergedVertex mergedVertex = new MergedVertex(vertices, vertex.getType(), vertex.getValue());
                        mergedGraph.addVertex(mergedVertex);
                        idToMergedVertex.put(vertex.getId(), mergedVertex);
                        vertexToMergedVertex.put(vertex, mergedVertex);
                    }

                } else {
                    HashSet<Vertex> vertices = new HashSet<>();
                    vertices.add(vertex);
                    MergedVertex mergedVertex = new MergedVertex(vertices, vertex.getType(), vertex.getValue());
                    mergedGraph.addVertex(mergedVertex);
                    vertexToMergedVertex.put(vertex, mergedVertex);
                }
            }
            for (Edge edge : graph.edgeSet()) {
                Vertex source = edge.getSource();
                Vertex target = edge.getTarget();
                MergedVertex mergedSource = vertexToMergedVertex.get(source);
                MergedVertex mergedTarget = vertexToMergedVertex.get(target);

                if (mergedGraph.containsEdge(mergedSource, mergedTarget)) {
                    mergedGraph.getEdge(mergedSource, mergedTarget).addEdge(edge);
                } else {
                    MergedEdge mergedEdge = new MergedEdge(mergedSource, mergedTarget, edge.getRoleName());
                    mergedEdge.addEdge(edge);
                    mergedGraph.addEdge(mergedSource, mergedTarget, mergedEdge);
                }
            }
        }
        System.out.println(">>>>>> Initialize Merged Graph");
        System.out.println("Vertex size " + mergedGraph.vertexSet().size());
        System.out.println("Edge size " + mergedGraph.edgeSet().size());

    }

    public static void initMergedGraph2(MergedGraph mergedGraph, ArrayList<Graph> graphArrayList) {
        HashMap<Integer, MergedVertex> idToMergedVertex = new HashMap<>();
        HashMap<Vertex, MergedVertex> vertexToMergedVertex = new HashMap<>();
        for (Graph graph : graphArrayList) {
            for (Vertex vertex : graph.vertexSet()) {
                if (idToMergedVertex.containsKey(vertex.getId())) {
                    idToMergedVertex.get(vertex.getId()).addVertex(vertex);
                    vertexToMergedVertex.put(vertex, idToMergedVertex.get(vertex.getId()));
                } else {
                    HashSet<Vertex> vertices = new HashSet<>();
                    vertices.add(vertex);
                    MergedVertex mergedVertex = new MergedVertex(vertices, vertex.getType(), vertex.getValue());
                    mergedGraph.addVertex(mergedVertex);
                    idToMergedVertex.put(vertex.getId(), mergedVertex);
                    vertexToMergedVertex.put(vertex, mergedVertex);
                }

            }
            for (Edge edge : graph.edgeSet()) {
                Vertex source = edge.getSource();
                Vertex target = edge.getTarget();
                MergedVertex mergedSource = vertexToMergedVertex.get(source);
                MergedVertex mergedTarget = vertexToMergedVertex.get(target);

                if (mergedGraph.containsEdge(mergedSource, mergedTarget)) {
                    mergedGraph.getEdge(mergedSource, mergedTarget).addEdge(edge);
                } else {
                    MergedEdge mergedEdge = new MergedEdge(mergedSource, mergedTarget, edge.getRoleName());
                    mergedEdge.addEdge(edge);
                    mergedGraph.addEdge(mergedSource, mergedTarget, mergedEdge);
                }
            }
        }
        System.out.println(">>>>>> Initialize Merged Graph");
        System.out.println("Vertex size " + mergedGraph.vertexSet().size());
        System.out.println("Edge size " + mergedGraph.edgeSet().size());

    }

    public void BigraphTest() {
//        FileImporter importer = new FileImporter();
//
//        Graph graph1 = importer.readGraph(1);
//        Graph graph2 = importer.readGraph(2);
//        System.out.println("read finished");
//        HashMap<String, HashSet<Vertex>> mergeVertexToVertexSet = importer.readMatch(1);
//        HashSet<Graph> graphHashSet = new HashSet<>();
//        graphHashSet.add(graph1);
//        graphHashSet.add(graph2);
//
//        long startTime = System.currentTimeMillis();
//        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
//        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);
//        mergedGraghInfo.generateMergeGraphByMatch(mergeVertexToVertexSet);
//        long endTime = System.currentTimeMillis();
//        System.out.println("time");
//        System.out.println(endTime - startTime);
//
//        BasicEntropyCalculator basicEntropyCalculator = new BasicEntropyCalculator();
//        basicEntropyCalculator.calculateEntropy(mergedGraghInfo);
//
//        Bigraph bigraph = mergedGraghInfo.getBiGraph();
//        MaximumWeightBipartiteMatching<Vertex, DefaultWeightedEdge> bipartiteMatching
//                = new MaximumWeightBipartiteMatching<>(bigraph, graph1.vertexSet(), graph2.vertexSet());
//        Matching<Vertex, DefaultWeightedEdge> matching = bipartiteMatching.getMatching();
//        System.out.println(matching.getEdges().size());
    }


}
