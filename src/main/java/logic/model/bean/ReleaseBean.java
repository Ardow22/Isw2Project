package logic.model.bean;

import java.util.List;

import logic.model.entity.Commit;
import logic.model.entity.JavaClass;
import logic.model.entity.Ticket;

public class ReleaseBean {
	private String releaseDate;
	private String nameRelease;
	private String idRelease;
	private int numberOfRelease;
	private long ageOfRelease;
	private Commit lastCommit;
	private List<Commit> commits;
	private List<Ticket> ticketsClosedBug;
	private List<JavaClass> javaClasses;
	
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
	
	public List<Commit> getCommits() {
		return commits;
	}
	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}
	public List<Ticket> getTicketsClosedBug() {
		return ticketsClosedBug;
	}
	public void setTicketsClosedBug(List<Ticket> ticketsClosedBug) {
		this.ticketsClosedBug = ticketsClosedBug;
	}
	public int getNumberOfRelease() {
		return numberOfRelease;
	}
	public void setNumberOfRelease(int numberOfRelease) {
		this.numberOfRelease = numberOfRelease;
	}
	public List<JavaClass> getJavaClasses() {
		return javaClasses;
	}
	public void setJavaClasses(List<JavaClass> javaClasses) {
		this.javaClasses = javaClasses;
	}
	public long getAgeOfRelease() {
		return ageOfRelease;
	}
	public void setAgeOfRelease(long ageofRelease) {
		ageOfRelease = ageofRelease;
	}
	public Commit getLastCommit() {
		return lastCommit;
	}
	public void setLastCommit(Commit lastCommit) {
		this.lastCommit = lastCommit;
	}

}
