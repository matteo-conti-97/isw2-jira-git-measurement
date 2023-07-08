package com.isw2.weka;

import com.isw2.util.ClassifierType;
import com.isw2.util.ExperimentType;
import com.isw2.util.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.classifiers.meta.FilteredClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WekaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WekaAnalyzer.class);
    private static final String DATASET_SET_PATH = "src/main/java/resource/arff/dataset_";
    private static final String[] FEATURES={ "#AuthorsInRelease", "LOC", "AvgChurnInRelease", "AvgChurnFromStart", "AvgLOCAddedInRelease", "AvgLOCAddedFromStart", "#RevisionInRelease", "#RevisionFromStart", "#BugFixInRelease", "#BugFixFromStart"};

    List<List<String>> generateDatasetPaths(String project, int datasetNum) {
        List<List<String>> ret = new ArrayList<>();
        for (int i = 2; i <= datasetNum; i++) {
            List<String> dataset = new ArrayList<>();
            String trainingSetPath=DATASET_SET_PATH + project + "_" + i + "Train.arff";
            String testingSetPath=DATASET_SET_PATH + project + "_" + i + "Test.arff";
            dataset.add(trainingSetPath);
            dataset.add(testingSetPath);
            ret.add(dataset);
        }
        return ret;
    }

    private List<Double> evualuateClassifier(Instances trainingSet, Instances testingSet, ClassifierType classifierType, FilteredClassifier filteredClassifier) throws Exception {
        List<Double> ret = new ArrayList<>();
        Classifier classifier = null;

        switch(classifierType) {
            case NAIVE_BAYES:
                classifier = new NaiveBayes();
                LOGGER.info("\nUsing Naive Bayes classifier");
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                LOGGER.info("\nUsing Random Forest classifier");
                break;
            case IBK:
                classifier = new IBk();
                LOGGER.info("\nUsing IBK classifier");
                break;
            default:
                LOGGER.error("\nInvalid classifier type");
        }

        assert classifier != null;


        Evaluation eval = new Evaluation(testingSet);
        if(filteredClassifier!=null) { //Uso il classificatore che fa sampling
            filteredClassifier.setClassifier(classifier);
            filteredClassifier.buildClassifier(trainingSet);
            eval.evaluateModel(filteredClassifier, testingSet);
        }
        else {
            classifier.buildClassifier(trainingSet);
            eval.evaluateModel(classifier, testingSet);
        }

        int classIndex = 0; // First class, the buggy "YES"
        double kappa = eval.kappa();
        ret.add(kappa);
        LOGGER.info("Kappa = {}", kappa);
        double precision = eval.precision(classIndex);
        ret.add(precision);
        LOGGER.info("Precision = {}", precision);
        double recall = eval.recall(classIndex);
        ret.add(recall);
        LOGGER.info("Recall = {}", recall);
        double auc = eval.areaUnderROC(classIndex);
        ret.add(auc);
        LOGGER.info("AUC = {}", auc);
        double[][] confusionMatrix = eval.confusionMatrix();
        double tp = confusionMatrix[0][0];
        double fn = confusionMatrix[0][1];
        double fp = confusionMatrix[1][0];
        double tn = confusionMatrix[1][1];
        ret.add(tp);
        ret.add(fn);
        ret.add(fp);
        ret.add(tn);
        LOGGER.info("TP: {} FN: {}: FP: {} TN: {}\n\n", tp, fn, fp, tn);
        return ret;
    }

    private List<Instances> featureSelection(Instances trainingSet, Instances testingSet) throws Exception {
        CfsSubsetEval subsetEval = new CfsSubsetEval();
        BestFirst bestFirstSrc = new BestFirst();
        bestFirstSrc.setDirection(new SelectedTag(BestFirst.TAGS_SELECTION[2].getID(), BestFirst.TAGS_SELECTION)); //Bidirectional ASSUNZIONE 24
        bestFirstSrc.setSearchTermination(10);
        AttributeSelection filter = new AttributeSelection();
        filter.setEvaluator(subsetEval);
        filter.setSearch(bestFirstSrc);
        filter.setInputFormat(trainingSet);
        Instances filteredTrainingSet = Filter.useFilter(trainingSet, filter);
        Instances filteredTestingSet = Filter.useFilter(testingSet, filter);
        // Print the selected attribute subset
        int[] selectedAttributes = bestFirstSrc.search(subsetEval, trainingSet);
        LOGGER.info("Selected features: ");
        for (int selectedAttribute : selectedAttributes) {
            LOGGER.info("Feature: {}", FEATURES[selectedAttribute]);
        }
        return Arrays.asList(filteredTrainingSet, filteredTestingSet);
    }

    public void runExperiment(String project, int datasetNum, ExperimentType type) throws Exception {
        List<List<String>> datasetPath= generateDatasetPaths(project, datasetNum);
        List<Double> nvEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        List<Double> ibkEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        List<Double> rfEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        int numDataset = datasetPath.size();
        LOGGER.info("Analyzing project: {}", project);
        for(List<String> dataset: datasetPath){
            String trainingSetPath=dataset.get(0);
            String testingSetPath=dataset.get(1);
            LOGGER.info("Training set path: {}", trainingSetPath);
            LOGGER.info("Testing set path: {}\n", testingSetPath);

            //load datasets
            DataSource trainingSetSrc = new DataSource(dataset.get(0));
            DataSource testingSetSrc = new DataSource(dataset.get(1));
            Instances vanillaTrainingSet = trainingSetSrc.getDataSet();
            Instances vanillaTestingSet = testingSetSrc.getDataSet();
            Instances trainingSet = null;
            Instances testingSet = null;
            List<Instances> filteredSets = null;
            FilteredClassifier samplingFilter = null;
            int numAttr = 0;

            switch(type){
                case FEATURE_SELECTION: //Feature selection backwards
                    LOGGER.info("Running feature selection experiment");
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    break;

                case FEATURE_SELECTION_WITH_UNDER_SAMPLING:
                    LOGGER.info("Running feature selection with undersampling experiment");
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    SpreadSubsample spreadSubsample = new SpreadSubsample();
                    spreadSubsample.setInputFormat(trainingSet);
                    spreadSubsample.setOptions(new String[] {"-M", "1.0"});
                    samplingFilter = new FilteredClassifier();
                    samplingFilter.setFilter(spreadSubsample);
                    break;

                case FEATURE_SELECTION_WITH_OVER_SAMPLING:
                    LOGGER.info("Running feature selection with oversampling experiment");
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    Resample resample = new Resample();
                    resample.setInputFormat(trainingSet);
                    String sampleSizePercentage = computeSampleSizePercentage(trainingSet);
                    resample.setOptions(new String[] {"-B", "1.0", "-S", "1", "-Z", sampleSizePercentage});
                    samplingFilter = new FilteredClassifier();
                    samplingFilter.setFilter(resample);
                    break;

                case FEATURE_SELECTION_WITH_COST_SENSITIVE:
                    LOGGER.info("Running feature selection with cost sensitive experiment");
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    break;

                default:
                    LOGGER.error("Running vanilla experiment");
                    trainingSet = vanillaTrainingSet;
                    testingSet = vanillaTestingSet;
                    numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    break;
            }

            List<Double> tmpNb;
            List<Double> tmpIbk;
            List<Double> tmpRf;

            //Evaluate classifiers
            tmpNb=evualuateClassifier(trainingSet, testingSet, ClassifierType.NAIVE_BAYES, samplingFilter);
            tmpIbk=evualuateClassifier(trainingSet, testingSet, ClassifierType.IBK, samplingFilter);
            tmpRf=evualuateClassifier(trainingSet, testingSet, ClassifierType.RANDOM_FOREST, samplingFilter);
            //Sum up the evaluation results
            for(int i=0;i<8;i++){
                nvEval.set(i, nvEval.get(i)+tmpNb.get(i));
                ibkEval.set(i, ibkEval.get(i)+tmpIbk.get(i));
                rfEval.set(i, rfEval.get(i)+tmpRf.get(i));
            }
        }
        //Average the evaluation results
        List<Double> nbAvg = computeMeanEval(nvEval, numDataset);
        List<Double> ibkAvg = computeMeanEval(ibkEval, numDataset);
        List<Double> rfAvg = computeMeanEval(rfEval, numDataset);

        //Print the average evaluation results
        Printer.printMeanEval(nbAvg, ClassifierType.NAIVE_BAYES);
        Printer.printMeanEval(ibkAvg, ClassifierType.IBK);
        Printer.printMeanEval(rfAvg, ClassifierType.RANDOM_FOREST);
    }

    private String computeSampleSizePercentage(Instances trainingSet) {
        double ret;
        int positive=0;
        int negative=0;
        for (weka.core.Instance instance : trainingSet) {
            if(instance.classValue()==0.0) positive++;
            else negative++;
        }
        assert negative !=0;
        if(positive>negative) ret= 100*(positive - negative)/(double)negative;
        else ret= 100*(negative - positive)/(double)positive;
        return String.valueOf(ret);
    }

    private List<Double> computeMeanEval(List<Double> eval, int numDataset){
        return new ArrayList<>(Arrays.asList(eval.get(0)/numDataset, eval.get(1)/numDataset, eval.get(2)/numDataset, eval.get(3)/numDataset, eval.get(4)/numDataset, eval.get(5)/numDataset, eval.get(6)/numDataset, eval.get(7)/numDataset));
    }
}




