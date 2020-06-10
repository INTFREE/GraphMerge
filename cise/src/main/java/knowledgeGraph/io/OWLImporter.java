package knowledgeGraph.io;


import knowledgeGraph.ExperimentMain;
import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.util.*;

public class OWLImporter implements BasicImporter {
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

    public OWLImporter(String dirName) {
        this.dirName = dirName + "/";
        valueToVertex = new HashMap<>();
        valueToId = new HashMap<>();
        keyToVertex = new HashMap<>();
        keyToId = new HashMap<>();
    }

    public Graph readGraph(Integer order, String fileName) {
        this.graph = new Graph(order.toString());
        HashMap<String, Vertex> keyToValueVertex = new HashMap<>();

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        try {
            ontModel.read(new FileInputStream(dirName + fileName + ".owl"), "");
        } catch (Exception e) {
            System.out.println("read owl error " + e);
        }

        // init entity and name
        for (Iterator i = ontModel.listClasses(); i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            if (c.getLabel("") == null) {
                continue;
            }
            String vertexKey = c.getLocalName();
            String vertexName = String.join(" ", c.getLabel("").toLowerCase().split("_"));
            // init entity
            Vertex entity = new Vertex(vertexId++, "Entity", vertexName);
            keyToId.put(vertexKey, entity.getId());
            keyToVertex.put(vertexKey, entity);
            dealVertexName(vertexName, entity);
            // init value
            if (!this.valueToId.containsKey(vertexName)) {
                this.valueToId.put(vertexName, vertexId++);
            }
            Integer id = valueToId.get(vertexName);
            Vertex valueVertex;
            if (!this.valueToVertex.containsKey(vertexName)) {
                valueVertex = new Vertex(id, "Value", vertexName);
                this.valueToVertex.put(vertexName, valueVertex);
            }
            valueVertex = this.valueToVertex.get(vertexName);

            Vertex relation = new Vertex(vertexId++, "Relation", "name");
            addVertexToGraph(entity, relation, valueVertex);

        }
        // init value Vertex

        for (Iterator r = ontModel.listStatements(new SimpleSelector() {
            @Override
            public boolean selects(Statement s) {
                if (s.getSubject().getURI() == null) {
                    return false;
                }
                return s.getSubject().getURI().matches(".*?genid.*?");
            }
        }); r.hasNext(); ) {
            Statement s = (Statement) r.next();
            String key = s.getSubject().getURI();
            String value = s.getString().toLowerCase();
            if (!this.valueToId.containsKey(value)) {
                this.valueToId.put(value, vertexId++);
            }
            Integer id = valueToId.get(value);
            Vertex valueVertex;
            if (!this.valueToVertex.containsKey(value)) {
                valueVertex = new Vertex(id, "Value", value);
                this.valueToVertex.put(value, valueVertex);
            }
            valueVertex = this.valueToVertex.get(value);
            graph.addVertex(valueVertex);
            valueVertex.setGraph(graph);
            keyToValueVertex.put(key, valueVertex);
        }

        // init
        int count_relation = 0;
        int count_attr = 0;
        for (Iterator i = ontModel.listClasses(); i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            if (c.getLabel("") == null) {
                continue;
            }
            Set<OntProperty> a = c.listDeclaredProperties().toSet();
            String vertexKey = c.getLocalName();
            Vertex entity = keyToVertex.get(vertexKey);
            for (Iterator it = c.listSuperClasses(); it.hasNext(); ) {
                OntClass sp = (OntClass) it.next();
                if (sp.isRestriction()) {
                    Restriction r = sp.asRestriction();
                    if (r.isSomeValuesFromRestriction()) {
                        SomeValuesFromRestriction temp = r.asSomeValuesFromRestriction();
                        Vertex target = keyToVertex.get(temp.getSomeValuesFrom().getURI().split("#")[1]);
                        if (target == null) {
//                            System.out.println("target error " + entity.getValue() + "\t" + temp.getSomeValuesFrom().getURI());
                            continue;
                        }
                        count_relation += 1;
                        Vertex relation = new Vertex(vertexId++, "Relation", temp.getOnProperty().getURI());
                        addVertexToGraph(entity, relation, target);
                    } else {
                        System.out.println("not exists");
                    }
                } else {
                    String key = sp.getURI().split("#")[1];
                    Vertex target = keyToVertex.get(key);
                    if (target == null) {
//                        System.out.println("target error " + entity.getValue() + "\t" + sp);
                        continue;
                    }
                    count_relation += 1;
                    Vertex relation = new Vertex(vertexId++, "Relation", "subclass");
                    addVertexToGraph(entity, relation, target);
                }
            }
            for (Iterator it = c.listProperties(); it.hasNext(); ) {
                Statement st = ((StmtIterator) it).next();
                try {
                    if (a.contains(st.getPredicate())) {
                        Vertex value = keyToValueVertex.get(st.getResource().toString());
                        if (value == null) {
                            System.out.println("value error " + entity.getValue() + "\t" + st.getResource());
                        }
                        count_attr += 1;
                        Vertex relation = new Vertex(vertexId++, "Relation", st.getPredicate().toString());
                        addVertexToGraph(entity, relation, value);
                    }
                } catch (Exception e) {
//                    System.out.println("read proterty error " + e);
                }
            }
        }
        System.out.println("relation node " + count_relation);
        System.out.println("attr node " + count_attr);
        return graph;
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

    private void dealVertexName(String vertexName, Vertex entity) {
        allWords = Arrays.asList(vertexName.split(" "));
        for (String word : allWords) {
            String keyword = word.replaceAll(regex, "").replaceAll(regexEnd, "");
            this.graph.addKeyWord(keyword, entity);
        }
    }

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
                String vertexName1 = line1.split("\t")[0].split("#")[1];
                String vertexName2 = line1.split("\t")[1].split("#")[1];
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

    public static void main(String argv[]) throws IOException {
        OWLImporter importer = new OWLImporter("anatomy-dataset");
        importer.readGraph(1, "1.owl");
        importer.readGraph(2, "2.owl");
        importer.readAns();
//        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//        ontModel.read(new FileInputStream("anatomy-dataset/1.owl"), "");
//        System.out.println(ontModel.listClasses().toList().size());
//        for (Iterator i = ontModel.listClasses(); i.hasNext(); ) {
//            OntClass c = (OntClass) i.next();
//            Set<OntProperty> a = c.listDeclaredProperties().toSet();
//            if (c.getLabel("") == null) {
//                continue;
//            }
//            if (!c.getLabel("").equalsIgnoreCase("Ear_Skin")) {
//                continue;
//            }
//            System.out.println(c.getLocalName());
//            System.out.println(c.getLocalName());
//            String name = c.getLabel("").toLowerCase();
//            System.out.println(String.join(" ", name.split("_")));
//            System.out.println(c.getLabel(""));
//            for (Iterator it = c.listSuperClasses(); it.hasNext(); ) {
//                OntClass sp = (OntClass) it.next();
//                System.out.println(sp);
//                if (sp.isRestriction()) {
//                    System.out.println("res");
//                    Restriction r = sp.asRestriction();
//                    if (r.isSomeValuesFromRestriction()) {
//                        SomeValuesFromRestriction temp = r.asSomeValuesFromRestriction();
//                        System.out.println(temp.getSomeValuesFrom().getURI() + "\t" + temp.getOnProperty().getURI());
//                    } else {
//                        System.out.println("not exists");
//                    }
//                } else {
//                    System.out.println("else");
//                    String strSP = sp.getURI();
//                    System.out.println(strSP);
//                }
//            }
//            System.out.println("test");
//            for (Iterator it = c.listProperties(); it.hasNext(); ) {
//                Statement st = ((StmtIterator) it).next();
//                if (a.contains(st.getPredicate())) {
//                    System.out.println(st);
//                }
//            }
//        }
    }

}
