package de.thm.todoist.Controller;

import android.content.Context;
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

/**
 * Created by Benedikt on 21.01.14.
 */
public class TaskListAdapter extends ArrayAdapter<Task> {

    private ArrayList<Task> taskList;
    private Context context;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;


    public TaskListAdapter(ArrayList<Task> taskList, Context ctx, CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        super(ctx, R.layout.row_task, taskList);
        this.taskList = taskList;
        this.context = ctx;
        this.checkedChangeListener = checkedChangeListener;
    }

    public ArrayList<Task> getData() {
        return taskList;
    }

    public void setData(ArrayList<Task> newTaskList) {
        taskList = newTaskList;
    }

    public int getCount() {
        return taskList.size();
    }

    public Task getItem(int position) {
        return taskList.get(position);
    }

    public long getItemId(int position) {
        return 0;
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

        Task t = taskList.get(position);
        holder.doneView.setTag(t);
        holder.taskNameView.setText(t.getTitle());
        if (t.hasEndDate()) {
            holder.enddateView.setVisibility(View.VISIBLE);
            holder.enddateView.setText("Enddatum: " + t.getViewDateString());
        } else {
            holder.enddateView.setVisibility(View.INVISIBLE);
        }
        holder.doneView.setOnCheckedChangeListener(null);
        holder.doneView.setChecked(t.isDone());
        holder.doneView.setOnCheckedChangeListener(checkedChangeListener);
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
