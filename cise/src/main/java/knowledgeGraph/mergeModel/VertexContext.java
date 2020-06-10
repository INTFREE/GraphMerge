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
    Integer totalId;

    public VertexContext(Integer id) {
        contexts = new HashSet<>();
        contextHashMapToId = new HashMap<>();
        contextId = new HashSet<>();
        totalId = id;
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

    public HashMap<HashMap<String, MergedVertex>, String> getContextHashMapToId() {
        return contextHashMapToId;
    }

    public HashSet<String> getContextId() {
        return contextId;
    }

    public void addContext(HashMap<String, MergedVertex> context) {
        String key = "context" + totalId + "_" + id;
        roleNum = context.size();
        contextHashMapToId.put(context, key);
        contextId.add(key);
        id += 1;
        this.contexts.add(context);
    }

    public double getSimilarity(VertexContext anotherContext) {
        if (this.contextId.isEmpty() || anotherContext.contextId.isEmpty()) {
            return 0.0;
        }
        DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> bigraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (String context_id : contextId) {
            bigraph.addVertex(context_id);
        }
        for (String context_id : anotherContext.getContextId()) {
            bigraph.addVertex(context_id);
        }
        for (HashMap<String, MergedVertex> context : this.contexts) {
            String key1 = contextHashMapToId.get(context);
            for (HashMap<String, MergedVertex> context1 : anotherContext.getContexts()) {
                String key2 = anotherContext.getContextHashMapToId().get(context1);
                double similarity = calculateHashMapSimilarity(context, context1);
                bigraph.setEdgeWeight(bigraph.addEdge(key1, key2), similarity);
            }
        }
        MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(bigraph, contextId, anotherContext.getContextId());
        return matching.getMatching().getWeight() / (roleNum * Math.max(contexts.size(), anotherContext.getContextId().size()));
    }

    public double calculateHashMapSimilarity(HashMap<String, MergedVertex> context1, HashMap<String, MergedVertex> context2) {
        double res = 0;
        for (String key1 : context1.keySet()) {
            if (context2.containsKey(key1)) {
                res += calculateMergedVertexSimilarity(key1, context1.get(key1), context2.get(key1));
            }
        }
        return res;
    }

    public double calculateMergedVertexSimilarity(String type, MergedVertex mergedVertex1, MergedVertex mergedVertex2) {
        if (!mergedVertex1.getType().equalsIgnoreCase(mergedVertex2.getType())) {
            //System.out.println("calculate similarity error " + type + "\t" + mergedVertex1.getId() + "\t" +  mergedVertex2.getId());
            return 0;
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
