package test.acquire;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acquire.Main;
import org.junit.Assert;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StateTester {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String INPUT_DIRECTORY = System.getProperty("inputDirectory"),
    OUTPUT_DIRECTORY = System.getProperty("outputDirectory");
    @Test
    public void testEndToEnd() throws IOException {
        System.out.println(INPUT_DIRECTORY);
        LinkedHashMap<File,File> inputOutputMapper = getInputOutputFiles();
        for(Map.Entry<File, File> inputFile:inputOutputMapper.entrySet()){
            System.out.println("Testing file: "+inputFile.getKey().getName());
            JsonNode input = mapper.readTree(inputFile.getKey());
            JsonNode expectedOutput = mapper.readTree(inputFile.getValue());
            JsonNode actualOutput = Main.processJson(input);
            System.out.println(actualOutput);
//            Assert.assertEquals(expectedOutput.asText(), actualOutput.asText());
        }
    }

    private LinkedHashMap<File, File> getInputOutputFiles() {
        LinkedHashMap<File,File> result = new LinkedHashMap<>();
        List<File> inputFiles = Arrays.stream(new File(INPUT_DIRECTORY).listFiles())
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        List<File> outputFiles = Arrays.stream(new File(OUTPUT_DIRECTORY).listFiles())
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        Assert.assertEquals(inputFiles.size(), outputFiles.size());
        Iterator<File> inputIterator = inputFiles.iterator(),
                outputIterator = outputFiles.iterator();
        while (inputIterator.hasNext()){
            result.put(inputIterator.next(), outputIterator.next());
        }
        return result;
    }
}
