package logic.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import logic.model.entity.JavaClass;
import logic.model.entity.Commit;
import logic.model.entity.Release;
import logic.model.entity.Ticket;

public class MetricsController {
    private static final String FILE_PATH = "C:\\Users\\HP\\Desktop\\Progetti Apache\\";
    String sUFFIX1 = "\\";
    String sUFFIX2 = "/.git";
	
	
	public void calculateMetrics(Release release, List<Ticket> myTicketList, String repo) throws IOException, JSONException {
		int nR = 0;
		int nFix = 0;
		int changeSetSize = 0;
		int maxChangeSetSize = 0;
		Commit lastCommit = release.getLastCommit();
		for (JavaClass jClass: release.getJavaClasses()) {			
			
			//1 CALCOLO NUMERO DI AUTORI [Nauth]
			List<String> totAuth = calculateAuthors(release, jClass);
			jClass.setAuthors(totAuth);

			//2 CALCOLO LOC DELL'ULTIMO COMMIT DELLA RELEASE [SIZE(LOC)]
			//3 CALCOLO LINEE DI COMMENTI DELL'ULTIMO COMMIT
			List<Integer> lines = countInClass(jClass, lastCommit, repo);
			jClass.setLOC(lines.get(0));
			jClass.setLinesOfComments(lines.get(1));

			//4 CALCOLO NUMERO DI COMMIT CONTENENTE LA CLASSE [NR]
			nR = countCommits(release, jClass);
			jClass.setNumberOfCommits(nR);

			//5 CALCOLO NUMERO DI COMMIT FIXANTI IN CUI COMPARE LA CLASSE [Nfix]
			nFix = countFixCommits(myTicketList, release, jClass);
			jClass.setNumberOfFixDefects(nFix);

			//6 CALCOLO ETà DELLA RELEASE [AGE OF RELEASE]
			release.setAgeOfRelease(calculateAgeOfRelease(release));

			//7 CALCOLO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE (PRENDI ULTIMO COMMIT) [CHANGE SET SIZE]
			changeSetSize = countFiles(lastCommit);
			jClass.setChangeSetSize(changeSetSize);

			//8 CALCOLO MASSIMO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE [MAX CHANGE SET]
			maxChangeSetSize = maxCountFiles(jClass, release);
			jClass.setMaxChangeSetSize(maxChangeSetSize);
			
			}
		
	}
	
	
	public List<String> calculateAuthors(Release r, JavaClass jClass) {
		 List<String> totAuthors = new ArrayList<>();
		 for (Commit c: r.getCommits()) {
			 List<String> jc = c.getClassesTouched();
		     for (String jName: jc) {
		    	 if (jClass.getNamePath().equals(jName) && !totAuthors.contains(c.getAuthor()))  {
		    		 totAuthors.add(c.getAuthor());
					 	
				 }
			 }
		 }
		 return totAuthors;
	 }
	
	public List<Integer> countInClass(JavaClass jClass, Commit lastCommit, String repo) throws IOException, JSONException {
		 /*int LOC = 0;
		 int linesOfComments = 0;
		 int numMethods = 0;
		 int numAttributes = 0;*/
		 /*LOC = countLinesOfCode(jClass, lastCommit.getCommit(), repo);
	     total.add(LOC);
	     linesOfComments = countLinesOfComments(jClass, lastCommit.getCommit(), repo);
	     total.add(linesOfComments);
	     numMethods = countLinesOfMethods(jClass, lastCommit.getCommit(), repo);
	     total.add(numMethods);
	     numAttributes = countLinesOfAttributes(jClass, lastCommit.getCommit(), repo);
	     total.add(numMethods);*/
		 return countAll(jClass, lastCommit.getCommit(), repo);
	}
	
