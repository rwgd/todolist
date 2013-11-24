package de.thm.todoist;

import java.util.ArrayList;

public class User {
	
	public String name, email, password;
	public ArrayList<Task> tasks;
	
	public User(String name, String email, String password,
			ArrayList<Task> tasks) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
		this.tasks = tasks;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}
	
	

}
