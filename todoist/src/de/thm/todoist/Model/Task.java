package de.thm.todoist.Model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Task implements Serializable {

    private String title, description;
    private GregorianCalendar enddate;
    private boolean done;
    private boolean hasEndDate;
    private int priority;
    private String id;

    public Task(String id, String title, String description, GregorianCalendar enddate, boolean done, int priority, boolean hasEndDate) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.enddate = enddate;
        this.hasEndDate = hasEndDate;
        this.done = done;
        this.priority = priority;
    }

    public Task(String id, String title, String description, GregorianCalendar enddate, boolean done, int priority) {
        this(id, title, description, enddate, done, priority, true);
    }

    private TimeZone mTZ = TimeZone.getTimeZone("Europe/Berlin");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSS'Z'");

    public String getDateString() {
        df.setTimeZone(mTZ);
        return df.format(enddate.getTime());
    }

    private DateFormat viewDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public String getViewDateString() {
        viewDF.setTimeZone(mTZ);
        return viewDF.format(enddate.getTime());
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


    public GregorianCalendar getEnddate() {
        return enddate;
    }


    public void setEnddate(GregorianCalendar enddate) {
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


    public boolean hasEndDate() {
        return hasEndDate;
    }

    public void setHasEndDate(boolean hasEndDate) {
        this.hasEndDate = hasEndDate;
    }
}
