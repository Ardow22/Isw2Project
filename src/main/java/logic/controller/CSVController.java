package logic.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import logic.model.entity.Commit;
import logic.model.entity.JavaClass;
import logic.model.entity.Release;
import logic.model.entity.Ticket;

public class CSVController {
	
	public void createDataset(ArrayList<Release> releaseList, ArrayList<Ticket> ticketList, String projName) {
		 FileWriter fileWriter = null;
		 try {
	            fileWriter = null;
	            String outname = projName + "Buggy.csv";
					    //Name of CSV for output
					    fileWriter = new FileWriter(outname);
	            fileWriter.append("Project,Class,Release,Buggy");
	            fileWriter.append("\n");
	            fileWriter.append(projName);
	            System.out.println("STO QUI");
	            //String buggyness = "No";
	            for (Release r: releaseList) {
	            	int releaseNumber = r.getNumberOfRelease();
	            	for (Commit c: r.getCommits()) {
	            		for (JavaClass jvc: c.getClassesTouched()) {
	            			fileWriter.append(String.format("%s,%s,%d,%s\n", projName, jvc.getNamePath(), releaseNumber, jvc.isBuggy()));
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
