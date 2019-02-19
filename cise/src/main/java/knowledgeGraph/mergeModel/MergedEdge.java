package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.Edge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MergedEdge {
    private MergedVertex source;
    private MergedVertex target;
    private Set<Edge> edgeSet;
    private MergedGraph mergedGraph;
    private String roleName;

    public MergedEdge(MergedVertex source, MergedVertex target, String roleName) {
        this.source = source;
        this.target = target;
        this.edgeSet = new HashSet<>();
        this.roleName = roleName;
        this.mergedGraph = null;
    }

    public String getRoleName() {
        return roleName;
    }

    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public Set<Edge> getEdgeSet() {
        return edgeSet;
    }

    public MergedVertex getSource() {
        return source;
    }

    public MergedVertex getTarget() {
        return target;
    }

    public void setMergedGraph(MergedGraph mergedGraph) {
        this.mergedGraph = mergedGraph;
    }

    public void setTarget(MergedVertex target) {
        this.target = target;
    }

    public void setSource(MergedVertex source) {
        this.source = source;
    }

    public void setEdgeSet(Set<Edge> edgeSet) {
        this.edgeSet = edgeSet;
    }

    public void addEdge(Edge e) {
        this.edgeSet.add(e);
    }

    public void addAllEdge(Collection<Edge> edgeCollection) {
        this.edgeSet.addAll(edgeCollection);
    }

}
