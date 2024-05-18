package logic.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import logic.model.entity.Commit;
import logic.model.entity.Release;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class CommitController {
	//String fILE_PATH = "C:\\Users\\HP\\Desktop\\Progetti Apache\\";
	String suffix1 = "\\";
	String suffix2 = "/.git";
	String filePath;
	
	public CommitController() {
		this.filePath = "C:\\Users\\HP\\Desktop\\Progetti Apache\\";
	}
	
	public String getFpath() {
		return filePath;
	}
	
	public void setFpath(String fPath) {
		this.filePath = fPath;
	}
		
	public List<Commit> getCommitsForRelease(Release release, String repo, String releaseDateSince, int iter) 
			throws IOException, JSONException, RevisionSyntaxException, ParseException {
		List<Commit> commitsForThisRelease = new ArrayList<>();
        try (Repository repository = new FileRepository(new File(getFpath() + repo +suffix1+suffix2))) {
            try (Git git = new Git(repository)) {
                try (RevWalk revWalk = new RevWalk(repository)) {
                    // Definiamo il formato della data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    // Impostiamo la data di inizio per il filtro
                         
                    Date endDate = dateFormat.parse(release.getReleaseDate());

                    //Ottienimento  dell'ID del commit più recente
                    ObjectId head = repository.resolve("HEAD");

                    // Impostazione del commit più recente come punto di partenza per la ricerca dei commit
                    revWalk.markStart(revWalk.parseCommit(head));
                    
                    if (iter == 0) {
                    	//Aggiunta di un filtro per la data ai commit
                        revWalk.setRevFilter(CommitTimeRevFilter.before(endDate));
                    }
                    
                    else {
                    	Date startDate = dateFormat.parse(releaseDateSince);
                    	// Aggiungi un filtro per l'intervallo di date ai commit
                        revWalk.setRevFilter(CommitTimeRevFilter.between(startDate, endDate));
                    }

                    //Iterazione sui commit che soddisfano il filtro di data
                    for (RevCommit commit : revWalk) {
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
				        myCommit.setRelease(release);
				        myCommit.setMessage(completeCommitMessage);
				        myCommit.setCommit(commit);
				        commitsForThisRelease.add(myCommit);
                    }
                }
            }
        }    
        return commitsForThisRelease;
    }
	
	public List<String> getClasses(RevCommit commit, String repo) throws IOException, JSONException {
		List<String> classes = new ArrayList<>();
		 try (Repository repository = new FileRepository(new File(getFpath() + repo + suffix1 + suffix2))) {
			 try (TreeWalk treeWalk = new TreeWalk(repository)) {
	            treeWalk.addTree(commit.getTree());
	            treeWalk.setRecursive(true);
	            while (treeWalk.next()) {
	                if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/") 
	                		&& !treeWalk.getPathString().contains("package-info.java")) {
	                	classes.add(treeWalk.getPathString());	
	                }
	            }
	        }
	    }
		 return classes;
	 }

	
	public List<String> getModifiedClasses(RevCommit commit, String project) throws IOException {
		List<String> modifiedClasses = new ArrayList<>();	
		try (Repository repo = new FileRepository(new File(getFpath() + project + suffix1 + suffix2))) {
			try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE); 
			ObjectReader reader = repo.newObjectReader()) {			
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			ObjectId newTree = commit.getTree();
			newTreeIter.reset(reader, newTree);
			RevCommit commitParent = commit.getParent(0);	
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			ObjectId oldTree = commitParent.getTree();
			oldTreeIter.reset(reader, oldTree);
			diffFormatter.setRepository(repo);
			List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

			for(DiffEntry entry : entries) {
				if(entry.getChangeType().equals(ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/") && !entry.getNewPath().contains("package-info.java")) {
					modifiedClasses.add(entry.getNewPath());
				}
			}
		    } catch(ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
		     }
		return modifiedClasses;
	    }
    }
}
