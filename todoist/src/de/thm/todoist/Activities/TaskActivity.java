package de.thm.todoist.Activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.devspark.appmsg.AppMsg;
import de.thm.todoist.Controller.TaskListAdapter;
import de.thm.todoist.Dialoge.TaskDialog;
import de.thm.todoist.Helper.*;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

public class TaskActivity extends FragmentActivity
        implements Constants, TaskDialog.NoticeDialogListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private ListView lvTasks;
    private TaskListAdapter tasksAdapter;
    private Context ctxt;
    private SharedPreferences mPreferences;
    private static final int REQUEST_PICK_FILE = 1;
    private RequestQueue queue;
    private XMLBuilder xmlb;
    private XMLReader xmlR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        ctxt = this;

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(this.getString(R.string.app_name));
            ab.setSubtitle("tasks");
        }

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);

        lvTasks = (ListView) findViewById(R.id.listView1);
        tasksAdapter = new TaskListAdapter(new ArrayList<Task>(), ctxt, this);
        lvTasks.setAdapter(tasksAdapter);
        lvTasks.setOnItemClickListener(this);

    }

    public void onResume() {
        super.onResume();
        xmlb = new XMLBuilder(this);
        xmlR = new XMLReader(this);
        ArrayList<Task> tasks = xmlR.loadXML(true, Constants.SAVE_DIR);
        tasksAdapter.setData(tasks);
        LoadTasksAsync pc = new LoadTasksAsync();
        pc.execute();
    }

    public void onPause() {
        xmlb.generateXML(tasksAdapter.getData(), true, Constants.SAVE_DIR);
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private MenuItem mRefresh = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_task_actions, menu);
        mRefresh = menu.findItem(R.id.action_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_backspace:
                xmlb.generateXML(tasksAdapter.getData(), true, Constants.SAVE_DIR);
                logout();
                return true;
            case R.id.action_import_export:
                xmlb.generateXML(tasksAdapter.getData(), true, Constants.SAVE_DIR);
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);
                // Only make .json files visible
                ArrayList<String> extensions = new ArrayList<String>();
                extensions.add(".json");
                extensions.add(".abk");
                intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS,
                        extensions);

                startActivityForResult(intent, REQUEST_PICK_FILE);
                return true;
            case R.id.action_refresh:
                xmlb.generateXML(tasksAdapter.getData(), true, Constants.SAVE_DIR);
                sendUpdates();
                getTasks();
                return true;
            case R.id.action_new:
                TaskDialog dialog = new TaskDialog();
                dialog.show(getFragmentManager(), "test");
                return true;
            case R.id.action_exportxml:
                xmlb.generateXML(tasksAdapter.getData(), false, Constants.SAVE_DIR_XML);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_FILE: //Wunderlist Import
                    if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
                        // Get the file path
                        File f = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
                        ArrayList<Task> importedTasks = JSONImporter.parseFile(f, this);
                        for (Task task : importedTasks) {
                            addTaskToTasksArray(task);
                        }
//                        sendUpdates();
                    }
            }
        }
    }


    public void refreshList() {
        tasksAdapter.refreshArrayLists();
        xmlb.generateXML(tasksAdapter.getData(), true, Constants.SAVE_DIR);
        tasksAdapter.notifyDataSetChanged();
    }


    public void deleteAllTasks() {
        tasksAdapter.setData(new ArrayList<Task>());
    }

    public void addTaskToTasksArray(Task t) {
        ArrayList<Task> tempTaskList = tasksAdapter.getData();
        boolean inArray = false;
        for (int i = 0; i < tempTaskList.size(); i++) {
            Task savedTask = tempTaskList.get(i);
            if (savedTask.getId().equals(t.getId())) {
                if (savedTask.getMode() != 0 && savedTask.getLastUpdated().after(t.getId())) {
                    ServerLib.sendTask(savedTask, mPreferences, queue, this);
                } else {
                    tempTaskList.remove(i);
                    tempTaskList.add(i, t);
                }
                inArray = true;
                break;
            }
        }
        if (!inArray) tempTaskList.add(t);
        tasksAdapter.setData(tempTaskList);
        refreshList();
    }

    private LayoutInflater mInflater;

    public void sendUpdates() {
        for (Task task : tasksAdapter.getData()) {
            task.sync(mPreferences, queue, this);
        }
    }

    public void getTasks() {
        if (mInflater == null) mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) mInflater.inflate(R.layout.refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        if (rotation != null && iv != null && mRefresh != null) {
            rotation.setRepeatCount(Animation.INFINITE);
            iv.startAnimation(rotation);
            mRefresh.setActionView(iv);
        }
        ServerLib.getAllTasks(mPreferences, queue, this);
    }

    public void logout() {
        ServerLib.logout(mPreferences, queue, this);
    }

    public void stopRefreshView() {
        if (mRefresh.getActionView() != null) {
            mRefresh.getActionView().clearAnimation();
            mRefresh.setActionView(null);
        }

    }

    @Override
    public void onDialogPositiveClick(String id, String name, Boolean dateEnabled, GregorianCalendar enddate, String description) {
        if (!id.equals("")) {
            for (Task actualTask : tasksAdapter.getData()) {
                if (actualTask.getId().equals(id)) {
                    actualTask.setDescription(description);
                    actualTask.setTitle(name);
                    actualTask.setHasEndDate(dateEnabled);
                    actualTask.setEnddate(enddate);
                    actualTask.sync(mPreferences, queue, this);
                    break;
                }
            }
        } else {
            Task newTask = new Task(UUID.randomUUID().toString(), name, description, enddate, false, 0, dateEnabled);
            ServerLib.sendTask(newTask, mPreferences, queue, this);
            addTaskToTasksArray(newTask);
        }
        tasksAdapter.refreshArrayLists();
        refreshList();
    }

    @Override
    public void onDialogNeutralClick(String id) {
        for (Task actualTask : tasksAdapter.getData()) {
            if (actualTask.getId().equals(id)) {
                actualTask.delete();
                actualTask.sync(mPreferences, queue, this);
                tasksAdapter.refreshArrayLists();
                break;
            }
        }
        refreshList();
        lvTasks.invalidateViews();
    }

    public void deleteTask(Task t) {
        for (Task actualTask : tasksAdapter.getData()) {
            if (actualTask.getId().equals(t.getId())) {
                tasksAdapter.remove(actualTask);
                break;
            }
        }
        tasksAdapter.refreshArrayLists();
        refreshList();
    }


    @Override
    public void onDialogNegativeClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Task choosenTask = tasksAdapter.getItem(position);
        TaskDialog dialog = new TaskDialog(choosenTask);
        dialog.show(getFragmentManager(), "test");

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        CheckBox cb = (CheckBox) buttonView;
        Task task = (Task) cb.getTag();
        task.setDone(isChecked);
        task.sync(mPreferences, queue, this);
        refreshList();
    }

    private class LoadTasksAsync extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            boolean check;
            check = FktLib.ping(CHECK_URL);
            if (check) sendUpdates();
            return check;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                getTasks();
            } else {
                AppMsg.makeText(TaskActivity.this, TaskActivity.this.getText(R.string.no_server_connection), AppMsg.STYLE_ALERT).show();
            }
            if (tasksAdapter.getCount() > 0) {
                refreshList();
            }

        }
    }
}
