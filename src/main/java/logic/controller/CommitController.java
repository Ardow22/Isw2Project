package logic.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class CommitController {
	private static final String filepath = "C:\\Users\\HP\\Desktop\\Progetti Apache\\bookkeeper\\";
	
	public ArrayList<Commit> getCommitsForRelease(Release release, String repo, String releaseDateSince, int iter) throws IOException, JSONException, RevisionSyntaxException, NoHeadException, GitAPIException {
		ArrayList<Commit> commitsForThisRelease = new ArrayList<Commit>();
		Repository repository = new FileRepository(filepath+"/.git/");
        Git git = new Git(repository);
        
        List<RevCommit> allCommitsList = new ArrayList<>();
        List<Ref> branchesList = git.branchList().setListMode(ListMode.ALL).call();
        
        for (Ref branch: branchesList) {
        	Iterable<RevCommit> iterable = git.log().add(repository.resolve(branch.getName())).call();
        	for (RevCommit commit : iterable) {
                // Ora puoi accedere a ciascun commit tramite la variabile 'commit'
                System.out.println("Commit ID: " + commit.getId().getName());
                System.out.println("Autore: " + commit.getAuthorIdent().getName());
                System.out.println("Email dell'autore: " + commit.getAuthorIdent().getEmailAddress());
                System.out.println("Messaggio: " + commit.getFullMessage());
                System.out.println("Tree: " + commit.getTree());
                System.out.println("Tempo: " + commit.getAuthorIdent().getWhen());
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
		            LocalDate date2 = LocalDate.parse(release.getReleaseDate());
		        	if (date1.isBefore(date2)) {
		        		Commit myCommit = new Commit();
				        myCommit.setId(idCommit);
				        myCommit.setAuthor(nameAuthor);
				        myCommit.setDate(dateCommit);
				        myCommit.setMessage(completeCommitMessage);
				        commitsForThisRelease.add(myCommit);	
		        	}
		        	else {
		        		break;
		        	}
		        }
		        else {
		        	LocalDate date1 = LocalDate.parse(dateCommit);
		            LocalDate date2 = LocalDate.parse(release.getReleaseDate());
		            LocalDate date3 = LocalDate.parse(releaseDateSince);
		        	if (date1.isBefore(date2) && date3.isBefore(date1)) {
		        		Commit myCommit = new Commit();
				        myCommit.setId(idCommit);
				        myCommit.setAuthor(nameAuthor);
				        myCommit.setDate(dateCommit);
				        myCommit.setMessage(completeCommitMessage);
				        commitsForThisRelease.add(myCommit);
		        	}
		        	else {
		        		break;
		        	}
		        }
		        
		        
            }
        	
        }
        git.close();

        
		return null;
	 }


}
