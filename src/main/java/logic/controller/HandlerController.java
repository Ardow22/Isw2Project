package logic.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.json.JSONException;

import logic.model.entity.Release;
import logic.model.entity.Ticket;
import logic.model.entity.Commit;
import logic.model.entity.JavaClass;
import logic.utils.Printer;

public class HandlerController {
	
	public static final ReleaseController Rc = new ReleaseController();
	public static final CommitController Cc = new CommitController();
	public static final TicketController Tc = new TicketController();
	public static final MetricsController Mc = new MetricsController();
	public static final CSVController csv = new CSVController();
	public static final Printer printer = new Printer();
	
	public void startAnalysis(String repository) throws IOException, JSONException, RevisionSyntaxException, NoHeadException, GitAPIException, ParseException{
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
	    for (Release r: releaseList) {
	    	r.setCommits(Cc.getCommitsForRelease(r, repository, lastReleaseDate, iteration));
	    	System.out.println("RELEASE " + r.getNameRelease());
	    	System.out.println("COMMIT TOTALI:"+r.getCommits().size());
	    	System.out.println("\n");
	    	lastReleaseDate = r.getReleaseDate();
	    	iteration++;
	    }
	    
	    //RECUPERO TUTTI I TICKET 
	    printer.printString("Recupero dei ticket relativi ai bug chiusi del progetto in corso...");
	    ArrayList<Ticket> ticketList = new ArrayList<Ticket>();
		ticketList = Tc.retrieveTicketsID(repository.toUpperCase(), releaseList);
		Collections.reverse(ticketList);
		for (Ticket t: ticketList) {
			System.out.println("Ticket: "+t.getKey());
		}
		System.out.println("I ticket trovati (CON FIX VERSION) sono in tutto "+ticketList.size());
		
		//ASSOCIO I COMMIT AD I RELATIVI TICKET
		printer.printString("Recupero dei commit associati al ticket specifico in corso...");
		for (Ticket t: ticketList) {
			t.setCommitsForTicket(Tc.searchCommitsForTicket(t, releaseList));	
			System.out.println("TICKET: "+t.getKey());
			System.out.println("COMMIT TROVATI: "+t.getCommitsForTicket().size());
			System.out.println("\n");
		}
		
		//ELIMINO TUTTI I TICKET CHE NON HANNO COMMIT ASSOCIATI
		ArrayList<Ticket> myTktList = new ArrayList<Ticket>();
		for (Ticket t: ticketList) {
			if (t.getCommitsForTicket().size() != 0) {
				myTktList.add(t);
			}
		}
		
		printer.printString("Calcolo delle Opening Version e delle Injected Version in corso...");
	    for (Ticket t: myTktList) {
	    	//IL PRIMO AV DEVE ESSERE PRIMA DI OV, QUINDI IV <= OV
	    	t.setOpeningVersion(Tc.calculateOpeningVersion(t, releaseList));
	    }
	    
	    //TOLGO TUTTI I TICKET CON VALORI DI FV E OV NON CONGRUI
	    ArrayList<Ticket> myTktList2 = new ArrayList<Ticket>();
	    for (Ticket t: myTktList) {
	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList2.add(t);
	    	}
	    }
	    
	    for (Ticket t: myTktList2) {
	    	t.setInjectedVersion(Tc.calculateInjectedVersion(t, releaseList, myTktList2, Rc));
	    }
	   
	    
	    //CONSIDERO SOLO I TICKET CHE HANNO IV E OV CONGRUI
	    ArrayList<Ticket> myTktList3 = new ArrayList<Ticket>();
	    for (Ticket t: myTktList2) {
	    	if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList3.add(t);
	    	} 
	    }
	    
	    //TOLGO ANCHE I TICKET CHE HANNO INJECTED VERSION E FIX VERSION UGUALI, PERCHé SIGNIFICA CHE IL BUG NON C'è
	    ArrayList<Ticket> myTicketList = new ArrayList<Ticket>();
	    for (Ticket t: myTktList3) {
	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
	    		myTicketList.add(t);
	    	} 
	    }
	    
	    //ELENCO DEI TICKET RIMASTI
	    for (Ticket t: myTicketList) {
	    	System.out.println("Ticket: "+t.getKey());
	    	System.out.println("IV: "+t.getInjectedVersion().getNumberOfRelease());
	    	System.out.println("OV: "+t.getOpeningVersion().getNumberOfRelease());
	    	System.out.println("FV: "+t.getFixVersion().getNumberOfRelease());
	    	System.out.println("\n"); 	
	    }
	    /*
	    //CONSIDERO SOLO LA PRIMA METà DELLE RELEASE
	    int halfSize = releaseList.size() / 2;
		ArrayList<Release> myReleaseList = new ArrayList<>(releaseList.subList(0, halfSize));
		
		printer.printString("Recupero delle classi toccate da ogni commit di ogni release in corso...");
		for (Release r: myReleaseList) {
			printer.printString("\n");
			printer.printString("In corso l'analisi della release: " + r.getNameRelease());
			ArrayList<String> myClassList = new ArrayList<String>();//lista che conterrà tutte le classi del progetto nella release.
	    	for (Commit c: r.getCommits()) {
	    		printer.printString("In corso l'analisi del commit: "+ c.getId());
	    		c.setClassesTouched(Cc.getClasses(c.getCommit(), repository));
	    		for (JavaClass jvc: c.getClassesTouched()) {
	    			jvc.setRelease(r);
	    			if (!myClassList.contains(jvc.getNamePath())) {
	    				myClassList.add(jvc.getNamePath());
	    			}
	    		}
	    		if(c.getTicket() != null && myTicketList.contains(c.getTicket())) {//Verifico che il ticket sia congruo
	    			if (c.getTicket().getAffversions().size() != 0) {
	    				if (c.getTicket().getAffversions().contains(r)) {
	    					for (JavaClass jclass: c.getClassesTouched()) {
	    						jclass.setBuggy(true);
	    					}
                    	}
                    }
                    else {
                    	if (r.getNumberOfRelease() >= c.getTicket().getInjectedVersion().getNumberOfRelease() && r.getNumberOfRelease() < c.getTicket().getFixVersion().getNumberOfRelease()) {
                    		for (JavaClass jclass: c.getClassesTouched()) {
                    			jclass.setBuggy(true);
	    					}
	    				}	
                    }
	    		}
	    	}
	    	//printer.printString("\nCalcolo delle metriche in corso...");
	    	//Mc.calculateMetrics(r, myClassList, myTicketList, repository);
	    }
		
		printer.printString("Creazione del file csv in corso...");
		csv.createDataset(myReleaseList, myTicketList, repository);*/
	
		
	}

}
