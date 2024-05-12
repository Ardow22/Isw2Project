package logic.model.entity;

import java.util.List;

public class JavaClass {
	
	private String namePath;
	private int lOC;
	private int linesOfComments;
	private List<String> authors;
	private Release release;
	private int numberOfCommits;
	private int numberOfFixDefects;
	private int changeSetSize;
	private int maxChangeSetSize;
	private boolean buggy;

	public String getNamePath() {
		return namePath;
	}

	public void setNamePath(String namePath) {
		this.namePath = namePath;
	}

	public int getLOC() {
		return lOC;
	}

	public void setLOC(int loC) {
		lOC = loC;
	}

	public int getNumberOfAuthors() {
		return authors.size();
	}
	
	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> auth) {
		this.authors = auth;
	}


	public int getNumberOfFixDefects() {
		return numberOfFixDefects;
	}

	public void setNumberOfFixDefects(int numberOfFixDefects) {
		this.numberOfFixDefects = numberOfFixDefects;
	}

	public boolean isBuggy() {
		return buggy;
	}

	public void setBuggy(boolean buggy) {
		this.buggy = buggy;
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
	}

	public int getNumberOfCommits() {
		return numberOfCommits;
	}

	public void setNumberOfCommits(int numberOfCommits) {
		this.numberOfCommits = numberOfCommits;
	}

	public int getLinesOfComments() {
		return linesOfComments;
	}

	public void setLinesOfComments(int linesOfComments) {
		this.linesOfComments = linesOfComments;
	}

	public int getChangeSetSize() {
		return changeSetSize;
	}

	public void setChangeSetSize(int changeSetSize) {
		this.changeSetSize = changeSetSize;
	}

	public int getMaxChangeSetSize() {
		return maxChangeSetSize;
	}

	public void setMaxChangeSetSize(int maxChangeSetSize) {
		this.maxChangeSetSize = maxChangeSetSize;
	}
}
