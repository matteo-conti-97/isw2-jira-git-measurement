package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;
import com.isw2.util.AuthJsonParser;

import java.sql.SQLException;
import java.text.ParseException;


public class Main {
    private static final String AUTHOR = "apache";
    private static final String ZOOKEEPER = "zookeeper";
    private static final String BOOKKEEPER = "bookkeeper";
    private static final String BOOKKEEPER_CREATION = "2011-03-30";
    private static final String ZOOKEEPER_CREATION = "2008-05-19";
    private static final String LAST_BOOKKEEPER_RELEASE = "4.5.0";
    private static final String LAST_BOOKKEEPER_RELEASE_END = "2017-06-16";
    private static final String LAST_ZOOKEEPER_RELEASE = "3.5.3";
    private static final String[][] COLD_START_PROJECTS = { //Prese la meta delle release su jira
            {"accumulo", "1.7.0", "2011-10-06"},
            {"tajo", "0.11.0", "2011-12-09"},
            {"pig", "0.13.0", "2007-10-29"},
            {"syncope", "2.0.4", "2010-04-29"},
            {"avro", "1.7.0", "2009-05-21"},
            {"kafka", "2.6.2", "2011-08-01"}
    };

    public static void main(String[] args) throws ParseException, SQLException {
        ScraperController scraperController = new ScraperController(BOOKKEEPER, AUTHOR, BOOKKEEPER_CREATION);
        //scraperController.saveProjectDataOnDb(LAST_ZOOKEEPER_RELEASE,null);
        scraperController.getProjectDataFromDb();
        MeasureController measureController = new MeasureController(scraperController.getProject());
        for (String[] project : COLD_START_PROJECTS) {
            System.out.println("Processing cold start data of project: " + project[0] + " until release " + project[1]);
            ScraperController coldStartScraperController = new ScraperController(project[0], AUTHOR, project[2]);
            //coldStartScraperController.saveColdStartDataOnDb(project[1]);
            coldStartScraperController.getColdStartDataFromDb();
            measureController.addColdStartProportionProject(coldStartScraperController.getProject());
        }
        double coldStartProportion = measureController.computeColdStartProportion();
        System.out.println("Cold start proportion: " + coldStartProportion);
        measureController.setColdStartProportion(coldStartProportion);
        measureController.createWalkForwardDatasets();
    }
}
