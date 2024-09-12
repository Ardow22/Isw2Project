package logic.model.bean;

import java.util.List;

import logic.model.entity.Commit;

public class ReleaseBean {
	private List<Commit> commits;
	
	public List<Commit> getCommits() {
		return commits;
	}
	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}
	
}
