package knowledgeGraph.io;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FileImporter {
    HashMap<String, Vertex> vertexHashMap;
    String data_path = System.getProperty("user.dir") + "/src/entropy_calc/data/";
    String match_path = System.getProperty("user.dir") + "/src/entropy_calc/match/";
    Integer vertexId = 1;
    Integer edgeId = 1;
    Graph graph;
    Integer order;

    public FileImporter() {
        this.vertexHashMap = new HashMap<>();
    }

    public Graph readGraph(Integer order) {
        this.order = order;
        this.graph = new Graph(order.toString());
        readVertex();
        readAttr();
        readRelation();
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
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey = line.split("\\|")[0];
                String vertexName = line.split("\\|")[1];
                if (vertexName.equalsIgnoreCase("__null__")) {
                    vertexName = "";
                }
                Vertex entity = new Vertex(vertexId++, "Entity");
                Vertex value = new Vertex(vertexId++, "Value", vertexName);
                Vertex relation = new Vertex(vertexId++, "Relation", "name");
                vertexHashMap.put(vertexKey, entity);
                graph.addVertex(entity);
                graph.addVertex(value);
                graph.addVertex(relation);

                entity.setGraph(graph);
                value.setGraph(graph);
                relation.setGraph(graph);

                graph.getRelationToVertex().put(relation, new HashSet<>());
                graph.getRelationToVertex().get(relation).add(entity);
                graph.getRelationToVertex().get(relation).add(value);
                //init Edge
                Edge entityEdge = new Edge(edgeId++, relation, entity, "name-source");
                Edge valueEdge = new Edge(edgeId++, relation, value, "name-target");
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
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String value = line.split("\\|")[2];
                Vertex entity = this.vertexHashMap.get(vertexKey);
                if (entity == null) {
                    System.out.println("entity not exists");
                    return;
                }
                Vertex relationVertex = new Vertex(vertexId++, "Relation", attr);
                Vertex valueVertex = new Vertex(vertexId++, "Value", value);
                graph.addVertex(valueVertex);
                graph.addVertex(relationVertex);

                valueVertex.setGraph(graph);
                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity);
                graph.getRelationToVertex().get(relationVertex).add(valueVertex);

                //init Edge
                Edge entityEdge = new Edge(edgeId++, relationVertex, entity, attr + "-source");
                Edge valueEdge = new Edge(edgeId++, relationVertex, valueVertex, attr + "-target");
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
            while ((line = bufferedReader.readLine()) != null) {
                // init vertex
                String vertexKey1 = line.split("\\|")[0];
                String attr = line.split("\\|")[1];
                String vertexKey2 = line.split("\\|")[2];
                Vertex entity1 = this.vertexHashMap.get(vertexKey1);
                Vertex entity2 = this.vertexHashMap.get(vertexKey2);
                if (entity1 == null || entity2 == null) {
                    System.out.println("entity not exists");
                    return;
                }
                Vertex relationVertex = new Vertex(vertexId++, "Relation", attr);
                graph.addVertex(relationVertex);

                relationVertex.setGraph(graph);

                graph.getRelationToVertex().put(relationVertex, new HashSet<>());
                graph.getRelationToVertex().get(relationVertex).add(entity1);
                graph.getRelationToVertex().get(relationVertex).add(entity2);

                //init Edge
                Edge entityEdge1 = new Edge(edgeId++, relationVertex, entity1, attr + "-source");
                Edge entityEdge2 = new Edge(edgeId++, relationVertex, entity2, attr + "-target");
                graph.addEdge(relationVertex, entity1, entityEdge1);
                graph.addEdge(relationVertex, entity2, entityEdge2);


            }
            bufferedReader.close();


        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public HashMap<String, HashSet<Vertex>> readMatch(Integer match_order) {
        InputStream inputStream;
        HashMap<String, HashSet<Vertex>> mergeVertexToVertex = new HashMap<>();
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
                String vertexKey = line.split("\\|")[0];
                String mergeVertexId = line.split("\\|")[1];
                Vertex vertex = vertexHashMap.get(vertexKey);
                if (vertex == null) {
                    System.out.println("vertex not exists");
                }
                if (!mergeVertexToVertex.containsKey(mergeVertexId)) {
                    mergeVertexToVertex.put(mergeVertexId, new HashSet<>());
                }
                mergeVertexToVertex.get(mergeVertexId).add(vertex);
            }
            bufferedReader.close();

        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return mergeVertexToVertex;
    }


}
