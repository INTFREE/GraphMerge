package knowledgeGraph.io;

import knowledgeGraph.baseModel.*;
import org.neo4j.register.Register;

import java.util.*;

public class GraphImporter {
    static HashMap<Vertex, Integer> Neo4jVertexToId = new HashMap<>();
    static Integer edgeId = 1;
    static Set<String> keyNames;

    public GraphImporter() {
        keyNames = new HashSet<>();
        keyNames.add("姓名");
        keyNames.add("片名");
        keyNames.add("名称");
    }

    public Graph readGraph(Importer importer, String projectName, String userName) {
//        ArrayList<NodeModel> nodeModelArrayList = importer.getNodeModel("大话西游-电影人物关系图谱");
//        ArrayList<RelationModel> relationModelArrayList = importer.getRelationModel("大话西游-电影人物关系图谱");
        ArrayList<RelationInstance> relationInstanceArrayList = importer.getRelationInstance(projectName, userName);
        ArrayList<NodeInstance> nodeInstanceArrayList = importer.getNodeInstance(projectName, userName);

        // 读入node数据，初始化nodeVertex
        ArrayList<Vertex> vertexArrayList = new ArrayList<>();
        HashMap<Integer, Vertex> vertexHashMap = new HashMap<>();
        for (NodeInstance nodeInstance : nodeInstanceArrayList) {
            Vertex vertex = new Vertex(nodeInstance.id, nodeInstance.type, nodeInstance.value);
            //System.out.println(nodeInstance.id + " " + nodeInstance.type + " " + nodeInstance.value);
            vertexArrayList.add(vertex);
            vertexHashMap.put(vertex.getId(), vertex);
            Neo4jVertexToId.put(vertex, vertex.getId());
        }

        // 初始化relation数据，添加edge
        ArrayList<Edge> edgeArrayList = new ArrayList<>();
        for (RelationInstance relationInstance : relationInstanceArrayList) {
            Vertex relationVertex = vertexHashMap.get(relationInstance.id);
            if (relationVertex == null) {
                relationVertex = new Vertex(relationInstance.id, "relation");
                vertexHashMap.put(relationInstance.id, relationVertex);
                vertexArrayList.add(relationVertex);
            }
            Vertex nodeVertex = vertexHashMap.get(relationInstance.roleId);
            if (nodeVertex == null) {
                System.out.println(relationInstance.roleId.toString() + " error");
            }
            Edge edge = new Edge(edgeId, relationVertex, nodeVertex, relationInstance.roleName);
            edgeId += 1;
            edgeArrayList.add(edge);
        }
        // set baseModel id
        ArrayList<InstanceOf> instanceOfArrayList = importer.getInstanceOf(projectName, userName);
        for (InstanceOf instanceOf : instanceOfArrayList) {
            Vertex vertex = vertexHashMap.get(instanceOf.getiId());
            if (vertex == null) {
                System.out.println(userName + " instance error " + instanceOf.getiId().toString());
            } else {
                vertex.setModelId(instanceOf.getjId());
            }
        }
        Set<Vertex> vertexSet = new HashSet<>(vertexArrayList);
        Set<Edge> edgeSet = new HashSet<>(edgeArrayList);
        Graph graph = new Graph(userName);
        for (Vertex vertex : vertexSet) {
            graph.addVertex(vertex);
        }
        for (Edge edge : edgeSet) {
            graph.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        for (Edge edge : graph.edgeSet()) {
            edge.setGraph(graph);
        }
        for (Vertex vertex : graph.vertexSet()) {
            vertex.setGraph(graph);
        }
        for (Vertex vertex : vertexSet) {
            if (vertex.getType().equalsIgnoreCase("entity")) {
                Vertex relationVertex = null;
                Set<Edge> edgeHashSet = graph.incomingEdgesOf(vertex);
                for (Edge edge : edgeHashSet) {
                    if (edge.getRoleName().isEmpty()) {
                        relationVertex = edge.getSource();
                    }
                }
                for (Edge edge : graph.outgoingEdgesOf(relationVertex)) {
                    if (keyNames.contains(edge.getRoleName())) {
                        vertex.setValue(edge.getTarget().getValue());
                    }
                }
            }

        }
        return graph;
    }
}
