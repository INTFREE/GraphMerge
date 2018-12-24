package knowledgeGraph;

import knowledgeGraph.io.Importer;
import knowledgeGraph.model.*;

import java.util.ArrayList;


public class Main {
    public static void main(String argv[]) {
        Importer importer = new Importer();
        ArrayList<NodeModel> nodeModelArrayList = importer.getNodeModel("大话西游-电影人物关系图谱");
        for (NodeModel nodeModel : nodeModelArrayList) {
            System.out.println(nodeModel.toString());
        }
        ArrayList<RelationModel> relationModelArrayList = importer.getRelationModel("大话西游-电影人物关系图谱");
        for (RelationModel relationModel : relationModelArrayList) {
                System.out.println(relationModel.toString());
        }

        ArrayList<NodeInstance> nodeInstanceArrayList = importer.getNodeInstance("大话西游-电影人物关系图谱", "464408345@qq.com");
        for(NodeInstance nodeInstance:nodeInstanceArrayList){
            System.out.println(nodeInstance.toString());
        }
        ArrayList<RelationInstance> relationInstanceArrayList = importer.getRelationInstance("大话西游-电影人物关系图谱", "464408345@qq.com");
        for(RelationInstance relationInstance: relationInstanceArrayList){
            System.out.println(relationInstance.toString());
        }
        ArrayList<InstanceOf> instanceOfArrayList = importer.getInstanceOf("大话西游-电影人物关系图谱", "464408345@qq.com");
        for(InstanceOf instanceOf:instanceOfArrayList){
            System.out.println(instanceOf.toString());
        }
        importer.finishImport();
    }
}
