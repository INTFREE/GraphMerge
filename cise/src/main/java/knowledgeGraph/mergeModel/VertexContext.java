package knowledgeGraph.mergeModel;

import javafx.util.Pair;

import java.util.HashSet;

public class VertexContext {
    HashSet<Pair<String, HashSet<Integer>>> contexts;

    VertexContext() {
        contexts = new HashSet<>();
    }

    public void setContext(HashSet<Pair<String, HashSet<Integer>>> contexts) {
        this.contexts = contexts;
    }

    public HashSet<Pair<String, HashSet<Integer>>> getContexts() {
        return contexts;
    }

    public void addContext(Pair<String, HashSet<Integer>> context) {
        this.contexts.add(context);
    }

    public double getSimilarity(VertexContext vertexContext) {
        HashSet<Pair<String, HashSet<Integer>>> context = vertexContext.getContexts();
        context.retainAll(contexts);
        int retainSize = context.size();
        context.addAll(contexts);
        return (double) retainSize / context.size();
    }
}
