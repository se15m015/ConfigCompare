package at.rwinterhalder.cc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        String seperator = "<SEPERATOR>";
        String fileName1 = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\mars\\config.properties";
        String fileName2 = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\mars\\mars_dev_disme_FINAL.properties";
        String outputFilename = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\mars\\mars_config_CC.properties";

        Map<String,String> properties1 = new TreeMap<>();
        readFile(fileName1, properties1);

        Map<String,String> properties2 = new TreeMap<>();
        readFile(fileName2, properties2);
        Map<String,String> sameProperties = new TreeMap<>();


        Set<String> uniqueKeys = new HashSet<>();
        Set<String> uniqueKeys1 = new HashSet<>();
        Set<String> uniqueKeys2 = new HashSet<>();

        for(final Map.Entry<String, String> entry : properties1.entrySet()) {
            if(!properties2.containsKey(entry.getKey())){
                uniqueKeys1.add(entry.getKey());
            }else{
                sameProperties.put(entry.getKey(), entry.getValue());
            }
        }

        for(final Map.Entry<String, String> entry : properties2.entrySet()) {
            if(!properties1.containsKey(entry.getKey())){
                uniqueKeys2.add(entry.getKey());
            }else{
                if(!sameProperties.get(entry.getKey()).equals(entry.getValue())){
                    sameProperties.put(entry.getKey(),sameProperties.get(entry.getKey()) + seperator + entry.getValue());
                }
            }
        }
        uniqueKeys.addAll(uniqueKeys1);
        uniqueKeys.addAll(uniqueKeys2);

        // Write File
        try {
            Files.deleteIfExists(Paths.get(outputFilename));
            FileWriter fstream = new FileWriter(outputFilename);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write(String.format("UNIQUE-KEYS 1: Count: %s => %s \n", uniqueKeys1.size(), fileName1));
            writeMap(properties1, uniqueKeys1, out);
            out.write("\n");

            out.write(String.format("UNIQUE-KEYS 2: Count: %s => %s \n", uniqueKeys2.size(), fileName2));
            writeMap(properties2, uniqueKeys2, out);
            out.write("\n");

            out.write(String.format("SAME-KEYS: Count: %s\n", sameProperties.size()));
            for(Map.Entry<String, String> entry : sameProperties.entrySet()){
                out.write(entry.getKey());
                String[] val = entry.getValue().split(seperator,2);
                if(val.length == 2){
                    out.write("=");
                    out.write(String.format("%s\n",val[0]));
                    for(int i = 0; i < entry.getKey().length(); i++){
                        out.write(" ");
                    }
                    out.write("=");
                    out.write(String.format("%s\n",val[1]));
                }else{
                    out.write("=");
                    out.write(String.format("%s\n",val[0]));
                }
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeMap(Map<String, String> properties, Set<String> missingKeys, BufferedWriter out) throws IOException {
        for(String key : missingKeys){
            out.write(key);
            out.write("=");
            out.write(properties.get(key));
            out.write("\n");
        }
    }

    private static void readFile(String fileName, Map<String, String> properties) {
        //read file into stream, try-with-resources
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {

           lines.forEach((x) -> {
                String[] line = x.split("=", 2);
               if(line.length == 2)
                properties.put(line[0].trim(), line[1].trim());
           });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
