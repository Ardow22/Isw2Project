package logic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import logic.model.entity.Release;
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
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.Filter;
import weka.core.Instance;

public class WekaController {
	
	/*protected static final List<String> FeatureSelection = new ArrayList<>(Arrays.asList("No selection", "Backward Search", "Forward Search", "Bidirectional"));
	protected static final List<String> Sampling = new ArrayList<>(Arrays.asList("No sampling", "Oversampling", "Undersampling", "SMOTE"));
	protected static final List<String> CostSensitive = new ArrayList<>(Arrays.asList("No cost sensitive", "Sensitive Threshold", "Sensitive Learning"));*/
	protected static final List<String> Configuration_first = new ArrayList<>(Arrays.asList("No selection", "No sampling", "No cost sensitive"));
	protected static final List<String> Configuration_second = new ArrayList<>(Arrays.asList("Forward Search", "No sampling", "No cost sensitive"));
	protected static final List<String> Configuration_third = new ArrayList<>(Arrays.asList("Forward Search", "SMOTE", "No cost sensitive"));
	protected static final List<String> Configuration_fourth = new ArrayList<>(Arrays.asList("Forward Search", "No sampling", "Sensitive Threshold"));
	protected static final List<String> Classifiers = new ArrayList<>(Arrays.asList("Random Forest", "NaiveBayes", "IBK"));
	protected static final List<String> Accuracy = new ArrayList<>(Arrays.asList("Precision", "Recall", "AUC", "Kappa"));

	public void walkForward(List<Release> myReleaseList, String repo, CSVController csv, Logger logger) throws Exception {
		String csvF = csv.createCsv(repo);
		for (Release r: myReleaseList) {
			if (r.getNumberOfRelease() > 0) { //la prima release la consideriamo solo come training
				
				List<Release> testingSet = new ArrayList<>();
				List<Release> trainingSet = retrieveTrainingSet(r, myReleaseList);
			    testingSet.add(r);
			    List<String> arffFiles = createFileArff(trainingSet, testingSet, csv, logger);
				List<Instances> trainingSetANDtestingSet = retrieveDataSet(arffFiles);
				
				System.out.println("TRAINING SET: ");
				for (Release re: trainingSet) {
					System.out.println("NUMERO RELEASE "+re.getNumberOfRelease());
				}
				System.out.println("TESTING SET: "+testingSet.get(0).getNumberOfRelease());
				int configuration = 1;
				for (configuration = 1; configuration < 5; configuration++) {
					startWalkForward(trainingSetANDtestingSet, repo, csv, testingSet.get(0), logger, csvF, configuration);
				}
				System.out.println("\n\n");
			}
		}
	}
	
	private void startWalkForward(List<Instances> trainingSetANDtestingSet, String repo,
			CSVController csv, Release testSet, Logger logger, String csvName, int conf) throws Exception {
		List<String> parameters = new ArrayList<>();
		if (conf == 1) {
			for (String param: Configuration_first) {
				parameters.add(param);
			}
		}
		else if (conf == 2) {
			for (String param: Configuration_second) {
				parameters.add(param);
			}
		}
		else if (conf == 3) {
			for (String param: Configuration_third) {
				parameters.add(param);
			}
		}
		else {
			for (String param: Configuration_fourth) {
				parameters.add(param);
			}
		}
		execute(trainingSetANDtestingSet, parameters, csv, testSet, logger, csvName);
	}
	
