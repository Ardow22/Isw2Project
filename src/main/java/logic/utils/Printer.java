package logic.utils;

import java.util.List;
import java.util.logging.Logger;

import logic.model.entity.Release;

public class Printer {
	
	private static final Logger LOGGER = Logger.getLogger(Printer.class.getName());
	
	public void printReleases(List<Release> r) {
		System.out.println("Elenco di tutte le release:");
		for (int i = 0; i < r.size(); i++)
			 System.out.println("RELEASE NUMBER: "+ r.get(i).getNumberOfRelease() +" = "+"RELEASE NAME: "+r.get(i).getNameRelease() + " RELEASE DATE: "+r.get(i).getReleaseDate()+ " RELEASE ID: "+r.get(i).getIdRelease()); 	 	
	}
	
	public void printStringInfo(String s) {
		LOGGER.info(s);
	}
	
	public void printStringWarning(String s) {
		LOGGER.warning(s);
	}
	
	public void printStringSevere(String s) {
		LOGGER.severe(s);
	}


}
