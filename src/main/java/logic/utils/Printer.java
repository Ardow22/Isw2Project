package logic.utils;

import java.util.List;

import logic.model.entity.Release;

public class Printer {
	
	public void printReleases(List<Release> r) {
		System.out.println("Elenco di tutte le release:");
		for (int i = 0; i < r.size(); i++)
			 System.out.println("RELEASE NUMBER: "+ r.get(i).getNumberOfRelease() +" = "+"RELEASE NAME: "+r.get(i).getNameRelease() + " RELEASE DATE: "+r.get(i).getReleaseDate()+ " RELEASE ID: "+r.get(i).getIdRelease()); 	 	
	}
	
	public void printString(String s) {
		System.out.println(s);
	}


}
