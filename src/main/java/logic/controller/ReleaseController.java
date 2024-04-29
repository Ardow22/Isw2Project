package logic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.entity.Release;
import logic.utils.JsonFileHandler;

public class ReleaseController {
	
	public static HashMap<LocalDateTime, String> releaseNames;
	public static HashMap<LocalDateTime, String> releaseID;
	public static ArrayList<LocalDateTime> releases;
	public static Integer numVersions;
	public static JsonFileHandler JSFH;

	
	public ArrayList<Release> ListRelease(String projName) throws IOException, JSONException {
		 //Fills the arraylist with releases dates and orders them
		   //Ignores releases with missing dates
		   ArrayList<Release> releaseList = new ArrayList<Release>();
		   releases = new ArrayList<LocalDateTime>();
		         Integer i;
		         String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		         JSONObject json = JSFH.readJsonFromUrl(url);
		         JSONArray versions = json.getJSONArray("versions");
		         releaseNames = new HashMap<LocalDateTime, String>();
		         releaseID = new HashMap<LocalDateTime, String> ();
		         for (i = 0; i < versions.length(); i++ ) {
		            String name = "";
		            String id = "";
		            String date = "";
		            Release release = new Release();
		            if(versions.getJSONObject(i).has("releaseDate")) {
		               date = versions.getJSONObject(i).get("releaseDate").toString();
		               release.setReleaseDate(date);
		               
		               if (versions.getJSONObject(i).has("name")) {
		                  name = versions.getJSONObject(i).get("name").toString();
		                  release.setNameRelease(name); 
		               }
		               if (versions.getJSONObject(i).has("id")) {
		                  id = versions.getJSONObject(i).get("id").toString();
		                  release.setIdRelease(id);
		               }
		               addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
		                          name,id);
		               releaseList.add(release);
		            }
		         }
		         
		         // order releases by date
		         Collections.sort(releases, new Comparator<LocalDateTime>(){
		            //@Override
		            public int compare(LocalDateTime o1, LocalDateTime o2) {
		                return o1.compareTo(o2);
		            }
		         });
		         
		         Collections.sort(releaseList, new Comparator<Release>() {
		        	 //@Override
		             public int compare(Release r1, Release r2) {
		            	 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		                 // Converte le stringhe data in LocalDateTime
		                 LocalDate data1 = LocalDate.parse(r1.getReleaseDate(), formatter);
		                 LocalDate data2 = LocalDate.parse(r2.getReleaseDate(), formatter);

		                 // Ordina in base alla data
		                 return data1.compareTo(data2);
		             }
		         }); 
		         //System.out.println("DOPO " + releases);
		         if (releases.size() < 6)
		            return new ArrayList<>();
		         return releaseList;	    
	}
	
	public static void addRelease(String strDate, String name, String id) {
	    LocalDate date = LocalDate.parse(strDate);
	    LocalDateTime dateTime = date.atStartOfDay();
	    if (!releases.contains(dateTime))
	       releases.add(dateTime);
	    releaseNames.put(dateTime, name);
	    releaseID.put(dateTime, id);
	    return;
	}
	
	public void setNumberReleases(ArrayList<Release> releaseList) {
		for (int i = 0; i < releaseList.size(); i++) {
			releaseList.get(i).setNumberOfRelease(i);
		}
	}


}
