package knowledgeGraph;

import knowledgeGraph.io.Importer;
import knowledgeGraph.model.*;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
    public static void main(String argv[]) {
        Importer importer = new Importer();
        ArrayList<NodeModel> nodeModelArrayList = importer.getNodeModel("大话西游-电影人物关系图谱");
        for (NodeModel nodeModel : nodeModelArrayList) {
            System.out.println(nodeModel.toString());
        }
//        ArrayList<RelationModel> relationModelArrayList = importer.getRelationModel("大话西游-电影人物关系图谱");
//        for (RelationModel relationModel : relationModelArrayList) {
//                System.out.println(relationModel.toString());
//        }
        ArrayList<RelationInstance> relationInstanceArrayList = importer.getRelationInstance("大话西游-电影人物关系图谱", "464408345@qq.com");
        ArrayList<NodeInstance> nodeInstanceArrayList = importer.getNodeInstance("大话西游-电影人物关系图谱", "464408345@qq.com");

        // 读入node数据，初始化nodeVertex
        ArrayList<NodeVertex> nodeVertexArrayList = new ArrayList<>();
        HashMap<Integer, NodeVertex> nodeVertexHashMap = new HashMap<>();
        for (NodeInstance nodeInstance : nodeInstanceArrayList) {
            NodeVertex nodeVertex = new NodeVertex(nodeInstance.id, nodeInstance.type, nodeInstance.value);
            nodeVertexArrayList.add(nodeVertex);
            nodeVertexHashMap.put(nodeVertex.id, nodeVertex);
        }
        ArrayList<InstanceOf> instanceOfArrayList = importer.getInstanceOf("大话西游-电影人物关系图谱", "464408345@qq.com");
        for (InstanceOf instanceOf : instanceOfArrayList) {
            NodeVertex nodeVertex = nodeVertexHashMap.get(instanceOf.iId);
            if(nodeVertex == null){
                System.out.println("null " + instanceOf.toString());
            }
            else{
                nodeVertex.setModelId(instanceOf.jId);
            }
        }
        // 初始化relation数据，添加edge
        ArrayList<RelationVertex> relationVertexArrayList = new ArrayList<>();
        ArrayList<Edge> edgeArrayList = new ArrayList<>();
        HashMap<Integer, RelationVertex> relationVertexHashMap = new HashMap<>();
        Integer edgeId = 1;
        for (RelationInstance relationInstance : relationInstanceArrayList) {
            RelationVertex relationVertex = relationVertexHashMap.get(relationInstance.id);
            if (relationVertex == null) {
                relationVertex = new RelationVertex(relationInstance.id);
                relationVertexHashMap.put(relationInstance.id, relationVertex);
                relationVertexArrayList.add(relationVertex);
            }
            NodeVertex nodeVertex = nodeVertexHashMap.get(relationInstance.roleId);
            if (nodeVertex == null) {
                System.out.println(relationInstance.roleId.toString() + " error");
            }
            Edge edge = new Edge(edgeId, relationVertex, nodeVertex, relationInstance.roleName);
            edgeId += 1;
            edgeArrayList.add(edge);
        }
        for (Edge edge:edgeArrayList){
            System.out.println(edge.toString());
        }

        importer.finishImport();
    }
}
