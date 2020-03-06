package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.ga.VertexSimilarity;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.HashSet;

public class VertexContext {
    HashSet<HashMap<String, MergedVertex>> contexts;
    HashMap<HashMap<String, MergedVertex>, String> contextHashMapToId;
    HashSet<String> contextId;
    Integer id = 1;
    Integer roleNum = 1;

    public VertexContext() {
        contexts = new HashSet<>();
        contextHashMapToId = new HashMap<>();
        contextId = new HashSet<>();
    }

    public void print() {
        System.out.println("size: " + contexts.size());
        for (HashMap<String, MergedVertex> context : contexts) {
            System.out.println("context");
            for (String key : context.keySet()) {
                System.out.println(key + "\t" + context.get(key));
            }
        }
    }

    public HashSet<HashMap<String, MergedVertex>> getContexts() {
        return contexts;
    }

    public void addContext(HashMap<String, MergedVertex> context) {
        roleNum = context.size();
        contextHashMapToId.put(context, "context_" + id);
        contextId.add("context_" + id);
        id += 1;
        this.contexts.add(context);
    }

    public double getSimilarity(VertexContext vertexContext) {
        HashSet<HashMap<String, MergedVertex>> anotherContext = vertexContext.getContexts();
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> bigraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (String context_id : contextId) {
            bigraph.addVertex(context_id);
        }
        HashSet<String> context_2 = new HashSet<>();
        Integer temp_id = 1;
        HashMap<HashMap<String, MergedVertex>, String> contextHashMap2 = new HashMap<>();
        for (HashMap<String, MergedVertex> key : anotherContext) {
            contextHashMap2.put(key, "another_context_" + temp_id);
            context_2.add("another_context_" + temp_id);
            bigraph.addVertex("another_context_" + temp_id);
            temp_id += 1;
        }
        for (HashMap<String, MergedVertex> context : this.contexts) {
            String key1 = contextHashMapToId.get(context);
            for (HashMap<String, MergedVertex> context1 : anotherContext) {
                String key2 = contextHashMap2.get(context1);
                double similarity = calculateHashMapSimilarity(context, context1);
                bigraph.setEdgeWeight(bigraph.addEdge(key1, key2), similarity);
            }
        }
        MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(bigraph, contextId, context_2);
        return matching.getMatching().getWeight() / (roleNum * Math.max(contexts.size(), anotherContext.size()));
    }

    public double calculateHashMapSimilarity(HashMap<String, MergedVertex> context1, HashMap<String, MergedVertex> context2) {
        double res = 0;
        for (String key1 : context1.keySet()) {
            if (context2.containsKey(key1)) {
                res += calculateMergedVertexSimilarity(context1.get(key1), context2.get(key1));
            }
        }
        return res;
    }

    public double calculateMergedVertexSimilarity(MergedVertex mergedVertex1, MergedVertex mergedVertex2) {
        if (!mergedVertex1.getType().equalsIgnoreCase(mergedVertex2.getType())) {
            System.out.println("calculate similarity error " + mergedVertex1.getId() + mergedVertex2.getId());
        }
        if (mergedVertex1.getType().equalsIgnoreCase("entity")) {
            if (mergedVertex1.getId() == mergedVertex2.getId()) {
                return 1;
            } else {
                return 0;
            }
        }
        if (mergedVertex1.getType().equalsIgnoreCase("value")) {
            // 如果融合节点是值节点，拿出其中的一个子节点，计算相似度，这里假设值节点只有值相同才融合
            Vertex vertex1 = mergedVertex1.getVertexSet().iterator().next();
            Vertex vertex2 = mergedVertex2.getVertexSet().iterator().next();
            return VertexSimilarity.calcSimilarity(vertex1, vertex2);
        }
        return 0;
    }
}
