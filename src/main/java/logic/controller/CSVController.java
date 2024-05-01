package logic.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import logic.model.entity.JavaClass;
import logic.model.entity.Release;

public class CSVController {
	
	public void createDataset(ArrayList<Release> releaseList, String projName) {
		 FileWriter fileWriter = null;
		 try {
	            fileWriter = null;
	            String outname = projName + "Buggy.csv";
					    //Name of CSV for output
					    fileWriter = new FileWriter(outname);
	            fileWriter.append("Project,Class,Release,nAuth,ChangeSetSize,LinesOfComment,LOC,maxChangeSetSize,nAttributes,nMethods,nCommits,nFixCommits,Age,Buggy");
	            fileWriter.append("\n");
	            fileWriter.append(projName);
	            for (Release r: releaseList) {
	            	int releaseNumber = r.getNumberOfRelease();
	            	if (r.getCommits().size() != 0) {
	            		for (JavaClass jvc: r.getJavaClasses()) {
		                    fileWriter.append(String.format("%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s \n", projName, jvc.getNamePath(), releaseNumber, jvc.getNumberOfAuthors(), jvc.getChangeSetSize(), jvc.getLinesOfComments(), jvc.getLOC(),jvc.getMaxChangeSetSize(), jvc.getNumberOfAttributes(), jvc.getNumberOfMethods(), jvc.getNumberOfCommits(), jvc.getNumberOfFixDefects(), r.getAgeOfRelease(), jvc.isBuggy()));
		            	}
	            	}
	            }
	         } catch (Exception e) {
	            System.out.println("Error in csv writer");
	            e.printStackTrace();
	         } finally {
	            try {
	               fileWriter.flush();
	               fileWriter.close();
	            } catch (IOException e) {
	               System.out.println("Error while flushing/closing fileWriter !!!");
	               e.printStackTrace();
	            }
	         }
	         return;
	   }

}
