package de.thm.todoist.Model;

import java.util.ArrayList;

public class TaskListModel {
    private static TaskListModel mySingelton = null;
    private ArrayList<Task> taskList;

    private TaskListModel() {
    }

    public static TaskListModel getInstance() {
        if (mySingelton == null)
            mySingelton = new TaskListModel();
        return mySingelton;
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }
}
