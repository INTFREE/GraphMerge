package knowledgeGraph;

import java.io.*;
import java.util.Arrays;
import java.util.List;


public class Main2 {
    public static void main(String argv[]) {
//        Importer importer = new Importer();
//        ArrayList<String> userNameList = importer.getUserNameList();
//        GraphImporter graphImporter = new GraphImporter();
//        ArrayList<Graph> graphArrayList = new ArrayList<>();
//        for (String userName : userNameList) {
//            Graph graph = graphImporter.readGraph(importer, "大话西游-电影人物关系图谱", userName);
//            graphArrayList.add(graph);
//        }
//        System.out.println(graphArrayList.size());
//        importer.finishImport();
//
//        GAProcess ga = new GAProcess(1, 0.2, 1000, 200, graphArrayList);
//        ga.quiet = true;
//        ga.parallel = false;
//        ga.Run();

//        FileImporter importer = new FileImporter();
//
//        Graph graph1 = importer.readGraph(1);
//        Graph graph2 = importer.readGraph(2);
//        System.out.println("read finished");
//        HashMap<String, HashSet<Vertex>> mergeVertexToVertexSet = importer.readMatch(1);
//        HashSet<Graph> graphHashSet = new HashSet<>();
//        graphHashSet.add(graph1);
//        graphHashSet.add(graph2);
//        GraphsInfo graphsInfo = new GraphsInfo(graphHashSet);
//        MergedGraghInfo mergedGraghInfo = new MergedGraghInfo(graphsInfo);
//        mergedGraghInfo.generateMergeGraphByMatch(mergeVertexToVertexSet);
//        WordEmbedding embedding = new WordEmbedding();
//        HashMap<String, double[]> result = embedding.getEmbedding();
////        测试用
//        System.out.println(result.size());
//        for (String key : result.keySet()) {
//            System.out.println(key);
//            System.out.println(result.get(key));
//            System.out.println(result.get(key).length);
//            System.out.println(result.get(key)[0]);
//            break;
//        }
//        return;
        InputStream inputStream;
        try {
            // read vertex file
            String data_path = System.getProperty("user.dir") + "/src/BootEA_datasets/BootEA_DBP_WD_100K/";
            String vertexFileName = data_path + "entity_local_name_1";
            File vertexFile = new File(vertexFileName);

            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            String pattern = "\\(.*?\\)";
            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                String vertexKey = strings.get(0);
                String vertexName = "";
                if (strings.size() == 2) {
                    vertexName = strings.get(1);
                }
                if (vertexName.endsWith(")")){
                    System.out.println(vertexName.replaceAll(pattern, ""));
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }


}
