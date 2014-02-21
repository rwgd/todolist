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
import de.thm.todoist.Helper.Constants;
import de.thm.todoist.Helper.FktLib;
import de.thm.todoist.Helper.ServerLib;
import de.thm.todoist.Helper.XMLBuilder;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class TaskActivity extends FragmentActivity
        implements Constants, TaskDialog.NoticeDialogListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private ListView lvTasks;
    private TaskListAdapter tasksAdapter;
    private Context ctxt;
    private SharedPreferences mPreferences;
    private static final int REQUEST_PICK_FILE = 1;
    private RequestQueue queue;

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

        LoadTasksAsync pc = new LoadTasksAsync();
        pc.execute();
    }

    public void onPause() {
        if (tasksAdapter.getCount() > 0) {
            FktLib.saveTasks(tasksAdapter.getData());
        }
        super.onPause();
    }

    public void onDestroy() {
        if (tasksAdapter.getCount() > 0) {
            FktLib.saveTasks(tasksAdapter.getData());
        }
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
                logout();
                return true;
            case R.id.action_import_export:
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);
                // Only make .json files visible
                ArrayList<String> extensions = new ArrayList<String>();
                extensions.add(".json");
                intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS,
                        extensions);

                startActivityForResult(intent, REQUEST_PICK_FILE);
                return true;
            case R.id.action_refresh:
                sendUpdates();
                getTasks();
//                sendTask();
                return true;
            case R.id.action_new:
                TaskDialog dialog = new TaskDialog();
                dialog.show(getFragmentManager(), "test");
                return true;
            case R.id.action_exportxml:
                XMLBuilder xmlb = new XMLBuilder(tasksAdapter.getData(), this);
                try {
                    xmlb.generateXML();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                        File f = new File(
                                data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
                        if (f.getPath().contains(".json")) {
                            try {
                                String jsonString = FktLib.readFile(f);
                                JSONObject jsnobject = new JSONObject(jsonString);
                                JSONArray jsonArray = jsnobject.getJSONArray("tasks");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject explrObject = jsonArray.getJSONObject(i);

                                    String date = explrObject.getString("created_at");
                                    String title = explrObject.getString("title");

                                    String id = explrObject.getString("id");
                                    Boolean done = !explrObject
                                            .isNull("completed_at");
                                    //TODO: String to GregorianCalender
                                    if (!date.equals("")) {
//                                        addTaskToTasksArray(new Task(id, title, "", new GregorianCalendar(date), done, 0, false), true);
                                    } else {
//                                         addTaskToTasksArray(new Task(id, title, "", null, done, 0, false), true);
                                    }
                                }
                                refreshList();
                            } catch (Exception e) {
                                // Fehler beim Auslesen der JSON-Datei
                            }
                        }
                    }
            }
        }
    }


    public void refreshList() {
        tasksAdapter.notifyDataSetChanged();
    }


    public void addTaskToTasksArray(Task t, boolean sync) {

        boolean isTaskInArray = false;
        for (Task actualTask : tasksAdapter.getData()) {
            if (actualTask.getId().equals(t.getId())) {
                isTaskInArray = true;
            }
        }
        if (!isTaskInArray) {
            tasksAdapter.add(t);
            tasksAdapter.notifyDataSetChanged();
            if (sync) {
                ServerLib.sendTask(t, mPreferences, queue, this);
            }
        }
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
                    ServerLib.editTask(actualTask, mPreferences, queue, this);
                    refreshList();
                    break;
                }
            }
        } else {
            Task newTask = new Task("", name, description, enddate, false, 0, dateEnabled);
            ServerLib.sendTask(newTask, mPreferences, queue, this);
            tasksAdapter.add(newTask);
            refreshList();
        }
    }

    @Override
    public void onDialogNeutralClick(String id) {
        for (Task actualTask : tasksAdapter.getData()) {
            if (actualTask.getId().equals(id)) {
                ServerLib.deleteTask(actualTask, mPreferences, queue, this);
                actualTask.delete();
                tasksAdapter.refreshArrayLists();
                tasksAdapter.notifyDataSetChanged();
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
                tasksAdapter.notifyDataSetChanged();
                break;
            }
        }
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
        ServerLib.editTask(task, mPreferences, queue, this);
        refreshList();
    }

    private class LoadTasksAsync extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<Task> tasks = FktLib.readTasks();
            tasksAdapter.setData(tasks);
            sendUpdates();
            boolean check;
            check = FktLib.ping(CHECK_URL);
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
                FktLib.saveTasks(tasksAdapter.getData());
            }

        }
    }
}
