package knowledgeGraph.util;

import knowledgeGraph.ga.VertexSimilarity;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class RelationMap {
    HashMap<String, String> relationMap; // 直接这么操作会溢出

    public static void main(String argv[]) {
        readRelationMap();
    }

    public static void readRelationMap() {
        InputStream inputStream;
        try {
            // read embedding file
            File relation_file = new File(System.getProperty("user.dir") + "/relation_map_res");
            inputStream = new FileInputStream(relation_file);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> bigraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            HashSet<String> relation_1 = new HashSet<>();
            HashSet<String> relation_2 = new HashSet<>();
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                String[] relations = line.split("\t");

                System.out.println(relations[0]);
                System.out.println(relations[1]);
                relation_1.add(relations[0]);
                relation_2.add(relations[1]);
                bigraph.addVertex(relations[0]);
                bigraph.addVertex(relations[1]);
                bigraph.setEdgeWeight(bigraph.addEdge(relations[0], relations[1]), VertexSimilarity.getEditDistance(relations[0], relations[1]));
            }
            System.out.println(relation_1.size());
            System.out.println(relation_2.size());
            MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(bigraph, relation_1, relation_2);
//            File relation_res = new File(System.getProperty("user.dir") + "/relation_match_res");
//            FileOutputStream os = new FileOutputStream(relation_res);
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
//            for (DefaultWeightedEdge edge : matching.getMatching().getEdges()) {
//                writer.write(bigraph.getEdgeSource(edge) + "\t" + bigraph.getEdgeTarget(edge) + "\n");
//            }
//            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }

}
