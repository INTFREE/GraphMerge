package knowledgeGraph.ga;

import org.apache.commons.lang3.tuple.*;

import knowledgeGraph.baseModel.Edge;
import knowledgeGraph.baseModel.MigratePlan;
import knowledgeGraph.baseModel.Plan;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.*;

import java.util.HashSet;
import java.util.Set;

public class BasicPlanExecutor implements PlanExecutor {
    MergedGraghInfo mergedGraghInfo;
    Set<String> targetGraphSet;
    Set<MergedEdge> relatedMergedEdges;
    Set<MergedEdge> targetRelatedMergedEdges;
    Set<Pair<Vertex, Edge>> relatedVertexAndEdge;
    HashSet<MergedEdge> removedEdge;
    HashSet<MergedVertex> removedVertex;

    public BasicPlanExecutor(MergedGraghInfo mergedGraghInfo) {
        this.mergedGraghInfo = mergedGraghInfo;
        targetGraphSet = new HashSet<>();
        relatedMergedEdges = new HashSet<>();
        targetRelatedMergedEdges = new HashSet<>();
        relatedVertexAndEdge = new HashSet<>();
        removedEdge = new HashSet<>();
        removedVertex = new HashSet<>();
    }

    @Override
    public void ExecutePlan(MigratePlan migratePlan, boolean relationMigrate, boolean cleanGraph) {
        for (Plan plan : migratePlan.getPlanArrayList()) {
            Vertex vertex = plan.getVertex();
            MergedVertex source = plan.getSource();
            MergedVertex target = plan.getTarget();
            if (checkSameGraph(vertex, target)) {
                continue;
            }
            MigratePlan relationMigratePlan = doExecutePlan(vertex, source, target, relationMigrate);
            for (Plan relationPlan : relationMigratePlan.getPlanArrayList()) {
                Vertex relationVertex = relationPlan.getVertex();
                MergedVertex relationSource = relationPlan.getSource();
                MergedVertex relationTarget = relationPlan.getTarget();
                if (checkSameGraph(relationVertex, relationTarget)) {
                    continue;
                }
                doExecutePlan(relationVertex, relationSource, relationTarget, false);
            }
        }
        if (cleanGraph) {
            this.cleanGraph();
        }
    }

    private boolean checkSameGraph(Vertex vertex, MergedVertex target) {
        String vertexGraph = vertex.getGraph().getUserName();
        targetGraphSet.clear();
        for (Vertex vertex1 : target.getVertexSet()) {
            targetGraphSet.add(vertex1.getGraph().getUserName());
        }
        return targetGraphSet.contains(vertexGraph);
    }

