package knowledgeGraph.io;

import knowledgeGraph.baseModel.*;
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

    public ArrayList<NodeModel> getNodeModel(String projectName) {
        ArrayList<NodeModel> modelNodeArrayList = new ArrayList<>();
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
                modelNodeArrayList.add(new NodeModel(nodeId, value, tag));
            }
        }
        return modelNodeArrayList;
    }

    public ArrayList<RelationModel> getRelationModel(String projectName) {
        ArrayList<RelationModel> modelRelationArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult relationModels = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (p)-[:has]->(r:Relation)" +
                            "MATCH (r)-[hr:has_role]->(tgt)" +
                            "RETURN id(r) AS relationId, r.value AS value, r.desc AS desc, hr.name AS roleName, id(tgt) AS roleId",
                    parameters("project_name", projectName));

            while (relationModels.hasNext()) {
                Map<Integer, String> roles = new HashMap<>();
                boolean flag = false;
                Record relationModel = relationModels.next();
                Integer relationId = relationModel.get("relationId").asInt();
                String relationValue = relationModel.get("value").asString();
                String roleName = relationModel.get("roleName").asString();
                Integer roleId = relationModel.get("roleId").asInt();
                roles.put(roleId, roleName);
                for (RelationModel modelRelation : modelRelationArrayList) {
                    if (modelRelation.id.equals(relationId)) {
                        modelRelation.addRole(roles);
                        flag = true;
                    }
                }
                if (!flag) {
                    RelationModel modelRelation = new RelationModel(relationId, relationValue, roles);
                    modelRelationArrayList.add(modelRelation);
                }

            }
        }
        return modelRelationArrayList;
    }

    public ArrayList<NodeInstance> getNodeInstance(String projectName, String userName) {
        ArrayList<NodeInstance> nodeInstanceArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult nodeInstances = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (u:User {name: $user_name})" +
                            "MATCH (p)-[:has]->(i:Inst)<-[:refer]-(u)" +
                            "RETURN id(i) AS nodeId, i AS node",
                    parameters("project_name", projectName, "user_name", userName));
            while (nodeInstances.hasNext()) {
                Record nodeInstance = nodeInstances.next();
                Integer id = nodeInstance.get("nodeId").asInt();
                Node node = nodeInstance.get("node").asNode();
                String value = node.get("value").asString();
                NodeInstance nodeInstance1;
                if (value.isEmpty()) {
                    nodeInstance1 = new NodeInstance(id, "", "Entity");
                } else {
                    nodeInstance1 = new NodeInstance(id, value, "Value");
                }
                nodeInstanceArrayList.add(nodeInstance1);
            }
        }
        return nodeInstanceArrayList;

    }

    public ArrayList<RelationInstance> getRelationInstance(String projectName, String userName) {
        ArrayList<RelationInstance> relationInstanceArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult relationInstances = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (u:User {name: $user_name})" +
                            "MATCH (p)-[:has]->(r:RelInst)<-[:refer]-(u)" +
                            "MATCH (r)-[hr:has_role]->(tgt)" +
                            "RETURN id(r) AS relationId, hr.name AS roleName, id(tgt) AS roleId",
                    parameters("project_name", projectName, "user_name", userName));
            while (relationInstances.hasNext()) {
                Record record = relationInstances.next();
                Integer relationId = record.get("relationId").asInt();
                String roleName = record.get("roleName").asString();
                Integer roleId = record.get("roleId").asInt();
                RelationInstance relationInstance = new RelationInstance(relationId, roleId, roleName);
                relationInstanceArrayList.add(relationInstance);
            }
        }
        return relationInstanceArrayList;
    }

    public ArrayList<InstanceOf> getInstanceOf(String projectName, String userName) {
        ArrayList<InstanceOf> instanceOfArrayList = new ArrayList<>();
        try (Session session = this.driver.session()) {
            StatementResult instanceOfs = session.run("MATCH (p:Project {name: $project_name})" +
                            "MATCH (u:User {name: $user_name})" +
                            "MATCH (p)-[:has]->(iof:inst_of)<-[:refer]-(u)" +
                            "MATCH (i)-[:from]->(iof)-[:to]->(j)" +
                            "RETURN id(i) AS iId, id(j) AS jId",
                    parameters("project_name", projectName, "user_name", userName));
            while (instanceOfs.hasNext()) {
                Record record = instanceOfs.next();
                Integer iId = record.get("iId").asInt();
                Integer jId = record.get("jId").asInt();
                InstanceOf instanceOf = new InstanceOf(iId, jId);
                instanceOfArrayList.add(instanceOf);
            }
        }
        return instanceOfArrayList;
    }

    public ArrayList<String> getUserNameList() {
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
