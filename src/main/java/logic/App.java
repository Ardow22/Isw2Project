package logic;

import java.io.IOException;

import org.json.JSONException;

import logic.controller.HandlerController;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws JSONException, IOException {
    	HandlerController Hc = new HandlerController();
    	String repository = "bookkeeper";
        Hc.startAnalysis(repository);
    }
}
