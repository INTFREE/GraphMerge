package knowledgeGraph.io;

import com.sun.scenario.effect.Merge;
import javafx.util.Pair;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.MergedEdge;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestFileImporter {
    String prefix = System.getProperty("user.dir") + "/src/test_data/";
    Integer map_size;
    HashSet<Graph> graphHashSet;
    MergedGraph mergedGraph;

    public TestFileImporter(Integer size) {
        this.map_size = size;
        this.graphHashSet = new HashSet<>();
        this.mergedGraph = new MergedGraph();
    }

    public Pair<HashMap<String, Vertex>, MergedGraph> readGraph() throws IOException {
        HashMap<String, Vertex> vertexHashMap = new HashMap<>();
        for (int i = 1; i <= this.map_size; i++) {
            Graph graph = new Graph(String.valueOf(i));
            String data_path = prefix + "graph_" + String.valueOf(i) + ".txt";
            List<String> lines = Files.readAllLines(Paths.get(data_path), StandardCharsets.UTF_8);
            System.out.println("graph " + String.valueOf(i));
            String[] entityList = lines.get(0).split(" ");
            Integer id = 1;
            for (String entity : entityList) {
                Vertex vertex = new Vertex(id++, "entity");
                graph.addVertex(vertex);
                vertex.setGraph(graph);
                vertexHashMap.put(entity, vertex);
            }
            lines.remove(0);
            id = 1;
            for (String line : lines) {
                String[] relation = line.split(" ");
                Edge edge = new Edge(id++, vertexHashMap.get(relation[0]), vertexHashMap.get(relation[1]), "");
                graph.addEdge(vertexHashMap.get(relation[0]), vertexHashMap.get(relation[1]), edge);
            }
            this.graphHashSet.add(graph);
        }

        String merge_graph_path = prefix + "merge_graph" + ".txt";
        List<String> lines = Files.readAllLines(Paths.get(merge_graph_path), StandardCharsets.UTF_8);
        for (String line : lines) {
            MergedVertex mergedVertex = new MergedVertex("entity");
            String[] vertexes = line.split(" ");
            for (String vertexName : vertexes) {
                Vertex vertex = vertexHashMap.get(vertexName);
                if (vertex == null) {
                    System.out.println("vertex name error.");
                    continue;
                }
                mergedVertex.addVertex(vertex);
                vertex.setMergedVertex(mergedVertex);
            }
            this.mergedGraph.addVertex(mergedVertex);
        }
        for (MergedVertex mergedVertex : this.mergedGraph.vertexSet()) {
            for (Vertex vertex : mergedVertex.getVertexSet()) {
                Set<Edge> edgeSet = vertex.getGraph().incomingEdgesOf(vertex);
                System.out.println("incoming edge set size " + edgeSet.size());
                for (Edge edge : edgeSet) {
                    MergedVertex mergedVertexSource = edge.getSource().getMergedVertex();
                    MergedEdge mergedEdge = this.mergedGraph.getEdge(mergedVertexSource, mergedVertex);
                    if (mergedEdge == null) {
                        mergedEdge = new MergedEdge(mergedVertexSource, mergedVertex, "");
                        this.mergedGraph.addEdge(mergedVertexSource, mergedVertex, mergedEdge);
                    }
                    mergedEdge.addEdge(edge);
                }
                edgeSet = vertex.getGraph().outgoingEdgesOf(vertex);
                System.out.println("outgoing edge set size " + edgeSet.size());
                for (Edge edge : edgeSet) {
                    MergedVertex mergedVertexTarget = edge.getTarget().getMergedVertex();
                    MergedEdge mergedEdge = this.mergedGraph.getEdge(mergedVertex, mergedVertexTarget);
                    if (mergedEdge == null) {
                        mergedEdge = new MergedEdge(mergedVertex, mergedVertexTarget, "");
                        this.mergedGraph.addEdge(mergedVertex, mergedVertexTarget, mergedEdge);
                    }
                    mergedEdge.addEdge(edge);
                }
            }
        }
        System.out.println(">>>>>>> MergedGraphInfo");
        System.out.println(this.mergedGraph.vertexSet().size());
        System.out.println(this.mergedGraph.edgeSet().size());
        System.out.println(">>>>>>> MergedVertexInfo");
        for (MergedVertex mergedVertex : this.mergedGraph.vertexSet()) {
            System.out.println(mergedVertex.getVertexSet().size());
        }
        System.out.println(">>>>>>> MergedEdgeInfo");
        for (MergedEdge mergedEdge : this.mergedGraph.edgeSet()) {
            System.out.println(mergedEdge.getEdgeSet().size());
        }

        return new Pair<>(vertexHashMap, this.mergedGraph);
    }
}
