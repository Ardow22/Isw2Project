package logic.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.entity.JavaClass;
import logic.model.entity.Commit;
import logic.model.entity.Release;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;

public class CommitController {
	private static final String filepath = "C:\\Users\\HP\\Desktop\\Progetti Apache\\bookkeeper\\";
	
	public ArrayList<Commit> getCommitsForRelease2(Release release, String repo, String releaseDateSince, int iter) throws IOException, JSONException, RevisionSyntaxException, NoHeadException, GitAPIException, ParseException {
		ArrayList<Commit> commitsForThisRelease = new ArrayList<Commit>();
		Repository repository = new FileRepository(filepath+"/.git/");
        Git git = new Git(repository);
        List<Ref> branchesList = git.branchList().setListMode(ListMode.ALL).call();
        
        for (Ref branch: branchesList) {
        	Iterable<RevCommit> iterable = git.log().add(repository.resolve(branch.getName())).call();
        	for (RevCommit commit : iterable) {
                // Ora puoi accedere a ciascun commit tramite la variabile 'commit'
                System.out.println("\nNUOVO Commit ID: " + commit.getId().getName());
                System.out.println("Autore: " + commit.getAuthorIdent().getName());
                System.out.println("Email dell'autore: " + commit.getAuthorIdent().getEmailAddress());
                System.out.println("Messaggio: " + commit.getFullMessage());
                System.out.println("Tree: " + commit.getTree());
                System.out.println("Tempo: " + commit.getAuthorIdent().getWhen());
                System.out.println("TEMPO: "+commit.getCommitTime() *1000L);
                System.out.println("-------------------------------------------");
                String completeCommitMessage = commit.getFullMessage();
		        String idCommit = commit.getId().getName();
		        String nameAuthor = commit.getAuthorIdent().getName();
		        Date date = commit.getAuthorIdent().getWhen();
		        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

		        String formattedDate = outputFormat.format(date);
		        String dateCommit = formattedDate;
		        if (iter == 0) {
		        	LocalDate date1 = LocalDate.parse(dateCommit);
		        	System.out.println("LA DATA DEL COMMIT è: "+date1);
		            LocalDate date2 = LocalDate.parse(release.getReleaseDate());
		            System.out.println("LA DATA DELLA RELEASE è: "+date2);
		        	if (date1.isBefore(date2)) {
		        		System.out.println("AGGIUNGO IL COMMIT ALLA RELEASE");
		        		Commit myCommit = new Commit();
				        myCommit.setId(idCommit);
				        myCommit.setAuthor(nameAuthor);
				        myCommit.setDate(dateCommit);
				        myCommit.setMessage(completeCommitMessage);
				        commitsForThisRelease.add(myCommit);	
		        	}
		        }
		        else {
		        	Date commitDate = new Date((long) commit.getCommitTime() * 1000L);
		        	Date startDate = outputFormat.parse(releaseDateSince);
                    Date endDate = outputFormat.parse(release.getReleaseDate());
                    System.out.println("LA DATA DEL COMMIT è: "+commitDate);
                    System.out.println("LA DATA DELLA RELEASE START è: "+releaseDateSince);
                    System.out.println("LA DATA DELLA RELEASE FINISH è: "+release.getReleaseDate());
                    if (commitDate.after(startDate) && commitDate.before(endDate)) {
                    	System.out.println("AGGIUNGO IL COMMIT ALLA RELEASE");
		        		Commit myCommit = new Commit();
				        myCommit.setId(idCommit);
				        myCommit.setAuthor(nameAuthor);
				        myCommit.setDate(dateCommit);
				        myCommit.setMessage(completeCommitMessage);
				        commitsForThisRelease.add(myCommit);
		        	}
		        	
		        }
		        
		        
            }
        	
        }
        git.close();  
		return commitsForThisRelease;
	 }
	
