package at.rwinterhalder.cc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        String fileName1 = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\CC_Test\\t1.properties";
        String fileName2 = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\CC_Test\\t2.properties";
        String outputFilename = "C:\\Users\\rwinterhalder\\Desktop\\Lucky7\\disme config\\CC_Test\\o.properties";

        Map<String,String> properties1 = new TreeMap<>();
        readFile(fileName1, properties1);

        Map<String,String> properties2 = new TreeMap<>();
        readFile(fileName2, properties2);

        Map<String,Value> sameProperties = new TreeMap<>();


        Set<String> uniqueKeys = new HashSet<>();
        Set<String> uniqueKeys1 = new HashSet<>();
        Set<String> uniqueKeys2 = new HashSet<>();

        for(final Map.Entry<String, String> entry : properties1.entrySet()) {
            if(!properties2.containsKey(entry.getKey())){
                uniqueKeys1.add(entry.getKey().trim());
            }else{
                sameProperties.put(entry.getKey(), new Value(entry.getValue().trim()));
            }
        }

        for(final Map.Entry<String, String> entry : properties2.entrySet()) {
            if(!properties1.containsKey(entry.getKey())){
                uniqueKeys2.add(entry.getKey().trim());
            }else{
                sameProperties.get(entry.getKey()).setVal2(entry.getValue().trim());
            }
        }
        uniqueKeys.addAll(uniqueKeys1);
        uniqueKeys.addAll(uniqueKeys2);

        //SAME-KEY-SAME-VALUE
        Map<String,Value> samePropertiesSameValue = sameProperties.entrySet().stream()
                .filter(val -> val.getValue().isSame() == true)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        //SAME-KEY-OTHER-VALUE
        Map<String,Value> samePropertiesOtherValue = sameProperties.entrySet().stream()
                .filter(val -> val.getValue().isSame() == false)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

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

            out.write(String.format("SAME-KEYS-SAME-VALUES: Count: %s\n", samePropertiesSameValue.size()));

            for(Map.Entry<String, Value> entry : samePropertiesSameValue.entrySet()){
                out.write(entry.getKey());
                out.write("=");
                out.write(String.format("%s\n",entry.getValue().getVal1()));
            }
            out.write("\n");

            out.write(String.format("SAME-KEYS-OTHER-VALUES: Count: %s\n", samePropertiesOtherValue.size()));
            for(Map.Entry<String, Value> entry : samePropertiesSameValue.entrySet()){
                out.write(entry.getKey());
                out.write("=");
                out.write(String.format("%s\n",entry.getValue().getVal1()));
                for(int i = 0; i < entry.getKey().length(); i++){
                    out.write(" ");
                }
                out.write("=");
                out.write(String.format("%s\n",entry.getValue().getVal2()));
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
               if(readRules(line)) {
                   properties.put(line[0].trim(), line[1].trim());
               }
           });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean readRules(String[] line){
        if(line.length != 2){
            return false;
        }
        if(line[0].startsWith("#")){
            return false;
        }
        return true;
    }

    private static class Value{
        private String val1;
        private String val2;
        private boolean same;

        public Value(String val1) {
            this.val1 = val1;
            this.val2 = null;
            compare();
        }

        public String getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }

        public void setVal2(String val2) {
            this.val2 = val2;
            compare();
        }

        private void compare(){
            if(val1.equals(val2)){
                same = true;
            }else {
                same = false;
            }
        }

        public boolean isSame() {
            return same;
        }
    }
}
