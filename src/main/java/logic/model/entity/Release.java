package logic.model.entity;

import java.util.ArrayList;

public class Release {
	private String releaseDate;
	private String nameRelease;
	private String idRelease;
	private int numberOfRelease;
	private long AgeOfRelease;
	private Commit lastCommit;
	private ArrayList<Commit> commits;
	private ArrayList<Ticket> ticketsClosedBug;
	private ArrayList<JavaClass> javaClasses;
	
	public String getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	public String getNameRelease() {
		return nameRelease;
	}
	public void setNameRelease(String nameRelease) {
		this.nameRelease = nameRelease;
	}
	public String getIdRelease() {
		return idRelease;
	}
	public void setIdRelease(String idRelease) {
		this.idRelease = idRelease;
	}
	
	public ArrayList<Commit> getCommits() {
		return commits;
	}
	public void setCommits(ArrayList<Commit> commits) {
		this.commits = commits;
	}
	public ArrayList<Ticket> getTicketsClosedBug() {
		return ticketsClosedBug;
	}
	public void setTicketsClosedBug(ArrayList<Ticket> ticketsClosedBug) {
		this.ticketsClosedBug = ticketsClosedBug;
	}
	public int getNumberOfRelease() {
		return numberOfRelease;
	}
	public void setNumberOfRelease(int numberOfRelease) {
		this.numberOfRelease = numberOfRelease;
	}
	public ArrayList<JavaClass> getJavaClasses() {
		return javaClasses;
	}
	public void setJavaClasses(ArrayList<JavaClass> javaClasses) {
		this.javaClasses = javaClasses;
	}
	public long getAgeOfRelease() {
		return AgeOfRelease;
	}
	public void setAgeOfRelease(long ageOfRelease) {
		AgeOfRelease = ageOfRelease;
	}
	public Commit getLastCommit() {
		return lastCommit;
	}
	public void setLastCommit(Commit lastCommit) {
		this.lastCommit = lastCommit;
	}
	

}
