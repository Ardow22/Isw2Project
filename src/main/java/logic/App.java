package logic;

import java.io.IOException;
import java.text.ParseException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.json.JSONException;

import logic.controller.HandlerController;

/**
 * Hello world!
 *
 */
public class App {
	
    public static void main( String[] args ) throws JSONException, IOException, RevisionSyntaxException, NoHeadException, GitAPIException, ParseException {
    	HandlerController Hc = new HandlerController();
    	String repository = "bookkeeper";
        Hc.startAnalysis(repository);
    }
}
