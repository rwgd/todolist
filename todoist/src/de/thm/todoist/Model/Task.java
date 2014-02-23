package de.thm.todoist.Model;

import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.RequestQueue;
import de.thm.todoist.Activities.TaskActivity;
import de.thm.todoist.Helper.ServerLib;

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
    private SyncState syncState = new StateCreated();
    private boolean isDeleted = false;
    private GregorianCalendar last_updated = new GregorianCalendar();

    public boolean isDeleted() {
        return isDeleted;
    }

    abstract class SyncState implements Serializable {
        public void sync(SharedPreferences mPreferences, RequestQueue queue, TaskActivity callingAct) {
        }

        public boolean isSynced() {
            return false;
        }

        public void changeToState(SyncState newState) {
            if (newState instanceof StateCreated)
                Log.e("This should not happen", "You try to create a Task, which is allready created");
            syncState = newState;
            last_updated = new GregorianCalendar();
        }
    }

    class StateSynced extends SyncState {
        public boolean isSynced() {
            return true;
        }
    }

    class StateEdited extends SyncState {
        @Override
        public void sync(SharedPreferences mPreferences, RequestQueue queue, TaskActivity callingAct) {
            ServerLib.editTask(Task.this, mPreferences, queue, callingAct, true);
        }
    }

    class StateDeleted extends SyncState {
        @Override
        public void sync(SharedPreferences mPreferences, RequestQueue queue, TaskActivity callingAct) {
            ServerLib.deleteTask(Task.this, mPreferences, queue, callingAct, true);
        }
    }

    class StateCreated extends SyncState {
        @Override
        public void sync(SharedPreferences mPreferences, RequestQueue queue, TaskActivity callingAct) {
            ServerLib.sendTask(Task.this, mPreferences, queue, callingAct, true);
        }

        public void changeToState(SyncState newState) {
            if (newState instanceof StateSynced) syncState = newState;
            if (newState instanceof StateDeleted) syncState = new StateSynced();
            last_updated = new GregorianCalendar();
        }
    }

    public boolean isSynced() {
        return syncState.isSynced();
    }

    public void setSynced() {
        syncState.changeToState(new StateSynced());
    }

    public void delete() {
        syncState.changeToState(new StateDeleted());
        isDeleted = true;
    }


    public void sync(SharedPreferences mPreferences, RequestQueue queue, TaskActivity callingAct) {
        syncState.sync(mPreferences, queue, callingAct);
    }

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

    public String getLastUpdatedString() {
        df.setTimeZone(mTZ);
        return df.format(last_updated.getTime());
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
        syncState.changeToState(new StateEdited());
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        syncState.changeToState(new StateEdited());
    }


    public GregorianCalendar getEnddate() {
        return enddate;
    }


    public void setEnddate(GregorianCalendar enddate) {
        this.enddate = enddate;
        syncState.changeToState(new StateEdited());
    }


    public boolean isDone() {
        return done;
    }


    public void setDone(boolean done) {
        this.done = done;
        syncState.changeToState(new StateEdited());
    }


    public int getPriority() {
        return priority;
    }


    public void setPriority(int priority) {
        this.priority = priority;
        syncState.changeToState(new StateEdited());
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
        syncState.changeToState(new StateEdited());
    }


    public boolean hasEndDate() {
        return hasEndDate;
    }

    public void setHasEndDate(boolean hasEndDate) {
        this.hasEndDate = hasEndDate;
        syncState.changeToState(new StateEdited());
    }
}
