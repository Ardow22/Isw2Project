package logic.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import logic.model.entity.JavaClass;
import logic.model.entity.Release;
import logic.utils.Printer;

public class CSVController {
	
	String alert1 = "Error in csv writer";
	String alert2 = "Error while flushing/closing fileWriter !!!";
	
	public String createDataset(List<Release> releaseList, String projName, Printer printer) {
		 FileWriter fileWriter = null;
		 String outname = "";
		 try {
	            fileWriter = null;
	            if (projName.equals("bookkeeper") || projName.equals("openjpa")) {
	            	outname = projName + "Buggy.csv";
	            }
					    //Name of CSV for output
					    fileWriter = new FileWriter(outname);
	            fileWriter.append("Project,Class,Release,nAuth,ChangeSetSize,LinesOfComment,LOC,maxChangeSetSize,nCommits,nFixCommits,Age,Buggy");
	            fileWriter.append("\n");
	            fileWriter.append(projName);
	            String buggyness = "No";
	            for (Release r: releaseList) {
	            	int releaseNumber = r.getNumberOfRelease();
	            	//if (r.getCommits().size() != 0) {
	            		for (JavaClass jvc: r.getJavaClasses()) {
	            			if (jvc.isBuggy()) {
	            				buggyness = "Yes";
	            			}
	            			else {
	            				buggyness = "No";
	            			}
		                    fileWriter.append(String.format("%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s \n", projName, jvc.getNamePath(), releaseNumber, jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(),jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), r.getAgeOfRelease(), buggyness));
		            	}
	            	//}
	            }
	         } catch (Exception e) {
	            printer.printStringSevere(alert1);
	            e.printStackTrace();
	         } finally {
	            try {
	               fileWriter.flush();
	               fileWriter.close();
	            } catch (IOException e) {
	               printer.printStringSevere(alert2);
	               e.printStackTrace();
	            }
	         }
	         return outname;
	   }
	
	public String createWekaDataset(List<Release> releaseList, String projName, Printer printer) {
		 FileWriter fileWriter = null;
		 String outname = "";
		 try {
	            fileWriter = null;
	            
	            outname = projName+ ".csv";
	            
					    //Name of CSV for output
					    fileWriter = new FileWriter(outname);
	            fileWriter.append("nAuth,ChangeSetSize,LinesOfComment,LOC,maxChangeSetSize,nCommits,nFixCommits,Age,Buggy");
	            fileWriter.append("\n");
	            String buggyness = "No";
	            for (Release r: releaseList) {
	            	//if (r.getCommits().size() != 0) {
	            		for (JavaClass jvc: r.getJavaClasses()) {
	            			if (jvc.isBuggy()) {
	            				buggyness = "Yes";
	            			}
	            			else {
	            				buggyness = "No";
	            			}
		                    fileWriter.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%s \n", jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(),jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), r.getAgeOfRelease(), buggyness));
		            	}
	            	//}
	            }
	         } catch (Exception e) {
	            printer.printStringSevere(alert1);
	            e.printStackTrace();
	         } finally {
	            try {
	               fileWriter.flush();
	               fileWriter.close();
	            } catch (IOException e) {
	               printer.printStringSevere(alert2);
	               e.printStackTrace();
	            }
	         }
	         return outname;
	   }

	public void writeResults(String repo, int nRelease, String classifier, double precision, double recall, double kappa, double auc, Printer printer) {
	    FileWriter fileWriter = null;
	    String outname = repo.equals("bookkeeper") || repo.equals("openjpa") ? repo + "MachineLearning.csv" : "";

	    try {
	        // Verifica se il file esiste già
	        boolean fileExists = new File(outname).exists();
	        
	        // Apre il FileWriter in modalità di aggiunta se il file esiste, altrimenti lo crea
	        fileWriter = new FileWriter(outname, true);
	        
	        // Scrive l'intestazione solo se il file è appena stato creato
	        if (!fileExists) {
	            fileWriter.append("Dataset, #TestingRelease, Classifier, Precision, Recall, AUC, Kappa");
	            fileWriter.append("\n");
	        }
	        
	        // Scrive i dati
	        fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f\n", repo, nRelease, classifier, precision, recall, kappa, auc));

	    } catch (IOException e) {
	        printer.printStringSevere(alert1);
	        e.printStackTrace();
	    } finally {
	        try {
	            if (fileWriter != null) {
	                fileWriter.flush();
	                fileWriter.close();
	            }
	        } catch (IOException e) {
	            printer.printStringSevere(alert2);
	            e.printStackTrace();
	        }
	    }
	}


}