	public ArrayList<Commit> getCommitsForRelease(Release release, String repo, String releaseDateSince, int iter) 
			throws IOException, JSONException, RevisionSyntaxException, NoHeadException, GitAPIException, ParseException {
		ArrayList<Commit> commitsForThisRelease = new ArrayList<Commit>();
        try (Repository repository = new FileRepository(new File(filepath + "/.git"))) {
            try (Git git = new Git(repository)) {
                try (RevWalk revWalk = new RevWalk(repository)) {
                    // Definiamo il formato della data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    // Impostiamo la data di inizio per il filtro
                         
                    Date endDate = dateFormat.parse(release.getReleaseDate());

                    // Ottieni l'ID del commit più recente
                    ObjectId head = repository.resolve("HEAD");

                    // Imposta il commit più recente come punto di partenza per la ricerca dei commit
                    revWalk.markStart(revWalk.parseCommit(head));
                    
                    if (iter == 0) {
                    	// Aggiungi un filtro per la data ai commit
                        revWalk.setRevFilter(CommitTimeRevFilter.before(endDate));
                    }
                    
                    else {
                    	Date startDate = dateFormat.parse(releaseDateSince);
                    	// Aggiungi un filtro per l'intervallo di date ai commit
                        revWalk.setRevFilter(CommitTimeRevFilter.between(startDate, endDate));
                    }

                    // Itera sui commit che soddisfano il filtro di data
                    for (RevCommit commit : revWalk) {
                        /* Fai qualcosa con il commit che corrisponde al filtro di data
                        System.out.println("Commit ID: " + commit.getId().getName());
                        System.out.println("Autore: " + commit.getAuthorIdent().getName());
                        System.out.println("Email dell'autore: " + commit.getAuthorIdent().getEmailAddress());
                        System.out.println("Messaggio: " + commit.getFullMessage());
                        System.out.println("Tempo: " + commit.getAuthorIdent().getWhen());
                        System.out.println("-------------------------------------------");*/
                        String completeCommitMessage = commit.getFullMessage();
        		        String idCommit = commit.getId().getName();
        		        String nameAuthor = commit.getAuthorIdent().getName();
        		        Date date = commit.getAuthorIdent().getWhen();
        		        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        		        String formattedDate = outputFormat.format(date);
        		        String dateCommit = formattedDate;
        		        
        		        Commit myCommit = new Commit();
				        myCommit.setId(idCommit);
				        myCommit.setAuthor(nameAuthor);
				        myCommit.setDate(dateCommit);
				        myCommit.setMessage(completeCommitMessage);
				        myCommit.setCommit(commit);
				        commitsForThisRelease.add(myCommit);

                    }
                }
            }
        }
        return commitsForThisRelease;
    }
	
	public ArrayList<JavaClass> getClasses(RevCommit commit) throws IOException, JSONException {
		 ArrayList<JavaClass> classes = new ArrayList<JavaClass>();
		 try (Repository repository = new FileRepository(new File(filepath + "/.git"))) {
			 try (TreeWalk treeWalk = new TreeWalk(repository)) {
	            treeWalk.addTree(commit.getTree());
	            treeWalk.setRecursive(true);
	            while (treeWalk.next()) {
	                if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("test") 
	                		&& !treeWalk.getPathString().contains("package-info.java")) {
	                	System.out.println("Includo la classe: "+treeWalk.getPathString());
	                	JavaClass jClass = new JavaClass();
	   				    jClass.setNamePath(treeWalk.getPathString());
	   				    jClass.setBuggy(false);
	   				    classes.add(jClass);
	                }
	            }
	        }
	    }
		 
		 
		 
		 
		 /*for (int i = 0; i < treeJsonArray.length(); i++) {
			 if (pathFromJSonObject.endsWith(".java") && !pathFromJSonObject.contains("Test") && !pathFromJSonObject.contains("package-info.java")) {
				 System.out.println("Includo la classe: "+pathFromJSonObject);
				 String urlFromJSonObject = fileJSonObject.getString("url");
				 JavaClass jClass = new JavaClass();
				 jClass.setNamePath(pathFromJSonObject);
				 jClass.setUrlClass(urlFromJSonObject);
				 classes.add(jClass);
			 };	
		 }*/
		 return classes;
	 }
}
