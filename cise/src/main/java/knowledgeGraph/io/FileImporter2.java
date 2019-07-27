package knowledgeGraph.io;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class FileImporter2 {
    HashMap<String, Vertex> vertexHashMap;
    String data_path = System.getProperty("user.dir") + "/cise/src/entropy_calc/data/";
    String match_path = System.getProperty("user.dir") + "/cise/src/entropy_calc/match/";
    Integer vertexId = 0;
    Integer edgeId = 0;
    Graph graph;
    Integer order;
    HashMap<String, Integer> Entity2Id;
    HashMap<String, Integer> Val2Id;
    HashMap<String, Integer> Rel2Id;
    HashMap<String, Integer> Edge2Id;
    HashMap<String, Vertex> Vaule2Vertex;


    public FileImporter2() {
        this.vertexHashMap = new HashMap<>();
        this.Vaule2Vertex = new HashMap<>();
    }

    public Graph readGraph(Integer order, Integer mOrder, HashMap<String, Integer> Entity2Id, HashMap<String, Integer> Val2Id, HashMap<String, Integer> Rel2Id, HashMap<String, Integer> Edge2Id) {
        this.order = order;
        this.graph = new Graph(order.toString());
        //好丑
        this.Entity2Id = readMatch(mOrder);
        if (Entity2Id.size() == 0) for (String key : this.Entity2Id.keySet()) Entity2Id.put(key, this.Entity2Id.get(key));
        this.Val2Id =  Val2Id;
        this.Rel2Id =  Rel2Id;
        this.Edge2Id =  Edge2Id;

        for (String key : Entity2Id.keySet()) vertexId = Math.max(vertexId, Entity2Id.get(key));
        for (String key : Val2Id.keySet()) vertexId = Math.max(vertexId, Val2Id.get(key));
        for (String key : Rel2Id.keySet()) vertexId = Math.max(vertexId, Rel2Id.get(key));
        vertexId++;

        for (String key : Edge2Id.keySet()) edgeId = Math.max(edgeId, Edge2Id.get(key));
        edgeId++;
        System.out.println("vertexId start: " + vertexId);
        System.out.println("edgeId start: " + edgeId);
        readVertex();
        readAttr();
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
                if (Entity2Id.get(vertexKey) == null) System.out.println("Vertex Not Found!");
                s = Entity2Id.get(vertexKey);
                Vertex entity = new Vertex(s, "Entity");
                vertexHashMap.put(vertexKey, entity);
                //处理Value
                //value在同一个图谱中是有重名的，重名的只创建一个节点
                if (Val2Id.get(vertexName) == null) Val2Id.put(vertexName, vertexId++);
                o = Val2Id.get(vertexName);
                Vertex value;
                if (Vaule2Vertex.get(vertexName) == null) {//必然之前是Val2Id.get(vertexName) == null
                    value = new Vertex(o, "Value", vertexName);
                    Vaule2Vertex.put(vertexName, value);
                }
                value = Vaule2Vertex.get(vertexName);
                //处理Relation
                String tmp = s+"|"+"name"+"|"+o;
                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relation = new Vertex(p, "Relation", "name");

                graph.addVertex(entity);
                graph.addVertex(value); //这样是可以的吧，因为如果已包含会返回false
                graph.addVertex(relation);

                entity.setGraph(graph);
                value.setGraph(graph);
                relation.setGraph(graph);

                graph.getRelationToVertex().put(relation, new HashSet<>());
                graph.getRelationToVertex().get(relation).add(entity);
                graph.getRelationToVertex().get(relation).add(value);
                //init Edge
                tmp = p+"|name-source|"+s;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), relation, entity, "name-source");
                tmp = p+"|name-target|"+o;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge valueEdge = new Edge(Edge2Id.get(tmp), relation, value, "name-target");

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
                    System.out.println("entity not exists");
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

                String tmp = s+"|"+attr+"|"+o;
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
                tmp = p+"|"+attr+"-source|"+s;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge = new Edge(Edge2Id.get(tmp), relationVertex, entity, attr + "-source");
                tmp = p+"|"+attr+"-target|"+o;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge valueEdge = new Edge(Edge2Id.get(tmp), relationVertex, valueVertex, attr + "-target");

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

                String tmp = s+"|"+attr+"|"+o;
                if (Rel2Id.get(tmp) == null) Rel2Id.put(tmp, vertexId++);
                p = Rel2Id.get(tmp);
                Vertex relationVertex = new Vertex(p, "Relation", attr);
                graph.addVertex(relationVertex);

                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity1);
                graph.getRelationToVertex().get(relationVertex).add(entity2);

                //init Edge
                tmp = p+"|"+attr+"-source|"+s;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge1 = new Edge(Edge2Id.get(tmp), relationVertex, entity1, attr + "-source");
                tmp = p+"|"+attr+"-target|"+o;
                if(Edge2Id.get(tmp) == null) Edge2Id.put(tmp, edgeId++);
                Edge entityEdge2 = new Edge(Edge2Id.get(tmp), relationVertex, entity2, attr + "-target");

                graph.addEdge(relationVertex, entity1, entityEdge1);
                graph.addEdge(relationVertex, entity2, entityEdge2);


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
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return Entity2Id;
    }


}
