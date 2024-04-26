package logic.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.json.JSONException;

import logic.model.entity.Release;
import logic.utils.Printer;

public class HandlerController {
	
	public static final ReleaseController Rc = new ReleaseController();
	public static final CommitController Cc = new CommitController();
	public static final Printer printer = new Printer();
	
	public void startAnalysis(String repository) throws IOException, JSONException, RevisionSyntaxException, NoHeadException, GitAPIException{
		System.out.println("Analisi del progetto "+ repository);
		
		//RECUPERO LA LISTA DELLE RELEASE
		ArrayList<Release> releaseList = new ArrayList<Release>();
	    releaseList = Rc.ListRelease(repository.toUpperCase());
	    Rc.setNumberReleases(releaseList);
	    printer.printReleases(releaseList);
	   
	  //RECUPERO TUTTI I COMMIT DI OGNI RELEASE
	    printer.printString("Recupero dei commit relativi ad ogni release in corso...");
	    String lastReleaseDate = null;
	    int iteration = 0;
	    //int countFRABERNA = 0;
	    for (Release r: releaseList) {
	    	//r.setCommits(Gc.getCommitsForRelease(r, repository));
	    	r.setCommits(Cc.getCommitsForRelease(r, repository, lastReleaseDate, iteration));
	    	//System.out.println("RELEASE " + r.getNameRelease());
	    	//System.out.println("COMMIT TOTALI:"+r.getCommits().size());
	    	//System.out.println("\n");
	    	//countFRABERNA += r.getCommits().size();
	    	lastReleaseDate = r.getReleaseDate();
	    	iteration++;
	    	break;
	    }
	}

}
