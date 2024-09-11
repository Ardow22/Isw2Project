package logic;

import logic.controller.HandlerController;


public class App {
	
    public static void main( String[] args ) throws Exception {
    	HandlerController hc = new HandlerController();
    	String repository1 = "bookkeeper";
	String repository2 = "openjpa";
        hc.startAnalysis(repository1);
	hc.startAnalysis(repository2);
    }
}