	public void execute(List<Instances> trainingAndTesting, List<String> param, CSVController csv, Release testingRelease, Logger logger, String csvN) throws Exception {
		String feature = param.get(0); 
		String sampling = param.get(1);
		String costSensitive = param.get(2);
		//String classifier = param.get(3);
		System.out.println("EXECUTE CON TESTING: "+testingRelease.getNumberOfRelease());
		Instances trainingSet = trainingAndTesting.get(0);
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		Instances testingSet = trainingAndTesting.get(1);
		testingSet.setClassIndex(testingSet.numAttributes() - 1);

		if (feature.equals("No selection")) {
			System.out.println("FEATURE NO SELECTION");
		}
		else {
			// Crea un oggetto CfsSubsetEval (valutatore)
			AttributeSelection attsel = new AttributeSelection();
            CfsSubsetEval eval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

            String[] evalOptions = {"-P", "1", "-E", "1"};
            eval.setOptions(evalOptions);

            String[] searchOptions = {"-D", "1", "-N", "5"};
            search.setOptions(searchOptions);

            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            attsel.setInputFormat(trainingSet);


            // Applica il filtro al dataset
            Instances filteredData = Filter.useFilter(trainingSet, attsel);
            trainingSet = filteredData;
            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

            // Applicazione del filtro al set di test
            Instances filteredTestingData = Filter.useFilter(testingSet, attsel);
            testingSet = filteredTestingData;
            // Impostazione dell'indice dell'attributo di classe per il set di test
            testingSet.setClassIndex(filteredTestingData.numAttributes() - 1);
            System.out.println("BEST FIRST");
			
		}
		
		Instances minorityClassInstances = new Instances(trainingSet, 0);
        Instances majorityClassInstances = new Instances(trainingSet, 0);
        for (Instance instance : trainingSet) {
            if (instance.stringValue(trainingSet.numAttributes()-1).equals("yes")) {
                minorityClassInstances.add(instance);
            } else {
                majorityClassInstances.add(instance);
            }
        }

        // Calculate the oversampling ratio
        int minoritySize = minorityClassInstances.size();
        int majoritySize = majorityClassInstances.size();
        double oversamplingRatio = (double) majoritySize / minoritySize;
		
		if (sampling.equals("No sampling")) {
			System.out.println("NO SAMPLING");
		}
		else if (sampling.equals("Oversampling")) {
            Resample resampleFilter = new Resample();
            resampleFilter.setSampleSizePercent(oversamplingRatio*100);
            resampleFilter.setBiasToUniformClass(1.0);

            // Oversample the minority class
            resampleFilter.setInputFormat(minorityClassInstances);
            Instances oversampledMinorityInstances = Filter.useFilter(minorityClassInstances, resampleFilter);

            //Combina la minoranza delle istanze (maggiorata) con la maggioranza delle istanze
            Instances oversampledData = new Instances(trainingSet, 0);
            oversampledData.addAll(majorityClassInstances);
            oversampledData.addAll(oversampledMinorityInstances);

            trainingSet = oversampledData;
            System.out.println("OVERSAMPLING");
		}
		else if (sampling.equals("Undersampling")) {
	        Filter samplingFilter = new SpreadSubsample();
	        String[] options = new String[]{"-M", "1.0"};
	        samplingFilter.setOptions(options);
	        samplingFilter.setInputFormat(trainingSet);
	        Instances undersampledData = Filter.useFilter(trainingSet, samplingFilter);
	        trainingSet = undersampledData;
	        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
	        
	        System.out.println("UNDERSAMPLING");
			
		}
		else if (sampling.equals("SMOTE")) {
			SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);

            //creazione di gruppi della stessa dimensione
            String percentageToCreate = "0";
            if(minoritySize !=0)
                percentageToCreate= String.valueOf((majoritySize-minoritySize)/(double)minoritySize*100.0);

            String[] opts = new String[]{ "-P", percentageToCreate};
            smote.setOptions(opts);

            trainingSet = Filter.useFilter(trainingSet, smote);
            System.out.println("SMOTE");
		}
		
