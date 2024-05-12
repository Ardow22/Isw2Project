package logic;

import logic.controller.HandlerController;


public class App {
	
    public static void main( String[] args ) throws Exception {
    	HandlerController hc = new HandlerController();
    	String repository = "openjpa";
        hc.startAnalysis(repository);
    }
}
