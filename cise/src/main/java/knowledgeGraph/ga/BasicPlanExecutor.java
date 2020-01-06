package knowledgeGraph.ga;

import javafx.util.Pair;
import knowledgeGraph.baseModel.*;
import knowledgeGraph.mergeModel.*;

import java.util.HashSet;
import java.util.Set;

public class BasicPlanExecutor implements PlanExecutor {
    MergedGraghInfo mergedGraghInfo;

    public BasicPlanExecutor(MergedGraghInfo mergedGraghInfo) {
        this.mergedGraghInfo = mergedGraghInfo;
    }

    @Override
    public void ExecutePlan(MigratePlan migratePlan) {
        System.out.println(">>>>> execute plan: ");
        for (Plan plan : migratePlan.getPlanArrayList()) {
            Vertex vertex = plan.getVertex();
            MergedVertex source = plan.getSource();
            MergedVertex target = plan.getTarget();
            System.out.println("migrate vertex " + vertex.getValue());
            if (checkSameGraph(vertex, target)) {
                continue;
            }
            MigratePlan relationMigratePlan = doExecutePlan(vertex, source, target);
            for (Plan relationPlan : relationMigratePlan.getPlanArrayList()) {
                Vertex relationVertex = relationPlan.getVertex();
                MergedVertex relationSource = relationPlan.getSource();
                MergedVertex relationTarget = relationPlan.getTarget();
                if (checkSameGraph(relationVertex, relationTarget)) {
                    continue;
                }
                doExecutePlan(relationVertex, relationSource, relationTarget);
            }

            this.cleanGraph();
            break;
        }

    }

    // TODO:直接调用计算熵值函数效率太低.
    public boolean checkEntropy() {
        return true;
    }

    private boolean checkSameGraph(Vertex vertex, MergedVertex target) {
        Graph vertexGraph = vertex.getGraph();
        Set<Graph> targetGraphSet = new HashSet<>();
        for (Vertex vertex1 : target.getVertexSet()) {
            targetGraphSet.add(vertex1.getGraph());
        }
        return targetGraphSet.contains(vertexGraph);
    }

    private MigratePlan doExecutePlan(Vertex vertex, MergedVertex source, MergedVertex target) {
        MergedGraph mergedGraph = this.mergedGraghInfo.getMergedGraph();

        // 找到所有和迁移源点相连的融合边
        Set<MergedEdge> relatedMergedEdges = new HashSet<>();
        relatedMergedEdges.addAll(mergedGraph.incomingEdgesOf(source));
        relatedMergedEdges.addAll(mergedGraph.outgoingEdgesOf(source));
        System.out.println(relatedMergedEdges.size());
        // 记录所有和迁移节点有关系的边
        Set<Pair<Vertex, Edge>> relatedVertexAndEdge = new HashSet<>();
        String type = "";

        for (MergedEdge mergedEdge : relatedMergedEdges) {
            for (Edge edge : mergedEdge.getEdgeSet()) {
                if (edge.getTarget().equals(vertex)) {
                    type = "IN";
                    relatedVertexAndEdge.add(new Pair<>(edge.getSource(), edge));
                    mergedEdge.deleteEdge(edge);
                    break;
                } else if (edge.getSource().equals(vertex)) {
                    type = "OUT";
                    relatedVertexAndEdge.add(new Pair<>(edge.getTarget(), edge));
                    mergedEdge.deleteEdge(edge);
                    break;
                }
            }
        }
        source.removeVertex(vertex);
        // 迁移到新的节点
        target.addVertex(vertex);
        // 产生融合边

        // 如果是实体节点，需要迁移对应的relation节点
        boolean isEntity = vertex.getType().equalsIgnoreCase("entity");
        Set<MergedEdge> targetRelatedMergedEdges = new HashSet<>();
        if (isEntity) {
            targetRelatedMergedEdges.addAll(mergedGraph.incomingEdgesOf(target));
            targetRelatedMergedEdges.addAll(mergedGraph.outgoingEdgesOf(target));
        }
        MigratePlan relationMigratePlan = new MigratePlan();
        for (Pair<Vertex, Edge> entry : relatedVertexAndEdge) {
            MergedVertex relateMergedVertex = entry.getKey().getMergedVertex();
            Edge relatedEdge = entry.getValue();
            // 对于entity节点，如果在初始融合图中，就存在和相同roleName的边，那需要将relation节点也迁移过来
            if (isEntity) {
                for (MergedEdge mergedEdge : targetRelatedMergedEdges) {
                    if (mergedEdge.getRoleName().equalsIgnoreCase(relatedEdge.getRoleName())) {
                        relationMigratePlan.addPlan(new Plan(relatedEdge.getSource(), relatedEdge.getSource().getMergedVertex(), mergedEdge.getSource()));
                    }
                }
            }
            if (type.equalsIgnoreCase("IN")) {
                MergedEdge mergedEdge = mergedGraph.getEdge(relateMergedVertex, target);
                if (mergedEdge == null) {
                    mergedEdge = new MergedEdge(relateMergedVertex, target, entry.getValue().getRoleName());
                    mergedGraph.addEdge(relateMergedVertex, target, mergedEdge);
                } else {
                    mergedEdge.addEdge(entry.getValue());
                }
            } else if (type.equalsIgnoreCase("OUT")) {
                MergedEdge mergedEdge = mergedGraph.getEdge(target, relateMergedVertex);
                if (mergedEdge == null) {
                    mergedEdge = new MergedEdge(target, relateMergedVertex, entry.getValue().getRoleName());
                    mergedGraph.addEdge(target, relateMergedVertex, mergedEdge);
                } else {
                    mergedEdge.addEdge(entry.getValue());
                }
            } else {
                System.out.println("DoExecutionPlan Error: type has no value.");
            }


        }
        return relationMigratePlan;

    }

    private void cleanGraph() {
        HashSet<MergedEdge> removedEdge = new HashSet<>();
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        for (MergedEdge mergedEdge : mergedGraph.edgeSet()) {
            if (mergedEdge.getEdgeSet().size() == 0) {
                removedEdge.add(mergedEdge);
            }
        }
        mergedGraph.removeAllEdges(removedEdge);
        HashSet<MergedVertex> removedVertex = new HashSet<>();
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
}
