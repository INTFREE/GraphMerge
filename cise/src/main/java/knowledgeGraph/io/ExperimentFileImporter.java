package knowledgeGraph.io;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class ExperimentFileImporter {
    HashMap<String, Vertex> vertexHashMap;
    String data_path = System.getProperty("user.dir") + "/src/BootEA_datasets/BootEA_DBP_WD_100K/";
    String match_path = System.getProperty("user.dir") + "/src/entropy_calc/match/";
    Integer vertexId = 1;
    Integer edgeId = 1;
    Graph graph;
    Integer order;
    HashMap<String, Integer> Entity2Id;
    HashMap<String, Integer> Val2Id;
    HashMap<String, Integer> Rel2Id;
    HashMap<String, Integer> Edge2Id;
    HashMap<String, Vertex> Vaule2Vertex;

    public ExperimentFileImporter() {
        this.vertexHashMap = new HashMap<>();
        this.Entity2Id = new HashMap<>();
        this.Val2Id = new HashMap<>();
        this.Edge2Id = new HashMap<>();
        this.Rel2Id = new HashMap<>();
    }

    public Graph readGraph(Integer order) {
        this.order = order;
        this.graph = new Graph(order.toString());
        this.Vaule2Vertex = new HashMap<>();
        System.out.println("vertexId start: " + vertexId);
        System.out.println("edgeId start: " + edgeId);
        readVertex();
        System.out.println("vertex finish");
        readAttr();
        System.out.println("attr finish");
        readRelation();
        System.out.println("vertexSet:" + this.graph.vertexSet().size());
        System.out.println("edgeSet:" + this.graph.edgeSet().size());
        return this.graph;
    }

    private void readVertex() {
        InputStream inputStream;
        try {
            // read vertex file
            String vertexFileName = this.data_path + "entity_local_name_" + this.order.toString();
            File vertexFile = new File(vertexFileName);

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
                //处理Entity, 经确认数据集中没有同名节点
                if (this.Entity2Id.containsKey(vertexKey)) {
                    System.out.println("Vertex exists");
                    continue;
                }
                this.Entity2Id.put(vertexKey, this.vertexId++);
                s = this.Entity2Id.get(vertexKey);

                Vertex entity = new Vertex(s, "Entity", vertexName);
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
//                System.out.println(entity.getId());
//                System.out.println(value.getValue());
                graph.addVertex(entity);
                graph.addVertex(value); //这样是可以的吧，因为如果已包含会返回false
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

    private void readAttr() {
        InputStream inputStream;
        try {
            // read attr file
            String vertexFileName = data_path + "attr_triples_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                if (strings.size() != 3) {
                    System.out.println("attr exception : " + strings);
                    continue;
                }
                String vertexKey = strings.get(0);
                String attr = strings.get(1);
                String value = strings.get(2);
                //处理实体
                Vertex entity = this.vertexHashMap.get(vertexKey);
                if (entity == null) {
                    System.out.println("entity not exists " + vertexKey);
                    return;
                }
                s = Entity2Id.get(vertexKey);
                //处理value
                //value在同一个图谱中是有重名的，重名的只创建一个节点
                if (Val2Id.get(value) == null) Val2Id.put(value, vertexId++);
                o = Val2Id.get(value);
                Vertex valueVertex;
                if (Vaule2Vertex.get(value) == null) {
                    valueVertex = new Vertex(o, "Value", value);
                    Vaule2Vertex.put(value, valueVertex);
                }
                valueVertex = Vaule2Vertex.get(value);

                String tmp = s + "|" + attr + "|" + o;
                //if (mergeAttr) tmp = s+"|"+attr+"|"; 存在同名属性比较复杂

                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relationVertex = new Vertex(p, "Relation", attr);

                graph.addVertex(valueVertex);
                graph.addVertex(relationVertex);

                valueVertex.setGraph(graph);
                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity);
                graph.getRelationToVertex().get(relationVertex).add(valueVertex);

                //init Edge
                tmp = p + "|" + attr + "-source|" + s;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), relationVertex, entity, attr + "-source");
                tmp = p + "|" + attr + "-target|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge valueEdge = new Edge(Edge2Id.get(tmp), relationVertex, valueVertex, attr + "-target");

                graph.addEdge(relationVertex, entity, entityEdge);
                graph.addEdge(relationVertex, valueVertex, valueEdge);

                entityEdge.setGraph(graph);
                valueEdge.setGraph(graph);
            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    private void readRelation() {
        InputStream inputStream;
        try {
            // read vertex file
            String vertexFileName = data_path + "rel_triples_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
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
        try {
            // read vertex file
            String vertexFileName = data_path + "ent_links";
            File vertexFile1 = new File(vertexFileName);

            InputStream inputStream1 = new FileInputStream(vertexFile1);
            Reader reader1 = new InputStreamReader(inputStream1);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
            String line1;

            while ((line1 = bufferedReader1.readLine()) != null) {
                String vertexName1 = line1.split("\t")[0];
                String vertexName2 = line1.split("\t")[1];
                if (!Entity2Id.containsKey(vertexName1) || !Entity2Id.containsKey(vertexName2)) {
                    System.out.println("read ans error");
                }
                ExperimentMain.ans.put(Entity2Id.get(vertexName1), Entity2Id.get(vertexName2));
            }
            System.out.println("finish ans read. size is : " + ExperimentMain.ans.size());
            bufferedReader1.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

}
