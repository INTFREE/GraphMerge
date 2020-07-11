package knowledgeGraph.io;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.TripleMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;

import java.io.*;
import java.util.*;

public class TripleFileImporter implements BasicImporter {
    HashMap<String, Vertex> vertexHashMap;
    String data_path = "";
    Integer vertexId = 1;
    Integer edgeId = 1;
    Graph graph;
    Integer order;
    HashMap<String, Integer> AllEntity2Id;
    HashMap<String, Integer> Entity2Id;
    HashMap<Integer, Integer> MapAns;
    HashMap<String, Integer> Val2Id;
    HashMap<String, Integer> Rel2Id;
    HashMap<String, Integer> Edge2Id;
    HashMap<String, Vertex> Vaule2Vertex;
    List<String> allWords;
    String regex = "^[(,!:]+";
    String regexEnd = "[),!:]+$";
    String pattern = "\\(.*?\\)";


    public TripleFileImporter(String dirName) {
        this.vertexHashMap = new HashMap<>();
        this.Entity2Id = new HashMap<>();
        this.Val2Id = new HashMap<>();
        this.Edge2Id = new HashMap<>();
        this.Rel2Id = new HashMap<>();
        this.data_path = dirName + "/";
        this.MapAns = new HashMap<>();
        AllEntity2Id = new HashMap<>();
    }

    public Graph readGraph(Integer order, String fileName) {
        this.order = order;
        this.graph = new Graph(order.toString());
        this.Vaule2Vertex = new HashMap<>();
        this.Entity2Id = new HashMap<>();
        this.allWords = new ArrayList<>();
        readVertex(fileName + "/entity_local_name");
        readRelation(fileName + "/rel_triples");
        return this.graph;
    }

    private void readVertex(String fileName) {
        InputStream inputStream;
        try {
            // read vertex file
            File vertexFile = new File(fileName);

            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;

            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                String vertexKey = strings.get(0);
                //String vertexName = String.join(" ", strings.subList(1, strings.size()));
                String vertexName = "";
                if (strings.size() == 2) {
                    vertexName = strings.get(1);
                }
                if (vertexName.length() > ExperimentMain.max_lenth) {
                    ExperimentMain.max_lenth = vertexName.length();
                }
                this.Entity2Id.put(vertexKey, this.vertexId++);
                s = this.Entity2Id.get(vertexKey);

                // 如果存在同样key的节点即为map ans
                if (AllEntity2Id.containsKey(vertexKey)) {
                    MapAns.put(AllEntity2Id.get(vertexKey), s);
                } else {
                    AllEntity2Id.put(vertexKey, s);
                }

                Vertex entity = new Vertex(s, "Entity", vertexName);
                dealVertexName(vertexName, entity);
                vertexHashMap.put(vertexKey, entity);
                //处理Value
                //value在同一个图谱中是有重名的，重名的只创建一个节点
                if (!this.Val2Id.containsKey(vertexName)) {
                    Val2Id.put(vertexName, vertexId++);
                }
                o = Val2Id.get(vertexName);
                Vertex value;
                if (!this.Vaule2Vertex.containsKey(vertexName)) {//必然之前是Val2Id.get(vertexName) == null
                    value = new Vertex(o, "Value", vertexName);
                    Vaule2Vertex.put(vertexName, value);
                }
                value = Vaule2Vertex.get(vertexName);
                entity.setValue(value.getValue());
                //处理Relation
                String tmp = s + "|" + "name" + "|" + o;

                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relation = new Vertex(p, "Relation", "name");

                graph.addVertex(entity);
                graph.addVertex(value);
                graph.addVertex(relation);
                relation.addRelatedVertex(entity);
                relation.addRelatedVertex(value);

                entity.setGraph(graph);
                value.setGraph(graph);
                relation.setGraph(graph);

                graph.getRelationToVertex().put(relation, new HashSet<>());
                graph.getRelationToVertex().get(relation).add(entity);
                graph.getRelationToVertex().get(relation).add(value);


                //init Edge
                tmp = p + "|name-source|" + s;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), relation, entity, "name-source");
                tmp = p + "|name-target|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge valueEdge = new Edge(Edge2Id.get(tmp), relation, value, "name-target");

                graph.addEdge(relation, entity, entityEdge);
                graph.addEdge(relation, value, valueEdge);

                entityEdge.setGraph(graph);
                valueEdge.setGraph(graph);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }

    }

    private void dealVertexName(String vertexName, Vertex entity) {
        allWords = Arrays.asList(vertexName.split(" "));
        for (String word : allWords) {
            String keyword = word.replaceAll(regex, "").replaceAll(regexEnd, "");
            this.graph.addKeyWord(keyword, entity);
        }
    }

    private void readRelation(String fileName) {
        InputStream inputStream;
        try {
            // read vertex file
            File vertexFile = new File(fileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                if (strings.size() != 3) {
                    System.out.println("relation exception : " + strings);
                    continue;
                }
                String vertexKey1 = strings.get(0);
                String attr = strings.get(1);
                String vertexKey2 = strings.get(2);

                Vertex entity1 = this.vertexHashMap.get(vertexKey1);
                Vertex entity2 = this.vertexHashMap.get(vertexKey2);
                s = Entity2Id.get(vertexKey1);
                o = Entity2Id.get(vertexKey2);
                if (entity1 == null || entity2 == null) {
                    System.out.println("entity not exists");
                    return;
                }

                String tmp = s + "|" + attr + "|" + o;
                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relationVertex = new Vertex(p, "Relation", attr);
                graph.addVertex(relationVertex);

                relationVertex.addRelatedVertex(entity1);
                relationVertex.addRelatedVertex(entity2);
                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity1);
                graph.getRelationToVertex().get(relationVertex).add(entity2);

                //init Edge
                tmp = p + "|" + attr + "-source|" + s;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge1 = new Edge(Edge2Id.get(tmp), relationVertex, entity1, attr + "-source");
                tmp = p + "|" + attr + "-target|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge2 = new Edge(Edge2Id.get(tmp), relationVertex, entity2, attr + "-target");

                graph.addEdge(relationVertex, entity1, entityEdge1);
                graph.addEdge(relationVertex, entity2, entityEdge2);
                entityEdge1.setGraph(graph);
                entityEdge2.setGraph(graph);
            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public void readAns() {
        TripleMain.ans = this.MapAns;
    }

}
