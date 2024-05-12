package logic;

import logic.controller.HandlerController;


public class App {
	
    public static void main( String[] args ) throws Exception {
    	HandlerController Hc = new HandlerController();
    	String repository = "openjpa";
        Hc.startAnalysis(repository);
    }
}
