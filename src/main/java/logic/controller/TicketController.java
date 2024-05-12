package logic.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.entity.Commit;
import logic.model.entity.Release;
import logic.model.entity.Ticket;
import logic.utils.JsonFileHandler;

public class TicketController {
	
	public ArrayList<Ticket> retrieveTicketsID(String projName, List<Release> releaseList) throws IOException, JSONException {
		   ArrayList<Ticket> ticketList = new ArrayList<Ticket>();
		   String releaseDate = "";
		   String releaseName = "";
		   String fixReleaseDate = "";
		   String fixName = "";
		   String creationDate = "";
		   String resolutionDate = "";
		   Integer j = 0, i = 0, total = 1;
	       do {
	          j = i + 1000;
	          String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	              + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
	              + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created,fixVersions&startAt="
	              + i.toString() + "&maxResults=" + j.toString();
	          JSONObject json = JsonFileHandler.readJsonFromUrl(url);
	          JSONArray issues = json.getJSONArray("issues");
	          total = json.getInt("total");
	          for (; i < total && i < j; i++) {
	          //Iterate through each bug
	        	 Ticket tk = new Ticket();
	        	 ArrayList<Release> affVersList = new ArrayList<Release>();
	        	 String key = issues.getJSONObject(i%1000).get("key").toString();
	        	 tk.setKey(key);
	        	 JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
	        	 creationDate = fields.getString("created").substring(0, 10);
	    		 resolutionDate = fields.getString("resolutiondate").substring(0, 10);
	        	 tk.setCreationDate(creationDate);
	             tk.setResolutionDate(resolutionDate);
	        	 
	        	 int len1 = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("versions").length();
	        	 if (len1 > 0) {
	        		 JSONArray versions = fields.getJSONArray("versions");
	        		 for (int x = 0; x < versions.length(); x++) {
	                     JSONObject version = versions.getJSONObject(x);
	                     if (version.has("releaseDate")) {
	                    	 releaseDate = version.getString("releaseDate");
	                     }
	                     releaseName = version.getString("name");
	                     for (Release r: releaseList) {
	                    	 if (version.has("releaseDate")) {
	                    		 if (r.getReleaseDate().equals(releaseDate) || r.getNameRelease().equals(releaseName)) {
	                    			 affVersList.add(r);
	                             }
	                    	 }
	                    	 else {
	                    		 if (r.getNameRelease().equals(releaseName)) {
	                    			 affVersList.add(r);
	                    		 }
	                    	 }
	                     }
	        		 }
	        	 }
	        	 tk.setAffversions(affVersList);
	        	 
	        	 int len2 = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").length();
	        	 if (len2 > 0) {
	        		 ArrayList<Release> fixV = new ArrayList<Release>();
	        		 JSONArray fixVersions = fields.getJSONArray("fixVersions");
	        		 for (int y = 0; y < fixVersions.length(); y++) {
	        			 JSONObject fixVersion = fixVersions.getJSONObject(y);
	        			 if (fixVersion.has("releaseDate")) {
	        				 fixReleaseDate = fixVersion.getString("releaseDate");
	        			 }
	        			 fixName = fixVersion.getString("name");
	        			 for (Release r: releaseList) {
	        				 if (fixVersion.has("releaseDate")) {
	                    		 if (r.getReleaseDate().equals(fixReleaseDate) || r.getNameRelease().equals(fixName)) {
	                    			 if (len2 == 1) {
	                    				tk.setFixVersion(r); 
	                    			 }
	                    			 else {
	                    				fixV.add(r);
	                    			 }
	                             }
	                    	 }
	                    	 else {
	                    		 if (r.getNameRelease().equals(fixName)) {
	                    			 if (len2 == 1) {
	                    				 tk.setFixVersion(r); 
		                    		 }
		                    		 else {
		                    			 fixV.add(r);
		                    		 }
	                    		 }
	                    	 } 
	        			 }
	        		 }
	        		 if (fixV.size() > 1) {
	        			 Release maxFv = new Release();
		        		 maxFv.setNumberOfRelease(-1);
		        		 for (Release fv: fixV) {
		        			 if (fv.getNumberOfRelease() > maxFv.getNumberOfRelease()) {
		        				 maxFv = fv;
		        			 }
		        		 }
		        		 tk.setFixVersion(maxFv);
	        		 }
	        	 }
	        	 ticketList.add(tk);
	          }  
	       } while (i < total);
	         ArrayList<Ticket> listTicket = new ArrayList<Ticket>();
	         for (Ticket t: ticketList) {
	        	 if (t.getFixVersion() != null) {
	        		 listTicket.add(t);
	        	 }
	         }
	         
	         return listTicket;
	 }
	
