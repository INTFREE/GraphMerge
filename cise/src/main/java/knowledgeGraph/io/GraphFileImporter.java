package knowledgeGraph.io;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;
import org.neo4j.register.Register;

import java.io.*;
import java.util.HashMap;

public class GraphFileImporter {
    private HashMap<String, HashMap<Integer, Vertex>> graphToidToVertex;
    private HashMap<String, HashMap<Integer, Edge>> graphToidToEdge;

    public GraphFileImporter() {
        graphToidToVertex = new HashMap<>();
        graphToidToEdge = new HashMap<>();
    }

    public void readGraphFile(Integer graphNum) {
        for (int i = 1; i <= graphNum; i++) {
            Graph graph = readGraphFromFile(i);
            System.out.println("vertex size " + graph.vertexSet().size());
            System.out.println("edge size " + graph.edgeSet().size());
        }
        MergedGraph mergedGraph = readMergedGraphFromFile();
        System.out.println("merged vertex size : " + mergedGraph.vertexSet().size());
        System.out.println("merged edge size : " + mergedGraph.edgeSet().size());
    }


    public MergedGraph readMergedGraphFromFile() {
        InputStream inputStream;
        MergedGraph mergedGraph = new MergedGraph();
        HashMap<Integer, MergedVertex> idToMergedVertex = new HashMap<>();
        try {
            File mergedGraphFile = new File("MergedGraph");
            inputStream = new FileInputStream(mergedGraphFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
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
            String vertexFileName = "Graph_" + graphName;
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
                Vertex vertex = new Vertex(id, attrs[1], attrs[2]);
                idToVertex.put(id, vertex);
                graph.addVertex(vertex);
            }

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
                idToEdge.put(id, edge);
                graph.addEdge(source, target, edge);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        graphToidToVertex.put(graphName, idToVertex);
        graphToidToEdge.put(graphName, idToEdge);
        return graph;
    }
}