	public List<Integer> countAll(JavaClass jClass, RevCommit commit, String repo) throws IOException {
		List<Integer> totalMetrics = new ArrayList<>();
		int linesOfCode = 0;
		int linesOfComment = 0;
		try (Repository repository = new FileRepository(new File(FILE_PATH + repo + sUFFIX1 + sUFFIX2))) {
        	try (TreeWalk treeWalk = new TreeWalk(repository)) {
        		treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                	if (treeWalk.getPathString().equals(jClass.getNamePath())) {
                		String content = new String(repository.open(treeWalk.getObjectId(0)).getBytes());
                        linesOfCode = countNonEmptyLines(content);
                        linesOfComment = countComments(content);
                        totalMetrics.add(linesOfCode);
                        totalMetrics.add(linesOfComment);
                        break;
                    }
                }
            }
        }
		return totalMetrics;
	}
			
	public int countLinesOfCode(JavaClass jClass, RevCommit commit, String repo) throws IOException {
        int linesOfCode = 0;
        try (Repository repository = new FileRepository(new File(FILE_PATH + repo + sUFFIX1 + sUFFIX2))) {
        	try (TreeWalk treeWalk = new TreeWalk(repository)) {
        		treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                	if (treeWalk.getPathString().equals(jClass.getNamePath())) {
                		String content = new String(repository.open(treeWalk.getObjectId(0)).getBytes());
                        linesOfCode = countNonEmptyLines(content);
                        break;
                    }
                }
            }
        }
        return linesOfCode;
    }

    public int countNonEmptyLines(String content) {
        int nonEmptyLines = 0;
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("//")) {
                nonEmptyLines++;
            }
        }
        return nonEmptyLines;
    }
    
    public int countLinesOfComments(JavaClass jClass, RevCommit commit, String repo) throws IOException {
        int linesOfComments = 0;
        try (Repository repository = new FileRepository(new File(FILE_PATH + repo + sUFFIX1 + sUFFIX2))) {
        	try (TreeWalk treeWalk = new TreeWalk(repository)) {
        		treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                	if (treeWalk.getPathString().equals(jClass.getNamePath())) {
                		String content = new String(repository.open(treeWalk.getObjectId(0)).getBytes());
                        linesOfComments = countComments(content);
                        break;
                    }
                }
            }
        }
        return linesOfComments;
    }
	
	public int countComments(String code) {
		 int commentLines = 0;

		 String[] lines = code.split("\\r?\\n");
		 for (String line : lines) {
			 String trimmedLine = line.trim();
		     // Controlla se la riga inizia con '//' o '/*' per determinare se è una linea di commento
			 if (trimmedLine.startsWith("//") || trimmedLine.startsWith("/*")) {
				 commentLines++;
		     }
		 }
		 return commentLines;		
	}
	
	public int countCommits(Release r, JavaClass jClass) {
		int count = 0;
		for (Commit c: r.getCommits()) {
			for (String jName: c.getClassesTouched()) {
				if (jName.equals(jClass.getNamePath())) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int countFixCommits(List<Ticket> ticketList, Release r, JavaClass jClass) {
		int count = 0;
		for (Commit c: r.getCommits()) {
			boolean result = false;
			result = checkCommitTicket(c, ticketList);
			if (result) {
				for (String jName: c.getClassesTouched()) {
					if (jName.equals(jClass.getNamePath())) {
						count++;
					}
				}
			}
		}
		return count;
	}
	
	public boolean checkCommitTicket(Commit c, List<Ticket> ticketList) {
		for (Ticket t: ticketList) {
			if (t.getCommitsForTicket().contains(c)) {
				return true;
			}
		}
		return false;
	}
	
	public long calculateAgeOfRelease(Release release) {
  	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Converte le stringhe data in LocalDateTime
        LocalDate releaseDate = LocalDate.parse(release.getReleaseDate(), formatter);
        LocalDate currentDate = LocalDate.now();
        long daysSinceRelease = currentDate.toEpochDay() - releaseDate.toEpochDay();
        return daysSinceRelease / 365;  
    }
	
	public int countFiles(Commit lastCommit) {
		return lastCommit.getClassesTouched().size();
		//return release.getJavaClasses().size();
	}
	 
	public int maxCountFiles(JavaClass jClass, Release r) {
		int max = 0;
		for (Commit c: r.getCommits()) {
			for (String jName: c.getClassesTouched()) {
				if (jName.equals(jClass.getNamePath()) && c.getClassesTouched().size() > max) {
					max = c.getClassesTouched().size();
				}
			} 
		 }
		 return max;
	 }
	 
	 public List<Commit> selectCommitsWithTicket(List<Commit> listCommit, List<Ticket> myTicketList) {
		 List<Commit> commits = new ArrayList<>();
		 for (Commit c: listCommit) {
			 if (c.getTicket() != null && myTicketList.contains(c.getTicket())) {
				 commits.add(c);
			 }
		 }
		 return commits; 
	 }


	public void calculateBuggyness(List<Release> myReleaseList, List<Ticket> myTicketList) {
		for (Ticket t: myTicketList) {
			for (Commit c: t.getCommitsForTicket()) {
				for (String jName: c.getClassesTouched()) {
					checkJavaClass(jName, myReleaseList, c.getRelease());	
				}
			}
		}
		
	}


	private void checkJavaClass(String jName, List<Release> myReleaseList, Release commitRelease) {
		for (Release r: myReleaseList) {
			//if (r.getCommits().size() != 0) {
				for (JavaClass jc: r.getJavaClasses()) {
					if (jc.getNamePath().equals(jName) && r.getNumberOfRelease() < commitRelease.getNumberOfRelease()) {
						jc.setBuggy(true);
						
					}
				}
			//}
			
		}
		
	}


	public void setMetrics(Release release, String repo) throws JSONException, IOException {
		for (JavaClass jClass: release.getJavaClasses()) {			
			//1 CALCOLO NUMERO DI AUTORI [Nauth]
			List<String> totAuth = new ArrayList<>();
			jClass.setAuthors(totAuth);

			Commit lastCommit = release.getLastCommit();
			//2 CALCOLO LOC DELL'ULTIMO COMMIT DELLA RELEASE [SIZE(LOC)]
			//3 CALCOLO LINEE DI COMMENTI DELL'ULTIMO COMMIT
			List<Integer> lines = countInClass(jClass, lastCommit, repo);
			jClass.setLOC(lines.get(0));
			jClass.setLinesOfComments(lines.get(1));

			//4 CALCOLO NUMERO DI COMMIT CONTENENTE LA CLASSE [NR]
			jClass.setNumberOfCommits(0);

			//5 CALCOLO NUMERO DI COMMIT FIXANTI IN CUI COMPARE LA CLASSE [Nfix]
			jClass.setNumberOfFixDefects(0);

			//6 CALCOLO ETà DELLA RELEASE [AGE OF RELEASE]
			release.setAgeOfRelease(calculateAgeOfRelease(release));

			//7 CALCOLO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE (PRENDI ULTIMO COMMIT) [CHANGE SET SIZE]
			jClass.setChangeSetSize(0);

			//8 CALCOLO MASSIMO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE [MAX CHANGE SET]
			jClass.setMaxChangeSetSize(0);
		}
	}
} 




