package knowledgeGraph.io;

import knowledgeGraph.baseModel.*;

import java.util.*;

public class GraphImporter {
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
            vertexArrayList.add(vertex);
            vertexHashMap.put(vertex.getId(), vertex);
        }
        // 初始化relation数据，添加edge
        ArrayList<Edge> edgeArrayList = new ArrayList<>();
        Integer edgeId = 1;
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
        Set<Edge> edgeSet = new HashSet<>(edgeArrayList);
        Set<Vertex> vertexSet = new HashSet<>(vertexArrayList);
        Graph graph = new Graph(edgeSet, vertexSet, userName);
        for (Edge edge : graph.getEdgeSet()) {
            edge.setGraph(graph);
        }
        for (Vertex vertex : graph.getVertexSet()) {
            vertex.setGraph(graph);
        }
        return graph;
    }
}
