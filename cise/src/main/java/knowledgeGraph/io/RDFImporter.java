package knowledgeGraph.io;

import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.util.*;

public class RDFImporter implements BasicImporter {
    HashMap<String, Vertex> keyToVertex;
    HashMap<String, Vertex> valueToVertex;
    HashMap<String, Integer> keyToId;
    String dirName;
    Integer vertexId = 1;
    Integer edgeId = 1;
    HashMap<String, Integer> valueToId;
    List<String> allWords;
    String regex = "^[(,!:]+";
    String regexEnd = "[),!:]+$";
    String pattern = "\\(.*?\\)";
    Graph graph;

    public RDFImporter(String dirName) {
        this.dirName = dirName + "/";
        valueToVertex = new HashMap<>();
        valueToId = new HashMap<>();
        keyToVertex = new HashMap<>();
        keyToId = new HashMap<>();
    }

    private void dealVertexName(String vertexName, Vertex entity) {
        allWords = Arrays.asList(vertexName.split("[_]"));
        for (String word : allWords) {
            String keyword = word.replaceAll(regex, "").replaceAll(regexEnd, "");
            this.graph.addKeyWord(keyword, entity);
        }
    }

    public Graph readGraph(Integer order, String fileName) {
        this.graph = new Graph(order.toString());
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(dirName + fileName);
        model.read(in, "");
        System.out.println("object length " + model.listSubjects().toList().size());
        // init entity vertex and name
        for (Iterator it = model.listSubjects(); it.hasNext(); ) {
            Resource entityResource = (Resource) it.next();
            String vertexKey = entityResource.getURI();
            if (vertexKey.contains(".png") || vertexKey.contains(".jpg") || vertexKey.contains(".jpeg") || vertexKey.contains(".gif")) {
                continue;
            }
            String vertexName = vertexKey.split("/")[vertexKey.split("/").length - 1];
            vertexName = vertexName.toLowerCase();
            Vertex entity = new Vertex(vertexId++, "Entity", vertexName);
            dealVertexName(vertexName, entity);
            keyToId.put(vertexKey, entity.getId());
            keyToVertex.put(vertexKey, entity);
            if (!this.valueToId.containsKey(vertexName)) {
                this.valueToId.put(vertexName, vertexId++);
            }
            Integer valueId = valueToId.get(vertexName);
            Vertex valueVertex;
            if (!this.valueToVertex.containsKey(vertexName)) {
                valueVertex = new Vertex(valueId, "Value", vertexName);
                this.valueToVertex.put(vertexName, valueVertex);
            }
            valueVertex = this.valueToVertex.get(vertexName);

            Vertex relation = new Vertex(vertexId++, "Relation", "name");
            addVertexToGraph(entity, relation, valueVertex);
        }

        for (Iterator it = model.listSubjects(); it.hasNext(); ) {
            Resource entityResource = (Resource) it.next();
            String vertexKey = entityResource.getURI();
            Vertex entity = keyToVertex.get(vertexKey);
            if (entity == null) {
                continue;
            }
            // add relation
            for (Iterator it2 = entityResource.listProperties(); it2.hasNext(); ) {
                Statement property = (Statement) it2.next();
                RDFNode object = property.getObject();
                if (object instanceof Resource) {
                    Vertex target = this.keyToVertex.get(((Resource) object).getURI());
                    if (target == null) {
                        //System.out.println("empty entity error: " + entity.getValue() + "\t" + ((Resource) object).getURI());
                        continue;
                    }
                    Vertex relation = new Vertex(vertexId++, "Relation", property.getPredicate().getLocalName() + "-relation");
                    addVertexToGraph(entity, relation, target);
                } else {
                    String value = object.toString();
                    if (value.length() > 1500) {
                        continue;
                    }
                    if (!this.valueToId.containsKey(value)) {
                        this.valueToId.put(value, vertexId++);
                    }
                    Integer valueId = valueToId.get(value);
                    Vertex valueVertex;
                    if (!this.valueToVertex.containsKey(value)) {
                        valueVertex = new Vertex(valueId, "Value", value);
                        this.valueToVertex.put(value, valueVertex);
                    }
                    Vertex relation = new Vertex(vertexId++, "Relation", property.getPredicate().getLocalName() + "-property");
                    valueVertex = this.valueToVertex.get(value);
                    addVertexToGraph(entity, relation, valueVertex);
                }
            }
        }

        return this.graph;
    }

    @Override
    public void readAns() {
        try {
            // read vertex file
            String vertexFileName = dirName + "reference";
            File vertexFile1 = new File(vertexFileName);

            InputStream inputStream1 = new FileInputStream(vertexFile1);
            Reader reader1 = new InputStreamReader(inputStream1);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
            String line1;

            while ((line1 = bufferedReader1.readLine()) != null) {
                String vertexName1 = line1.split("\t")[0];
                String vertexName2 = line1.split("\t")[1];
                if (!keyToId.containsKey(vertexName1) || !keyToId.containsKey(vertexName2)) {
                    System.out.println("read ans error");
                    continue;
                }
                if (ExperimentMain.ans.containsKey(keyToId.get(vertexName2))) {
                    System.out.println("exist id " + vertexName1);
                }
                ExperimentMain.ans.put(keyToId.get(vertexName2), keyToId.get(vertexName1));
            }
            System.out.println("finish ans read. size is : " + ExperimentMain.ans.size());
            bufferedReader1.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public void addVertexToGraph(Vertex entity, Vertex relation, Vertex value) {
        graph.addVertex(entity);
        graph.addVertex(value);
        graph.addVertex(relation);
        relation.addRelatedVertex(entity);
        relation.addRelatedVertex(value);
        entity.setGraph(graph);
        relation.setGraph(graph);
        value.setGraph(graph);
        graph.getRelationToVertex().put(relation, new HashSet<>());
        graph.getRelationToVertex().get(relation).add(entity);
        graph.getRelationToVertex().get(relation).add(value);
        Edge entityEdge = new Edge(edgeId++, relation, entity, relation.getValue() + "-source");
        Edge valueEdge = new Edge(edgeId++, relation, value, relation.getValue() + "-target");
        graph.addEdge(relation, entity, entityEdge);
        graph.addEdge(relation, value, valueEdge);
        entityEdge.setGraph(graph);
        valueEdge.setGraph(graph);
    }

    public static void main(String argv[]) throws IOException {
        RDFImporter importer = new RDFImporter("rdf-dataset");
        Graph graph1 = importer.readGraph(1, "memory-alpha.xml");
        Graph graph2 = importer.readGraph(2, "stexpanded.xml");
        graph1.print();
        graph2.print();
        importer.readAns();

    }


}
