package de.thm.todoist.Activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.thm.todoist.Controller.TaskListAdapter;
import de.thm.todoist.Dialoge.TaskDialog;
import de.thm.todoist.Helper.Constants;
import de.thm.todoist.Helper.FktLib;
import de.thm.todoist.Helper.ServerLib;
import de.thm.todoist.Helper.XMLBuilder;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
            ab.setTitle(":todoist");
            ab.setSubtitle("tasks");
        }

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);

        lvTasks = (ListView) findViewById(R.id.listView1);

        tasksAdapter = new TaskListAdapter(new ArrayList<Task>(), ctxt, this);
        lvTasks.setAdapter(tasksAdapter);
        lvTasks.setOnItemClickListener(this);
        PingCheck pc = new PingCheck();
        pc.execute();

    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        if (tasksAdapter.getCount() > 0) {
            try {
                FktLib.saveTasks(tasksAdapter.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    private void refreshList() {
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
                ServerLib.sendTask(t, mPreferences, queue);
            }
        }
    }

    private LayoutInflater mInflater;

    public void getTasks() {
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String URL = TASKS_URL + "/?user_token=" + mPreferences.getString("AuthToken", "");
        ImageView iv = (ImageView) mInflater.inflate(R.layout.refresh, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        if (rotation != null) {
            rotation.setRepeatCount(Animation.INFINITE);
            iv.startAnimation(rotation);
        }

        mRefresh.setActionView(iv);
        StringRequest postRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("Response", response);
                JsonArray jArray = new JsonParser().parse(response).getAsJsonArray();
                for (int i = 0; i < jArray.size(); i++) {
                    JsonObject jsonObject = jArray.get(i).getAsJsonObject();
                    String id = "";
                    String title = "";
                    String description = "";
                    GregorianCalendar duedate = null;
                    Boolean done = false;
                    Boolean hasDueDate = true;
                    int priority = 0;
                    if (!jsonObject.get("id").isJsonNull()) {
                        id = jsonObject.get("id").toString().replace("\"", "");
                    }
                    if (!jsonObject.get("title").isJsonNull()) {
                        title = jsonObject.get("title").toString().replace("\"", "");
                    }
                    if (!jsonObject.get("description").isJsonNull()) {
                        description = jsonObject.get("description").toString().replace("\"", "");
                    }
                    if (!jsonObject.get("duedate").isJsonNull()) {
                        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        df.setTimeZone(tz);
                        GregorianCalendar cal = new GregorianCalendar();
                        try {
                            cal.setTime(df.parse(jsonObject.get("duedate").toString().replace("\"", "").substring(0, 19)));
                            duedate = cal;

                        } catch (ParseException e) {
                            Log.e("Parsing error", "fuck no");
                        }

                    }
                    if (!jsonObject.get("enabledDueDate").isJsonNull()) {
                        String enabledDueDateStr = jsonObject.get("enabledDueDate").toString().replace("\"", "");
                        if (enabledDueDateStr.equals("0") || enabledDueDateStr.equals("false")) {
                            hasDueDate = false;
                        }
                    }
                    if (!jsonObject.get("done").isJsonNull()) {
                        String doneStr = jsonObject.get("done").toString().replace("\"", "");
                        if (doneStr.equals("1") || doneStr.equals("true")) {
                            done = true;
                        }
                    }
                    if (jsonObject.get("priority").isJsonNull()) {
                        try {
                            priority = jsonObject.get("priority").getAsInt();
                        } catch (NumberFormatException e) {
                            Log.e("nfe", "false number");
                            priority = 0;
                        }

                    }

                    addTaskToTasksArray(new Task(id, title, description, duedate, done, priority, hasDueDate), false);
                    //actualTasks.add(new Task(id, title,description, duedate, done, priority));
                }
                mRefresh.getActionView().clearAnimation();
                mRefresh.setActionView(null);
                refreshList();

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mRefresh.getActionView() != null) mRefresh.getActionView().clearAnimation();
                        mRefresh.setActionView(null);
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");

                return params;
            }
        };

        queue.add(postRequest);

    }

    public void logout() {

        JSONObject authObj = new JSONObject();
        try {
            authObj.put("user_token", mPreferences.getString("AuthToken", ""));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Method.DELETE, LOGOUT_URL, authObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            if (response.getBoolean("success")) {
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.remove("AuthToken");
                                editor.remove("UserMail");
                                editor.commit();

                                Intent intent = new Intent(TaskActivity.this,
                                        LoginActivity.class);
                                startActivityForResult(intent, 0);
                            }
                            Toast.makeText(ctxt, response.getString("info"),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(ctxt, e.getMessage(), Toast.LENGTH_LONG)
                                    .show();


                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("X-AUTH-TOKEN", mPreferences.getString("AuthToken", ""));

                return params;
            }
        };

        queue.add(req);

    }


    @Override
    public void onDialogPositiveClick(String id, String name, Boolean dateEnabled, GregorianCalendar enddate, String description) {
        if (!id.equals("")) {
            for (Task actualTask : tasksAdapter.getData()) {
                if (actualTask.getId() == id) {
                    actualTask.setDescription(description);
                    actualTask.setTitle(name);
                    actualTask.setHasEndDate(dateEnabled);
                    actualTask.setEnddate(enddate);
                    ServerLib.editTask(actualTask, mPreferences, queue);
                    refreshList();
                    break;
                }
            }
        } else {
            Task newTask = new Task("", name, description, enddate, false, 0, dateEnabled);
            ServerLib.sendTask(newTask, mPreferences, queue);
            tasksAdapter.add(newTask);
        }
    }

    @Override
    public void onDialogNeutralClick(String id) {
        ServerLib.deleteTask(id, mPreferences, queue);
        for (Task actualTask : tasksAdapter.getData()) {
            if (actualTask.getId() == id) {
                tasksAdapter.remove(actualTask);
                tasksAdapter.notifyDataSetChanged();
                break;
            }
        }
        refreshList();
        lvTasks.invalidateViews();
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
        ServerLib.editTask(task, mPreferences, queue);
        refreshList();
    }

    private class PingCheck extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean check = false;
            check = FktLib.ping(CHECK_URL);
            return check;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                //Verbindung besteht
                getTasks();
            } else {
                Toast.makeText(ctxt, "Es besteht keine Verbindung zum Server!", Toast.LENGTH_LONG).show();
                try {
                    tasksAdapter.setData(FktLib.readTasks());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    tasksAdapter.setData(new ArrayList<Task>());
                }
                if (tasksAdapter.getCount() > 0) {
                    refreshList();
                }
            }

        }
    }
}
