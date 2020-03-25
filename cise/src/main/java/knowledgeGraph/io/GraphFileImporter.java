package knowledgeGraph.io;

import javafx.util.Pair;
import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class GraphFileImporter {
    String data_path;
    private HashMap<String, HashMap<Integer, Vertex>> graphToidToVertex;
    private HashMap<String, HashMap<Integer, Edge>> graphToidToEdge;

    public GraphFileImporter() {
        data_path = System.getProperty("user.dir");
        graphToidToVertex = new HashMap<>();
        graphToidToEdge = new HashMap<>();
    }

    public GraphFileImporter(String path) {
        data_path = System.getProperty("user.dir") + "/" + path ;
        graphToidToVertex = new HashMap<>();
        graphToidToEdge = new HashMap<>();
    }

    public Pair<MergedGraph, ArrayList<Graph>> readGraphFile(Integer graphNum) {
        ArrayList<Graph> graphs = new ArrayList<>();
        for (int i = 1; i <= graphNum; i++) {
            Graph graph = readGraphFromFile(i);
            graphs.add(graph);
        }
        MergedGraph mergedGraph = readMergedGraphFromFile();
        return new Pair<>(mergedGraph, graphs);
    }

    private void saveRelation(Graph graph) {
        try {
            File file = new File("Graph_relation_" + graph.getUserName());
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            for (Vertex vertex : graph.vertexSet()) {
                if (vertex.getType().equalsIgnoreCase("relation") && !vertex.getValue().equalsIgnoreCase("name")) {
                    writer.write(vertex.getValue() + "\n");
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("save MergedVertex error " + e);
        }
    }

    public MergedGraph readMergedGraphFromFile() {
        InputStream inputStream;
        MergedGraph mergedGraph = new MergedGraph();
        HashMap<Integer, MergedVertex> idToMergedVertex = new HashMap<>();
        try {
            File mergedGraphFile = new File(data_path + "/MergedGraph");
            inputStream = new FileInputStream(mergedGraphFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            Integer vertexNum = Integer.parseInt(bufferedReader.readLine());
            Integer edgeNum = Integer.parseInt(bufferedReader.readLine());
            String[] attrs;
            String[] details;
            for (int i = 0; i < vertexNum; i++) {
                attrs = bufferedReader.readLine().split("\t");
                Integer id = Integer.parseInt(attrs[0]);
                MergedVertex mergedVertex = new MergedVertex(id, attrs[1]);
                attrs = bufferedReader.readLine().split("\\|");
                for (int j = 0; j < attrs.length; j += 1) {
                    details = attrs[j].split("\t");
                    Vertex vertex = graphToidToVertex.get(details[0]).get(Integer.parseInt(details[1]));
                    if (vertex == null) {
                        System.out.println("read Vertex error");
                    }
                    mergedVertex.addVertex(vertex);
                    vertex.setMergedVertex(mergedVertex);
                }
                idToMergedVertex.put(id, mergedVertex);
                mergedGraph.addVertex(mergedVertex);
            }
            for (int i = 0; i < edgeNum; i++) {
                attrs = bufferedReader.readLine().split("\t");
                MergedVertex source = idToMergedVertex.get(Integer.parseInt(attrs[0]));
                MergedVertex target = idToMergedVertex.get(Integer.parseInt(attrs[1]));
                if (source == null || target == null) {
                    System.out.println("mergedVertex error");
                }
                MergedEdge mergedEdge = new MergedEdge(source, target, attrs[2]);
                attrs = bufferedReader.readLine().split("\\|");
                for (int j = 0; j < attrs.length; j += 1) {
                    details = attrs[j].split("\t");
                    Edge edge = graphToidToEdge.get(details[0]).get(Integer.parseInt(details[1]));
                    if (edge == null) {
                        System.out.println("read Edge error");
                    }
                    mergedEdge.addEdge(edge);
                }
                mergedGraph.addEdge(source, target, mergedEdge);
            }

        } catch (Exception e) {
            System.out.println("read mergedGraph file error" + e.toString());
        }
        return mergedGraph;
    }


    private Graph readGraphFromFile(Integer graphNum) {
        InputStream inputStream;
        String graphName = graphNum.toString();
        Graph graph = new Graph(graphName);
        HashMap<Integer, Vertex> idToVertex = new HashMap<>();
        HashMap<Integer, Edge> idToEdge = new HashMap<>();
        try {
            // read vertex file
            String vertexFileName = data_path + "/Graph_" + graphName;
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer vertexNum = Integer.parseInt(bufferedReader.readLine());
            Integer edgeNum = Integer.parseInt(bufferedReader.readLine());
            String[] attrs;
            for (int i = 0; i < vertexNum; i++) {
                line = bufferedReader.readLine();
                attrs = line.split("\t");
                Integer id = Integer.parseInt(attrs[0]);
                Vertex vertex;
                if (attrs.length != 3) {
                    vertex = new Vertex(id, attrs[1], "");
                } else {
                    vertex = new Vertex(id, attrs[1], attrs[2]);
                }
                vertex.setGraph(graph);
                idToVertex.put(id, vertex);
                graph.addVertex(vertex);
            }
            System.out.println("vertex finish");
            for (int i = 0; i < edgeNum; i++) {
                line = bufferedReader.readLine();
                attrs = line.split("\t");
                Integer id = Integer.parseInt(attrs[0]);
                Vertex source = idToVertex.get(Integer.parseInt(attrs[1]));
                Vertex target = idToVertex.get(Integer.parseInt(attrs[2]));
                if (source == null || target == null) {
                    System.out.println(line);
                }
                Edge edge = new Edge(id, source, target, attrs[3]);
                edge.setGraph(graph);
                idToEdge.put(id, edge);
                source.addRelatedVertex(target);
                target.addRelatedVertex(source);
                graph.addEdge(source, target, edge);
            }
            System.out.println("edge finish");
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        graphToidToVertex.put(graphName, idToVertex);
        graphToidToEdge.put(graphName, idToEdge);
        return graph;
    }
}
