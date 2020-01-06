package knowledgeGraph.ga;

import com.sun.scenario.effect.Merge;
import knowledgeGraph.baseModel.MigratePlan;
import knowledgeGraph.baseModel.Plan;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedGraph;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.mergeModel.MigratePlanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MergeMigratePlanner implements MigratePlanner {
    @Override
    public MigratePlan getVertexMigratePlan(MergedGraghInfo mergedGraghInfo) {
        MergedGraph mergedGraph = mergedGraghInfo.getMergedGraph();
        // 寻找只有一个节点的融合节点
        HashMap<String, HashSet<MergedVertex>> oneVertexMergedVertexMap = new HashMap<>();
        for (MergedVertex mergedVertex : mergedGraph.vertexSet()) {
            if (mergedVertex.getVertexSet().size() == 1 && mergedVertex.getType().equalsIgnoreCase("entity")) {
                Vertex vertex = mergedVertex.getVertexSet().iterator().next();
                if (!oneVertexMergedVertexMap.containsKey(vertex.getValue())) {
                    oneVertexMergedVertexMap.put(vertex.getValue(), new HashSet<>());
                }
                oneVertexMergedVertexMap.get(vertex.getValue()).add(mergedVertex);
            }
        }
        MigratePlan migratePlan = new MigratePlan();
        for (String value : oneVertexMergedVertexMap.keySet()) {
            if (oneVertexMergedVertexMap.get(value).size() > 1) {
                migratePlan.addPlans(generatePlans(oneVertexMergedVertexMap.get(value)));
            }
        }

        return migratePlan;
    }

    // 将只有一个节点的融合节点根据关键属性进行迁移
    public ArrayList<Plan> generatePlans(HashSet<MergedVertex> mergedVertexHashSet) {
        MergedVertex baseMergedVertex = null;
        ArrayList<Plan> plans = new ArrayList<>();
        for (MergedVertex mergedVertex : mergedVertexHashSet) {
            if (baseMergedVertex != null) {
                Plan plan = new Plan(mergedVertex.getVertexSet().iterator().next(), mergedVertex, baseMergedVertex);
                plans.add(plan);
            } else {
                baseMergedVertex = mergedVertex;
            }
        }
        return plans;
    }
}
