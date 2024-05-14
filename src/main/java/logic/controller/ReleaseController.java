package logic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.entity.Commit;
import logic.model.entity.JavaClass;
import logic.model.entity.Release;
import logic.utils.JsonFileHandler;

public class ReleaseController {
	
	protected static Map<LocalDateTime, String> releaseNames;
	protected static Map<LocalDateTime, String> releaseID;
	protected static List<LocalDateTime> releases;
	protected static Integer numVersions;
	protected static String releaseDate = "releaseDate";

	
	public static List<Release> listRelease(String projName) throws IOException, JSONException {
		 //Fills the arraylist with releases dates and orders them
		   //Ignores releases with missing dates
		   List<Release> releaseList = new ArrayList<>();
		   releases = new ArrayList<>();
		         Integer i;
		         String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		         JSONObject json = JsonFileHandler.readJsonFromUrl(url);
		         JSONArray versions = json.getJSONArray("versions");
		         releaseNames = new HashMap<>();
		         releaseID = new HashMap<> ();
		         for (i = 0; i < versions.length(); i++ ) {
		            String name = "";
		            String id = "";
		            String date = "";
		            Release release = new Release();
		            if(versions.getJSONObject(i).has(releaseDate)) {
		               date = versions.getJSONObject(i).get(releaseDate).toString();
		               release.setReleaseDate(date);
		               
		               if (versions.getJSONObject(i).has("name")) {
		                  name = versions.getJSONObject(i).get("name").toString();
		                  release.setNameRelease(name); 
		               }
		               if (versions.getJSONObject(i).has("id")) {
		                  id = versions.getJSONObject(i).get("id").toString();
		                  release.setIdRelease(id);
		               }
		               addRelease(versions.getJSONObject(i).get(releaseDate).toString(),
		                          name,id);
		               releaseList.add(release);
		            }
		         }
		         Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));
		         
		         /*order releases by date
		         Collections.sort(releases, new Comparator<LocalDateTime>(){
		            //@Override
		            public int compare(LocalDateTime o1, LocalDateTime o2) {
		                return o1.compareTo(o2);
		            }
		         });*/
		         
		         /*Collections.sort(releaseList, new Comparator<Release>() {
		        	 //@Override
		             public int compare(Release r1, Release r2) {
		            	 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		                 // Converte le stringhe data in LocalDateTime
		                 LocalDate data1 = LocalDate.parse(r1.getReleaseDate(), formatter);
		                 LocalDate data2 = LocalDate.parse(r2.getReleaseDate(), formatter);

		                 // Ordina in base alla data
		                 return data1.compareTo(data2);
		             }
		         }); */
		         Collections.sort(releaseList, (r1, r2) -> {
		        	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		        	    // Converte le stringhe data in LocalDate
		        	    LocalDate data1 = LocalDate.parse(r1.getReleaseDate(), formatter);
		        	    LocalDate data2 = LocalDate.parse(r2.getReleaseDate(), formatter);

		        	    // Ordina in base alla data
		        	    return data1.compareTo(data2);
		        	});
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
	}
	
	public void setNumberReleases(List<Release> releaseList) {
		for (int i = 0; i < releaseList.size(); i++) {
			releaseList.get(i).setNumberOfRelease(i);
		}
	}

	public List<String> retrieveClassesForRelease(Release release) throws JSONException {
		Commit lastCommit = release.getLastCommit();
		return lastCommit.getClassesTouched();
	}
	
	public List<String> retrieveClassesForRelease2(Release release, CommitController cc, String repo) throws JSONException, IOException {
		Commit lastCommit = release.getLastCommit();
		return cc.getClasses(lastCommit.getCommit(), repo);
	}

	public List<JavaClass> createClasses(Release r, List<String> nameClasses) {
		List<JavaClass> classList = new ArrayList<>();
		for (String className: nameClasses) {
			JavaClass jClass = new JavaClass();
			jClass.setNamePath(className);
			jClass.setRelease(r);
			jClass.setBuggy(false);
			classList.add(jClass);
		}
		return classList;
		
	}

	public Commit retrieveLastCommit(Release r) {
		if (!r.getCommits().isEmpty()) {
			return r.getCommits().get(0);
		}
		else {
			return r.getFakeCommits().get(0);
		}
	}
}
