package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;
import com.isw2.util.CsvHandler;
import com.isw2.util.ExperimentType;
import com.isw2.weka.WekaAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());
    private static final String AUTHOR = "apache";
    private static final String ZOOKEEPER = "zookeeper";
    private static final String BOOKKEEPER = "bookkeeper";
    private static final String BOOKKEEPER_CREATION = "2011-03-30";
    private static final String ZOOKEEPER_CREATION = "2008-05-19";
    private static final String LAST_BOOKKEEPER_RELEASE = "4.3.0";
    private static final String LAST_ZOOKEEPER_RELEASE = "3.5.1";
    private static final String[][] COLD_START_PROJECTS = { // Prese la meta delle release su jira
            { "accumulo", "1.7.0", "2011-10-06" },
            { "tajo", "0.11.0", "2011-12-09" },
            { "pig", "0.13.0", "2007-10-29" },
            { "syncope", "2.0.4", "2010-04-29" },
            { "avro", "1.7.0", "2009-05-21" },
            { "kafka", "2.6.2", "2011-08-01" }
    };

    // Zookeeper report start from release 2 cause of NaN values
    public static void main(String[] args) throws ParseException, SQLException {
        long startTime = System.nanoTime();
        int bookkeeperDatasetNum = 6;
        int zookeeperDatasetNum = 22;
        if (CsvHandler.checkIfOutDirIsEmpty()) {
            bookkeeperDatasetNum = createDataset(BOOKKEEPER, BOOKKEEPER_CREATION, LAST_BOOKKEEPER_RELEASE);
            zookeeperDatasetNum = createDataset(ZOOKEEPER, ZOOKEEPER_CREATION, LAST_ZOOKEEPER_RELEASE);
        }
        analyzeDataset(BOOKKEEPER, bookkeeperDatasetNum);
        CsvHandler.collectDataExperiment(BOOKKEEPER, bookkeeperDatasetNum);
        analyzeDataset(ZOOKEEPER, zookeeperDatasetNum);
        CsvHandler.collectDataExperiment(ZOOKEEPER, zookeeperDatasetNum);
        long elapsedTime = System.nanoTime() - startTime;
        LOGGER.info("Execution has taken {} ms", elapsedTime / 1000000);

    }

    public static void analyzeDataset(String project, int datasetNum) {
        WekaAnalyzer wekaAnalyzer = new WekaAnalyzer();
        try {
            LOGGER.info("\n\n*************VANILLA EXPERIMENT**************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.VANILLA);
            LOGGER.info("\n\n*************FEATURE SELECTION EXPERIMENT***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.FEATURE_SELECTION);
            LOGGER.info(
                    "\n\n*************FEATURE SELECTION WITH UNDER SAMPLING EXPERIMENT***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.FEATURE_SELECTION_WITH_UNDER_SAMPLING);
            LOGGER.info(
                    "\n\n*************FEATURE SELECTION EXPERIMENT WITH OVER SAMPLING***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.FEATURE_SELECTION_WITH_OVER_SAMPLING);
            LOGGER.info(
                    "\n\n*************FEATURE SELECTION EXPERIMENT WITH COST SENSITIVE CLASSIFIER THRESHOLD***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum,
                    ExperimentType.FEATURE_SELECTION_WITH_COST_SENSITIVE_CLASSIFIER);
            LOGGER.info(
                    "\n\n*************FEATURE SELECTION EXPERIMENT WITH COST SENSITIVE LEARNING***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum,
                    ExperimentType.FEATURE_SELECTION_WITH_COST_SENSITIVE_LEARNING);
            LOGGER.info(
                    "\n\n************EXPERIMENT WITH COST SENSITIVE CLASSIFIER THRESHOLD***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.COST_SENSITIVE_CLASSIFIER);
            LOGGER.info(
                    "\n\n*************EXPERIMENT WITH COST SENSITIVE LEARNING***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.COST_SENSITIVE_LEARNING);
            LOGGER.info("\n\n*************EXPERIMENT WITH OVER SAMPLING***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.OVER_SAMPLING);
            LOGGER.info("\n\n*************EXPERIMENT WITH UNDER SAMPLING***************************************\n\n");
            wekaAnalyzer.runExperiment(project, datasetNum, ExperimentType.UNDER_SAMPLING);

        } catch (Exception e) {
            LOGGER.error("Error while running experiment", e);
        }
    }

    public static int createDataset(String project, String projectCreationDate, String lastRelease)
            throws ParseException, SQLException {
        ScraperController scraperController = new ScraperController(project, AUTHOR, projectCreationDate);
        scraperController.getProjectDataFromDb(lastRelease);
        MeasureController measureController = new MeasureController(scraperController.getProject());
        for (String[] coldStartProject : COLD_START_PROJECTS) {
            LOGGER.info("Processing cold start data of project: {} until release {}", coldStartProject[0],
                    coldStartProject[1]);
            ScraperController coldStartScraperController = new ScraperController(coldStartProject[0], AUTHOR,
                    coldStartProject[2]);
            coldStartScraperController.getColdStartDataFromDb();
            measureController.addColdStartProportionProject(coldStartScraperController.getProject());
        }
        double coldStartProportion = measureController.computeColdStartProportion();
        LOGGER.info("Cold start proportion: {}", coldStartProportion);
        measureController.setColdStartProportion(coldStartProportion);
        measureController.createWalkForwardDatasets();
        return measureController.getDatasetNum();
    }

    public static void scrapeDatasetData(String project, String projectCreationDate) throws ParseException {
        ScraperController scraperController1 = new ScraperController(project, AUTHOR, projectCreationDate);
        scraperController1.saveProjectDataOnDb();
        for (String[] coldStartProject : COLD_START_PROJECTS) {
            LOGGER.info("Scraping cold start data of project: {} until release {}", coldStartProject[0],
                    coldStartProject[1]);
            ScraperController coldStartScraperController = new ScraperController(coldStartProject[0], AUTHOR,
                    coldStartProject[2]);
            coldStartScraperController.saveColdStartDataOnDb(coldStartProject[1]);
        }
    }
}
