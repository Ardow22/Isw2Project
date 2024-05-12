package logic.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import logic.model.entity.JavaClass;
import logic.model.entity.Commit;
import logic.model.entity.Release;
import logic.model.entity.Ticket;
import logic.utils.JsonFileHandler;

public class MetricsController {
    public static JsonFileHandler JSFH;
    private static final String filepath = "C:\\Users\\HP\\Desktop\\Progetti Apache\\";
	
	
	public void calculateMetrics(Release release, ArrayList<Ticket> myTicketList, String repo) throws IOException, JSONException {
		ArrayList<String> totAuth = new ArrayList<String>();
		int NR = 0;
		int NFix = 0;
		int changeSetSize = 0;
		int maxChangeSetSize = 0;
		ArrayList<Integer> lines = new ArrayList<Integer>();
		Commit lastCommit = new Commit();
		System.out.println("Calcolo delle metriche nella release "+release.getNameRelease());
		lastCommit = release.getLastCommit();
		for (JavaClass jClass: release.getJavaClasses()) {			
			
			//1 CALCOLO NUMERO DI AUTORI [Nauth]
			totAuth = calculateAuthors(release, jClass);
			System.out.println("Il numero di autori è: "+totAuth);
			jClass.setAuthors(totAuth);

			//2 CALCOLO LOC DELL'ULTIMO COMMIT DELLA RELEASE [SIZE(LOC)]
			//3 CALCOLO LINEE DI COMMENTI DELL'ULTIMO COMMIT
			lines = countInClass(jClass, lastCommit, repo);
			System.out.println("Il numero di linee di codice è: "+lines.get(0));
			System.out.println("Il numero di linee di commenti è: "+lines.get(1));
			jClass.setLOC(lines.get(0));
			jClass.setLinesOfComments(lines.get(1));

			//4 CALCOLO NUMERO DI COMMIT CONTENENTE LA CLASSE [NR]
			NR = countCommits(release, jClass);
			System.out.println("Il numero di commit contenente la classe è: "+NR);
			jClass.setNumberOfCommits(NR);

			//5 CALCOLO NUMERO DI COMMIT FIXANTI IN CUI COMPARE LA CLASSE [Nfix]
			NFix = countFixCommits(myTicketList, release, jClass);
			System.out.println("Il numero di commit fixanti in cui compare la classe è: "+NFix);
			jClass.setNumberOfFixDefects(NFix);

			//6 CALCOLO ETà DELLA RELEASE [AGE OF RELEASE]
			release.setAgeOfRelease(calculateAgeOfRelease(release));
			System.out.println("L'età della release è: "+release.getAgeOfRelease());

			//7 CALCOLO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE (PRENDI ULTIMO COMMIT) [CHANGE SET SIZE]
			changeSetSize = countFiles(lastCommit);
			System.out.println("Il numero di file committed insieme alla classe è: "+changeSetSize);
			jClass.setChangeSetSize(changeSetSize);

			//8 CALCOLO MASSIMO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE [MAX CHANGE SET]
			maxChangeSetSize = maxCountFiles(jClass, release);
			System.out.println("Il numero massimo di file committed insieme alla classe è: "+maxChangeSetSize);
			jClass.setMaxChangeSetSize(maxChangeSetSize);
			
			}
		
	}
	
	
	public ArrayList<String> calculateAuthors(Release r, JavaClass jClass) {
		 ArrayList<String> totAuthors = new ArrayList<String>();
		 for (Commit c: r.getCommits()) {
			 ArrayList<String> jc = c.getClassesTouched();
		     for (String jName: jc) {
		    	 if (jClass.getNamePath().equals(jName))  {
		    		 if (!totAuthors.contains(c.getAuthor())) {
							 totAuthors.add(c.getAuthor());
					 }	
				 }
			 }
		 }
		 return totAuthors;
	 }
	
	public ArrayList<Integer> countInClass(JavaClass jClass, Commit lastCommit, String repo) throws IOException, JSONException {
		 /*int LOC = 0;
		 int linesOfComments = 0;
		 int numMethods = 0;
		 int numAttributes = 0;*/
		 ArrayList<Integer> total = new ArrayList<Integer>();
		 /*LOC = countLinesOfCode(jClass, lastCommit.getCommit(), repo);
	     total.add(LOC);
	     linesOfComments = countLinesOfComments(jClass, lastCommit.getCommit(), repo);
	     total.add(linesOfComments);
	     numMethods = countLinesOfMethods(jClass, lastCommit.getCommit(), repo);
	     total.add(numMethods);
	     numAttributes = countLinesOfAttributes(jClass, lastCommit.getCommit(), repo);
	     total.add(numMethods);*/
		 total = countAll(jClass, lastCommit.getCommit(), repo);
		 return total;
	}
	
