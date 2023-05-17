package com.isw2.util;

import com.isw2.entity.JavaFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvHandler {

    private CsvHandler() {}

    //takes a list of lists which containt all the filenames of a release for each release
    public static void writeDataLineByLine(List<List<JavaFile>> files, int numReleases) {
        // first create file object for file placed at location
        // specified by filepath
        String filePath = "src/main/java/resource/csv/dataset" + numReleases + ".csv";
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter

            FileWriter outputfile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"Release", "File", "# Authors in Release", "LOC", "Avg Churn in Release", "# Revision in Release", "Avg LOC Added in release", "Feature 6", "Feature 7", "Feature 8", "Feature 9", "Feature 10", "Buggy"};
            writer.writeNext(header);
            for (int i = 0; i < files.size(); i++) {
                for (int j = 0; j < files.get(i).size(); j++) {
                    String filename = files.get(i).get(j).getName();
                    String nAuthorsInRel = files.get(i).get(j).getnAuthorInRelease();
                    String locInRel = files.get(i).get(j).getLocAtEndRelease();
                    String avgChurnInRel = files.get(i).get(j).getAvgChurnInRelease();
                    String nRevInRel= files.get(i).get(j).getnRevInRelease();
                    String avgAddInRel = files.get(i).get(j).getAvgLocAddedInRelease();
                    String[] data = {Integer.toString(i + 1), filename, nAuthorsInRel,locInRel, avgChurnInRel, nRevInRel, avgAddInRel, "null", "null", "null", "null", "null", "null"};
                    writer.writeNext(data);
                }
            }
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readCsv(String path) {
        List<String[]> ret = new ArrayList<>();
        try {
            // Create an object of file reader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(path);

            // create csvReader object and skip first Line
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); //Skip title line
            ret = csvReader.readAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
