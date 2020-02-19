package knowledgeGraph.io;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;


public class FileImporter2 {
    HashMap<String, Vertex> vertexHashMap;
    //    String data_path = System.getProperty("user.dir") + "/src/entropy_calc/data_10000/";
    String data_path;
    String match_path = System.getProperty("user.dir") + "/src/entropy_calc/match/";
    Integer vertexId = 0;
    Integer edgeId = 0;
    Graph graph;
    Integer order;
    boolean mergeAttr = false;
    boolean withOutRelation = true;
    HashMap<String, Integer> Entity2Id;
    HashMap<String, Integer> Val2Id;
    HashMap<String, Integer> Rel2Id;
    HashMap<String, Integer> Edge2Id;
    HashMap<String, Vertex> Value2Vertex;


    private void init() {
        this.vertexHashMap = new HashMap<>();
        this.Entity2Id = new HashMap<>();
        this.Val2Id = new HashMap<>();
        this.Edge2Id = new HashMap<>();
        this.Rel2Id = new HashMap<>();
    }

    public FileImporter2(Integer data_size) {
        init();
        this.data_path = System.getProperty("user.dir") + "/src/entropy_calc/data_" + data_size + "/";
    }

    public FileImporter2(Integer data_size, boolean mergeAttr, boolean withOutRelation) {
        init();
        this.data_path = System.getProperty("user.dir") + "/src/entropy_calc/data_" + data_size + "/";
        this.mergeAttr = mergeAttr;
        this.withOutRelation = withOutRelation;
    }

