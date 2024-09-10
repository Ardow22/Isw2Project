package logic.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import java.util.Locale;

import org.slf4j.Logger;

import logic.model.entity.JavaClass;
import logic.model.entity.Release;

public class CSVController {
	
	String alert1 = "Error in csv writer";
	String alert2 = "Error while flushing/closing fileWriter !!!";
	String openjpa = "openjpa";
	String bookkeeper = "bookkeeper";
	
	public String createDataset(List<Release> releaseList, String projName, Logger logger) {
		 String outname = "";
		 if (projName.equals(bookkeeper) || projName.equals(openjpa)) {
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
		String outname = repo.equals(bookkeeper) || repo.equals(openjpa) ? repo + "MachineLearning.csv" : "";
		try (FileWriter fileWriter = new FileWriter(outname)) {

			//Name of CSV for output
			fileWriter.append("#TestingRelease, Classifier, Precision, Recall, AUC, Kappa");
			fileWriter.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outname;
	}
	
	public String createAcumeCsv(String repo) {
		String outname = repo.equals(bookkeeper) || repo.equals(openjpa) ? repo + "Acume.csv" : "";
		try (FileWriter fileWriter = new FileWriter(outname)) {

			//Name of CSV for output
			fileWriter.append("ID, Size, Prediction, Actual");
			fileWriter.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outname;
	}
	
	public void writeAcumeFile(int id, double size, double prediction, boolean actual, String name) {
        String act = "No";
		if (actual) {
        	act = "Yes";
        }
		try (FileWriter fileWriter = new FileWriter(name, true)) {
			String formatted = String.format(Locale.US, "%d,%.6f,%.6f,%s%n", id, size, prediction, act);
			fileWriter.append(formatted);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}


		
	public void writeResults(int nRelease, String classifier, double precision, double recall, double kappa, double auc, String name) {

		try (FileWriter fileWriter = new FileWriter(name, true)) {
		    // Scrive i dati
		    //fileWriter.append(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f \n", repo, nRelease, classifier, precision, recall, kappa, auc));
		    //fileWriter.append(String.format("%d,%s,%.6f,%.6f,%.6f,%.6f%n", nRelease, classifier, precision, recall, kappa, auc));
			String formatted = String.format(Locale.US, "%d,%s,%.6f,%.6f,%.6f,%.6f%n", nRelease, classifier, precision, recall, kappa, auc);
			fileWriter.append(formatted);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	 
}
