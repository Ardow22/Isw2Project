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
    private static final String filepath = "C:\\Users\\HP\\Desktop\\Progetti Apache\\bookkeeper\\";
	
	
	public void calculateMetrics(Release release, ArrayList<String> classList, ArrayList<Ticket> myTicketList) throws IOException, JSONException {
		System.out.println("CALCOLIAMO LE METRICHE PER LA CLASSE");
		ArrayList<String> totAuth = new ArrayList<String>();
		int NR = 0;
		int NFix = 0;
		int changeSetSize = 0;
		int maxChangeSetSize = 0;
		ArrayList<Integer> lines = new ArrayList<Integer>();
		Commit lastCommit = new Commit();
		if (release.getNameRelease().equals("4.2.0")) {
			System.out.println("La release 4.2.0 non ha commit");
		}
		else {
			System.out.println("Calcolo delle metriche nella release "+release.getNameRelease());
			for (String jvName: classList) {
				lastCommit = findLastCommit(release, jvName);
				if (lastCommit == null) {
					System.out.println("QUESTA CLASSE NON HA COMMIT");
				}
				else {
					System.out.println("L'ULTIMO COMMIT è: "+lastCommit.getId());
					
					//1 CALCOLO NUMERO DI AUTORI [Nauth]
					totAuth = calculateAuthors(release, jvName);
					System.out.println("Il numero di autori è: "+totAuth);
					
					//2 CALCOLO LOC DELL'ULTIMO COMMIT DELLA RELEASE [SIZE(LOC)]
					//3 CALCOLO LINEE DI COMMENTI DELL'ULTIMO COMMIT
					//4 CALCOLO NUMERO DI METODI DELLA CLASSE
					//5 CALCOLO NUMERO DI ATTRIBUTI DELLA CLASSE
					lines = countInClass(jvName, lastCommit);
					System.out.println("Il numero di linee di codice è: "+lines.get(0));
					System.out.println("Il numero di linee di commenti è: "+lines.get(1));
					//System.out.println("Il numero di metodi nella classe è: "+lines.get(2));
					//System.out.println("Il numero di attributi nella classe è: "+lines.get(3));
					
					//6 CALCOLO NUMERO DI COMMIT CONTENENTE LA CLASSE [NR]
					NR = countCommits(release, jvName);
					System.out.println("Il numero di commit contenente la classe è: "+NR);
					
					//7 CALCOLO NUMERO DI COMMIT FIXANTI IN CUI COMPARE LA CLASSE [Nfix]
					NFix = countFixCommits(myTicketList, release, jvName);
					System.out.println("Il numero di commit fixanti in cui compare la classe è: "+NFix);
					
					//8 CALCOLO ETà DELLA RELEASE [AGE OF RELEASE]
					release.setAgeOfRelease(calculateAgeOfRelease(release));
					System.out.println("L'età della release è: "+release.getAgeOfRelease());
					
					//9 CALCOLO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE (PRENDI ULTIMO COMMIT) [CHANGE SET SIZE]
					changeSetSize = countFiles(jvName, lastCommit);
					System.out.println("Il numero di file committed insieme alla classe è: "+changeSetSize);
					
					//10 CALCOLO MASSIMO NUMERO DI FILE COMMITTED INSIEME ALLA CLASSE [MAX CHANGE SET]
					maxChangeSetSize = maxCountFiles(jvName, release);
					System.out.println("Il numero massimo di file committed insieme alla classe è: "+maxChangeSetSize);
					
				}
				linkAuthToClass(jvName, totAuth, release);
				linkLinesClass(jvName, lines, lastCommit);
				linkNRtoClass(jvName, NR, release);
				linkNFixtoClass(jvName, NFix, release);
				linkCountFilesClass(jvName, changeSetSize, lastCommit);
				linkMaxCountFilesClass(jvName, maxChangeSetSize, release);
			}
		}
	}
	
	public Commit findLastCommit(Release r, String className) {
		Commit lastCommit = new Commit();
		for (Commit c: r.getCommits()) {
			for (JavaClass jc: c.getClassesTouched()) {
				if (jc.getNamePath().equals(className)) {
					lastCommit = c;
				}
			}		
		}
		return lastCommit;//da verificare se i commit in r.getCommits() sono ordinati temporalmente
	}
	
	public ArrayList<String> calculateAuthors(Release r, String className) {
		 ArrayList<String> totAuthors = new ArrayList<String>();
		 ArrayList<Commit> commitList = r.getCommits();
		 for (Commit c: commitList) {
			 ArrayList<JavaClass> jc = c.getClassesTouched();
		     for (JavaClass jClass: jc) {
		    	 if (jClass.getNamePath().equals(className))  {
		    		 if (!totAuthors.contains(c.getAuthor())) {
							 totAuthors.add(c.getAuthor());
					 }	
				 }
			 }
		 }
		 return totAuthors;
	 }
	
	public void linkAuthToClass(String className, ArrayList<String> totAuth, Release r) {
		 ArrayList<Commit> commitList = r.getCommits();
		 for (Commit c: commitList) {
			 ArrayList<JavaClass> jc = c.getClassesTouched();
			 for (JavaClass jClass: jc) {
				 if (jClass.getNamePath() == className && jClass.getRelease().getNameRelease() == r.getNameRelease()) {
					 jClass.setAuthors(totAuth);
				 }	
			 }
		 }	 
	}
	
	public ArrayList<Integer> countInClass(String nameClass, Commit lastCommit) throws IOException, JSONException {
		 String urlClass = retrieveUrlClass(nameClass, lastCommit);
		 int LOC = 0;
		 int linesOfComments = 0;
		 //int numberOfMethods = 0;
		 //int numberOfAttributes = 0;
		 ArrayList<Integer> total = new ArrayList<Integer>();
		 LOC = countLinesOfCode(nameClass, lastCommit.getCommit());
	     total.add(LOC);
	     linesOfComments = countLinesOfComments(nameClass, lastCommit.getCommit());
	     total.add(linesOfComments);
	     //numberOfMethods = countMethods(decodedContent);
	     //total.add(numberOfMethods);
	     /*numberOfAttributes = countAttributes(decodedContent);
	     total.add(numberOfAttributes);*/
		 return total;
	}
	
	public String retrieveUrlClass(String nameClass, Commit lastCommit) {
		 for (JavaClass jvc: lastCommit.getClassesTouched()) {
			 if (nameClass.equals(jvc.getNamePath())) {
				 return jvc.getUrlClass();
			 }
		 }
		 return null;
	}
	
	public int countLinesOfCode(String fileName, RevCommit commit) throws IOException {
        int linesOfCode = 0;
        try (Repository repository = new FileRepository(new File(filepath + "/.git"))) {
        	try (TreeWalk treeWalk = new TreeWalk(repository)) {
        		treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                	if (treeWalk.getPathString().equals(fileName)) {
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
    
    public int countLinesOfComments(String fileName, RevCommit commit) throws IOException {
        int linesOfComments = 0;
        try (Repository repository = new FileRepository(new File(filepath + "/.git"))) {
        	try (TreeWalk treeWalk = new TreeWalk(repository)) {
        		treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                	if (treeWalk.getPathString().equals(fileName)) {
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
	
	public int countMethods(String code) {
	    int methodCount = 0;

	    // Rimuovi i commenti multilinea
	    String codeWithoutComments = code.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");

	    // Rimuovi i commenti singola riga
	    codeWithoutComments = codeWithoutComments.replaceAll("//.*", "");

	    // Divide il codice in righe
	    String[] lines = codeWithoutComments.split("\\r?\\n");

	    // Indica se siamo all'interno di un metodo
	    boolean insideMethod = false;

	    // Loop attraverso le linee di codice
	    for (String line : lines) {
	        // Rimuovi spazi iniziali e finali dalla linea
	        String trimmedLine = line.trim();

	        // Controlla se la linea inizia con la firma di un metodo
	        if (trimmedLine.startsWith("public") || trimmedLine.startsWith("protected") || trimmedLine.startsWith("private") || trimmedLine.startsWith("static")) {
	            insideMethod = true;
	            methodCount++;
	        } else if (trimmedLine.startsWith("{")) {
	            // Se la linea inizia con '{', potrebbe essere la definizione del corpo di un metodo
	            if (insideMethod) {
	                methodCount++;
	            }
	        } else if (trimmedLine.startsWith("}")) {
	            // Se la linea inizia con '}', potrebbe indicare la fine di un metodo
	            if (insideMethod) {
	                insideMethod = false;
	            }
	        }
	    }
	    return methodCount;
    }
	
	public int countAttributes(String code) {
	    int attributeCount = 0;

	    // Rimuovi i commenti multilinea
	    String codeWithoutComments = code.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");

	    // Rimuovi i commenti singola riga
	    codeWithoutComments = codeWithoutComments.replaceAll("//.*", "");

	    // Trova l'inizio e la fine del corpo della classe
	    int classBodyStartIndex = codeWithoutComments.indexOf("{");
	    int classBodyEndIndex = codeWithoutComments.lastIndexOf("}");

	    if (classBodyStartIndex != -1 && classBodyEndIndex != -1) {
	        // Estrai il corpo della classe
	        String classBody = codeWithoutComments.substring(classBodyStartIndex + 1, classBodyEndIndex);

	        // Dividi il corpo della classe in righe
	        String[] lines = classBody.split("\\r?\\n");

	        // Loop attraverso le linee di codice nel corpo della classe
	        for (String line : lines) {
	            // Rimuovi spazi iniziali e finali dalla linea
	            String trimmedLine = line.trim();

	            // Controlla se la linea contiene una dichiarazione di attributo
	            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("public") && !trimmedLine.startsWith("private") &&
	                    !trimmedLine.startsWith("protected") && !trimmedLine.startsWith("static") &&
	                    !trimmedLine.startsWith("class") && !trimmedLine.startsWith("interface") &&
	                    !trimmedLine.startsWith("enum") && !trimmedLine.startsWith("}") &&
	                    !trimmedLine.startsWith("{") && !trimmedLine.startsWith("/*") &&
	                    !trimmedLine.startsWith("*")) {
	                attributeCount++;
	            }
	         }
	     }
	     return attributeCount;
      }
	
	public void linkLinesClass(String className, ArrayList<Integer> lines, Commit lastCommit) {
		 ArrayList<JavaClass> jc = lastCommit.getClassesTouched();
		 int LOC = lines.get(0);
		 int linesOfComments = lines.get(1);
		 //int numberOfMethods = lines.get(2);
		 //int numberOfAttributes = lines.get(3);
		 for (JavaClass jClass: jc) {
			 if (jClass.getNamePath().equals(className)) {
				 jClass.setLOC(LOC);
				 jClass.setLinesOfComments(linesOfComments);
				 //jClass.setNumberOfMethods(numberOfMethods);
				 //jClass.setNumberOfAttributes(numberOfAttributes);
				 
		     }
	     }
	 }
	
	public int countCommits(Release r, String className) {
		int count = 0;
		for (Commit c: r.getCommits()) {
			for (JavaClass jc: c.getClassesTouched()) {
				if (jc.getNamePath().equals(className)) {
					count++;
				}
			}
		}
		return count;
	}
	
	public void linkNRtoClass(String className, int NR, Release r) {
		ArrayList<Commit> commitList = r.getCommits();
		for (Commit c: commitList) {
			ArrayList<JavaClass> jc = c.getClassesTouched();
			for (JavaClass jClass: jc) {
				if (jClass.getNamePath().equals(className) && jClass.getRelease().getNameRelease() == r.getNameRelease()) {
					jClass.setNumberOfCommits(NR);
				}	
			}
		}	
	}
	
	public int countFixCommits(ArrayList<Ticket> ticketList, Release r, String className) {
		int count = 0;
		for (Commit c: r.getCommits()) {
			boolean result = false;
			result = checkCommitTicket(c, ticketList);
			if (result) {
				for (JavaClass jc: c.getClassesTouched()) {
					if (jc.getNamePath().equals(className)) {
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
	
	public void linkNFixtoClass(String className, int NFix, Release r) {
		ArrayList<Commit> commitList = r.getCommits();
		for (Commit c: commitList) {
			ArrayList<JavaClass> jc = c.getClassesTouched();
			for (JavaClass jClass: jc) {
				if (jClass.getNamePath().equals(className) && jClass.getRelease().getNameRelease() == r.getNameRelease()) {
					jClass.setNumberOfFixDefects(NFix);
				}	
			}
		}	
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
	
	public int countFiles(String className, Commit lastCommit) {
		 return lastCommit.getClassesTouched().size();//DA VERIFICARE SE CONSIDERARE ANCHE I FILE CHE NON SIANO CLASSI
	}
	
	public void linkCountFilesClass(String className, int changeSetSize, Commit lastCommit) {
		 ArrayList<JavaClass> jc = lastCommit.getClassesTouched();
		 for (JavaClass jClass: jc) {
			 if (jClass.getNamePath().equals(className)) {
				 jClass.setChangeSetSize(changeSetSize);
		     }
	     }
	 }
	 
	 public int maxCountFiles(String className, Release r) {
		 int max = 0;
		 for (Commit c: r.getCommits()) {
			for (JavaClass jvc: c.getClassesTouched()) {
				if (jvc.getNamePath().equals(className)) {
					if (c.getClassesTouched().size() > max) {
					     max = c.getClassesTouched().size();
					}
				}
			} 
		 }
		 return max;
	 }
	 
	 public void linkMaxCountFilesClass(String className, int maxChangeSetSize, Release r) {
		 ArrayList<Commit> commitList = r.getCommits();
		 for (Commit c: commitList) {
			ArrayList<JavaClass> jc = c.getClassesTouched();
			for (JavaClass jClass: jc) {
				if (jClass.getNamePath().equals(className) && jClass.getRelease().getNameRelease() == r.getNameRelease()) {
					jClass.setMaxChangeSetSize(maxChangeSetSize);
				}	
			} 
		 }	 
	 }
	
} 




