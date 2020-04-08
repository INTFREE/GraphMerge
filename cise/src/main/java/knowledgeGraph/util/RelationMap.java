package knowledgeGraph.util;

import knowledgeGraph.ga.VertexSimilarity;
import knowledgeGraph.wordSim.RelatedWord;
import knowledgeGraph.wordSim.WordEmbedding;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class RelationMap {
    HashMap<String, String> relationMap; // 直接这么操作会溢出
    HashMap<String, HashMap<String, String>> fileToValueMap;
    static WordEmbedding wordEmbedding;

    public RelationMap() {
        this.relationMap = new HashMap<>();
        this.fileToValueMap = new HashMap<>();
    }

    public void setRelationMap() {
        readData("1");
        readData("2");
    }

    public String getRelationMap(String relation) {
        if (relationMap.size() == 0) {
            this.setRelationMap();
        }
        if (!relationMap.containsKey(relation)) {
            return relation;
        }
        return relationMap.get(relation);
    }

    public static void main(String argv[]) {
//        wordEmbedding = new WordEmbedding();
//        wordEmbedding.setEmbedding();
        RelationMap relationMap = new RelationMap();
        HashSet<String> relation_1 = relationMap.readRelationData("relation_1_map");
        HashSet<String> relation_2 = relationMap.readRelationData("relation_2_map");
        System.out.println(relation_1.size());
        System.out.println(relation_2.size());
        HashSet<String> same_value = new HashSet<>(relation_1);
        same_value.retainAll(relation_2);
        System.out.println("same size :" + same_value.size());
        HashMap<String, String> relation_map = relationMap.readFinalRelation();
////        relation_1.removeAll(same_value);
////        relation_2.removeAll(same_value);
////        HashMap<String, String> relations = relationMap.produceRelationMapByEditDistance(relation_1, relation_2);
        relationMap.writeRelationData("1", relation_map);
        relationMap.writeRelationData("2", relation_map);
    }

    public HashMap<String, String> readFinalRelation() {
        InputStream inputStream;
        HashMap<String, String> relation_map = new HashMap<>();
        try {
            // read embedding file
            File relation_file = new File(System.getProperty("user.dir") + "/relation_map");
            inputStream = new FileInputStream(relation_file);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] relations = line.split("\t");
                if (relations.length != 2) {
                    System.out.println("relation map error " + line);
                    continue;
                }
                relation_map.put(relations[0], relations[1]);
            }
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        System.out.println("relation map size " + relation_map.size());
        return relation_map;
    }

    public void writeRelationData(String file_name, HashMap<String, String> relations) {
        try {
            File file = new File(file_name);
            FileOutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            HashMap<String, String> valueToKey = fileToValueMap.get("relation_" + file_name + "_map");
            for (String value : valueToKey.keySet()) {
                if (file_name.equalsIgnoreCase("1") && relations.containsKey(value)) {
                    writer.write(valueToKey.get(value) + "\t" + relations.get(value) + "\n");
                } else {
                    writer.write(valueToKey.get(value) + "\t" + value + "\n");
                }
            }
            writer.close();
            os.close();
        } catch (Exception e) {
            System.out.println("write file error" + e.toString());
        }

    }

    public void readData(String file_name) {
        InputStream inputStream;
        try {
            File relation_file = new File(System.getProperty("user.dir") + "/" + file_name);
            inputStream = new FileInputStream(relation_file);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] relations = line.split("\t");
                if (relationMap.containsKey(relations[0])) {
                    System.out.println("same key " + relations[0]);
                }
                relationMap.put(relations[0], relations[1]);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

    public HashSet<String> readRelationData(String file_name) {
        InputStream inputStream;
        HashSet<String> relation = new HashSet<>();
        HashMap<String, String> valueToKey = new HashMap<>();
        try {
            File relation_file = new File(System.getProperty("user.dir") + "/" + file_name);
            inputStream = new FileInputStream(relation_file);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] relations = line.split("\t");
                relation.add(relations[1]);
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

    public void produceRelationMapByEmbedding(HashSet<String> relation_set_1, HashSet<String> relation_set_2) {
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

    public HashMap<String, String> produceRelationMapByEditDistance(HashSet<String> relation_set_1, HashSet<String> relation_set_2) {
        String regex = "^[(,!:]+";
        String regexEnd = "[),!:]+$";
        HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "the", "an", "of", "A", "The", "on", "with"));
        HashMap<String, HashSet<String>> keyWordToRelation = new HashMap<>();
        RelatedWord relatedWord = new RelatedWord();
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> bigraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (String relation_1 : relation_set_1) {
            bigraph.addVertex(relation_1);
        }
        for (String relation_2 : relation_set_2) {
            bigraph.addVertex(relation_2);
            String[] words = relation_2.split(" ");
            for (String word : words) {
                String temp_word = word.replaceAll(regex, "").replaceAll(regexEnd, "");
                if (!keyWordToRelation.containsKey(temp_word)) {
                    keyWordToRelation.put(temp_word, new HashSet<>());
                }
                keyWordToRelation.get(temp_word).add(relation_2);
            }
        }
        HashSet<String> relatedRelation = new HashSet<>();
        String[] relatedWords;
        for (String relation_1 : relation_set_1) {
            relatedRelation.clear();
            String[] relations = relation_1.split(" ");
            for (String relation : relations) {
                String temp_word = relation.replaceAll(regex, "").replaceAll(regexEnd, "");
                if (stopWords.contains(temp_word)) {
                    continue;
                }
                if (keyWordToRelation.containsKey(temp_word)) {
                    System.out.println("exists related word " + temp_word);
                    relatedRelation.addAll(keyWordToRelation.get(temp_word));
                }
                if (relatedWord.getRelatedWord().containsKey(temp_word)) {
                    System.out.println("relatedWord exists " + temp_word);
                    relatedWords = relatedWord.getRelatedWord().get(temp_word);
                    for (String temp_related_word : relatedWords) {
                        if (keyWordToRelation.containsKey(temp_related_word)) {
                            relatedRelation.addAll(keyWordToRelation.get(temp_related_word));
                        }
                    }
                }
            }
            for (String relation_2 : relatedRelation) {
                if (relation_set_2.contains(relation_2)) {
                    bigraph.setEdgeWeight(bigraph.addEdge(relation_1, relation_2), VertexSimilarity.getEditDistance(relation_1, relation_2));
                }
            }
        }
        System.out.println("bigraph info");
        System.out.println(bigraph.vertexSet().size());
        System.out.println(bigraph.edgeSet().size());
        MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(bigraph, relation_set_1, relation_set_2);
        HashMap<String, String> res = new HashMap<>();
        for (DefaultWeightedEdge edge : matching.getMatching().getEdges()) {
            System.out.println(bigraph.getEdgeSource(edge) + "\t" + bigraph.getEdgeTarget(edge) + "\t" + bigraph.getEdgeWeight(edge));
            res.put(bigraph.getEdgeSource(edge), bigraph.getEdgeTarget(edge));
        }
        return res;
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
