package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;

import java.util.HashSet;
import java.util.Set;

public class MergedGraph {
    private Set<MergedVertex> mergedVertexSet;
    private Set<MergedEdge> mergedEdgeSet;

    public MergedGraph() {
        this.mergedEdgeSet = new HashSet<>();
        this.mergedEdgeSet = new HashSet<>();
    }

    @Override
    public String toString() {
        return "merged graph has " + this.mergedEdgeSet.size() + " edges and " + this.mergedVertexSet.size() + "vertex";
    }

    public Set<MergedEdge> getIncomingEdge(MergedVertex mergedVertex) {
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        for (MergedEdge mergedEdge : this.mergedEdgeSet) {
            if (mergedEdge.getTarget().equals(mergedVertex)) {
                mergedEdgeSet.add(mergedEdge);
            }
        }
        return mergedEdgeSet;
    }

    public Set<MergedEdge> getOutcomingEdge(MergedVertex mergedVertex) {
        Set<MergedEdge> mergedEdgeSet = new HashSet<>();
        for (MergedEdge mergedEdge : this.mergedEdgeSet) {
            if (mergedEdge.getSource().equals(mergedVertex)) {
                mergedEdgeSet.add(mergedEdge);
            }
        }
        return mergedEdgeSet;
    }

    public boolean addVertex(MergedVertex mergedVertex){
        if(this.mergedVertexSet.contains(mergedVertex)){
            return false;
        }
        this.mergedVertexSet.add(mergedVertex);
        return true;
    }
    public Set<MergedEdge> getMergedEdgeSet() {
        return mergedEdgeSet;
    }

    public Set<MergedVertex> getMergedVertexSet() {
        return mergedVertexSet;
    }

    public void setMergedEdgeSet(Set<MergedEdge> mergedEdgeSet) {
        this.mergedEdgeSet = mergedEdgeSet;
    }

    public void setMergedVertexSet(Set<MergedVertex> mergedVertexSet) {
        this.mergedVertexSet = mergedVertexSet;
    }
}
