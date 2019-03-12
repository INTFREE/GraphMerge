package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.Vertex;
import knowledgeGraph.mergeModel.MergedGraghInfo;
import knowledgeGraph.mergeModel.MergedVertex;
import knowledgeGraph.mergeModel.Mutator;
import knowledgeGraph.util.UtilFunction.RandomUtil;
import knowledgeGraph.util.UtilFunction.CollectionUtil;

import java.util.*;


public class BasicMutator implements Mutator {
    public double mutationRate = 0.0;
    public double typeMutationRate = 0.5;

    public BasicMutator(double rate) {
        if (rate >= 0 && rate <= 1.0) {
            mutationRate = rate;
        }
    }

    @Override
    public boolean mutate(MergedGraghInfo inputMergedGraph) {
        if (!RandomUtil.randomTest(mutationRate)) {
            return true;
        }

        // 对于每个类型的节点，进行多次次变异尝试

        for (Integer type : inputMergedGraph.getAllTypes()) {

            // entry不变异
//            if (type.equals(StringType.entryType))
//                continue;

            if (!RandomUtil.randomTest(typeMutationRate)) {
                continue;
            }

            int mutateTimes = 1;
            while (mutateTimes > 0) {
                mutateTimes--;
                mutateForType(inputMergedGraph, type);
            }

            inputMergedGraph.isChanged = true;
        }
        // 变异完成，更新融合图信息中的映射与边融合
//        inputMergedGraph.trim();
//        inputMergedGraph.updateMapping();
//        inputMergedGraph.mergeEdges();
        return true;
    }

    protected void mutateForType(MergedGraghInfo mergedGraghInfo, Integer type) {
        Set<MergedVertex> vertexSet = mergedGraghInfo.getMergedVertexByType(type);
        // 如果这个集合是空的，也就是没有这个类型的融合节点，则不需要变异
        if (vertexSet.isEmpty()) {
            return;
        }

        // 否则，向节点集合中插入一个空节点，之后更新一下刚才拿到的集合
        MergedVertex newEmptyVertex = new MergedVertex(type, mergedGraghInfo.getMergedGraph());
        mergedGraghInfo.getMergedGraph().addVertex(newEmptyVertex);
        vertexSet = mergedGraghInfo.getMergedVertexByType(type);


        // 从中随机选择两个融合节点
        List<MergedVertex> randomPickedVertexPair = new CollectionUtil<MergedVertex>()
                .pickRandom(vertexSet, 2);

        if (randomPickedVertexPair.size() < 2) {
            return;
        }

        // 查找这两个融合节点所对应的待融合图都有哪些
        Set<Graph> graphSet = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            MergedVertex mVertex = randomPickedVertexPair.get(i);
            for (Vertex v : mVertex.getVertexSet()) {
                graphSet.add(v.getGraph());
            }
        }
        int graphNum = graphSet.size();

        MergedVertex mVertex0 = randomPickedVertexPair.get(0);
        MergedVertex mVertex1 = randomPickedVertexPair.get(1);

        // 表示有多大概率，在两个融合图中交换来自同一个待融合图的节点
        // 0.5表示有一半的待融合图的节点会被交换
        double exchangeRate = 0.5;
        int exchangeGraphNum = (int) (exchangeRate * graphNum);
        // 对于融合图集合中的每个图，随机交换两个融合节点中来自该图的节点

        List<Graph> shuffledGraphList = new ArrayList<>(graphSet);
        Collections.shuffle(shuffledGraphList);

        for (Graph graph : shuffledGraphList) {
//            // 生成随机数，判定是否交换这个图的节点
//            double randomResult = new Random().nextDouble();
//            if (randomResult > exchangeRate) {
//                continue;
//            }

            // 进行交换，直到交换次数达到
            if (exchangeGraphNum <= 0) {
                break;
            }
            exchangeGraphNum--;

            // 找到两个融合节点中，来自这个图的节点
            // 由于同一个图的两个节点不能融合在一起，所以两个图中各自的最多找到一个
            Vertex vFromMergedVertex0 = null;
            Vertex vFromMergedVertex1 = null;
            for (Vertex v : mVertex0.getVertexSet()) {
                if (v.getGraph().equals(graph)) {
                    vFromMergedVertex0 = v;
                    break;
                }
            }
            for (Vertex v : mVertex1.getVertexSet()) {
                if (v.getGraph().equals(graph)) {
                    vFromMergedVertex1 = v;
                    break;
                }
            }

            //System.out.println("before: " + mVertex0 + mVertex1);

            // 交换找到的两个节点
            if (vFromMergedVertex0 != null) {
                mVertex0.removeVertex(vFromMergedVertex0);
                mVertex1.addVertex(vFromMergedVertex0);
            }
            if (vFromMergedVertex1 != null) {
                mVertex1.removeVertex(vFromMergedVertex1);
                mVertex0.addVertex(vFromMergedVertex1);
            }

            //System.out.println("After: " + mVertex0 + mVertex1);
        }
    }
}
