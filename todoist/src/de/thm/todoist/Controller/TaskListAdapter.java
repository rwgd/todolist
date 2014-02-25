package de.thm.todoist.Controller;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Benedikt on 21.01.14.
 */
public class TaskListAdapter extends ArrayAdapter<Task> {

    private ArrayList<Task> taskList;
    private Context context;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;
    private ArrayList<Task> filteredtaskList;


    public TaskListAdapter(ArrayList<Task> taskList, Context ctx, CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        super(ctx, R.layout.row_task, taskList);
        this.taskList = taskList;
        refreshArrayLists();
        this.context = ctx;
        this.checkedChangeListener = checkedChangeListener;
    }

    public void refreshArrayLists() {
        filteredtaskList = new ArrayList<Task>();
        ArrayList<Task> tempTaskList = new ArrayList<Task>();
        for (Task task : taskList) {
            if (!(task.isDeleted() && task.isSynced())) tempTaskList.add(task);
            if (!task.isDeleted()) filteredtaskList.add(task);
        }
        taskList = tempTaskList;
    }

    public ArrayList<Task> getData() {
        return taskList;
    }

    public void setData(ArrayList<Task> newTaskList) {
        taskList = newTaskList;
        refreshArrayLists();
    }

    public int getCount() {
        return filteredtaskList.size();
    }

    public Task getItem(int position) {
        return filteredtaskList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void add(Task t) {
        taskList.add(t);
        refreshArrayLists();
    }

    @Override
    public void remove(Task t) {
        taskList.remove(t);
        refreshArrayLists();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        TaskViewHolder holder = new TaskViewHolder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.row_task, null);

            // Now we can fill the layout with the right values
            TextView taskNameView = (TextView) v.findViewById(R.id.tvtaskName);
            TextView endDateView = (TextView) v.findViewById(R.id.tvEnddate);
            CheckBox doneView = (CheckBox) v.findViewById(R.id.doneBox);


            holder.taskNameView = taskNameView;
            holder.enddateView = endDateView;
            holder.doneView = doneView;


            v.setTag(holder);
        } else holder = (TaskViewHolder) v.getTag();

        Task t = filteredtaskList.get(position);
        holder.doneView.setTag(t);
        holder.taskNameView.setText(t.getTitle());
        if (t.hasEndDate()) {
            holder.enddateView.setVisibility(View.VISIBLE);
            GregorianCalendar tommorow = new GregorianCalendar();
            GregorianCalendar today = tommorow;
            tommorow.add(GregorianCalendar.DAY_OF_MONTH, 1);
            if (t.getEnddate().before(today)) {
                holder.enddateView.setTextColor(Color.RED);
                holder.taskNameView.setTextColor(Color.RED);
            } else if (t.getEnddate().before(tommorow)) {
                holder.enddateView.setTextColor(Color.RED);
                holder.taskNameView.setTextColor(Color.BLACK);
            }
            holder.enddateView.setText("Enddatum: " + t.getViewDateString());
        } else {
            holder.enddateView.setTextColor(Color.BLACK);
            holder.taskNameView.setTextColor(Color.BLACK);
            holder.enddateView.setVisibility(View.INVISIBLE);
        }
        holder.doneView.setOnCheckedChangeListener(null);
        holder.doneView.setChecked(t.isDone());
        holder.doneView.setOnCheckedChangeListener(checkedChangeListener);
        if (t.isDone()) {
            holder.taskNameView.setTextColor(Color.GRAY);
            holder.enddateView.setTextColor(Color.GRAY);
        }
        return v;
    }

        /* *********************************
         * We use the holder pattern
         * It makes the view faster and avoid finding the component
         * **********************************/

    private class TaskViewHolder {
        public TextView taskNameView;
        public TextView enddateView;
        public CheckBox doneView;
    }

}
