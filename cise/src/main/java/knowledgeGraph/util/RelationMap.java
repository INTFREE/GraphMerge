package knowledgeGraph.util;

import knowledgeGraph.ga.VertexSimilarity;
import knowledgeGraph.wordSim.WordEmbedding;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.neo4j.register.Register;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class RelationMap {
    HashMap<String, String> relationMap; // 直接这么操作会溢出
    HashMap<String, HashMap<String, String>> fileToValueMap;
    static WordEmbedding wordEmbedding;

    RelationMap() {
        this.relationMap = new HashMap<>();
        this.fileToValueMap = new HashMap<>();
    }

    public static void main(String argv[]) {
        wordEmbedding = new WordEmbedding();
        wordEmbedding.setEmbedding();
        RelationMap relationMap = new RelationMap();
        HashSet<String> relation_1 = relationMap.readRelationData("1");
        HashSet<String> relation_2 = relationMap.readRelationData("2");
        System.out.println(relation_1.size());
        System.out.println(relation_2.size());
        HashSet<String> same_value = new HashSet<>(relation_1);
        same_value.retainAll(relation_2);
        System.out.println(same_value);
        relation_1.removeAll(same_value);
        relation_2.removeAll(same_value);
        System.out.println(relation_1.size());
        System.out.println(relation_2.size());
        relationMap.produceRelationMap(relation_1, relation_2);
    }

    public HashSet<String> readRelationData(String file_name) {
        InputStream inputStream;
        HashSet<String> relation = new HashSet<>();
        HashMap<String, String> valueToKey = new HashMap<>();
        try {
            // read embedding file
            File relation_file = new File(System.getProperty("user.dir") + "/relation_" + file_name + "_map");
            inputStream = new FileInputStream(relation_file);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] relations = line.split("\t");
                relation.add(relations[1]);
                System.out.println(relations[1]);
                if (valueToKey.containsKey(relations[1])) {
                    System.out.println("same key " + relations[1]);
                }
                valueToKey.put(relations[1], relations[0]);
            }
            fileToValueMap.put(file_name, valueToKey);
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return relation;
    }

    public void produceRelationMap(HashSet<String> relation_set_1, HashSet<String> relation_set_2) {
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> bigraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (String relation_1 : relation_set_1) {
            bigraph.addVertex(relation_1);
        }
        for (String relation_2 : relation_set_2) {
            bigraph.addVertex(relation_2);
        }
        HashMap<String, double[]> wordToEmbedding = new HashMap<>();
        for (String relation : relation_set_1) {
            wordToEmbedding.put(relation, getRelationEmbedding(relation));
        }
        for (String relation : relation_set_2) {
            wordToEmbedding.put(relation, getRelationEmbedding(relation));
        }
        for (String relation_1 : relation_set_1) {
            double[] embedding_1 = wordToEmbedding.get(relation_1);
            for (String relation_2 : relation_set_2) {
                double[] embedding_2 = wordToEmbedding.get(relation_2);
                double similarity = calculateSimilarity(embedding_1, embedding_2);

                bigraph.setEdgeWeight(bigraph.addEdge(relation_1, relation_2), similarity);
            }
        }
        System.out.println("bigraph info");
        System.out.println(bigraph.vertexSet().size());
        System.out.println(bigraph.edgeSet().size());
        MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(bigraph, relation_set_1, relation_set_2);
        for (DefaultWeightedEdge edge : matching.getMatching().getEdges()) {
            System.out.println(bigraph.getEdgeSource(edge) + "\t" + bigraph.getEdgeTarget(edge));
        }
    }

    public double[] getRelationEmbedding(String relation) {
        String[] relations = relation.split(" ");
        double[] embedding = new double[200];
        for (String tmp : relations) {
            double[] tmp_embedding = wordEmbedding.getWordEmbedding(tmp);
            for (int i = 0; i < 200; i++) {
                embedding[i] += tmp_embedding[i];
            }
        }
        return embedding;
    }

    public double calculateSimilarity(double[] array1, double[] array2) {
        if (array1.length != array2.length) {
            System.out.println("calculate Similarity error: array size not equal");
            return 0;
        }
        double multiple_ans = 0.0;
        double sql_1 = 0.0, sql_2 = 0.0;
        for (int i = 0; i < array1.length; i++) {
            multiple_ans += array1[i] * array2[i];
            sql_1 += array1[i] * array1[i];
            sql_2 += array2[i] * array2[i];
        }
        if (sql_1 == 0.0 || sql_2 == 0.0) {
            return 0;
        }
        return multiple_ans / (Math.sqrt(sql_1) * Math.sqrt(sql_2));

    }
}
