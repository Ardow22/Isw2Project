package logic.model.entity;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {
	private Ticket ticket; //sarebbe il ticket preso da JIRA
	private String id; //sarebbe lo SHA
	private String author;
	private String date;
	private String message;
	private RevCommit rCommit;
	private Release release;
	private List<String> classesTouched;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getClassesTouched() {
		return classesTouched;
	}

	public void setClassesTouched(List<String> classesTouched) {
		this.classesTouched = classesTouched;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public RevCommit getCommit() {
		return rCommit;
	}

	public void setCommit(RevCommit commit) {
		this.rCommit = commit;
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
	}


	
}