	public List<Commit> searchCommitsForTicket(Ticket ticket, List<Release> releaseList) {
		List<Commit> commitsForTicket = new ArrayList<Commit> ();
		List<Commit> commitList = new ArrayList<Commit> (); 
		for (Release r: releaseList) {
			if (r.getCommits().size() != 0) {
				commitList = r.getCommits();
				for (Commit c: commitList) {
					Pattern pattern = Pattern.compile("\\b" + Pattern.quote(ticket.getKey()) + "\\b");
					Matcher matcher = pattern.matcher(c.getMessage());
					while(matcher.find()) {
						String match = matcher.group();
						if (match.equals(ticket.getKey())) {
							c.setTicket(ticket);
							commitsForTicket.add(c);
							break;
						}

					} 
				}
			}
		}
		return commitsForTicket;
	}
	
	public Release calculateOpeningVersion(Ticket ticket, List<Release> releaseList) {
  	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  	  LocalDate creationDate = LocalDate.parse(ticket.getCreationDate(), formatter);
  	  for (Release r: releaseList) {
  		LocalDate releaseDate = LocalDate.parse(r.getReleaseDate(), formatter);
  		if (!releaseDate.isBefore(creationDate)) {
  			return r;
  		}  
  	  }
  	  return null;	  
    }
	
	public Release calculateInjectedVersion(Ticket ticket, List<Release> releaseList, List<Ticket> ticketList, ReleaseController Rc) throws JSONException, IOException, ParseException {
  	  if (ticket.getAffversions().size() != 0) {
  		  //ticket.getAffversions().sort(Comparator.comparing(release -> release.getNumberOfRelease()));
  		  return ticket.getAffversions().get(0);//nel caso aggiungere codice per verificare quale versione venga prima   
  	  }
  	  else if (ticket.getFixVersion().getNumberOfRelease() == 0) {
  		  return ticket.getFixVersion();
  	  }
  	  else {
  		  int P = Proportion(ticket, releaseList, ticketList, Rc);
  		  int FV = ticket.getFixVersion().getNumberOfRelease();
  		  int OV = ticket.getOpeningVersion().getNumberOfRelease();
  		  int difference = 0;
  		  if (FV - OV == 0) {
  			  difference = 1;//come specificato nel paper
  		  }
  		  else {
  			  difference = FV - OV;
  		  }
  		  int IV = FV - (difference * P);
  		  if (IV < 0) {
  			  IV = 0;
  		  }
  		  for (Release r: releaseList) {
  			  if (r.getNumberOfRelease() == IV) {
  				  return r;
  			  }
  		  } 
  	  }
  	  return null;
    }
    
    public int Proportion(Ticket ticket, List <Release> releaseList, List <Ticket> ticketList, ReleaseController Rc) throws JSONException, IOException, ParseException {
  	  int limitRelease;
  	  limitRelease = ticket.getFixVersion().getNumberOfRelease();
  	  ArrayList<Ticket> subList = new ArrayList<Ticket>();
  	  for (Ticket t: ticketList) {
  		  if (t.getFixVersion().getNumberOfRelease() < limitRelease) {
  			  subList.add(t);
  		  } 
  	  }
  	  
  	  if (subList.size() < 5) {
  		  subList = ColdStart(ticket, Rc);//COLDSTART
  	  }
  	  //INCREMENT PROPORTION
  	  ArrayList<Integer> listP = new ArrayList<Integer>();
  	  int FV = 1;
  	  int IV = 1;
  	  int OV = 1;
  	  int P = 1;
  	  for (Ticket t: subList) {
  		  FV = t.getFixVersion().getNumberOfRelease();
  		  if (t.getInjectedVersion() != null) {
  			  IV = t.getInjectedVersion().getNumberOfRelease();
  		  }
  		  OV = t.getOpeningVersion().getNumberOfRelease();
  		  int difference = 0;
  		  if ((FV - OV) == 0) {
  			  difference = 1;  
  	      }
  		  else {
  			  difference = FV - OV;
  		  }
  		  P = (FV - IV) / difference;
  		  if (P < 0) {
  			P = 1;
  		  }
  		  listP.add(P);
  	  }
  	 
  	  int sum = 0;
  	  for (int p: listP) {
  		  sum += p;
  	  }
  	  int averageP = 0;
  	  if (listP.size() != 0) {
  		  averageP = sum/listP.size();
  	  }
  	  return averageP;
    }
    
