package logic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonFileHandler {
	
	private JsonFileHandler() {
        // Il costruttore privato è vuoto poiché non vogliamo che venga istanziata alcuna istanza di questa classe
    }

	
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		URL url2 = new URL(url);
		URLConnection uc = url2.openConnection(); 
		
		try (InputStream is = uc.getInputStream()) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)); 
			String jsonText = readAll(rd); 
			return new JSONObject(jsonText); 
		} catch (Exception e) {   
	        return null; 
	    }
	}
	
	private static String readAll(Reader rd) throws IOException {    
		StringBuilder sb = new StringBuilder();
		int cp;
		cp = 0;
		while (cp != -1) { 
		    cp = rd.read();
		    sb.append((char) cp);
		}
		      
		return sb.toString();
	}
	
	public static JSONArray readJsonArrayFromUrl(String url, String user, String tkn, boolean auth) throws IOException, JSONException {
		URL url2 = new URL(url);
		URLConnection uc = url2.openConnection(); 
		if (auth) { 
			String userpass = user + ":" + tkn;
		    byte[] encodedBytes = Base64.getEncoder().encode(userpass.getBytes()); 
		    String basicAuth = "Basic " + new String(encodedBytes);
			uc.setRequestProperty("Authorization", basicAuth);
		} 
		
		try (InputStream is = uc.getInputStream()) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)); 
			String jsonText = readAll(rd); 
			return new JSONArray(jsonText); 
		} catch (Exception e) {  
	        return null; 
	    }
	}
	


}
