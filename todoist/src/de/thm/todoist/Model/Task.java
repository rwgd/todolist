package de.thm.todoist.Model;

import java.io.Serializable;

public class Task implements Serializable {

    private String title, description, enddate;
    private boolean done;
    private int priority;
    private String id;

    public Task(String id, String title, String description, String enddate, boolean done, int priority) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.enddate = enddate;
        this.done = done;
        this.priority = priority;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getEnddate() {
        return enddate;
    }


    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }


    public boolean isDone() {
        return done;
    }


    public void setDone(boolean done) {
        this.done = done;
    }


    public int getPriority() {
        return priority;
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


}