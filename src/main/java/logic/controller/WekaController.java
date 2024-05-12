package logic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logic.model.entity.Release;
import logic.utils.Printer;
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
	
	protected static final List<String> FeatureSelection = new ArrayList<>(Arrays.asList("No selection", "Backward Search", "Forward Search", "Bidirectional"));
	protected static final List<String> Sampling = new ArrayList<>(Arrays.asList("No sampling", "Oversampling", "Undersampling", "SMOTE"));
	protected static final List<String> CostSensitive = new ArrayList<>(Arrays.asList("No cost sensitive", "Sensitive Threshold", "Sensitive Learning"));
	protected static final List<String> Classifiers = new ArrayList<>(Arrays.asList("Random Forest", "NaiveBayes", "IBK"));
	protected static final List<String> Accuracy = new ArrayList<>(Arrays.asList("Precision", "Recall", "AUC", "Kappa"));

	public void walkForward(List<Release> myReleaseList, String repo, CSVController csv, Printer printer) throws Exception {
		for (Release r: myReleaseList) {
			if (r.getNumberOfRelease() > 0) { //la prima release la consideriamo solo come training
				
				List<Release> testingSet = new ArrayList<>();
				List<Release> trainingSet = retrieveTrainingSet(r, myReleaseList);
			    //if (r.getCommits().size() != 0) {
			    	testingSet.add(r);
			    /*}
			    else {
			    	testingSet.add(trainingSet.get(trainingSet.size() - 1));
			    }*/
			    List<String> arffFiles = createFileArff(trainingSet, testingSet, csv, printer);
				List<Instances> trainingSetANDtestingSet = retrieveDataSet(arffFiles);
				
				printer.printStringInfo("TRAINING SET: ");
				for (Release re: trainingSet) {
					System.out.println(re.getNumberOfRelease());
				}
				printer.printStringInfo("TESTING SET: "+testingSet.get(0).getNumberOfRelease());
			    
				for (String feature: FeatureSelection) {
			    	for (String sampling: Sampling) {
			    		for (String costSensitive: CostSensitive) {
			    			for (String classifier: Classifiers) {
			    				execute(trainingSetANDtestingSet, feature, sampling, costSensitive, classifier, repo, csv, testingSet.get(0), printer);
			    				printer.printStringInfo("%n%n");
			    			}
			    		}
			    	}
			    }
				printer.printStringInfo("%n%n");
			}
		}
	}
	
	
	
	public void execute(List<Instances> trainingAndTesting, String feature, String sampling, String costSensitive, String classifier, String repo, CSVController csv, Release testingRelease, Printer printer) throws Exception {
		printer.printStringInfo("EXECUTE CON TESTING: "+testingRelease.getNumberOfRelease());
		Instances trainingSet = trainingAndTesting.get(0);
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		Instances testingSet = trainingAndTesting.get(1);
		testingSet.setClassIndex(testingSet.numAttributes() - 1);

		if (feature.equals("No selection")) {
			//System.out.println("FEATURE NO SELECTION");
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
            //System.out.println("BEST FIRST");
			
		}
		/*else if (feature.equals("Backward Search")){
			// Crea un oggetto CfsSubsetEval (valutatore)
			CfsSubsetEval eval = new CfsSubsetEval();
			// Crea un oggetto BestFirst (algoritmo di ricerca)
			BestFirst search = new BestFirst();
			String[] options = new String[]{"-D", "0"};
			search.setOptions(options);

			// Crea un oggetto AttributeSelection
			AttributeSelection attsel = new AttributeSelection();
			// Imposta il valutatore e l'algoritmo di ricerca
			attsel.setEvaluator(eval);
			attsel.setSearch(search);
	        attsel.setInputFormat(trainingSet);
			
			// Creazione dell'oggetto AttributeSelection per il set di test
		    AttributeSelection attselTest = new AttributeSelection();
		    // Impostazione del valutatore e dell'algoritmo di ricerca per il set di test
		    attselTest.setEvaluator(eval);
		    attselTest.setSearch(search);
		    attselTest.setInputFormat(testingSet);

			
			// Applica il filtro al dataset
			Instances filteredData = Filter.useFilter(trainingSet, attsel);
			trainingSet = filteredData;
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
			
			// Applicazione del filtro al set di test
		    Instances filteredTestingData = Filter.useFilter(testingSet, attselTest);
		    testingSet = filteredTestingData;
		    // Impostazione dell'indice dell'attributo di classe per il set di test
		    testingSet.setClassIndex(filteredTestingData.numAttributes() - 1);

			System.out.println("BACKWARD SEARCH");
		}
		else if (feature.equals("Forward Search")){
			// Crea un oggetto CfsSubsetEval (valutatore)
			CfsSubsetEval eval = new CfsSubsetEval();
			// Crea un oggetto BestFirst (algoritmo di ricerca)
			BestFirst search = new BestFirst();
			String[] options = new String[]{"-D", "1"};
			search.setOptions(options);

			// Crea un oggetto AttributeSelection
			AttributeSelection attsel = new AttributeSelection();
			// Imposta il valutatore e l'algoritmo di ricerca
			attsel.setEvaluator(eval);
			attsel.setSearch(search);
			attsel.setInputFormat(trainingSet);
			
			// Creazione dell'oggetto AttributeSelection per il set di test
		    AttributeSelection attselTest = new AttributeSelection();
		    // Impostazione del valutatore e dell'algoritmo di ricerca per il set di test
		    attselTest.setEvaluator(eval);
		    attselTest.setSearch(search);
		    attselTest.setInputFormat(testingSet);

			// Applica il filtro al dataset
			Instances filteredData = Filter.useFilter(trainingSet, attsel);
			trainingSet = filteredData;
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);   
			
			// Applicazione del filtro al set di test
		    Instances filteredTestingData = Filter.useFilter(testingSet, attselTest);
		    testingSet = filteredTestingData;
		    // Impostazione dell'indice dell'attributo di classe per il set di test
		    testingSet.setClassIndex(testingSet.numAttributes() - 1);
			System.out.println("FORWARD SEARCH");
		}
		else if (feature.equals("Bidirectional")){
			// Crea un oggetto CfsSubsetEval (valutatore)
			CfsSubsetEval eval = new CfsSubsetEval();
			// Crea un oggetto BestFirst (algoritmo di ricerca)
			BestFirst search = new BestFirst();
			String[] options = new String[]{"-D", "2"};
			search.setOptions(options);

			// Crea un oggetto AttributeSelection
			AttributeSelection attsel = new AttributeSelection();
			// Imposta il valutatore e l'algoritmo di ricerca
			attsel.setEvaluator(eval);
			attsel.setSearch(search);
			attsel.setInputFormat(trainingSet);
			
			// Creazione dell'oggetto AttributeSelection per il set di test
		    AttributeSelection attselTest = new AttributeSelection();
		    // Impostazione del valutatore e dell'algoritmo di ricerca per il set di test
		    attselTest.setEvaluator(eval);
		    attselTest.setSearch(search);
		    attselTest.setInputFormat(testingSet);
		    
			// Applica il filtro al dataset
			Instances filteredData = Filter.useFilter(trainingSet, attsel);
			trainingSet = filteredData;
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);  
			
			// Applicazione del filtro al set di test
		    Instances filteredTestingData = Filter.useFilter(testingSet, attselTest);
		    testingSet = filteredTestingData;
		    // Impostazione dell'indice dell'attributo di classe per il set di test
		    testingSet.setClassIndex(testingSet.numAttributes() - 1);
		        
			System.out.println("BIDIRECTIONAL");
		}*/
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
			//System.out.println("NO SAMPLING");
		}
		else if (sampling.equals("Oversampling")) {
			// Apply Resample filter for oversampling
            Resample resampleFilter = new Resample();
            resampleFilter.setSampleSizePercent(oversamplingRatio*100);
            resampleFilter.setBiasToUniformClass(1.0);

            // Oversample the minority class
            resampleFilter.setInputFormat(minorityClassInstances);
            Instances oversampledMinorityInstances = Filter.useFilter(minorityClassInstances, resampleFilter);

            // Combine oversampled minority instances with majority instances
            Instances oversampledData = new Instances(trainingSet, 0);
            oversampledData.addAll(majorityClassInstances);
            oversampledData.addAll(oversampledMinorityInstances);

            trainingSet = oversampledData;
            //System.out.println("OVERSAMPLING");
		}
		else if (sampling.equals("Undersampling")) {
	        Filter samplingFilter = new SpreadSubsample();
	        String[] options = new String[]{"-M", "1.0"};
	        samplingFilter.setOptions(options);
	        samplingFilter.setInputFormat(trainingSet);
	        Instances undersampledData = Filter.useFilter(trainingSet, samplingFilter);
	        trainingSet = undersampledData;
	        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
	        
	        //System.out.println("UNDERSAMPLING");
			
		}
		else if (sampling.equals("SMOTE")) {
			SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);

            //make both groups the same dimension
            String percentageToCreate = "0";
            if(minoritySize !=0)
                percentageToCreate= String.valueOf((majoritySize-minoritySize)/(double)minoritySize*100.0);

            String[] opts = new String[]{ "-P", percentageToCreate};
            smote.setOptions(opts);

            trainingSet = Filter.useFilter(trainingSet, smote);
            //System.out.println("SMOTE");
		}
		
		Classifier actualClassifier = null;
		if (classifier.equals("Random Forest")) {
			actualClassifier = new RandomForest();
			//System.out.println("RANDOM FOREST");

		}
		else if (classifier.equals("NaiveBayes")) {
			actualClassifier = new NaiveBayes();
			//System.out.println("NAIVE BAYES");
		}
		else if (classifier.equals("IBK")) {
			actualClassifier = new IBk();
			//System.out.println("IBK");		
		}
		
		Evaluation eval = null;
		
		if (costSensitive.equals("No cost sensitive")) {			
			// Addestrare il modello utilizzando il training set
			actualClassifier.buildClassifier(trainingSet);
			// Valutazione delle prestazioni del modello utilizzando il testing set
			eval = new Evaluation(testingSet);
			eval.evaluateModel(actualClassifier, testingSet);
			//System.out.println("NO COST SENSITIVE");
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
	        //System.out.println("SENSITIVE THRESHOLD");


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
	        eval = new Evaluation(testingSet);
	        eval.evaluateModel(c1, testingSet);
	        //System.out.println("SENSITIVE LEARNING");
		}
		
		double precision = eval.precision(0);
		double recall = eval.recall(0);
		double kappa = eval.kappa();
		double auc = eval.areaUnderROC(0);
		csv.writeResults(repo, testingRelease.getNumberOfRelease(), classifier, precision, recall, kappa, auc, printer);
	} 
	

	private List<Instances> retrieveDataSet(List<String> arffFiles) throws IOException {
		List<Instances> trainingData_testingData = new ArrayList<>();
		
		for (String arffFile : arffFiles) {
            ArffLoader loader = new ArffLoader();
            loader.setFile(new File(arffFile));
            Instances data = loader.getDataSet();
            trainingData_testingData.add(data);
        }

        return trainingData_testingData;
	}




	private ArrayList<String> createFileArff(List<Release> trainingSet, List<Release> testingSet, CSVController csv, Printer printer) throws Exception {
		ArrayList<String> csvFileNames = new ArrayList<>();
		ArrayList<String> arffFileNames = new ArrayList<>();
		String trS = "trainingSet";
		String tsS = "testingSet";
	    csvFileNames.add(csv.createWekaDataset(trainingSet, trS, printer));
		csvFileNames.add(csv.createWekaDataset(testingSet, tsS, printer));
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
				//if (r.getCommits().size() != 0) {
					myTrainingSet.add(r);
				/*}
				else {
					myTrainingSet.add(myTrainingSet.get(myTrainingSet.size() - 1));
				}*/
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
