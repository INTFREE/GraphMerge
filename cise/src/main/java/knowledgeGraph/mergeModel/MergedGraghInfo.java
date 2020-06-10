package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.*;
import knowledgeGraph.ga.VertexSimilarity;
import knowledgeGraph.util.UtilFunction;

import java.io.*;
import java.util.*;

public class MergedGraghInfo {
    private GraphsInfo graphsInfo;
    private MergedGraph mergedGraph;
    public boolean isChanged = true;

    private Map<String, Set<MergedVertex>> typeToVertexSetMap;
    /**
     * 被融合图中的节点到融合图中节点的映射，即：被融合图中的某个节点属于融合图中的哪个节点
     */
    private Map<Vertex, MergedVertex> vertexToMergedVertexMap;

    /**
     * 被融合图中的边到融合图中边的映射，即：被融合图中的某个边属于融合图中的哪个边
     */
    private Map<Edge, MergedEdge> edgeToMergedEdgeMap;

    /**
     * 融合图的熵
     */
    double entropy = -1;

    /**
     * 融合图是否是二部图
     */
    boolean isBiGraph = false;

    /**
     * 融合图所需要的二部图
     */
    Bigraph biGraph;

    /**
     * 按熵值大小排序的MergedVertexToEntropy
     */
    List<Map.Entry<MergedVertex, Double>> mergedVertexToEntropy;

    List<MergedVertex> mergedVertexListSortedByEntropy;

    /**
     * 构造函数，接收一组待融合图信息作为参数
     *
     * @param graphsInfo 待融合图信息
     */
    public MergedGraghInfo(GraphsInfo graphsInfo) {
        this.graphsInfo = graphsInfo;
        this.mergedGraph = new MergedGraph();
        typeToVertexSetMap = new HashMap<>();
        vertexToMergedVertexMap = new HashMap<>();
        edgeToMergedEdgeMap = new HashMap<>();
        mergedVertexToEntropy = new ArrayList<>();
        mergedVertexListSortedByEntropy = new ArrayList<>();
    }

    public MergedGraghInfo(GraphsInfo graphsInfo, boolean isBiGraph) {
        this.graphsInfo = graphsInfo;
        this.mergedGraph = new MergedGraph();
        this.isBiGraph = isBiGraph;
        typeToVertexSetMap = new HashMap<>();
        vertexToMergedVertexMap = new HashMap<>();
        edgeToMergedEdgeMap = new HashMap<>();
        if (this.isBiGraph) {
            biGraph = new Bigraph();
        }
        mergedVertexListSortedByEntropy = new ArrayList<>();
        mergedVertexToEntropy = new ArrayList<>();
    }

