package logic.model.entity;

import java.util.List;

public class Ticket {
	private String key;
	private String creationDate;
	private String resolutionDate;
	private Release fixVersion;
	private Release openingVersion;
	private Release injectedVersion;
	private List<Release> affversions;
	private List<Commit> commitsForTicket;
	
	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(String resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public Release getFixVersion() {
		return fixVersion;
	}

	public void setFixVersion(Release fixVersion) {
		this.fixVersion = fixVersion;
	}

	public List<Release> getAffversions() {
		return affversions;
	}

	public void setAffversions(List<Release> affversions) {
		this.affversions = affversions;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Commit> getCommitsForTicket() {
		return commitsForTicket;
	}

	public void setCommitsForTicket(List<Commit> commitsForTicket) {
		this.commitsForTicket = commitsForTicket;
	}

	public Release getOpeningVersion() {
		return openingVersion;
	}

	public void setOpeningVersion(Release openingVersion) {
		this.openingVersion = openingVersion;
	}

	public Release getInjectedVersion() {
		return injectedVersion;
	}

	public void setInjectedVersion(Release injectedVersion) {
		this.injectedVersion = injectedVersion;
	}

		

}
