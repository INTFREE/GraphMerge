package knowledgeGraph.wordSim;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class RelatedWord {
    HashMap<String, String[]> relatedWord; // 直接这么操作会溢出

    String file_path = System.getProperty("user.dir") + "/src/offline_data/" + "related_words.txt";

    public RelatedWord() {
        relatedWord = new HashMap<String, String[]>();
    }

    public RelatedWord(String file_name) {
        this.file_path = System.getProperty("user.dir") + "/src/offline_data/" + file_name;
        relatedWord = new HashMap<String, String[]>();
    }

    public HashMap<String, String[]> setRelatedWord() {
        InputStream inputStream;
        try {
            // read embedding file
            File embeddingFile = new File(file_path);
            inputStream = new FileInputStream(embeddingFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String [] arr = line.split("\\s+");
                String w = arr[0];
                String[] relates = new  String[5];

                if (arr.length != 6) {
                    System.out.println("ERROR EMBEDDING LENGTH:" + arr.length);
                    continue;
                }

                System.arraycopy(arr, 1, relates, 0, 5);

                if (relatedWord.get(w) == null) relatedWord.put(w, relates);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
        return this.relatedWord;
    }

    public HashMap<String, String[]> getRelatedWord() {
        if (this.relatedWord.size() == 0) this.setRelatedWord();
        return this.relatedWord;
    }
}