		Classifier actualClassifier = new RandomForest();
		for (String classifier: Classifiers) {
			if (classifier.equals("Random Forest")) {
				actualClassifier = new RandomForest();
				System.out.println("RANDOM FOREST");
			}
			else if (classifier.equals("NaiveBayes")) {
				actualClassifier = new NaiveBayes();
				System.out.println("NAIVE BAYES");
			}
			else if (classifier.equals("IBK")) {
				actualClassifier = new IBk();
				System.out.println("IBK");		
			}


			Evaluation eval = new Evaluation(testingSet);

			if (costSensitive.equals("No cost sensitive")) {			
				// Addestrare il modello utilizzando il training set
				actualClassifier.buildClassifier(trainingSet);
				// Valutazione delle prestazioni del modello utilizzando il testing set
				//eval = new Evaluation(testingSet);
				eval.evaluateModel(actualClassifier, testingSet);
				System.out.println("NO COST SENSITIVE");
			}

			else if (costSensitive.equals("Sensitive Threshold")) {
				// Creare la matrice dei costi
				CostMatrix costMatrix = createCostMatrix(1.0, 1.0);	
				// Creare un CostSensitiveClassifier con Sensitive Threshold
				CostSensitiveClassifier c1 = new CostSensitiveClassifier();
				c1.setClassifier(actualClassifier); // Impostare il classificatore base
				c1.setCostMatrix(costMatrix); // Impostare la matrice dei costi
				// Addestrare il modello utilizzando il training set
				c1.buildClassifier(trainingSet);
				// Valutare le prestazioni del modello utilizzando il testing set
				eval = new Evaluation(testingSet, costMatrix);
				eval.evaluateModel(c1, testingSet);
				System.out.println("SENSITIVE THRESHOLD");
			}

			//actualClassifier.distributionForInstance(testingSet);
			double precision = eval.precision(0);
			double recall = eval.recall(0);
			double kappa = eval.kappa();
			double auc = eval.areaUnderROC(0);

			//csv.writeResults(repo, testingRelease.getNumberOfRelease(), classifier, precision, recall, kappa, auc, csvN);
			csv.writeResults(testingRelease.getNumberOfRelease(), classifier, precision, recall, kappa, auc, csvN);
		}
	}

	/*private void startWalkForward(List<Instances> trainingSetANDtestingSet, String repo,
			CSVController csv, Release testSet, Logger logger, String csvName) throws Exception {
		for (String feature: FeatureSelection) {
	    	for (String sampling: Sampling) {
	    		for (String costSensitive: CostSensitive) {
	    			for (String classifier: Classifiers) {
	    				List<String> parameters = new ArrayList<>();
	    				parameters.add(feature);
	    				parameters.add(sampling);
	    				parameters.add(costSensitive);
	    				parameters.add(classifier);
	    				execute(trainingSetANDtestingSet, parameters, repo, csv, testSet, logger, csvName);
	    				System.out.println("\n\n");
	    			}
	    		}
	    	}
	    }
		
	}



	public void execute(List<Instances> trainingAndTesting, List<String> param, String repo, CSVController csv, Release testingRelease, Logger logger, String csvN) throws Exception {
		String feature = param.get(0); 
		String sampling = param.get(1);
		String costSensitive = param.get(2);
		String classifier = param.get(3);
		System.out.println("EXECUTE CON TESTING: "+testingRelease.getNumberOfRelease());
		Instances trainingSet = trainingAndTesting.get(0);
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		Instances testingSet = trainingAndTesting.get(1);
		testingSet.setClassIndex(testingSet.numAttributes() - 1);

		if (feature.equals("No selection")) {
			System.out.println("FEATURE NO SELECTION");
		}
		else {
			// Crea un oggetto CfsSubsetEval (valutatore)
			AttributeSelection attsel = new AttributeSelection();
            CfsSubsetEval eval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

            String[] evalOptions = {"-P", "1", "-E", "1"};
            eval.setOptions(evalOptions);

            String[] searchOptions = {"-D", "1", "-N", "5"};
            search.setOptions(searchOptions);

            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            attsel.setInputFormat(trainingSet);


            // Applica il filtro al dataset
            Instances filteredData = Filter.useFilter(trainingSet, attsel);
            trainingSet = filteredData;
            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

            // Applicazione del filtro al set di test
            Instances filteredTestingData = Filter.useFilter(testingSet, attsel);
            testingSet = filteredTestingData;
            // Impostazione dell'indice dell'attributo di classe per il set di test
            testingSet.setClassIndex(filteredTestingData.numAttributes() - 1);
            System.out.println("BEST FIRST");
			
		}
		
		Instances minorityClassInstances = new Instances(trainingSet, 0);
        Instances majorityClassInstances = new Instances(trainingSet, 0);
        for (Instance instance : trainingSet) {
            if (instance.stringValue(trainingSet.numAttributes()-1).equals("yes")) {
                minorityClassInstances.add(instance);
            } else {
                majorityClassInstances.add(instance);
            }
        }

        // Calculate the oversampling ratio
        int minoritySize = minorityClassInstances.size();
        int majoritySize = majorityClassInstances.size();
        double oversamplingRatio = (double) majoritySize / minoritySize;
		
		if (sampling.equals("No sampling")) {
			System.out.println("NO SAMPLING");
		}
		else if (sampling.equals("Oversampling")) {
            Resample resampleFilter = new Resample();
            resampleFilter.setSampleSizePercent(oversamplingRatio*100);
            resampleFilter.setBiasToUniformClass(1.0);

            // Oversample the minority class
            resampleFilter.setInputFormat(minorityClassInstances);
            Instances oversampledMinorityInstances = Filter.useFilter(minorityClassInstances, resampleFilter);

            //Combina la minoranza delle istanze (maggiorata) con la maggioranza delle istanze
            Instances oversampledData = new Instances(trainingSet, 0);
            oversampledData.addAll(majorityClassInstances);
            oversampledData.addAll(oversampledMinorityInstances);

            trainingSet = oversampledData;
            System.out.println("OVERSAMPLING");
		}
		else if (sampling.equals("Undersampling")) {
	        Filter samplingFilter = new SpreadSubsample();
	        String[] options = new String[]{"-M", "1.0"};
	        samplingFilter.setOptions(options);
	        samplingFilter.setInputFormat(trainingSet);
	        Instances undersampledData = Filter.useFilter(trainingSet, samplingFilter);
	        trainingSet = undersampledData;
	        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
	        
	        System.out.println("UNDERSAMPLING");
			
		}
		else if (sampling.equals("SMOTE")) {
			SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);

            //creazione di gruppi della stessa dimensione
            String percentageToCreate = "0";
            if(minoritySize !=0)
                percentageToCreate= String.valueOf((majoritySize-minoritySize)/(double)minoritySize*100.0);

            String[] opts = new String[]{ "-P", percentageToCreate};
            smote.setOptions(opts);

            trainingSet = Filter.useFilter(trainingSet, smote);
            System.out.println("SMOTE");
		}
		
		Classifier actualClassifier = new RandomForest();
		if (classifier.equals("Random Forest")) {
			actualClassifier = new RandomForest();
			System.out.println("RANDOM FOREST");

		}
		else if (classifier.equals("NaiveBayes")) {
			actualClassifier = new NaiveBayes();
			System.out.println("NAIVE BAYES");
		}
		else if (classifier.equals("IBK")) {
			actualClassifier = new IBk();
			System.out.println("IBK");		
		}
		
		Evaluation eval = new Evaluation(testingSet);
		
		if (costSensitive.equals("No cost sensitive")) {			
			// Addestrare il modello utilizzando il training set
			actualClassifier.buildClassifier(trainingSet);
			// Valutazione delle prestazioni del modello utilizzando il testing set
			//eval = new Evaluation(testingSet);
			eval.evaluateModel(actualClassifier, testingSet);
			System.out.println("NO COST SENSITIVE");
		}
		
		else if (costSensitive.equals("Sensitive Threshold")) {
			// Creare la matrice dei costi
			CostMatrix costMatrix = createCostMatrix(1.0, 1.0);	
	        // Creare un CostSensitiveClassifier con Sensitive Threshold
	        CostSensitiveClassifier c1 = new CostSensitiveClassifier();
	        c1.setClassifier(actualClassifier); // Impostare il classificatore base
	        c1.setCostMatrix(costMatrix); // Impostare la matrice dei costi
	        // Addestrare il modello utilizzando il training set
	        c1.buildClassifier(trainingSet);
	        // Valutare le prestazioni del modello utilizzando il testing set
	        eval = new Evaluation(testingSet, costMatrix);
	        eval.evaluateModel(c1, testingSet);
	        System.out.println("SENSITIVE THRESHOLD");


		}
		else if (costSensitive.equals("Sensitive Learning")) {
			// Creare la matrice dei costi
			CostMatrix costMatrix = createCostMatrix(1.0, 10.0);	
			// Creare un CostSensitiveClassifier con Sensitive Learning
	        CostSensitiveClassifier c1 = new CostSensitiveClassifier();
	        c1.setClassifier(actualClassifier); // Impostare il classificatore
	        c1.setCostMatrix(costMatrix); // Impostare la matrice dei costi
	        // Impostare il Sensitive Learning
	        c1.setMinimizeExpectedCost(false); // Impostare il Sensitive Learning
	        // Addestrare il modello utilizzando il training set
	        c1.buildClassifier(trainingSet);
	        // Valutare le prestazioni del modello utilizzando il testing set
	        //eval = new Evaluation(testingSet);
	        eval.evaluateModel(c1, testingSet);
	        System.out.println("SENSITIVE LEARNING");
		}
		
		//actualClassifier.distributionForInstance(testingSet);
		double precision = eval.precision(0);
		double recall = eval.recall(0);
		double kappa = eval.kappa();
		double auc = eval.areaUnderROC(0);
		
		csv.writeResults(repo, testingRelease.getNumberOfRelease(), classifier, precision, recall, kappa, auc, csvN);
	} */
	

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
