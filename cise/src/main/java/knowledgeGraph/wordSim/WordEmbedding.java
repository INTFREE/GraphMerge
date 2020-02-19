package knowledgeGraph.wordSim;

import java.io.*;
import java.util.HashMap;

public class WordEmbedding {
    HashMap<String, Double[]> embedding; // 直接这么操作会溢出

    String file_path = System.getProperty("user.dir") + "/src/offline_data/" + "glove.6B.200d.txt";

    public WordEmbedding() {
        System.out.println("Test Embedding");
        embedding = new HashMap<String, Double[]>();
    }

    public WordEmbedding(String file_name) {
        this.file_path = System.getProperty("user.dir") + "/src/offline_data/" + file_name;
        embedding = new HashMap<String, Double[]>();
    }

    public HashMap<String, Double[]> setEmbedding() {
        InputStream inputStream;
        try {
            // read embedding file
            File embeddingFile = new File(file_path);
            inputStream = new FileInputStream(embeddingFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Double[] vector = new Double[200];
                String[] arr = line.split("\\s+");
                String w = arr[0];

                if (arr.length != 201) {
                    System.out.println("ERROR EMBEDDING LENGTH:" + arr.length);
                    continue;
                }

                for (int i = 0; i < 200; i++) {
                    vector[i] = Double.valueOf(arr[i + 1]);
                }

                if (embedding.get(w) == null) embedding.put(w, vector);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return this.embedding;
    }

    public HashMap<String, Double[]> getEmbedding() {
        if (this.embedding.size() == 0) this.setEmbedding();
        return this.embedding;
    }
}



