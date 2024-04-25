package logic.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import logic.model.entity.Release;
import logic.utils.Printer;

public class HandlerController {
	
	public static final ReleaseController Rc = new ReleaseController();
	public static final Printer printer = new Printer();
	
	public void startAnalysis(String repository) throws IOException, JSONException{
		System.out.println("Analisi del progetto "+ repository);
		
		//RECUPERO LA LISTA DELLE RELEASE
		ArrayList<Release> releaseList = new ArrayList<Release>();
	    releaseList = Rc.ListRelease(repository.toUpperCase());
	    Rc.setNumberReleases(releaseList);
	    printer.printReleases(releaseList);
	}

}
