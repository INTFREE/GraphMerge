package knowledgeGraph.io;

import knowledgeGraph.model.*;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Importer {
    private Driver driver;

    public Importer() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "cise"));
    }

    public ArrayList<ModelNode> getNodeModel(String projectName) {
        ArrayList<ModelNode> modelNodeArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult nodeModels = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (p)-[:has]->(c:Concept)" +
                            "OPTIONAL MATCH (c)-[:has_key_attr]->(rel)" +
                            "RETURN id(c) AS nodeId, c AS node, collect(distinct id(rel)) AS key_attr_list",
                    parameters("project_name", projectName));
            while (nodeModels.hasNext()) {
                Record nodeModel = nodeModels.next();
                Integer nodeId = nodeModel.get("nodeId").asInt();
                Node node = nodeModel.get("node").asNode();
                String value = node.get("value").asString();
                String tag = node.get("tag").asString();
                modelNodeArrayList.add(new ModelNode(nodeId, value, tag));
            }
        }
        return modelNodeArrayList;
    }

    public ArrayList<ModelRelation> getRelationModel(String projectName) {
        ArrayList<ModelRelation> modelRelationArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult relationModels = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (p)-[:has]->(r:Relation)" +
                            "MATCH (r)-[hr:has_role]->(tgt)" +
                            "RETURN id(r) AS relationId, r.value AS value, r.desc AS desc, hr.name AS roleName, id(tgt) AS roleId",
                    parameters("project_name", projectName));
            while (relationModels.hasNext()) {
                Integer relationId = -1;
                String relationValue = "";
                Map<Integer, String> roles = new HashMap<>();
                for (int i = 0; i < 2; i++) {
                    Record relationModel = relationModels.next();
                    relationId = relationModel.get("relationId").asInt();
                    relationValue = relationModel.get("value").asString();
                    String roleName = relationModel.get("roleName").asString();
                    Integer roleId = relationModel.get("roleId").asInt();
                    roles.put(roleId, roleName);
                }
                ModelRelation modelRelation = new ModelRelation(relationId, relationValue, roles);
                modelRelationArrayList.add(modelRelation);
            }
        }
        return modelRelationArrayList;
    }

    public ArrayList<String> getUser() {
        ArrayList<String> userNameList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult userNodes = session.run("MATCH (u:User) RETURN u.name AS name");
            while (userNodes.hasNext()) {
                Record userNode = userNodes.next();
                String userName = userNode.get("name").asString();
                userNameList.add(userName);
            }
        }
        return userNameList;
    }

    public void finishImport() {
        this.driver.close();
    }
}
