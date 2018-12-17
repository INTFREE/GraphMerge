package knowledgeGraph;

import knowledgeGraph.io.Importer;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import javax.xml.transform.Result;

import static org.neo4j.driver.v1.Values.parameters;


public class Main {
    public static void main(String argv[]) {
        Importer importer = new Importer();
        importer.getNodeModel("大话西游-电影人物关系图谱");
        importer.getRelationModel("大话西游-电影人物关系图谱");
        importer.finishImport();
    }
}
