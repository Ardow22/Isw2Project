package logic.model.entity;

import java.util.ArrayList;

import logic.model.entity.JavaClass;
import logic.model.entity.Ticket;

public class Commit {
	private Ticket ticket; //sarebbe il ticket preso da JIRA
	private String id; //sarebbe lo SHA
	private String author;
	private String date;
	private String message;
	private ArrayList<JavaClass> classesTouched;
	
	
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

	public ArrayList<JavaClass> getClassesTouched() {
		return classesTouched;
	}

	public void setClassesTouched(ArrayList<JavaClass> classesTouched) {
		this.classesTouched = classesTouched;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}


	
}
