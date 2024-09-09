package logic.controller;

//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;

import logic.model.entity.JavaClass;
import logic.model.entity.Release;

public class CSVController {
	
	String alert1 = "Error in csv writer";
	String alert2 = "Error while flushing/closing fileWriter !!!";
	
	public String createDataset(List<Release> releaseList, String projName, Logger logger) {
		 String outname = "";
		 if (projName.equals("bookkeeper") || projName.equals("openjpa")) {
	            outname = projName + "Buggy.csv";
	     }
		    try (FileWriter fileWriter = new FileWriter(outname)) {

		        //Name of CSV for output
		        fileWriter.append("Project,Class,Release,nAuth,ChangeSetSize,LinesOfComment,LOC,maxChangeSetSize,nCommits,nFixCommits,Age,LOCadded,maxLOCadded,Buggy");
		        fileWriter.append("\n");
		        fileWriter.append(projName);

		        String buggyness = "No";
		        for (Release r: releaseList) {
		            int releaseNumber = r.getNumberOfRelease();
		            for (JavaClass jvc: r.getJavaClasses()) {
		                if (jvc.isBuggy()) {
		                    buggyness = "Yes";
		                } else {
		                    buggyness = "No";
		                }
		                //fileWriter.append(String.format("%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s \n", projName, jvc.getNamePath(), releaseNumber, jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(), jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), r.getAgeOfRelease(), jvc.getlOCadded(), jvc.getMaxLocAdded(), buggyness));
		                fileWriter.append(String.format("%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s%n", projName, jvc.getNamePath(), releaseNumber, jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(), jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), r.getAgeOfRelease(), jvc.getlOCadded(), jvc.getMaxLocAdded(), buggyness));
		            }
		        }
		    } catch (IOException e) {
		        logger.info(alert1);
		        e.printStackTrace();
		    }

		    return outname;
	   }
	
	public String createWekaDataset(List<Release> releaseList, String projName, Logger logger) {
		String outname = projName + ".csv";
		try (FileWriter fileWriter = new FileWriter(outname)) {
			//Name of CSV for output
			fileWriter.append("nAuth,ChangeSetSize,LinesOfComment,LOC,maxChangeSetSize,nCommits,nFixCommits,locAdded,maxLocAdded,Age,Buggy");
			fileWriter.append("\n");

			String buggyness = "No";
			for (Release r : releaseList) {
				for (JavaClass jvc : r.getJavaClasses()) {
					if (jvc.isBuggy()) {
						buggyness = "Yes";
					} else {
						buggyness = "No";
					}
					//fileWriter.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s \n", jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(), jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), jvc.getlOCadded(), jvc.getMaxLocAdded(), r.getAgeOfRelease(), buggyness));
					fileWriter.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s%n", jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(), jvc.getMaxChangeSetSize(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), jvc.getlOCadded(), jvc.getMaxLocAdded(), r.getAgeOfRelease(), buggyness));
				}
			}
		} catch (IOException e) {
			logger.info(alert1);
			e.printStackTrace();
		}
		return outname;
	}
	
	public String createCsv(String repo) {
		String outname = repo.equals("bookkeeper") || repo.equals("openjpa") ? repo + "MachineLearning.csv" : "";
		try (FileWriter fileWriter = new FileWriter(outname)) {

			//Name of CSV for output
			fileWriter.append("#TestingRelease, Classifier, Precision, Recall, AUC, Kappa");
			fileWriter.append("\n");
		} catch (IOException e) {
			System.out.println(alert1);
			e.printStackTrace();
		}
		return outname;
	}

	/*public void writeResults(String repo, int nRelease, String classifier, double precision, double recall, double kappa, double auc) {
		String outname = repo.equals("bookkeeper") || repo.equals("openjpa") ? repo + "MachineLearning.csv" : "";

		try (FileWriter fileWriter = new FileWriter(outname, true)) {
		    // Verifica se il file esiste già
		    boolean fileExists = new File(outname).exists();

		    // Scrive l'intestazione solo se il file è appena stato creato
		    if (!fileExists) {
		        fileWriter.append("Dataset, #TestingRelease, Classifier, Precision, Recall, AUC, Kappa");
		        fileWriter.append("\n");
		    }

		    // Scrive i dati
		    //fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f \n", repo, nRelease, classifier, precision, recall, kappa, auc));
		    fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f%n", repo, nRelease, classifier, precision, recall, kappa, auc));
		} catch (IOException e) {
		    System.out.println(alert1);
		    e.printStackTrace();
		}
	}*/
	
	public void writeResults(int nRelease, String classifier, double precision, double recall, double kappa, double auc, String name) {

		try (FileWriter fileWriter = new FileWriter(name, true)) {
		    // Scrive i dati
		    //fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f \n", repo, nRelease, classifier, precision, recall, kappa, auc));
		    fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f%n", nRelease, classifier, precision, recall, kappa, auc));
		} catch (IOException e) {
		    System.out.println(alert1);
		    e.printStackTrace();
		}
	}
	
	 
}