    public Graph readGraph(Integer order, Integer mOrder) {
        this.order = order;
        this.graph = new Graph(order.toString());
        this.Value2Vertex = new HashMap<>();
        //好丑
        this.Entity2Id = readMatch(mOrder);
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
            String vertexFileName = this.data_path + "entity_" + this.order.toString();
            File vertexFile = new File(vertexFileName);

            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;

            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                String vertexKey = line.split("\\|")[0];
                String vertexName = line.split("\\|")[1];
                if (vertexName.equalsIgnoreCase("__null__")) {
                    vertexName = "";
                }

                //处理Entity
                if (this.Entity2Id.get(vertexKey) == null) System.out.println("Vertex Not Found!");
                s = this.Entity2Id.get(vertexKey);

                Vertex entity = new Vertex(s, "Entity", vertexName);
                vertexHashMap.put(vertexKey, entity);
                //处理Value
                //value在同一个图谱中是有重名的，重名的只创建一个节点
                if (Val2Id.get(vertexName) == null) Val2Id.put(vertexName, vertexId++);
                o = Val2Id.get(vertexName);
                Vertex value;
                if (Value2Vertex.get(vertexName) == null) {//必然之前是Val2Id.get(vertexName) == null
                    value = new Vertex(o, "Value", vertexName);
                    Value2Vertex.put(vertexName, value);
                }
                value = Value2Vertex.get(vertexName);
                entity.setValue(value.getValue());
                //处理Relation
                String tmp = s + "|" + "name" + "|" + o;
                if (mergeAttr) tmp = s + "|" + "name" + "|";

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

                // set entity value = key_value
                entity.setValue(value.getValue());

                //init Edge
                tmp = p + "|name-source|" + s;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), relation, entity, "name-source");
                tmp = p + "|name-target|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge valueEdge = new Edge(Edge2Id.get(tmp), relation, value, "name-target");
                entityEdge.setGraph(graph);
                valueEdge.setGraph(graph);
                graph.addEdge(relation, entity, entityEdge);
                graph.addEdge(relation, value, valueEdge);


            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    private void readAttr() {
        InputStream inputStream;
        try {
            // read vertex file
            String vertexFileName = data_path + "attr_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String value = line.split("\\|")[2];
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
                if (Value2Vertex.get(value) == null) {
                    valueVertex = new Vertex(o, "Value", value);
                    Value2Vertex.put(value, valueVertex);
                }
                valueVertex = Value2Vertex.get(value);

                String tmp = s + "|" + attr + "|" + o;
                //if (mergeAttr) tmp = s+"|"+attr+"|"; 存在同名属性比较复杂

                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relationVertex = new Vertex(p, "Relation", attr);

                graph.addVertex(valueVertex);
                graph.addVertex(relationVertex);
                relationVertex.addRelatedVertex(entity);
                relationVertex.addRelatedVertex(valueVertex);

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
                entityEdge.setGraph(graph);
                valueEdge.setGraph(graph);
                graph.addEdge(relationVertex, entity, entityEdge);
                graph.addEdge(relationVertex, valueVertex, valueEdge);


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
            String vertexFileName = data_path + "rel_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey1 = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String vertexKey2 = line.split("\\|")[2];
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
                entityEdge1.setGraph(graph);
                entityEdge2.setGraph(graph);
                graph.addEdge(relationVertex, entity1, entityEdge1);
                graph.addEdge(relationVertex, entity2, entityEdge2);


            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    private void readVertex2() {
        InputStream inputStream;
        try {
            // read vertex file
            String vertexFileName = this.data_path + "entity_" + this.order.toString();
            File vertexFile = new File(vertexFileName);

            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;

            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                String vertexKey = line.split("\\|")[0];
                String vertexName = line.split("\\|")[1];
                if (vertexName.equalsIgnoreCase("__null__")) {
                    vertexName = "";
                }

                //处理Entity
                if (this.Entity2Id.get(vertexKey) == null) System.out.println("Vertex Not Found!");
                s = this.Entity2Id.get(vertexKey);

                Vertex entity = new Vertex(s, "Entity");
                vertexHashMap.put(vertexKey, entity);
                //处理Value
                //value在同一个图谱中是有重名的，重名的只创建一个节点
                if (Val2Id.get(vertexName) == null) Val2Id.put(vertexName, vertexId++);
                o = Val2Id.get(vertexName);
                Vertex value;
                if (Value2Vertex.get(vertexName) == null) {//必然之前是Val2Id.get(vertexName) == null
                    value = new Vertex(o, "Value", vertexName);
                    Value2Vertex.put(vertexName, value);
                }
                value = Value2Vertex.get(vertexName);

                graph.addVertex(entity);
                graph.addVertex(value); //这样是可以的吧，因为如果已包含会返回false

                entity.setGraph(graph);
                entity.setValue(value.getValue());
                value.setGraph(graph);

                //init Edge
                String tmp = s + "|name|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), entity, value, "name");

                graph.addEdge(entity, value, entityEdge);

                entityEdge.setGraph(graph);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    private void readAttr2() {
        InputStream inputStream;
        try {
            // read attr file
            String vertexFileName = data_path + "attr_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String value = line.split("\\|")[2];
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
                if (Value2Vertex.get(value) == null) {
                    valueVertex = new Vertex(o, "Value", value);
                    Value2Vertex.put(value, valueVertex);
                }
                valueVertex = Value2Vertex.get(value);

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
                tmp = s + "|" + attr + "|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), entity, valueVertex, attr + "-source");

                graph.addEdge(entity, valueVertex, entityEdge);
                entityEdge.setGraph(graph);
            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    private void readRelation2() {
        InputStream inputStream;
        try {
            // read vertex file
            String vertexFileName = data_path + "rel_" + this.order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            Integer s, p, o; //subject, predict, object
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey1 = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String vertexKey2 = line.split("\\|")[2];
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

                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity1);
                graph.getRelationToVertex().get(relationVertex).add(entity2);

                //init Edge
                tmp = s + "|" + attr + "-source|" + o;
                if (Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge1 = new Edge(Edge2Id.get(tmp), entity1, entity2, attr + "-source");

                graph.addEdge(entity1, entity2, entityEdge1);
                entityEdge1.setGraph(graph);
            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public HashMap<String, Integer> readMatch(Integer match_order) {
        InputStream inputStream;
        HashMap<String, Integer> Entity2Id = new HashMap<>();
        try {
            // read vertex file
            String vertexFileName = match_path + match_order.toString();
            File vertexFile = new File(vertexFileName);
            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey = line.split("\\|e")[0];
                String mergeVertexId = line.split("\\|e")[1];
                Entity2Id.put(vertexKey, Integer.valueOf(mergeVertexId) + 1);//因为原数据是从0开始的
                vertexId = Math.max(vertexId, Integer.valueOf(mergeVertexId) + 1);
            }
            vertexId += 1;
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return Entity2Id;
    }

    public void readAns(int dataSize) {
        try {
            // read vertex file
            String vertexFileName = System.getProperty("user.dir") + "/src/entropy_calc/data_" + dataSize + "/" + "entity_";
            File vertexFile1 = new File(vertexFileName + "1");
            File vertexFile2 = new File(vertexFileName + "2");

            InputStream inputStream1 = new FileInputStream(vertexFile1);
            Reader reader1 = new InputStreamReader(inputStream1);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
            String line1;

            InputStream inputStream2 = new FileInputStream(vertexFile2);
            Reader reader2 = new InputStreamReader(inputStream2);
            BufferedReader bufferedReader2 = new BufferedReader(reader2);
            String line2;
            while ((line1 = bufferedReader1.readLine()) != null) {
                line2 = bufferedReader2.readLine();
                String vertexName1 = line1.split("\\|")[0];
                String vertexName2 = line2.split("\\|")[0];

                if (vertexName1.equalsIgnoreCase("__null__")) {
                    vertexName1 = "";
                }
                ExperimentMain.ans.put(Entity2Id.get(vertexName1), Entity2Id.get(vertexName2));
            }
            bufferedReader1.close();
            bufferedReader2.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

}
