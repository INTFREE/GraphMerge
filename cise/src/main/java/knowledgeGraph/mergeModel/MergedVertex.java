package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Vertex;

import java.util.Set;

public class MergedVertex {
    private Set<Vertex> vertexSet;
    private String type;
    private Integer modelId;
    private MergedGraph mergedGraph;

    public MergedVertex(Set<Vertex> vertexSet, String type, Integer modelId) {
        this.vertexSet = vertexSet;
        this.type = type;
        this.modelId = modelId;
        this.mergedGraph = null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public Set<Vertex> getVertexSet() {
        return vertexSet;
    }

    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public void setMergedGraph(MergedGraph mergedGraph) {
        this.mergedGraph = mergedGraph;
    }

    public void setVertexSet(Set<Vertex> vertexSet) {
        this.vertexSet = vertexSet;
    }

    public boolean containsVertex(Vertex vertex) {
        return this.vertexSet.contains(vertex);
    }
}