    public MergedGraghInfo(MergedGraph mergedGraph) {
        System.out.println(">>>>>> Initialize MergedGraphInfo");
        this.mergedGraph = mergedGraph;
        mergedVertexToEntropy = new ArrayList<>();
        typeToVertexSetMap = new HashMap<>();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (!typeToVertexSetMap.containsKey(mergedVertex.getType())) {
                typeToVertexSetMap.put(mergedVertex.getType(), new HashSet<>());
            }
            typeToVertexSetMap.get(mergedVertex.getType()).add(mergedVertex);
        }
        for (String type : typeToVertexSetMap.keySet()) {
            System.out.println("type: " + type + " " + typeToVertexSetMap.get(type).size());
        }
        edgeToMergedEdgeMap = new HashMap<>();
        for (MergedEdge mergedEdge : mergedGraph.edgeSet()) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                edgeToMergedEdgeMap.put(edge, mergedEdge);
            }
        }
        mergedVertexListSortedByEntropy = new ArrayList<>();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            mergedVertexListSortedByEntropy.add(mergedVertex);
        }
    }

    public double getEntropy() {
        return entropy;
    }

    public void setMergedGraph(MergedGraph mergedGraph) {
        this.mergedGraph = mergedGraph;
    }

    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public Bigraph getBiGraph() {
        return biGraph;
    }

    public Set<MergedVertex> getMergedVertexByType(String type) {
        if (typeToVertexSetMap.containsKey(type)) {
            return typeToVertexSetMap.get(type);
        }
        return new HashSet<>();
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public void setTypeToVertexSetMap(Map<String, Set<MergedVertex>> typeToVertexSetMap) {
        this.typeToVertexSetMap = typeToVertexSetMap;
    }

    public GraphsInfo getGraphsInfo() {
        return graphsInfo;
    }

    public Set<String> getAllTypes() {
        return this.typeToVertexSetMap.keySet();
    }

    public boolean isBiGraph() {
        return isBiGraph;
    }

    public void generateInitialMergeGraph() {
        this.mergedGraph = new MergedGraph();
        Set<MergedVertex> mergedVertexSet = new HashSet<>();
        // 随机生成初始融合图
        for (String type : this.graphsInfo.getVertexTypeSet()) {
            if (type.equalsIgnoreCase("Value")) {
                continue;
            }
            System.out.println(type);
            Set<MergedVertex> tmpMergedVertexSet = new HashSet<>();
            Set<Vertex> vertexSet = this.graphsInfo.getTypeToVertexSetMap().get(type);
            for (Vertex vertex : vertexSet) {
                MergedVertex mergedVertex = new MergedVertex(type);
                HashSet<MergedVertex> tmp = new HashSet<>();
                tmp.add(mergedVertex);
                // 来自同一图的点不能融合在同一节点
                for (MergedVertex mergedVertex1 : tmpMergedVertexSet) {
                    boolean flag = true;
                    for (Vertex vertex1 : mergedVertex1.getVertexSet()) {
                        if (vertex1.getGraph().equals(vertex.getGraph())) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        tmp.add(mergedVertex1);
                    }
                }
                // 随机挑选一个节点融合
                MergedVertex randomChose = new UtilFunction.CollectionUtil<MergedVertex>()
                        .pickRandom(tmp);
                randomChose.getVertexSet().add(vertex);
                vertex.setMergedVertex(randomChose);
                if (!tmpMergedVertexSet.contains(randomChose)) {
                    tmpMergedVertexSet.add(randomChose);
                }
            }
            mergedVertexSet.addAll(tmpMergedVertexSet);
        }
        // 对于每个value节点，单独生成一个融合节点
        Set<Vertex> vertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Value");
        for (Vertex vertex : vertexSet) {
            MergedVertex mergedVertex = new MergedVertex("Value");
            mergedVertex.getVertexSet().add(vertex);
            vertex.setMergedVertex(mergedVertex);
            mergedVertexSet.add(mergedVertex);
        }

        Set<Graph> graphSet = this.graphsInfo.getGraphSet();
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        for (Graph graph : graphSet) {
            for (Edge edge : graph.edgeSet()) {
                boolean flag = false;
                for (MergedEdge mergedEdge : mergedEdgeSet) {
                    MergedVertex source = mergedEdge.getSource();
                    MergedVertex target = mergedEdge.getTarget();
                    if (source.containsVertex(edge.getSource()) && target.containsVertex(edge.getTarget())) {
                        mergedEdge.addEdge(edge);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    MergedVertex source = null;
                    MergedVertex target = null;
                    for (MergedVertex mergedVertex : mergedVertexSet) {
                        if (mergedVertex.containsVertex(edge.getSource())) {
                            source = mergedVertex;
                        }
                        if (mergedVertex.containsVertex(edge.getTarget())) {
                            target = mergedVertex;
                        }
                    }
                    if (source == null || target == null) {
                        System.out.println("edge has no source or target");
                    } else {
                        MergedEdge mergedEdge = new MergedEdge(source, target, edge.getRoleName());
                        mergedEdge.addEdge(edge);
                        mergedEdgeSet.add(mergedEdge);
                    }
                }
            }
        }
        for (MergedVertex mergedVertex : mergedVertexSet) {
            this.mergedGraph.addVertex(mergedVertex);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            this.mergedGraph.addEdge(mergedEdge.getSource(), mergedEdge.getTarget(), mergedEdge);
        }
        for (MergedVertex mergedVertex : mergedVertexSet) {
            mergedVertex.setMergedGraph(this.mergedGraph);
        }
        for (MergedEdge mergedEdge : mergedEdgeSet) {
            mergedEdge.setMergedGraph(this.mergedGraph);
        }
    }

    public void generateMergeGraphByMatch(HashMap<String, HashSet<Vertex>> mergeVertexToVertex) {
        this.mergedGraph = new MergedGraph();
        Set<MergedVertex> mergedVertexSet = new HashSet<>();
        // initialize entity mergeVertex according to given match.
        for (String name : mergeVertexToVertex.keySet()) {
            HashSet<Vertex> vertices = mergeVertexToVertex.get(name);
            MergedVertex mergedVertex = new MergedVertex(vertices, "Entity", name);
            for (Vertex vertex : vertices) {
                vertex.setMergedVertex(mergedVertex);
            }
            mergedVertexSet.add(mergedVertex);
        }
        System.out.println("initialize entity vertex");
        // initialize value mergeVertex
        Set<Vertex> valueVertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Value");
        HashMap<String, HashSet<Vertex>> valueMergeVertexSet = new HashMap<>();

        for (Vertex vertex : valueVertexSet) {
            if (!valueMergeVertexSet.keySet().contains(vertex.getValue())) {
                valueMergeVertexSet.put(vertex.getValue(), new HashSet<>());

            }
            valueMergeVertexSet.get(vertex.getValue()).add(vertex);
        }
        for (String value : valueMergeVertexSet.keySet()) {
            MergedVertex mergedVertex = new MergedVertex(valueMergeVertexSet.get(value), "Value", value);
            for (Vertex vertex : valueMergeVertexSet.get(value)) {
                vertex.setMergedVertex(mergedVertex);
            }
            mergedVertexSet.add(mergedVertex);
        }
        System.out.println("initialize value vertex");
        // initialize relation mergeVertex. use the attr value.
        Set<Vertex> relationVertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Relation");
        HashSet<MergedVertex> relationMergeVertexSet = new HashSet<>();
        for (Vertex vertex : relationVertexSet) {
            boolean flag = false;
            HashSet<Vertex> connectedVertexSet = vertex.getGraph().getRelationToVertex().get(vertex);
            HashSet<MergedVertex> connectedMergeVertexSet = new HashSet<>();
            for (Vertex vertex1 : connectedVertexSet) {
                connectedMergeVertexSet.add(vertex1.getMergedVertex());
            }
            for (MergedVertex mergedVertex : relationMergeVertexSet) {
                if (!mergedVertex.getName().equalsIgnoreCase(vertex.getValue())) {
                    continue;
                }
                Vertex tempVertex = mergedVertex.getVertexSet().iterator().next();
                HashSet<Vertex> tempVertexSet = tempVertex.getGraph().getRelationToVertex().get(tempVertex);
                HashSet<MergedVertex> temp = new HashSet<>();
                for (Vertex vertex1 : tempVertexSet) {
                    temp.add(vertex1.getMergedVertex());
                }
                if (temp.containsAll(connectedMergeVertexSet)) {
                    flag = true;
                    mergedVertex.addVertex(vertex);
                    vertex.setMergedVertex(mergedVertex);
                    break;
                }
            }
            if (!flag) {
                MergedVertex mergedVertex = new MergedVertex("Relation");
                mergedVertex.setName(vertex.getValue());
                mergedVertex.addVertex(vertex);
                vertex.setMergedVertex(mergedVertex);
                relationMergeVertexSet.add(mergedVertex);
            }

        }
        mergedVertexSet.addAll(relationMergeVertexSet);
        System.out.println("initialize relation vertex");
        //Generate mergeEdge
        HashSet<MergedEdge> mergedEdgeHashSet = new HashSet<>();

        for (Graph graph : this.getGraphsInfo().getGraphSet()) {
            for (Edge edge : graph.edgeSet()) {
                boolean flag = false;
                for (MergedEdge mergedEdge : mergedEdgeHashSet) {
                    MergedVertex source = mergedEdge.getSource();
                    MergedVertex target = mergedEdge.getTarget();
                    if (source.getVertexSet().contains(edge.getSource()) && target.getVertexSet().contains(edge.getTarget())) {
                        flag = true;
                        mergedEdge.addEdge(edge);
                        break;
                    }
                }
                if (!flag) {
                    MergedEdge mergedEdge = new MergedEdge(edge.getSource().getMergedVertex(), edge.getTarget().getMergedVertex(), edge.getRoleName());
                    mergedEdge.addEdge(edge);
                    mergedEdgeHashSet.add(mergedEdge);
                }
            }
        }
        for (MergedEdge mergedEdge : mergedEdgeHashSet) {
            this.mergedGraph.addEdge(mergedEdge.getSource(), mergedEdge.getTarget(), mergedEdge);
        }
        System.out.println(this.mergedGraph.vertexSet().size());
        System.out.println(this.mergedGraph.edgeSet().size());
    }

    public void generateMergeGraphByMatch2() {
        this.mergedGraph = new MergedGraph();
        Set<MergedVertex> mergedVertexSet = new HashSet<>();
        HashSet<MergedEdge> mergedEdgeHashSet = new HashSet<>();

        // initialize entity mergeVertex
        this.typeToVertexSetMap.put("Entity", new HashSet<>());
        Set<Vertex> entityVertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Entity");
        System.out.println("entity vertex set size " + entityVertexSet.size());
        HashMap<Integer, HashSet<Vertex>> entityMergeVertexSet = new HashMap<>();
        for (Vertex vertex : entityVertexSet) {
            if (!entityMergeVertexSet.keySet().contains(vertex.getId())) {
                entityMergeVertexSet.put(vertex.getId(), new HashSet<>());
            }
            entityMergeVertexSet.get(vertex.getId()).add(vertex);
        }
        for (Integer id : entityMergeVertexSet.keySet()) {
            MergedVertex mergedVertex = new MergedVertex(entityMergeVertexSet.get(id), "Entity", "");

            for (Vertex vertex : entityMergeVertexSet.get(id)) {
                vertex.setMergedVertex(mergedVertex);
            }

            mergedVertexSet.add(mergedVertex);
            this.typeToVertexSetMap.get("Entity").add(mergedVertex);
        }

        System.out.println("initialize entity vertex " + this.typeToVertexSetMap.get("Entity").size());

        // initialize value mergeVertex
        this.typeToVertexSetMap.put("Value", new HashSet<>());
        Set<Vertex> valueVertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Value");
        HashMap<Integer, HashSet<Vertex>> valueMergeVertexSet = new HashMap<>();
        for (Vertex vertex : valueVertexSet) {
            if (!valueMergeVertexSet.keySet().contains(vertex.getId())) {
                valueMergeVertexSet.put(vertex.getId(), new HashSet<>());
            }
            valueMergeVertexSet.get(vertex.getId()).add(vertex);
        }
        for (Integer id : valueMergeVertexSet.keySet()) {
            MergedVertex mergedVertex = new MergedVertex(valueMergeVertexSet.get(id), "Value", "");
            for (Vertex vertex : valueMergeVertexSet.get(id)) {
                vertex.setMergedVertex(mergedVertex);
            }
            mergedVertexSet.add(mergedVertex);
            this.typeToVertexSetMap.get("Value").add(mergedVertex);
        }
        System.out.println("initialize value vertex " + this.typeToVertexSetMap.get("Value").size());

        // initialize relation mergeVertex
        this.typeToVertexSetMap.put("Relation", new HashSet<>());
        Set<Vertex> relationVertexSet = this.graphsInfo.getTypeToVertexSetMap().get("Relation");
        HashMap<Integer, HashSet<Vertex>> relationMergeVertexSet = new HashMap<>();
        for (Vertex vertex : relationVertexSet) {
            if (!relationMergeVertexSet.keySet().contains(vertex.getId())) {
                relationMergeVertexSet.put(vertex.getId(), new HashSet<>());
            }
            relationMergeVertexSet.get(vertex.getId()).add(vertex);
        }
        for (Integer id : relationMergeVertexSet.keySet()) {
            MergedVertex mergedVertex = new MergedVertex(relationMergeVertexSet.get(id), "Relation", "");

            for (Vertex vertex : relationMergeVertexSet.get(id)) {
                vertex.setMergedVertex(mergedVertex);
            }
            this.typeToVertexSetMap.get("Relation").add(mergedVertex);
            mergedVertexSet.add(mergedVertex);
        }
        System.out.println("initialize relation vertex"+ this.typeToVertexSetMap.get("Relation").size());

        for (MergedVertex mergedVertex : mergedVertexSet) {
            this.mergedGraph.addVertex(mergedVertex);
        }

        //Generate mergeEdge
        HashMap<Integer, HashSet<Edge>> mergeEdgeSet = new HashMap<>();
        for (Graph graph : this.getGraphsInfo().getGraphSet()) {
            for (Edge edge : graph.edgeSet()) {
                if (!mergeEdgeSet.keySet().contains(edge.getId())) {
                    mergeEdgeSet.put(edge.getId(), new HashSet<>());
                }
                mergeEdgeSet.get(edge.getId()).add(edge);
            }
        }
        for (Integer id : mergeEdgeSet.keySet()) {
            Edge edge = mergeEdgeSet.get(id).iterator().next();
            MergedVertex s = edge.getSource().getMergedVertex();
            MergedVertex t = edge.getTarget().getMergedVertex();
            String r = edge.getRoleName();
            MergedEdge mergedEdge = new MergedEdge(s, t, r);
            mergedEdge.addAllEdge(mergeEdgeSet.get(id));
            mergedEdgeHashSet.add(mergedEdge);
        }

        for (MergedEdge mergedEdge : mergedEdgeHashSet) {
            this.mergedGraph.addEdge(mergedEdge.getSource(), mergedEdge.getTarget(), mergedEdge);
        }

        System.out.println("merge graph info");
        System.out.println("vertexSet size " + this.mergedGraph.vertexSet().size());
        System.out.println("edgeSet size " + this.mergedGraph.edgeSet().size());
        for (String type : typeToVertexSetMap.keySet()) {
            System.out.println(type + " " + typeToVertexSetMap.get(type).size());
        }


    }

    public List<Map.Entry<MergedVertex, Double>> getMergedVertexToEntropy() {
        return mergedVertexToEntropy;
    }

    public Integer getMergedVertexIndexInEntropy(MergedVertex mergedVertex) {
        return mergedVertexListSortedByEntropy.indexOf(mergedVertex);
    }

    public double calculateEntropy(HashSet<Integer> ids) {
        double res = 0.0;
        for (Map.Entry<MergedVertex, Double> entry : mergedVertexToEntropy) {
            if (ids.contains(entry.getKey().getId())) {
                res += entry.getValue();
            }
        }
        return res;
    }

    public void setMergedVertexToEntropy(HashMap<MergedVertex, Double> mergedVertexToEntropy) {
        List<Map.Entry<MergedVertex, Double>> sortedEntropy =
                new ArrayList<>(mergedVertexToEntropy.entrySet());
        Collections.sort(sortedEntropy, new Comparator<Map.Entry<MergedVertex, Double>>() {
            public int compare(Map.Entry<MergedVertex, Double> o1,
                               Map.Entry<MergedVertex, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        mergedVertexListSortedByEntropy.clear();
        this.mergedVertexToEntropy.clear();
        for (Map.Entry<MergedVertex, Double> entry : sortedEntropy) {
            if (entry.getKey().getType().equalsIgnoreCase("entity")) {
                mergedVertexListSortedByEntropy.add(entry.getKey());
            }
        }
        System.out.println("entropy array size : " + mergedVertexListSortedByEntropy.size());
        this.mergedVertexToEntropy = sortedEntropy;
    }

    public void saveEntropy(String file_name) throws IOException {
        File file = new File(file_name);
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

        for (Map.Entry<MergedVertex, Double> entry : mergedVertexToEntropy) {
            if (entry.getKey().getVertexSet().size() == 2) {
                Iterator<Vertex> it = entry.getKey().getVertexSet().iterator();
                Vertex vertex1 = it.next();
                Vertex vertex2 = it.next();
                writer.write(entry.getKey().getId() + "\t" + entry.getValue() + "\t"
                        + vertex1.getValue() + "\t" + vertex2.getValue() + "\t"
                        + VertexSimilarity.getEditDistance(vertex1.getValue(), vertex2.getValue()) + "\t"
                        + VertexSimilarity.getEmbeddingSimilarity(vertex1, vertex2) + "\n");
            }
        }
        writer.close();
        os.close();
    }

    public void saveDetailEntropy(String file_name) throws IOException {
        File file = new File(file_name);
        FileOutputStream os = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        int count = 0;
        for (Map.Entry<MergedVertex, Double> entry : mergedVertexToEntropy) {
            writer.write(entry.getKey().getId() + "\t" + entry.getValue() + "\t" + count + "\t" + entry.getKey().getVertexSet().size() + "\n");
            for (Vertex vertex : entry.getKey().getVertexSet()) {
                writer.write(vertex.getGraph().getUserName() + "\t" + vertex.getId() + "\n");
            }
            count += 1;
        }
        writer.close();
        os.close();
    }

}
