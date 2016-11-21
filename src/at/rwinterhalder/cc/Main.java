package at.rwinterhalder.cc;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws URISyntaxException {

        // Copy the content from two config files you want to compare to config1.properties and config2.properties
        // inside the resource folder
        // OR
        // run in console with both files as parameter

        Path file1;
        Path file2;
        if(args.length == 2){
            file1 = Paths.get(args[0]);
            file2 = Paths.get(args[1]);
        }else{
            file1 = Paths.get(ClassLoader.getSystemResource("config1.properties").toURI());
            file2 = Paths.get(ClassLoader.getSystemResource("config2.properties").toURI());
        }
        ConfigCompare cc = new ConfigCompare(file1, file2);
        cc.compare();
    }
}
