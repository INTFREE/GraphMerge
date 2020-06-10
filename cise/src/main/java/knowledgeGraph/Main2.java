package knowledgeGraph;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class Main2 {
    public static void main(String argv[]) {
        InputStream inputStream;
        HashSet<String> name_1 = new HashSet<>();
        HashSet<String> name_2 = new HashSet<>();
        try {
            // read vertex file
            String data_path = System.getProperty("user.dir") + "/src/BootEA_datasets/BootEA_DBP_YG_100K/";
            String vertexFileName = data_path + "entity_local_name_1";
            File vertexFile = new File(vertexFileName);

            inputStream = new FileInputStream(vertexFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                String vertexKey = strings.get(0);
                String vertexName = "";
                if (strings.size() == 2) {
                    vertexName = strings.get(1);
                    vertexName = String.join(" ", vertexName.split("[_]"));
                    name_1.add(vertexName);
                }
            }
            vertexFileName = data_path + "entity_local_name_2";
            vertexFile = new File(vertexFileName);

            inputStream = new FileInputStream(vertexFile);
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            int count = 0;

            while ((line = bufferedReader.readLine()) != null) {

                // init vertex
                List<String> strings = Arrays.asList(line.split("\t"));
                String vertexKey = strings.get(0);
                String vertexName = "";
                if (strings.size() == 2) {
                    vertexName = strings.get(1);
                    vertexName = String.join(" ", vertexName.split("[_-]"));
                    if (name_1.contains(vertexName)){
                        name_1.remove(vertexName);
                        count += 1;
                    } else{
                        name_2.add(vertexName);
                    }
                }
            }
            System.out.println(count);
            System.out.println(name_1.size() + "\t" + name_2.size());
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("read file error" + e.toString());
        }
    }


}
