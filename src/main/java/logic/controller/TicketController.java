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
	String fORMAT = "yyyy-MM-dd";
	String stringReleaseDate = "releaseDate";
	String fIELDS = "fields";
	
	public List<Ticket> retrieveTicketsID(String projName, List<Release> releaseList) throws IOException, JSONException {
		   List<Ticket> ticketList = new ArrayList<>();
		   String releaseDate = "";
		   String fixReleaseDate = "";
		   String creationDate = "";
		   String resolutionDate = "";
		   Integer j = 0; 
		   Integer i = 0; 
		   Integer total = 1;
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
	        	 List<Release> affVersList = new ArrayList<>();
	        	 String key = issues.getJSONObject(i%1000).get("key").toString();
	        	 tk.setKey(key);
	        	 JSONObject fields = issues.getJSONObject(i%1000).getJSONObject(fIELDS);
	        	 creationDate = fields.getString("created").substring(0, 10);
	    		 resolutionDate = fields.getString("resolutiondate").substring(0, 10);
	        	 tk.setCreationDate(creationDate);
	             tk.setResolutionDate(resolutionDate);
	        	 
	        	 int len1 = issues.getJSONObject(i%1000).getJSONObject(fIELDS).getJSONArray("versions").length();
	        	 retrieveAffVersion(len1, fields, releaseDate, releaseList, affVersList, tk);
	        	 
	        	 int len2 = issues.getJSONObject(i%1000).getJSONObject(fIELDS).getJSONArray("fixVersions").length();
	        	 retrieveFixVersion(len2, fields, fixReleaseDate, releaseList, tk);
	        	 ticketList.add(tk);
	          }  
	       } while (i < total);
	         List<Ticket> listTicket = new ArrayList<>();
	         for (Ticket t: ticketList) {
	        	 if (t.getFixVersion() != null) {
	        		 listTicket.add(t);
	        	 }
	         }
	         
	         return listTicket;
	 }
	
	private void retrieveFixVersion(int len2, JSONObject fields2, String fixReleaseD, List<Release> releaseList, Ticket tk) {
		if (len2 > 0) {
			List<Release> fixV = new ArrayList<>();
			JSONArray fixVersions = fields2.getJSONArray("fixVersions");
			for (int y = 0; y < fixVersions.length(); y++) {
				JSONObject fixVersion = fixVersions.getJSONObject(y);
				if (fixVersion.has(stringReleaseDate)) {
					fixReleaseD = fixVersion.getString(stringReleaseDate);
				}
				String fixN = fixVersion.getString("name");
				checkForFixV(releaseList, fixVersion, fixN, fixReleaseD, tk, len2, fixV);
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
	}

	private void checkForFixV(List<Release> releaseList, JSONObject fixVersion, String fixNa, String fixReleaseDa,
			Ticket tk, int len2, List<Release> fixV) {
		for (Release r: releaseList) {
			if (fixVersion.has(stringReleaseDate)) {
				check2FixV(r, fixReleaseDa, fixNa, len2, tk, fixV);
			}
			else {
				check3FixV(r, fixNa, tk, fixV, len2);
			} 
		}	
	}

	private void check3FixV(Release r, String fixxNa, Ticket tk, List<Release> fixV, int len2) {
		if (r.getNameRelease().equals(fixxNa)) {
			if (len2 == 1) {
				tk.setFixVersion(r); 
			}
			else {
				fixV.add(r);
			}
		}	
	}

	private void check2FixV(Release r, String fixRelDat, String fixNam, int len2, Ticket tk, List<Release> fixV) {
		if (r.getReleaseDate().equals(fixRelDat) || r.getNameRelease().equals(fixNam)) {
			if (len2 == 1) {
				tk.setFixVersion(r); 
			}
			else {
				fixV.add(r);
			}
		}		
	}

	private void retrieveAffVersion(int len1, JSONObject fields2, String releaseDt, List<Release> releaseList, List<Release> affVersList, Ticket tk) {
		if (len1 > 0) {
			JSONArray versions = fields2.getJSONArray("versions");
			for (int x = 0; x < versions.length(); x++) {
				JSONObject version = versions.getJSONObject(x);
				if (version.has(stringReleaseDate)) {
					releaseDt = version.getString(stringReleaseDate);
				}
				String releaseNm = version.getString("name");
				checkForAffVersion(releaseList, version, releaseDt, releaseNm, affVersList);
			}
		}
   	  tk.setAffversions(affVersList);
		
	}

	private void checkForAffVersion(List<Release> releaseList, JSONObject version, String releaseDat, String releaseNam, List<Release> affVersList) {
		for (Release r: releaseList) {
			if (version.has(stringReleaseDate)) {
				if (r.getReleaseDate().equals(releaseDat) || r.getNameRelease().equals(releaseNam)) {
					affVersList.add(r);
				}
			}
			else {
				if (r.getNameRelease().equals(releaseNam)) {
					affVersList.add(r);
				}
			}
		}	
	}

	public List<Commit> searchCommitsForTicket(Ticket ticket, List<Release> releaseList) {
		List<Commit> commitsForTicket = new ArrayList<>(); 
		for (Release r: releaseList) {
			if (!r.getCommits().isEmpty()) {
				List<Commit> commitList = r.getCommits();
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
  	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fORMAT);
  	  LocalDate creationDate = LocalDate.parse(ticket.getCreationDate(), formatter);
  	  for (Release r: releaseList) {
  		LocalDate releaseDate = LocalDate.parse(r.getReleaseDate(), formatter);
  		if (!releaseDate.isBefore(creationDate)) {
  			return r;
  		}  
  	  }
  	  return null;	  
    }
	
	public Release calculateInjectedVersion(Ticket ticket, List<Release> releaseList, List<Ticket> ticketList, ReleaseController rc) throws JSONException, IOException, ParseException {
  	  if (!ticket.getAffversions().isEmpty()) {
  		  return ticket.getAffversions().get(0);   
  	  }
  	  else if (ticket.getFixVersion().getNumberOfRelease() == 0) {
  		  return ticket.getFixVersion();
  	  }
  	  else {
  		  int p = proportion(ticket, ticketList, rc);
  		  int fV = ticket.getFixVersion().getNumberOfRelease();
  		  int oV = ticket.getOpeningVersion().getNumberOfRelease();
  		  int difference = 0;
  		  if (fV - oV == 0) {
  			  difference = 1;//come specificato nel paper
  		  }
  		  else {
  			  difference = fV - oV;
  		  }
  		  int iV = fV - (difference * p);
  		  if (iV < 0) {
  			  iV = 0;
  		  }
  		  for (Release r: releaseList) {
  			  if (r.getNumberOfRelease() == iV) {
  				  return r;
  			  }
  		  } 
  	  }
  	  return null;
    }
    
    public int proportion(Ticket ticket, List <Ticket> ticketList, ReleaseController rc) throws JSONException, IOException, ParseException {
  	  int limitRelease;
  	  limitRelease = ticket.getFixVersion().getNumberOfRelease();
  	  List<Ticket> subList = new ArrayList<>();
  	  for (Ticket t: ticketList) {
  		  if (t.getFixVersion().getNumberOfRelease() < limitRelease) {
  			  subList.add(t);
  		  } 
  	  }
  	  
  	  if (subList.size() < 5) {
  		  subList = coldStart(ticket, rc);//COLDSTART
  	  }
  	  //INCREMENT PROPORTION
  	  List<Integer> listP = new ArrayList<>();
  	  int fV = 1;
  	  int iV = 1;
  	  int oV = 1;
  	  int p = 1;
  	  for (Ticket t: subList) {
  		  fV = t.getFixVersion().getNumberOfRelease();
  		  if (t.getInjectedVersion() != null) {
  			  iV = t.getInjectedVersion().getNumberOfRelease();
  		  }
  		  oV = t.getOpeningVersion().getNumberOfRelease();
  		  int difference = 0;
  		  if ((fV - oV) == 0) {
  			  difference = 1;  
  	      }
  		  else {
  			  difference = fV - oV;
  		  }
  		  p = (fV - iV) / difference;
  		  if (p < 0) {
  			p = 1;
  		  }
  		  listP.add(p);
  	  }
  	 
  	  int sum = 0;
  	  for (int p2: listP) {
  		  sum += p2;
  	  }
  	  int averageP = 0;
  	  if (!listP.isEmpty()) {
  		  averageP = sum/listP.size();
  	  }
  	  return averageP;
    }
    
    private List<Ticket> coldStart(Ticket ticket, ReleaseController rc) throws JSONException, IOException, ParseException {
    	 List<String> allProjects = new ArrayList<>(Arrays.asList("avro", "storm", "zookeeper", "syncope", "tajo"));
    	 List<Ticket> allTickets = new ArrayList<>();	
    	/*BOOKKEEPER: 2011 - 2017
    	 * OPENJPA: 2006 - 2022
    	 * 
    	 * AVRO: 2009 - 2023
    	 * STORM: 2013 - 2024
    	 * ZOOKKEEPER: 2008 - OLTRE IL 2020
    	 * SYNCOPE: 2012 - 2017
    	 * TAJO: 2013 - 2019 */
    	String limitDate = ticket.getFixVersion().getReleaseDate();
    	for (String project: allProjects) {
    		List<Release> releasesList = ReleaseController.listRelease(project.toUpperCase());
    	    rc.setNumberReleases(releasesList);
    	    
    	    List<Ticket> ticketsList = retrieveTicketsID(project.toUpperCase(), releasesList);
    		Collections.reverse(ticketsList);
    		
    		coldStarCalculateOpeningVersion(ticketsList, releasesList);
    		
    		//TOLGO TUTTI I TICKET CON VALORI DI FV E OV NON CONGRUI
    		List<Ticket> myTktsList2 = new ArrayList<>();
    		coldStartRemoveFvOvIncorrect(ticketsList, myTktsList2);
    	    
    	    //I TICKET SENZA AFFECTED VERSION NON CI INTERESSANO
    	    List<Ticket> myTktsList3 = new ArrayList<>();
    	    coldStartRemoveTktWithoutAffVersion(myTktsList2, myTktsList3);
    	    
    	    //CONSIDERO SOLO I TICKET CHE HANNO IV E OV CONGRUI
    	    List<Ticket> myTktsList4 = new ArrayList<>();
    	    coldStartIvOvCorrect(myTktsList4, myTktsList3);
    	    
    	    //TOLGO ANCHE I TICKET CHE HANNO INJECTED VERSION E FIX VERSION UGUALI, PERCHé SIGNIFICA CHE IL BUG NON C'è
    	    List<Ticket> myTktsList5 = new ArrayList<>();
    	    coldStartRemoveEqualIvFv(myTktsList5, myTktsList4);
    	    
    	    //AGGIUNGO I TICKET RIMASTI AI TICKET FINALI
    	    coldStartAddAllTickets(myTktsList5, allTickets);
    	}
    	
    	//PRENDO SOLO I TICKET FINO ALLA DATA CHE MI INTERESSA
    	List<Ticket> subTktList = new ArrayList<>();
    	SimpleDateFormat dateFormat = new SimpleDateFormat(fORMAT);
        Date date2 = dateFormat.parse(limitDate);
    	for (Ticket t: allTickets) {
    		Date date1 = dateFormat.parse(t.getFixVersion().getReleaseDate());
    		if (date1.before(date2)) {
    			 subTktList.add(t);
    		} 
    	}
    	
    	return subTktList;
    }
    
    private void coldStartAddAllTickets(List<Ticket> myTktsList5, List<Ticket> allTickets) {
    	for (Ticket t: myTktsList5) {
	    	allTickets.add(t);
	    }
	}

	private void coldStartRemoveEqualIvFv(List<Ticket> myTktsList5, List<Ticket> myTktsList4) {
    	for (Ticket t: myTktsList4) {
	    	if (t.getInjectedVersion().getNumberOfRelease() != t.getFixVersion().getNumberOfRelease()) {
	    		myTktsList5.add(t);
	    	} 
	    }
	}

	private void coldStartIvOvCorrect(List<Ticket> myTktsList4, List<Ticket> myTktsList3) {
    	for (Ticket t: myTktsList3) {
	    	if (t.getInjectedVersion().getNumberOfRelease() < t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktsList4.add(t);
	    	} 
	    }
	}

	private void coldStartRemoveTktWithoutAffVersion(List<Ticket> myTktsList2, List<Ticket> myTktsList3) {
    	for (Ticket t: myTktsList2) {
	    	if (!t.getAffversions().isEmpty()) {
	    		myTktsList3.add(t);
	    	}
	    }
	    
	    for (Ticket t: myTktsList3) {
	    	t.setInjectedVersion(coldStartInjectedVersion(t));
	    }
	}

	private void coldStartRemoveFvOvIncorrect(List<Ticket> ticketsList, List<Ticket> myTktsList2) {
    	for (Ticket t: ticketsList) {
	    	if (t.getFixVersion().getNumberOfRelease() >= t.getOpeningVersion().getNumberOfRelease()) {
	    		myTktsList2.add(t);
	    	}
	    }
	}

	private void coldStarCalculateOpeningVersion(List<Ticket> ticketsList, List<Release> releasesList) {
    	for (Ticket t: ticketsList) {
	    	t.setOpeningVersion(calculateOpeningVersion(t, releasesList));
	    }
	}

	public Release coldStartInjectedVersion(Ticket ticket) {//DA CONTROLLARE, A ME SERVONO SOLO I TICKET CHE GIà HANNO LE AFFECTED VERSION
    	if (!ticket.getAffversions().isEmpty()) {
    		final SimpleDateFormat sdf = new SimpleDateFormat(fORMAT);
    		Comparator<Release> comparator = (release1, release2) -> {
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
