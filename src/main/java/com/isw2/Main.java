package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;

import java.sql.SQLException;
import java.text.ParseException;


public class Main {
    private static final String AUTHOR = "apache";
    private static final String[] COLD_START_PROJECTS = {
            "accumulo",
            "tajo",
            "pig",
            "syncope",
            "avro",
            "cassandra",
            "hadoop",
            "kafka"
    };

    public static void main(String[] args) throws ParseException, SQLException {
        ScraperController bookkeeperScraperController = new ScraperController("bookkeeper", AUTHOR);
        //bookkeeperScraperController.getProjectDataFromDb();
        //bookkeeperScraperController.saveProjectDataOnDb();
        MeasureController bookkeeperMeasureController = new MeasureController(bookkeeperScraperController.getProject());
        for(String projectName: COLD_START_PROJECTS) {
            System.out.println("Processing cold start data of project: " + projectName);
            ScraperController scraperController = new ScraperController(projectName, AUTHOR);
            scraperController.getColdStartDataFromDb();
            bookkeeperMeasureController.addColdStartProportionProject(scraperController.getProject());
            //scraperController.saveColdStartDataOnDb();
        }
        //bookkeeperMeasureController.createWalkForwardDatasets();
        //scraperController.saveColdStartInfoOnDb();
    }
}