    private MigratePlan doExecutePlan(Vertex vertex, MergedVertex source, MergedVertex target, boolean relationMigrate) {
        MergedGraph mergedGraph = this.mergedGraghInfo.getMergedGraph();

        if (!source.getVertexSet().contains(vertex)) {
            System.out.println("DoExecutionPlan ERROR: vertex not in source MergedVertex.");
            System.out.println(vertex.getValue() + "\t" + vertex.getType());
            System.out.println(source.getVertexSet().size());
            return null;
        }

        // 找到所有和迁移源点相连的融合边
        relatedMergedEdges.clear();
        relatedMergedEdges.addAll(mergedGraph.incomingEdgesOf(source));
        relatedMergedEdges.addAll(mergedGraph.outgoingEdgesOf(source));

        // 记录所有和迁移节点有关系的边
        relatedVertexAndEdge.clear();
        String type = "";

        for (MergedEdge mergedEdge : relatedMergedEdges) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                if (edge.getTarget().equals(vertex)) {
                    type = "IN";
                    relatedVertexAndEdge.add(new ImmutablePair<>(edge.getSource(), edge));
                    mergedEdge.deleteEdge(edge);
                    break;
                } else if (edge.getSource().equals(vertex)) {
                    type = "OUT";
                    relatedVertexAndEdge.add(new ImmutablePair<>(edge.getTarget(), edge));
                    mergedEdge.deleteEdge(edge);
                    break;
                }
            }
        }
        // 产生融合边

        // 如果是实体节点，需要迁移对应的relation节点
        boolean isEntity = vertex.getType().equalsIgnoreCase("entity");
        targetRelatedMergedEdges.clear();
        if (isEntity) {
            targetRelatedMergedEdges.addAll(mergedGraph.incomingEdgesOf(target));
            targetRelatedMergedEdges.addAll(mergedGraph.outgoingEdgesOf(target));
        }
        MigratePlan relationMigratePlan = new MigratePlan();
        for (Pair<Vertex, Edge> entry : relatedVertexAndEdge) {
            MergedVertex relateMergedVertex = entry.getKey().getMergedVertex();
            Edge relatedEdge = entry.getValue();
            // 对于entity节点，如果在初始融合图中，就存在和相同roleName的边，且relation 迁移前后相连节点不变，那需要将relation节点也迁移过来
            if (isEntity && relationMigrate) {
                for (MergedEdge mergedEdge : targetRelatedMergedEdges) {
                    if (mergedEdge.getRoleName().equalsIgnoreCase(relatedEdge.getRoleName()) && checkContext(relatedEdge.getSource(), mergedEdge.getSource(), source, target)) {
                        relationMigratePlan.addPlan(new Plan(relatedEdge.getSource(), relatedEdge.getSource().getMergedVertex(), mergedEdge.getSource()));
                    }
                }
            }
            if (type.equalsIgnoreCase("IN")) {
                Set<MergedEdge> mergedEdges = mergedGraph.getAllEdges(relateMergedVertex, target);
                MergedEdge targetMergedEdge = null;
                for (MergedEdge mergedEdge : mergedEdges) {
                    if (mergedEdge.getRoleName().equalsIgnoreCase(relatedEdge.getRoleName())) {
                        targetMergedEdge = mergedEdge;
                        break;
                    }
                }
                if (targetMergedEdge == null) {
                    targetMergedEdge = new MergedEdge(relateMergedVertex, target, relatedEdge.getRoleName());
                    mergedGraph.addEdge(relateMergedVertex, target, targetMergedEdge);
                }
                targetMergedEdge.addEdge(entry.getValue());
            } else if (type.equalsIgnoreCase("OUT")) {
                Set<MergedEdge> mergedEdges = mergedGraph.getAllEdges(target, relateMergedVertex);
                MergedEdge targetMergedEdge = null;
                for (MergedEdge mergedEdge : mergedEdges) {
                    if (mergedEdge.getRoleName().equalsIgnoreCase(relatedEdge.getRoleName())) {
                        targetMergedEdge = mergedEdge;
                        break;
                    }
                }
                if (targetMergedEdge == null) {
                    targetMergedEdge = new MergedEdge(target, relateMergedVertex, relatedEdge.getRoleName());
                    mergedGraph.addEdge(target, relateMergedVertex, targetMergedEdge);
                }
                targetMergedEdge.addEdge(entry.getValue());
            } else {
                System.out.println("DoExecutionPlan Error: type has no value.");
            }


        }
        if (!source.removeVertex(vertex)) {
            System.out.println("DoExecutionPlan error : remove vertex error " + source.getId() + "\t" + vertex.getId());
        }
        // 迁移到新的节点
        target.addVertex(vertex);
        vertex.setMergedVertex(target);
        return relationMigratePlan;

    }

    private void cleanGraph() {
        removedVertex.clear();
        removedEdge.clear();
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        for (MergedEdge mergedEdge : mergedGraph.edgeSet()) {
            if (mergedEdge.getEdgeSet().size() == 0) {
                removedEdge.add(mergedEdge);
            }
        }
        mergedGraph.removeAllEdges(removedEdge);
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getVertexSet().size() == 0) {
                if (mergedGraph.incomingEdgesOf(mergedVertex).size() != 0 || mergedGraph.outgoingEdgesOf(mergedVertex).size() != 0) {
                    System.out.println("Clean MergedGraph Error: migrate error");
                } else {
                    removedVertex.add(mergedVertex);
                }
            }
        }
        mergedGraph.removeAllVertices(removedVertex);
    }


    private boolean checkContext(Vertex relationVertex, MergedVertex targetRelationMergedVertex, MergedVertex migrateSourceVertex, MergedVertex migrateTargetVertex) {
        HashSet<Vertex> relatedVertexSet = relationVertex.getRelatedVertex();
        HashSet<Integer> relatedMergedVertexSet = new HashSet<>();
        for (Vertex vertex : relatedVertexSet) {
            relatedMergedVertexSet.add(vertex.getMergedVertex().getId());
        }
        if (!relatedMergedVertexSet.contains(migrateSourceVertex.getId())) {
            System.out.println("Execute Plan ERROR: source not in related Set");
            return false;
        }
        relatedMergedVertexSet.remove(migrateSourceVertex.getId());
        HashSet<Integer> targetMergedVertexSet = new HashSet<>();
        Set<MergedEdge> connectedEdges = this.mergedGraghInfo.getMergedGraph().outgoingEdgesOf(targetRelationMergedVertex);
        for (MergedEdge mergedEdge : connectedEdges) {
            targetMergedVertexSet.add(mergedEdge.getTarget().getId());
        }
        if (!targetMergedVertexSet.contains(migrateTargetVertex.getId())) {
            System.out.println("Execute Plan ERROR: target not in related Set");
            return false;
        }
        targetMergedVertexSet.remove(migrateTargetVertex.getId());
        return targetMergedVertexSet.containsAll(relatedMergedVertexSet) && relatedMergedVertexSet.containsAll(targetMergedVertexSet);
    }
}
