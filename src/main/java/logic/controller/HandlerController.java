package logic.controller;

import logic.model.entity.Release;
import logic.model.entity.Ticket;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.model.bean.ReleaseBean;
import logic.model.entity.Commit;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerController {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerController.class);
	
	public void startAnalysis(String repository) throws JSONException, IOException, RevisionSyntaxException, ParseException, GitAPIException {
		System.out.println("Analisi del progetto "+ repository);
		
		//RECUPERO LA LISTA DELLE RELEASE
		ReleaseController rc = new ReleaseController();
		CommitController cc = new CommitController("C:\\Users\\HP\\Desktop\\Progetti Apache\\");
		TicketController tc = new TicketController();
		MetricsController mc = new MetricsController("C:\\Users\\HP\\Desktop\\Progetti Apache\\");
		CSVController csv = new CSVController();
		WekaController wc = new WekaController();
		List<Release> releaseList = ReleaseController.listRelease(repository.toUpperCase());
	    rc.setNumberReleases(releaseList);
	    for (int i = 0; i < releaseList.size(); i++)
			 System.out.println("RELEASE NUMBER: "+ releaseList.get(i).getNumberOfRelease() +" = "+"RELEASE NAME: "+releaseList.get(i).getNameRelease() + " RELEASE DATE: "+releaseList.get(i).getReleaseDate()+ " RELEASE ID: "+releaseList.get(i).getIdRelease()); 	 	
	  
	   
	    //RECUPERO TUTTI I COMMIT DI OGNI RELEASE
	    System.out.println("Recupero dei commit relativi ad ogni release in corso...");
	    retrieveCommitForRelease(repository, releaseList, cc);
	    /*String lastReleaseDate = null;
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
	    }*/
	    
	    //RECUPERO TUTTI I TICKET 
	    System.out.println("Recupero dei ticket relativi ai bug chiusi del progetto in corso...");
	    List<Ticket> ticketList = tc.retrieveTicketsID(repository.toUpperCase(), releaseList);
		Collections.reverse(ticketList);
		
		//ASSOCIO I COMMIT AD I RELATIVI TICKET
		System.out.println("Recupero dei commit associati al ticket specifico in corso...");
		retrieveCommitForTicket(ticketList, tc, releaseList);
		/*for (Ticket t: ticketList) {
			t.setCommitsForTicket(tc.searchCommitsForTicket(t, releaseList));	
		}*/
		
		//ELIMINO TUTTI I TICKET CHE NON HANNO COMMIT ASSOCIATI
		ArrayList<Ticket> myTktList = new ArrayList<>();
		removeTicketWithoutCommit(ticketList, myTktList);
		/*for (Ticket t: ticketList) {
			if (!t.getCommitsForTicket().isEmpty()) {
				myTktList.add(t);
			}
		}*/
		
		System.out.println("Calcolo delle Opening Version e delle Injected Version in corso...");
		calculateOvIv(myTktList, tc, releaseList);
	    /*for (Ticket t: myTktList) {
	    	//IL PRIMO AV DEVE ESSERE PRIMA DI OV, QUINDI IV <= OV
	    	t.setOpeningVersion(tc.calculateOpeningVersion(t, releaseList));
	    }*/
	    
	    //TOLGO TUTTI I TICKET CON VALORI DI FV E OV NON CONGRUI
	    ArrayList<Ticket> myTktList2 = new ArrayList<>();
	    removeFvOvIncorrect(myTktList, myTktList2);
	    /*for (Ticket t: myTktList) {
	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList2.add(t);
	    	}
	    }*/
	    
	    calculateIv(myTktList2, tc, releaseList, rc);
	    /*for (Ticket t: myTktList2) {
	    	t.setInjectedVersion(tc.calculateInjectedVersion(t, releaseList, myTktList2, rc));
	    }*/
	   
	    
	    //CONSIDERO SOLO I TICKET CHE HANNO IV E OV CONGRUI
	    List<Ticket> myTktList3 = new ArrayList<>();
	    removeIvOvIncorrect(myTktList3, myTktList2);
	    /*for (Ticket t: myTktList2) {
	    	if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList3.add(t);
	    	} 
	    }*/
	    
	    //TOLGO ANCHE I TICKET CHE HANNO INJECTED VERSION E FIX VERSION UGUALI, PERCHé SIGNIFICA CHE IL BUG NON C'è
	    List<Ticket> myTicketList = new ArrayList<>();
	    removeEqualIvFv(myTktList3, myTicketList);
	    /*for (Ticket t: myTktList3) {
	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
	    		myTicketList.add(t);
	    	} 
	    }*/
	    
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
		retrieveClassesFromCommit(myReleaseList, cc, repository);
		/*for (Release r: myReleaseList) {
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
		}*/
		
		retrieveClassesForTickets(myTicketList, cc, repository);
		/*for (Ticket tk: myTicketList) {
			for (Commit c: tk.getCommitsForTicket()) {
				c.setClassesTouched(cc.getModifiedClasses(c.getCommit(), repository));
			}
		}*/
		
		System.out.println("Recupero di tutte le classi di ogni release dall'ultimo commit in corso...");
		retrieveClassesForRelease(myReleaseList, rc, cc, repository);
		/*for (Release r: myReleaseList) {
			r.setLastCommit(rc.retrieveLastCommit(r));
			List<String> nameClasses = rc.retrieveClassesForRelease2(r, cc, repository);
			r.setJavaClasses(rc.createClasses(r, nameClasses));
		}*/
		
		System.out.println("Calcolo buggyness e metriche in corso....");
		mc.calculateBuggyness(myReleaseList, myTicketList);
		calculateMetrics(myReleaseList, myTicketList, repository, mc);
		/*for (Release r: myReleaseList) {
			if (!r.getCommits().isEmpty()) {
				mc.calculateMetrics(r, myTicketList, repository);
			}
			else {
				mc.setMetrics(r, repository);
			}
		}*/	
		
		System.out.println("Creazione del file csv in corso...");
		csv.createDataset(myReleaseList, repository, logger);
		
		//System.out.println("Analisi di Weka in corso...");
		//wc.walkForward(myReleaseList, repository, csv, logger);
		
		System.out.println("FINITOOOOO!");
	}

	public void retrieveCommitForRelease(String repository, List<Release> releaseList, CommitController cc) throws RevisionSyntaxException, JSONException, IOException, ParseException {
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
		}
	}
	
	public void retrieveCommitForTicket(List<Ticket> ticketList, TicketController tc, List<Release> releaseList) {
		for (Ticket t: ticketList) {
			t.setCommitsForTicket(tc.searchCommitsForTicket(t, releaseList));	
		}
	}
	
	public void removeTicketWithoutCommit(List<Ticket> ticketList, List<Ticket> myTktList) {
		for (Ticket t: ticketList) {
			if (!t.getCommitsForTicket().isEmpty()) {
				myTktList.add(t);
			}
		}
	}
	
	public void calculateOvIv(List<Ticket> myTktList, TicketController tc, List<Release> releaseList) {
		for (Ticket t: myTktList) {
	    	//IL PRIMO AV DEVE ESSERE PRIMA DI OV, QUINDI IV <= OV
	    	t.setOpeningVersion(tc.calculateOpeningVersion(t, releaseList));
	    }	
	}
	
	public void removeFvOvIncorrect(List<Ticket> myTktList, List<Ticket> myTktList2) {
		for (Ticket t: myTktList) {
	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktList2.add(t);
	    	}
	    }
	}
	
	public void calculateIv(List<Ticket> myTktList2, TicketController tc, List<Release> releaseList, ReleaseController rc) throws JSONException, IOException, ParseException {
		for (Ticket t: myTktList2) {
	    	t.setInjectedVersion(tc.calculateInjectedVersion(t, releaseList, myTktList2, rc));
	    }	
	}
	
	public void removeIvOvIncorrect(List<Ticket> myTktList3, List<Ticket> myTktList2) {
		for (Ticket t: myTktList2) {
			if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
				myTktList3.add(t);
			} 
		}
	}
	
	public void removeEqualIvFv(List<Ticket> myTktList3, List<Ticket> myTicketList) {
		for (Ticket t: myTktList3) {
	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
	    		myTicketList.add(t);
	    	} 
	    }
	}
	
	public void retrieveClassesFromCommit(List<Release> myReleaseList, CommitController cc, String repository) throws JSONException, IOException {
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
	}
	
	public void retrieveClassesForTickets(List<Ticket> myTicketList, CommitController cc, String repository) throws IOException {
		for (Ticket tk: myTicketList) {
			for (Commit c: tk.getCommitsForTicket()) {
				c.setClassesTouched(cc.getModifiedClasses(c.getCommit(), repository));
			}
		}	
	}
	
	public void retrieveClassesForRelease(List<Release> myReleaseList, ReleaseController rc, CommitController cc, String repository) throws JSONException, IOException {
		for (Release r: myReleaseList) {
			r.setLastCommit(rc.retrieveLastCommit(r));
			List<String> nameClasses = rc.retrieveClassesForRelease2(r, cc, repository);
			r.setJavaClasses(rc.createClasses(r, nameClasses));
		}		
	}
	
	public void calculateMetrics(List<Release> myReleaseList, List<Ticket> myTicketList, String repository, MetricsController mc) throws JSONException, IOException, GitAPIException {
		for (Release r: myReleaseList) {
			System.out.println("\n\nRELEASE "+r.getNameRelease());
			if (!r.getCommits().isEmpty()) {
				mc.calculateMetrics(r, myTicketList, repository);
			}
			else {
				mc.setMetrics(r, repository);
			}
		}	
	}
}
