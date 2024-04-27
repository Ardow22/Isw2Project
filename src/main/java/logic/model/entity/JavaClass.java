package logic.model.entity;

import java.util.ArrayList;



public class JavaClass {
	
	private String namePath;
	private String urlClass;
	private int LOC;
	private int linesOfComments;
	private ArrayList<String> Authors;
	private Release release;
	private int numberOfCommits;
	private int numberOfFixDefects;
	private int changeSetSize;
	private int maxChangeSetSize;
	private int addedLOC;
	private int maxAddedLOC;
	private int numberOfMethods;
	private int numberOfAttributes;
	private boolean buggy;

	public String getNamePath() {
		return namePath;
	}

	public void setNamePath(String namePath) {
		this.namePath = namePath;
	}

	public int getLOC() {
		return LOC;
	}

	public void setLOC(int lOC) {
		LOC = lOC;
	}

	public String getUrlClass() {
		return urlClass;
	}

	public void setUrlClass(String urlClass) {
		this.urlClass = urlClass;
	}

	public int getNumberOfAuthors() {
		return Authors.size();
	}
	
	public ArrayList<String> getAuthors() {
		return Authors;
	}

	public void setAuthors(ArrayList<String> authors) {
		this.Authors = authors;
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

	public int getAddedLOC() {
		return addedLOC;
	}

	public void setAddedLOC(int addedLOC) {
		this.addedLOC = addedLOC;
	}

	public int getMaxAddedLOC() {
		return maxAddedLOC;
	}

	public void setMaxAddedLOC(int maxAddedLOC) {
		this.maxAddedLOC = maxAddedLOC;
	}

	public int getNumberOfMethods() {
		return numberOfMethods;
	}

	public void setNumberOfMethods(int numberOfMethods) {
		this.numberOfMethods = numberOfMethods;
	}

	public int getNumberOfAttributes() {
		return numberOfAttributes;
	}

	public void setNumberOfAttributes(int numberOfAttributes) {
		this.numberOfAttributes = numberOfAttributes;
	}
}
