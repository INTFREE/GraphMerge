package knowledgeGraph.ga;

import knowledgeGraph.baseModel.Graph;
import knowledgeGraph.baseModel.GraphsInfo;
import knowledgeGraph.mergeModel.EntropyCalculator;
import knowledgeGraph.mergeModel.MergedGraghInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GAProcess {
    public int populationSize;
    public double mutationRate;
    public int maxGeneration;
    public double targetEntropy = 0;
    // 一个个体最多占总种群数量的多大比例
    public double maxDiversityRate = 0.03;
    public GraphsInfo graphsInfo;

    public int entropyType = 0;
    public List<Double> entropyTrace = new ArrayList<>(maxGeneration);

    public boolean quiet = true;
    public boolean parallel = false;
    public int threadNum = 4;

    private int currentGeneration = 0;

    private int lastEntropySize = 100;
    public double minEntropy = 2147483647.0;
    private int currentEntropyGens = 0;

    public int totalTime = 0;

    List<MergedGraghInfo> population = new ArrayList<>();
    List<MergedGraghInfo> newGeneration = Collections.synchronizedList(new ArrayList<>());

    public GAProcess(int size, double rate, int maxGeneration, int stopCounter, Collection<Graph> graphSet) {
        this.populationSize = size;
        this.mutationRate = rate;
        this.maxGeneration = maxGeneration;
        this.lastEntropySize = stopCounter;
        graphsInfo = new GraphsInfo(graphSet);
    }

    public GAProcess(int size, double rate, int maxGeneration, int stopCounter, Collection<Graph> graphSet, int threadNum) {
        this.populationSize = size;
        this.mutationRate = rate;
        this.maxGeneration = maxGeneration;
        this.lastEntropySize = stopCounter;
        graphsInfo = new GraphsInfo(graphSet);
        this.threadNum = threadNum;
    }

    public void calcAllEntropy() {

        ExecutorService entropyExecutor = Executors.newFixedThreadPool(parallel ? threadNum : 1);
        population.forEach(mergedGraphInfo -> entropyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                EntropyCalculator entropyCalculator = new BasicEntropyCalculator();

                mergedGraphInfo.setEntropy(entropyCalculator.calculateEntropy(mergedGraphInfo));
                System.out.println(mergedGraphInfo.getEntropy());
            }
        }));
        entropyExecutor.shutdown();
        try {
            entropyExecutor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

    }

    public void sortByEntropy() {
        population.sort((o1, o2) -> new Double(o1.getEntropy()).compareTo(o2.getEntropy()));
    }

    /**
     * 初始化种群
     */
    public void initialize() {
        // 随机生成size个融合图
        MergedGraghInfo individual = new MergedGraghInfo(graphsInfo);
        individual.generateInitialMergeGraph();
        population.add(individual);
    }

    public void Run() {
        long startTime = System.currentTimeMillis();

        MergedGraghInfo individual = new MergedGraghInfo(graphsInfo);
        individual.generateInitialMergeGraph();
        // 计算适应度
        EntropyCalculator entropyCalculator = new BasicEntropyCalculator();
        individual.setEntropy(entropyCalculator.calculateEntropy(individual));
        System.out.println(individual.getEntropy());
        BasicMutator mutator = new BasicMutator();
        mutator.mutate(individual);
        calcAllEntropy();

    }
}
