package logic.controller;

import logic.model.entity.Release;
import logic.model.entity.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.model.bean.ReleaseBean;
import logic.model.entity.Commit;
import logic.utils.Printer;

public class HandlerController {
	
	public static final ReleaseController rc = new ReleaseController();
	public static final CommitController cc = new CommitController();
	public static final TicketController tc = new TicketController();
	public static final MetricsController mc = new MetricsController();
	public static final CSVController csv = new CSVController();
	public static final WekaController wc = new WekaController();
	public static final Printer printer = new Printer();
	
	public void startAnalysis(String repository) throws Exception{
		printer.printStringInfo("Analisi del progetto "+ repository);
		
		//RECUPERO LA LISTA DELLE RELEASE
		List<Release> releaseList = new ArrayList<>();
	    releaseList = rc.ListRelease(repository.toUpperCase());
	    rc.setNumberReleases(releaseList);
	    printer.printReleases(releaseList);
	  
	   
	    //RECUPERO TUTTI I COMMIT DI OGNI RELEASE
	    printer.printStringInfo("Recupero dei commit relativi ad ogni release in corso...");
	    String lastReleaseDate = null;
	    int iteration = 0;
	    ReleaseBean rb = new ReleaseBean();
	    for (Release r: releaseList) {
	    	r.setCommits(cc.getCommitsForRelease(r, repository, lastReleaseDate, iteration));
	    	lastReleaseDate = r.getReleaseDate();
	    	iteration++;
	    	if (r.getCommits().size() == 0) {
	    		r.setFakeCommits(rb.getCommits());
	    	}
	    	rb.setCommits(r.getCommits());
	    	//IL PRIMO ELEMENTO DELLA LISTA DI COMMIT è IL PIù RECENTE, QUINDI L'ULTIMO
	    }
	    
	    //RECUPERO TUTTI I TICKET 
	    printer.printStringInfo("Recupero dei ticket relativi ai bug chiusi del progetto in corso...");
	    ArrayList<Ticket> ticketList = new ArrayList<>();
		ticketList = tc.retrieveTicketsID(repository.toUpperCase(), releaseList);
		Collections.reverse(ticketList);
		
		//ASSOCIO I COMMIT AD I RELATIVI TICKET
		printer.printStringInfo("Recupero dei commit associati al ticket specifico in corso...");
		for (Ticket t: ticketList) {
			t.setCommitsForTicket(tc.searchCommitsForTicket(t, releaseList));	
		}
		
		//ELIMINO TUTTI I TICKET CHE NON HANNO COMMIT ASSOCIATI
		ArrayList<Ticket> myTktList = new ArrayList<>();
		for (Ticket t: ticketList) {
			if (t.getCommitsForTicket().size() != 0) {
				myTktList.add(t);
			}
		}
		
		printer.printStringInfo("Calcolo delle Opening Version e delle Injected Version in corso...");
	    for (Ticket t: myTktList) {
	    	//IL PRIMO AV DEVE ESSERE PRIMA DI OV, QUINDI IV <= OV
	    	t.setOpeningVersion(tc.calculateOpeningVersion(t, releaseList));
	    }
	    
	    //TOLGO TUTTI I TICKET CON VALORI DI FV E OV NON CONGRUI
	    ArrayList<Ticket> myTktList2 = new ArrayList<>();
	    for (Ticket t: myTktList) {
	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList2.add(t);
	    	}
	    }
	    
	    for (Ticket t: myTktList2) {
	    	t.setInjectedVersion(tc.calculateInjectedVersion(t, releaseList, myTktList2, rc));
	    }
	   
	    
	    //CONSIDERO SOLO I TICKET CHE HANNO IV E OV CONGRUI
	    List<Ticket> myTktList3 = new ArrayList<>();
	    for (Ticket t: myTktList2) {
	    	if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList3.add(t);
	    	} 
	    }
	    
	    //TOLGO ANCHE I TICKET CHE HANNO INJECTED VERSION E FIX VERSION UGUALI, PERCHé SIGNIFICA CHE IL BUG NON C'è
	    List<Ticket> myTicketList = new ArrayList<>();
	    for (Ticket t: myTktList3) {
	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
	    		myTicketList.add(t);
	    	} 
	    }
	    
	    //ELENCO DEI TICKET RIMASTI
	    for (Ticket t: myTicketList) {
	    	printer.printStringInfo("Ticket: "+t.getKey());
	    	printer.printStringInfo("IV: "+t.getInjectedVersion().getNumberOfRelease());
	    	printer.printStringInfo("OV: "+t.getOpeningVersion().getNumberOfRelease());
	    	printer.printStringInfo("FV: "+t.getFixVersion().getNumberOfRelease());
	    	printer.printStringInfo("%n"); 	
	    }
	    
	    //CONSIDERO SOLO LA PRIMA METà DELLE RELEASE
	    int halfSize = releaseList.size() / 2;
		ArrayList<Release> myReleaseList = new ArrayList<>(releaseList.subList(0, halfSize));
		
		printer.printStringInfo("Recupero di tutte le classi toccate da ogni commit nelle varie release in corso...");
		for (Release r: myReleaseList) {
			if (r.getCommits().size() != 0) {
				for (Commit c: r.getCommits()) {
					//c.setClassesTouched(Cc.getClasses(c.getCommit(), repository, r));
					c.setClassesTouched(cc.getModifiedClasses(c.getCommit(), repository));
			    }	
			}
			else {
				for (Commit c2: r.getFakeCommits()) {
					c2.setClassesTouched(cc.getModifiedClasses(c2.getCommit(), repository));
				}	
			}
		}
		
		for (Ticket tk: myTicketList) {
			for (Commit c: tk.getCommitsForTicket()) {
				c.setClassesTouched(cc.getModifiedClasses(c.getCommit(), repository));
			}
		}
		
		printer.printStringInfo("Recupero di tutte le classi di ogni release dall'ultimo commit in corso...");
		ArrayList<String> nameClasses = new ArrayList<>();
		for (Release r: myReleaseList) {
			//if (r.getCommits().size() != 0) {
				r.setLastCommit(rc.retrieveLastCommit(r));
				//nameClasses = Rc.retrieveClassesForRelease(r);
				nameClasses = rc.retrieveClassesForRelease2(r, cc, repository);
				r.setJavaClasses(rc.createClasses(r, nameClasses));
			//}
		}
		
		mc.calculateBuggyness2(myReleaseList, cc, repository, myTicketList);
		for (Release r: myReleaseList) {
			if (r.getCommits().size() != 0) {
				//Mc.calculateBuggyness(r, Cc, repository, myTicketList);
				mc.calculateMetrics(r, myTicketList, repository, printer);
			}
			else {
				mc.setMetrics(r, repository, printer);
			}
		}	
		
		printer.printStringInfo("Creazione del file csv in corso...");
		csv.createDataset(myReleaseList, repository, printer);
		
		printer.printStringInfo("Analisi di Weka in corso...");
		wc.walkForward(myReleaseList, repository, csv, printer);
		
		printer.printStringInfo("FINITOOOOO!");
	}
}
