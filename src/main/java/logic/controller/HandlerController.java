package logic.controller;

import logic.model.entity.Release;
import logic.model.entity.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.model.bean.ReleaseBean;
import logic.model.entity.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerController {
	
	public static final ReleaseController rc = new ReleaseController();
	public static final CommitController cc = new CommitController();
	public static final TicketController tc = new TicketController();
	public static final MetricsController mc = new MetricsController();
	public static final CSVController csv = new CSVController();
	public static final WekaController wc = new WekaController();
	private static final Logger logger = LoggerFactory.getLogger(HandlerController.class);
	
	public void startAnalysis(String repository) throws Exception{
		System.out.println("Analisi del progetto "+ repository);
		
		//RECUPERO LA LISTA DELLE RELEASE
		List<Release> releaseList = ReleaseController.listRelease(repository.toUpperCase());
	    rc.setNumberReleases(releaseList);
	    for (int i = 0; i < releaseList.size(); i++)
			 System.out.println("RELEASE NUMBER: "+ releaseList.get(i).getNumberOfRelease() +" = "+"RELEASE NAME: "+releaseList.get(i).getNameRelease() + " RELEASE DATE: "+releaseList.get(i).getReleaseDate()+ " RELEASE ID: "+releaseList.get(i).getIdRelease()); 	 	
	  
	   
	    //RECUPERO TUTTI I COMMIT DI OGNI RELEASE
	    System.out.println("Recupero dei commit relativi ad ogni release in corso...");
	    String lastReleaseDate = null;
	    int iteration = 0;
	    ReleaseBean rb = new ReleaseBean();
	    for (Release r: releaseList) {
	    	r.setCommits(cc.getCommitsForRelease(r, repository, lastReleaseDate, iteration));
	    	lastReleaseDate = r.getReleaseDate();
	    	iteration++;
	    	if (r.getCommits().isEmpty()) {
	    		r.setFakeCommits(rb.getCommits());
	    	}
	    	rb.setCommits(r.getCommits());
	    	//IL PRIMO ELEMENTO DELLA LISTA DI COMMIT è IL PIù RECENTE, QUINDI L'ULTIMO
	    }
	    
	    //RECUPERO TUTTI I TICKET 
	    System.out.println("Recupero dei ticket relativi ai bug chiusi del progetto in corso...");
	    List<Ticket> ticketList = tc.retrieveTicketsID(repository.toUpperCase(), releaseList);
		Collections.reverse(ticketList);
		
		//ASSOCIO I COMMIT AD I RELATIVI TICKET
		System.out.println("Recupero dei commit associati al ticket specifico in corso...");
		for (Ticket t: ticketList) {
			t.setCommitsForTicket(tc.searchCommitsForTicket(t, releaseList));	
		}
		
		//ELIMINO TUTTI I TICKET CHE NON HANNO COMMIT ASSOCIATI
		ArrayList<Ticket> myTktList = new ArrayList<>();
		for (Ticket t: ticketList) {
			if (!t.getCommitsForTicket().isEmpty()) {
				myTktList.add(t);
			}
		}
		
		System.out.println("Calcolo delle Opening Version e delle Injected Version in corso...");
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
	    	System.out.println("Ticket: "+t.getKey());
	    	System.out.println("IV: "+t.getInjectedVersion().getNumberOfRelease());
	    	System.out.println("OV: "+t.getOpeningVersion().getNumberOfRelease());
	    	System.out.println("FV: "+t.getFixVersion().getNumberOfRelease());
	    	System.out.println("\n"); 	
	    }
	    
	    //CONSIDERO SOLO LA PRIMA METà DELLE RELEASE
	    int halfSize = releaseList.size() / 2;
		ArrayList<Release> myReleaseList = new ArrayList<>(releaseList.subList(0, halfSize));
		
		System.out.println("Recupero di tutte le classi toccate da ogni commit nelle varie release in corso...");
		for (Release r: myReleaseList) {
			System.out.println("\nRelease: "+r.getNameRelease());
			if (!r.getCommits().isEmpty()) {
				for (Commit c: r.getCommits()) {
					boolean isLast = r.getCommits().get(r.getCommits().size() - 1).equals(c);
					if (isLast) {
						c.setClassesTouched(cc.getClasses(c.getCommit(), repository));
					}
					else {
						c.setClassesTouched(cc.getModifiedClasses(c.getCommit(), repository));	
					}
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
		
		System.out.println("Recupero di tutte le classi di ogni release dall'ultimo commit in corso...");
		for (Release r: myReleaseList) {
			r.setLastCommit(rc.retrieveLastCommit(r));
			List<String> nameClasses = rc.retrieveClassesForRelease2(r, cc, repository);
			r.setJavaClasses(rc.createClasses(r, nameClasses));
		}
		
		System.out.println("Calcolo buggyness e metriche in corso....");
		mc.calculateBuggyness(myReleaseList, myTicketList);
		for (Release r: myReleaseList) {
			if (!r.getCommits().isEmpty()) {
				mc.calculateMetrics(r, myTicketList, repository);
			}
			else {
				mc.setMetrics(r, repository);
			}
		}	
		
		System.out.println("Creazione del file csv in corso...");
		csv.createDataset(myReleaseList, repository, logger);
		
		System.out.println("Analisi di Weka in corso...");
		wc.walkForward(myReleaseList, repository, csv, logger);
		
		System.out.println("FINITOOOOO!");
	}
}