	public ArrayList<Integer> countAll(JavaClass jClass, RevCommit commit, String repo) throws IOException {
		ArrayList<Integer> totalMetrics = new ArrayList<Integer>();
		int linesOfCode = 0;
		int linesOfComment = 0;
		try (Repository repository = new FileRepository(new File(filepath + repo + "\\" + "/.git"))) {
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
        try (Repository repository = new FileRepository(new File(filepath + repo + "\\" + "/.git"))) {
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
        try (Repository repository = new FileRepository(new File(filepath + repo + "\\" + "/.git"))) {
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
	
	public int countFixCommits(ArrayList<Ticket> ticketList, Release r, JavaClass jClass) {
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
	
	public boolean checkCommitTicket(Commit c, ArrayList<Ticket> ticketList) {
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
        // Conversione dei giorni in anni, mesi e giorni
        long yearsSinceRelease = daysSinceRelease / 365;
        return yearsSinceRelease;  
    }
	
	public int countFiles(Commit lastCommit) {
		return lastCommit.getClassesTouched().size();
		//return release.getJavaClasses().size();
	}
	 
	public int maxCountFiles(JavaClass jClass, Release r) {
		int max = 0;
		for (Commit c: r.getCommits()) {
			for (String jName: c.getClassesTouched()) {
				if (jName.equals(jClass.getNamePath())) {
					if (c.getClassesTouched().size() > max) {
					     max = c.getClassesTouched().size();
					}
				}
			} 
		 }
		 return max;
	 }
 
	 public void calculateBuggyness(Release release, CommitController Cc, String repository, ArrayList<Ticket> myTicketList) throws JSONException, IOException {
		 ArrayList<Commit> correctCommits = new ArrayList<Commit>();
		 correctCommits = selectCommitsWithTicket(release.getCommits(), myTicketList);
		 for (JavaClass jClass: release.getJavaClasses()) {
			 boolean result = checkBuggyness(jClass.getNamePath(), correctCommits, release);
			 if (result) {
				 jClass.setBuggy(true);
			 }
		 }
	 }
	 
	 public ArrayList<Commit> selectCommitsWithTicket(ArrayList<Commit> listCommit, ArrayList<Ticket> myTicketList) {
		 ArrayList<Commit> commits = new ArrayList<Commit>();
		 for (Commit c: listCommit) {
			 if (c.getTicket() != null && myTicketList.contains(c.getTicket())) {
				 commits.add(c);
			 }
		 }
		 return commits; 
	 }
	 
	 public boolean checkBuggyness(String className, ArrayList<Commit> listCorrectCommit, Release release) {
		 for (Commit c: listCorrectCommit) {
			 if (c.getClassesTouched().contains(className)) {
				 if (c.getTicket().getAffversions().size() != 0) {
					 if (c.getTicket().getAffversions().contains(release)) {
						 return true;
	    		     }
                 }
                 else {
                 	if (release.getNumberOfRelease() >= c.getTicket().getInjectedVersion().getNumberOfRelease() 
                 			&& release.getNumberOfRelease() < c.getTicket().getFixVersion().getNumberOfRelease()) {
                 		return true;	
	    			}	
                 } 
			 } 
		 }
		 return false;
	 }


	public void calculateBuggyness2(ArrayList<Release> myReleaseList, CommitController cc, String repository, ArrayList<Ticket> myTicketList) {
		for (Ticket t: myTicketList) {
			for (Commit c: t.getCommitsForTicket()) {
				for (String jName: c.getClassesTouched()) {
					checkJavaClass(jName, myReleaseList, c.getRelease());	
				}
			}
		}
		
	}


	private void checkJavaClass(String jName, ArrayList<Release> myReleaseList, Release commitRelease) {
		for (Release r: myReleaseList) {
			//if (r.getCommits().size() != 0) {
				for (JavaClass jc: r.getJavaClasses()) {
					if (jc.getNamePath().equals(jName)) {
						if (r.getNumberOfRelease() < commitRelease.getNumberOfRelease()) {
							jc.setBuggy(true);
						}
					}
				}
			//}
			
		}
		
	}


	public void setMetrics(Release release, String repo) throws JSONException, IOException {
		for (JavaClass jClass: release.getJavaClasses()) {			
			//1 CALCOLO NUMERO DI AUTORI [Nauth]
			ArrayList<String> totAuth = new ArrayList<String>();
			System.out.println("Il numero di autori è: "+totAuth);
			jClass.setAuthors(totAuth);

			ArrayList<Integer> lines = new ArrayList<Integer>();
			Commit lastCommit = new Commit();
			System.out.println("Calcolo delle metriche nella release "+release.getNameRelease());
			lastCommit = release.getLastCommit();
			//2 CALCOLO LOC DELL'ULTIMO COMMIT DELLA RELEASE [SIZE(LOC)]
			//3 CALCOLO LINEE DI COMMENTI DELL'ULTIMO COMMIT
			System.out.println("Il numero di linee di codice è: "+jClass.getLOC());
			System.out.println("Il numero di linee di commenti è: "+jClass.getLinesOfComments());
			lines = countInClass(jClass, lastCommit, repo);
			System.out.println("Il numero di linee di codice è: "+lines.get(0));
			System.out.println("Il numero di linee di commenti è: "+lines.get(1));
			jClass.setLOC(lines.get(0));
			jClass.setLinesOfComments(lines.get(1));

			//4 CALCOLO NUMERO DI COMMIT CONTENENTE LA CLASSE [NR]
			System.out.println("Il numero di commit contenente la classe è: "+0);
			jClass.setNumberOfCommits(0);

			//5 CALCOLO NUMERO DI COMMIT FIXANTI IN CUI COMPARE LA CLASSE [Nfix]
			System.out.println("Il numero di commit fixanti in cui compare la classe è: "+0);
			jClass.setNumberOfFixDefects(0);

			//6 CALCOLO ETà DELLA RELEASE [AGE OF RELEASE]
			release.setAgeOfRelease(calculateAgeOfRelease(release));
			System.out.println("L'età della release è: "+release.getAgeOfRelease());

			//7 CALCOLO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE (PRENDI ULTIMO COMMIT) [CHANGE SET SIZE]
			System.out.println("Il numero di file committed insieme alla classe è: "+0);
			jClass.setChangeSetSize(0);

			//8 CALCOLO MASSIMO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE [MAX CHANGE SET]
			System.out.println("Il numero massimo di file committed insieme alla classe è: "+0);
			jClass.setMaxChangeSetSize(0);
		}
	}
} 




