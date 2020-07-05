package knowledgeGraph.wordSim;

import java.io.*;
import java.util.HashMap;

public class WordEmbedding {
    HashMap<String, double[]> embedding; // 直接这么操作会溢出
    Double[] defaultData;
    double[] defaultValue = new double[200];
    String file_path = System.getProperty("user.dir") + "/src/offline_data/" + "glove.6B.200d.txt";

    public WordEmbedding() {
        System.out.println("Test Embedding");
        embedding = new HashMap<String, double[]>();
    }

    public WordEmbedding(String file_name) {
        this.file_path = System.getProperty("user.dir") + "/src/offline_data/" + file_name;
        embedding = new HashMap<String, double[]>();
    }

    public double[] getWordEmbedding(String word) {
        if (this.embedding.size() == 0) {
            System.out.println("not initialize");
            return defaultValue;
        }
        if (embedding.containsKey(word)) {
            return embedding.get(word);
        } else {
            return defaultValue;
        }
    }

    public HashMap<String, double[]> setEmbedding() {
        InputStream inputStream;
        try {
            // read embedding file
            File embeddingFile = new File(file_path);
            inputStream = new FileInputStream(embeddingFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split("\\s+");
                String w = arr[0];
                double[] vector = new double[200];
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

    public HashMap<String, double[]> getEmbedding() {
        if (this.embedding.size() == 0) this.setEmbedding();
        return this.embedding;
    }


}



