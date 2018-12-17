package knowledgeGraph.io;

import org.neo4j.driver.internal.spi.Connection;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.register.Register;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class Importer {
    public static void ReadGraph(String projectName) {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "cise"));
        ArrayList<String> userNameList = new ArrayList<String>();
        try (Session session = driver.session()) {
            System.out.println("neo4j connect successfully");
            StatementResult userNodes = session.run("MATCH (u:User) RETURN u.name AS name");
            Integer count = 0;
            while (userNodes.hasNext()) {
                Record userNode = userNodes.next();
                String userName = userNode.get("name").toString();
                userNameList.add(userName);
            }
        }
        System.out.println(userNameList);
        for (String userName : userNameList){
            try (Session session = driver.session()) {
                Map<String, Object> paras = new HashMap<String, Object>();
                paras.put("project_name", projectName);
                paras.put("user_name", userName);
                StatementResult nodeCyphers = session.run("MATCH (p:Project {name: $project_name})" +
                        "MATCH (u:User {name: $user_name})" +
                        "MATCH (p)-[:has]->(i:Inst)<-[:refer]-(u)" +
                        "RETURN id(i) AS nodeId, i AS node", paras);
                while (nodeCyphers.hasNext()) {
                    Record nodeCypher = nodeCyphers.next();
                    System.out.println(nodeCypher.get("nodeId").asInt());
                }
            }
        }
        driver.close();
    }

}
