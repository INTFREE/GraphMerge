package knowledgeGraph.mergeModel;

import knowledgeGraph.baseModel.MigratePlan;

public interface MigratePlanner {
    // 输入：一个需要迁移的融合图
    // 输出：Migration Plan
    public MigratePlan getVertexMigratePlan();
}
