package knowledgeGraph.io;

import knowledgeGraph.baseModel.Graph;

public interface BasicImporter {
    public Graph readGraph(Integer order, String fileName);

    public void readAns();
}
