package at.rwinterhalder.cc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigCompare {

    private Path file1;
    private Path file2;

    public ConfigCompare(Path file1, Path file2) {
        this.file1 = file1;
        this.file2 = file2;
    }

    public void compare() {

        Map<String, String> properties1 = new TreeMap<>();
        readFile(file1, properties1);

        Map<String, String> properties2 = new TreeMap<>();
        readFile(file2, properties2);

        Map<String, Value> sameProperties = new TreeMap<>();

        Set<String> uniqueKeys = new HashSet<>();
        Set<String> uniqueKeys1 = new HashSet<>();
        Set<String> uniqueKeys2 = new HashSet<>();

        for (final Map.Entry<String, String> entry : properties1.entrySet()) {
            if (!properties2.containsKey(entry.getKey())) {
                uniqueKeys1.add(entry.getKey().trim());
            } else {
                sameProperties.put(entry.getKey(), new Value(entry.getValue().trim()));
            }
        }

        for (final Map.Entry<String, String> entry : properties2.entrySet()) {
            if (!properties1.containsKey(entry.getKey())) {
                uniqueKeys2.add(entry.getKey().trim());
            } else {
                sameProperties.get(entry.getKey()).setVal2(entry.getValue().trim());
            }
        }
        uniqueKeys.addAll(uniqueKeys1);
        uniqueKeys.addAll(uniqueKeys2);

        // SAME-KEY-SAME-VALUE
        Map<String, Value> samePropertiesSameValue = sameProperties.entrySet().stream()
                .filter(val -> val.getValue().isSame() == true).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        // SAME-KEY-OTHER-VALUE
        Map<String, Value> samePropertiesOtherValue = sameProperties.entrySet().stream()
                .filter(val -> val.getValue().isSame() == false).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        // Write File
        try {
            List<String> out = new ArrayList<>();

            out.add(String.format("UNIQUE-KEYS 1: Count: %s => %s =======================================================", uniqueKeys1.size(), file1.getFileName()));
            out.add(System.lineSeparator());
            writeMap(properties1, uniqueKeys1, out);
            out.add(System.lineSeparator());

            out.add(String.format("UNIQUE-KEYS 2: Count: %s => %s =======================================================", uniqueKeys2.size(), file2.getFileName()));
            out.add(System.lineSeparator());
            writeMap(properties2, uniqueKeys2, out);
            out.add(System.lineSeparator());

            out.add(String.format("SAME-KEYS-SAME-VALUES: Count: %s =======================================================", samePropertiesSameValue.size()));

            for (Map.Entry<String, Value> entry : samePropertiesSameValue.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue().getVal1());
                out.add(sb.toString());
            }
            out.add(System.lineSeparator());

            out.add(String.format("SAME-KEYS-OTHER-VALUES: Count: %s =======================================================", samePropertiesOtherValue.size()));
            out.add(System.lineSeparator());
            for (Map.Entry<String, Value> entry : samePropertiesOtherValue.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(String.format("%s%n", entry.getValue().getVal1()));

                for (int i = 0; i < entry.getKey().length(); i++) {
                    sb.append(" ");
                }
                sb.append("=");
                sb.append(entry.getValue().getVal2());
                out.add(sb.toString());
            }

            Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "configCompare");
            if(!Files.exists(tempDirectory)){
                Files.createDirectory(tempDirectory);
            }
            Path tempFile = Files.createTempFile(tempDirectory, "output_", ".txt");
            Files.write(tempFile, out);

//            Runtime.getRuntime().exec("explorer.exe /select," + tempFile.toAbsolutePath());
            Runtime.getRuntime().exec("notepad.exe " + tempFile.toAbsolutePath());
            String s = "";
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeMap(Map<String, String> properties, Set<String> missingKeys, List<String> out) throws IOException {
        for (String key : missingKeys) {
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append("=");
            sb.append(properties.get(key));

            out.add(sb.toString());
        }
    }

    private static void readFile(Path file, Map<String, String> properties) {
        // read file into stream, try-with-resources

//        try (Stream<String> lines = Files.lines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))) {
        try (Stream<String> lines = Files.lines(file)) {

            lines.forEach((x) -> {
                String[] line = x.split("=", 2);
                if (readRules(line)) {
                    properties.put(line[0].trim(), line[1].trim());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean readRules(String[] line) {
        if (line.length != 2) {
            return false;
        }
        if (line[0].startsWith("#")) {
            return false;
        }
        return true;
    }

    private static class Value {

        private String  val1;
        private String  val2;
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

        private void compare() {
            if (val1.equals(val2)) {
                same = true;
            } else {
                same = false;
            }
        }

        public boolean isSame() {
            return same;
        }
    }
}
