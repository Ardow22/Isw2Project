package logic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.entity.Commit;
import logic.model.entity.Release;
import logic.model.entity.Ticket;
import logic.utils.JsonFileHandler;

public class TicketController {
	
	public static JsonFileHandler JSFH;
	
	public ArrayList<Ticket> retrieveTicketsID(String projName, ArrayList<Release> releaseList) throws IOException, JSONException {
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
	          JSONObject json = JSFH.readJsonFromUrl(url, "", "", false);
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
	                    			 tk.setFixVersion(r);
	                             }
	                    	 }
	                    	 else {
	                    		 if (r.getNameRelease().equals(fixName)) {
	                    			 tk.setFixVersion(r);
	                    		 }
	                    	 } 
	        			 }
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
	
	public ArrayList<Commit> searchCommitsForTicket(Ticket ticket, ArrayList<Release> releaseList) {
		ArrayList<Commit> commitsForTicket = new ArrayList<Commit> ();
		for (Release r: releaseList) {
	    	ArrayList<Commit> commitList = r.getCommits();
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
		return commitsForTicket;
	}
	
	public Release calculateOpeningVersion(Ticket ticket, ArrayList<Release> releaseList) {
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
	
	public Release calculateInjectedVersion(Ticket ticket, ArrayList<Release> releaseList, ArrayList<Ticket> ticketList) {
  	  if (ticket.getAffversions().size() != 0) {
  		  //ticket.getAffversions.sort(Comparator.comparing(Release::getVersionNumber));
  		  return ticket.getAffversions().get(0);//nel caso aggiungere codice per verificare quale versione venga prima   
  	  }
  	  else if (ticket.getFixVersion().getNumberOfRelease() == 0) {
  		  return ticket.getFixVersion();
  	  }
  	  else {
  		  int P = IncrementProportion(ticket, releaseList, ticketList);
  		  int FV = ticket.getFixVersion().getNumberOfRelease();
  		  int OV = ticket.getOpeningVersion().getNumberOfRelease();
  		  int difference = 0;
  		  if (FV - OV == 0) {
  			  difference = 1;//come specificato nel paper
  		  }
  		  else {
  			  difference = FV - OV;
  		  }
  		  int IV = FV - (difference) * P;
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
    
    public int IncrementProportion(Ticket ticket, ArrayList <Release> releaseList, ArrayList <Ticket> ticketList) {
  	  int limitRelease;
  	  limitRelease = ticket.getFixVersion().getNumberOfRelease();
  	  ArrayList<Ticket> subList = new ArrayList<Ticket>();
  	  for (Ticket t: ticketList) {
  		  if (t.getFixVersion().getNumberOfRelease() < limitRelease) {
  			  subList.add(t);
  		  } 
  	  }
  	  
  	  if (subList.size() < 5) {
  		  return 1;//QUI VA USATO COLDSTART
  	  }
 
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

		

}
