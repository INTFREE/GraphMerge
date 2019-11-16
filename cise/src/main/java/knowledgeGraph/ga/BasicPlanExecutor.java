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
        for (Plan plan : migratePlan.getPlanArrayList()) {
            Vertex vertex = plan.getVertex();
            MergedVertex source = plan.getSource();
            MergedVertex target = plan.getTarget();
            if (checkSameGraph(vertex, source, target)) {
                continue;
            }
            doExecutePlan(vertex, source, target);
        }
    }

    // TODO:直接调用计算熵值函数效率太低.
    public boolean checkEntropy() {
        return true;
    }

    public boolean checkSameGraph(Vertex vertex, MergedVertex source, MergedVertex target) {
        Graph vertexGraph = vertex.getGraph();
        Set<Graph> targetGraphSet = new HashSet<>();
        for (Vertex vertex1 : target.getVertexSet()) {
            targetGraphSet.add(vertex1.getGraph());
        }
        return targetGraphSet.contains(vertexGraph);
    }

    public void doExecutePlan(Vertex vertex, MergedVertex source, MergedVertex target) {
        MergedGraph mergedGraph = this.mergedGraghInfo.getMergedGraph();
        // 删除源节点的相邻边
        Set<MergedEdge> allMergedEdges = mergedGraph.incomingEdgesOf(source);
        allMergedEdges.addAll(mergedGraph.outgoingEdgesOf(source));

        // 记录所有和迁移节点有关系的边
        Set<Pair<Vertex, Edge>> relatedVertexAndEdge = new HashSet<>();
        String type = "";

        for (MergedEdge mergedEdge : allMergedEdges) {
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
            // 如果删除边后该融合边没有边存在，则删除该融合边
            if (mergedEdge.getEdgeSet().size() == 0) {
                mergedGraph.removeEdge(mergedEdge);
            }
        }
        source.removeVertex(vertex);
        // 迁移到新的节点
        target.addVertex(vertex);
        // 产生融合边
        // TODO:如果有两个融合节点之间已经存在融合边，但迁移过来的边类型不同如何处理， 如果已经存在边但rolename不同怎么处理
        for (Pair<Vertex, Edge> entry : relatedVertexAndEdge) {
            MergedVertex relateMergedVertex = entry.getKey().getMergedVertex();
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

    }
}