    private ArrayList<Ticket> ColdStart(Ticket ticket, ReleaseController Rc) throws JSONException, IOException, ParseException {
    	 ArrayList<String> allProjects = new ArrayList<>(Arrays.asList("avro", "storm", "zookeeper", "syncope", "tajo"));
    	 ArrayList<Ticket> allTickets = new ArrayList<Ticket>();	
    	/*BOOKKEEPER: 2011 - 2017
    	 * OPENJPA: 2006 - 2022
    	 * 
    	 * AVRO: 2009 - 2023
    	 * STORM: 2013 - 2024
    	 * ZOOKKEEPER: 2008 - OLTRE IL 2020
    	 * SYNCOPE: 2012 - 2017
    	 * TAJO: 2013 - 2019 */
    	String limitDate = "";
    	limitDate = ticket.getFixVersion().getReleaseDate();
    	for (String project: allProjects) {
    		List<Release> releasesList = new ArrayList<>();
    	    releasesList = Rc.ListRelease(project.toUpperCase());
    	    Rc.setNumberReleases(releasesList);
    	    
    	    ArrayList<Ticket> ticketsList = new ArrayList<Ticket>();
    		ticketsList = retrieveTicketsID(project.toUpperCase(), releasesList);
    		Collections.reverse(ticketsList);
    		
    		for (Ticket t: ticketsList) {
    	    	t.setOpeningVersion(calculateOpeningVersion(t, releasesList));
    	    }
    		
    		//TOLGO TUTTI I TICKET CON VALORI DI FV E OV NON CONGRUI
    		ArrayList<Ticket> myTktsList2 = new ArrayList<Ticket>();
    	    for (Ticket t: ticketsList) {
    	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
    	    		myTktsList2.add(t);
    	    	}
    	    }
    	    
    	    //I TICKET SENZA AFFECTED VERSION NON CI INTERESSANO
    	    ArrayList<Ticket> myTktsList3 = new ArrayList<Ticket>();
    	    for (Ticket t: myTktsList2) {
    	    	if (t.getAffversions().size() != 0) {
    	    		myTktsList3.add(t);
    	    	}
    	    }
    	    
    	    for (Ticket t: myTktsList3) {
    	    	t.setInjectedVersion(coldStartInjectedVersion(t));
    	    }
    	    
    	    //CONSIDERO SOLO I TICKET CHE HANNO IV E OV CONGRUI
    	    ArrayList<Ticket> myTktsList4 = new ArrayList<Ticket>();
    	    for (Ticket t: myTktsList3) {
    	    	if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
    	    		myTktsList4.add(t);
    	    	} 
    	    }
    	    
    	    //TOLGO ANCHE I TICKET CHE HANNO INJECTED VERSION E FIX VERSION UGUALI, PERCHé SIGNIFICA CHE IL BUG NON C'è
    	    ArrayList<Ticket> myTktsList5 = new ArrayList<Ticket>();
    	    for (Ticket t: myTktsList4) {
    	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
    	    		myTktsList5.add(t);
    	    	} 
    	    }
    	    
    	    //AGGIUNGO I TICKET RIMASTI AI TICKET FINALI
    	    for (Ticket t: myTktsList5) {
    	    	allTickets.add(t);
    	    } 
    	}
    	
    	//PRENDO SOLO I TICKET FINO ALLA DATA CHE MI INTERESSA
    	ArrayList<Ticket> subTktList = new ArrayList<Ticket>();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date2 = dateFormat.parse(limitDate);
    	for (Ticket t: allTickets) {
    		Date date1 = dateFormat.parse(t.getFixVersion().getReleaseDate());
    		if (date1.before(date2)) {
    			 subTktList.add(t);
    		} 
    	}
    	
    	return subTktList;
    }
    
    public Release coldStartInjectedVersion(Ticket ticket) {//DA CONTROLLARE, A ME SERVONO SOLO I TICKET CHE GIà HANNO LE AFFECTED VERSION
    	if (ticket.getAffversions().size() != 0) {
    		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Comparator<Release> comparator = new Comparator<Release>() {
                @Override
                public int compare(Release release1, Release release2) {
                    try {
                        // Converti le date in oggetti Date
                        Date date1 = sdf.parse(release1.getReleaseDate());
                        Date date2 = sdf.parse(release2.getReleaseDate());
                        // Confronta le date
                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            };
            Collections.sort(ticket.getAffversions(), comparator);
    		return ticket.getAffversions().get(0);   
    	}
    	else if (ticket.getFixVersion().getNumberOfRelease() == 0) {
    		return ticket.getFixVersion();
    	}
    	else {
    		return null;
    	}
    }

		

}
