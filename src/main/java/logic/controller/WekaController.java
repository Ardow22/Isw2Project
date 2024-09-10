package logic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import logic.model.entity.Release;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.classifiers.trees.RandomForest;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.Filter;

public class WekaController {

	public void walkForward(List<Release> myReleaseList, String repo, CSVController csv, Logger logger) throws Exception {
		String csvF = csv.createCsv(repo);
		String csvAcume = csv.createAcumeCsv(repo);
		for (Release r: myReleaseList) {
			if (r.getNumberOfRelease() > 0) { //la prima release la consideriamo solo come training
				
				List<Release> testingSet = new ArrayList<>();
				List<Release> trainingSet = retrieveTrainingSet(r, myReleaseList);
			    testingSet.add(r);
			    List<String> arffFiles = createFileArff(trainingSet, testingSet, csv, logger);
				List<Instances> trainingSetANDtestingSet = retrieveDataSet(arffFiles);
				
				startWalkForward(trainingSetANDtestingSet, repo, csv, testingSet.get(0), logger, csvF, csvAcume);
			}
		}
	}
	
	private void startWalkForward(List<Instances> trainingAndTesting, String repo,
			CSVController csv, Release testingRelease, Logger logger, String csvName, String csvAcume) throws Exception {
		double precision = 0.0;
		double recall = 0.0;
		double kappa = 0.0;
		double auc = 0.0;
		
		Instances trainingSet = trainingAndTesting.get(0);
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		Instances testingSet = trainingAndTesting.get(1);
		testingSet.setClassIndex(testingSet.numAttributes() - 1);
		
		Evaluation eval = new Evaluation(trainingSet);
		
		/* CONFIGURAZIONE 1 */
		RandomForest rf = new RandomForest();
		rf.buildClassifier(trainingSet);
		eval.evaluateModel(rf, testingSet);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(trainingSet, testingSet, rf, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Random Forest", precision, recall, kappa, auc, csvName);
		
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(trainingSet);
		eval.evaluateModel(nb, testingSet);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(trainingSet, testingSet, nb, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Naive Bayes", precision, recall, kappa, auc, csvName);
		
		IBk iBk = new IBk();
		iBk.buildClassifier(trainingSet);
		eval.evaluateModel(iBk, testingSet);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(trainingSet, testingSet, iBk, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "IBK", precision, recall, kappa, auc, csvName);
		
		/*CONFIGURAZIONE 2 */
		AttributeSelection attsel = new AttributeSelection();
		CfsSubsetEval csEval = new CfsSubsetEval();
		
		BestFirst bf = new BestFirst();
		bf.setOptions(new String[] {"-D", "2"});
		attsel.setSearch(bf);
		attsel.setEvaluator(csEval);
        attsel.setInputFormat(trainingSet);


        // Applica il filtro al dataset
        Instances filteredTrainingData = Filter.useFilter(trainingSet, attsel);
        filteredTrainingData.setClassIndex(filteredTrainingData.numAttributes() - 1);

        // Applicazione del filtro al set di test
        Instances filteredTestingData = Filter.useFilter(testingSet, attsel);
        // Impostazione dell'indice dell'attributo di classe per il set di test
        filteredTestingData.setClassIndex(filteredTestingData.numAttributes() - 1);
        
        eval = new Evaluation(filteredTrainingData);
        
        rf = new RandomForest();
		rf.buildClassifier(filteredTrainingData);
		eval.evaluateModel(rf, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, rf, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Random Forest", precision, recall, kappa, auc, csvName);
		
		nb = new NaiveBayes();
		nb.buildClassifier(filteredTrainingData);
		eval.evaluateModel(nb, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, nb, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Naive Bayes", precision, recall, kappa, auc, csvName);
		
		iBk = new IBk();
		iBk.buildClassifier(filteredTrainingData);
		eval.evaluateModel(iBk, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, iBk, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "IBK", precision, recall, kappa, auc, csvName);
		
		/*Configurazione 3*/
		Filter samplingFilter = new SpreadSubsample();
        String[] options = new String[]{"-M", "1.0"};
        samplingFilter.setOptions(options);
        samplingFilter.setInputFormat(trainingSet);
        FilteredClassifier filteredClassifier = new FilteredClassifier();
        filteredClassifier.setFilter(samplingFilter);
        
        eval = new Evaluation(filteredTrainingData);
        
        rf = new RandomForest();
        filteredClassifier.setClassifier(nb);
        filteredClassifier.buildClassifier(filteredTrainingData);
        eval.evaluateModel(filteredClassifier, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, rf, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Random Forest", precision, recall, kappa, auc, csvName);
		
		nb = new NaiveBayes();
		filteredClassifier.setClassifier(nb);
        filteredClassifier.buildClassifier(filteredTrainingData);
        eval.evaluateModel(filteredClassifier, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, nb, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Naive Bayes", precision, recall, kappa, auc, csvName);
		
		iBk = new IBk();
		filteredClassifier.setClassifier(nb);
        filteredClassifier.buildClassifier(filteredTrainingData);
        eval.evaluateModel(filteredClassifier, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, iBk, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "IBK", precision, recall, kappa, auc, csvName);
		
		/*Configurazione 4*/
		CostSensitiveClassifier c = new CostSensitiveClassifier();
		CostMatrix newCostMatrix = createCostMatrix(1.0, 1.0);
		
		rf = new RandomForest();
        c.setCostMatrix(newCostMatrix);
        c.setClassifier(rf);
        c.buildClassifier(filteredTrainingData);
        eval.evaluateModel(c, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, rf, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Random Forest", precision, recall, kappa, auc, csvName);
		
		nb = new NaiveBayes();
		c.setCostMatrix(newCostMatrix);
        c.setClassifier(nb);
        c.buildClassifier(filteredTrainingData);
        eval.evaluateModel(c, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, nb, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "Naive Bayes", precision, recall, kappa, auc, csvName);
		
		iBk = new IBk();
		c.setCostMatrix(newCostMatrix);
        c.setClassifier(iBk);
        c.buildClassifier(filteredTrainingData);
        eval.evaluateModel(c, filteredTestingData);
		precision = eval.precision(0);
		recall = eval.recall(0);
		kappa = eval.kappa();
		auc = eval.areaUnderROC(0);
		calculateNpofB(filteredTrainingData, filteredTestingData, iBk, csv, csvAcume);
		csv.writeResults(testingRelease.getNumberOfRelease(), "IBK", precision, recall, kappa, auc, csvName);
		
		
	}
	
	private static double getPrediction(Instance instance, Classifier classifier) throws Exception {
		double[] predDist = classifier.distributionForInstance(instance);
		for(int i=0;i<predDist.length;i++){
            if (instance.classAttribute().value(i).equals("Yes")){
                return predDist[i];
            }
        }
		return 0.0;
	}
	
	private void calculateNpofB(Instances training, Instances testing, Classifier actualClassifier, CSVController csv, String acumeFile) throws Exception {
		int numtesting = testing.numInstances();
		
		int lastAttrIndex = testing.numAttributes()-1;
	    
	    actualClassifier.buildClassifier(training);
	    
	    // Loop over each test instance.
	    for (int i = 0; i < numtesting; i++) {
	    	int id = i;
	    	double size = testing.get(i).value(0);
	    	double prediction = getPrediction(testing.get(i), actualClassifier);
	    	boolean actual = testing.get(i).toString(lastAttrIndex).equals("Yes");
	    	csv.writeAcumeFile(id, size, prediction, actual, acumeFile);
	    	}
	}
	
	private List<Instances> retrieveDataSet(List<String> arffFiles) throws IOException {
		List<Instances> trainingDataAndTestingData = new ArrayList<>();
		
		for (String arffFile : arffFiles) {
            ArffLoader loader = new ArffLoader();
            loader.setFile(new File(arffFile));
            Instances data = loader.getDataSet();
            trainingDataAndTestingData.add(data);
        }

        return trainingDataAndTestingData;
	}




	private ArrayList<String> createFileArff(List<Release> trainingSet, List<Release> testingSet, CSVController csv, Logger logger) throws IOException {
		ArrayList<String> csvFileNames = new ArrayList<>();
		ArrayList<String> arffFileNames = new ArrayList<>();
		String trS = "trainingSet";
		String tsS = "testingSet";
	    csvFileNames.add(csv.createWekaDataset(trainingSet, trS, logger));
		csvFileNames.add(csv.createWekaDataset(testingSet, tsS, logger));
		// Ciclo su ciascun nome di file CSV
        for (String csvFileName : csvFileNames) {
            // Carica il file CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(csvFileName));
            Instances data = loader.getDataSet();
            
            // Imposta l'attributo di classe
            data.setClassIndex(data.numAttributes() - 1);
            
            // Crea il nome del file ARFF di output
            String arffFileName = csvFileName.replace(".csv", ".arff");
            
            // Salva il dataset in un file ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(arffFileName));
            saver.writeBatch();
            
            arffFileNames.add(arffFileName);
        }
        return arffFileNames;
		
	}

	public List<Release> retrieveTrainingSet(Release testingRelease, List<Release> myReleaseList) {
		List<Release> myTrainingSet = new ArrayList<>();
		for (Release r: myReleaseList) {
			if (r.getNumberOfRelease() < testingRelease.getNumberOfRelease()) {
				myTrainingSet.add(r);
			}
			else {
				break;
			}
		}
		return myTrainingSet;
	}
	
	private static CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
		 CostMatrix costMatrix = new CostMatrix(2);
		 costMatrix.setCell(0, 0, 0.0);
		 costMatrix.setCell(0, 1, weightFalseNegative);
		 costMatrix.setCell(1, 0, weightFalsePositive);
		 costMatrix.setCell(1, 1, 0.0);
		 return costMatrix; 
	 }	   

 }


	
	