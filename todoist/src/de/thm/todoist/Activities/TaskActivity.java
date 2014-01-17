package de.thm.todoist.Activities;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.thm.todoist.Dialoge.TaskDialog;
import de.thm.todoist.Helper.*;
import de.thm.todoist.Model.Task;
import de.thm.todoist.Model.TaskListModel;
import de.thm.todoist.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskActivity extends FragmentActivity
        implements Constants, TaskDialog.NoticeDialogListener {

    private ListView lvTasks;
    private ArrayList<Task> actualTasks;
    private ArrayList<HashMap<String, Object>> myList;
    private Context ctxt;
    private SharedPreferences mPreferences;
    private static final int REQUEST_PICK_FILE = 1;
    private RequestQueue queue;
    private TaskListModel model;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        ctxt = this;
        model = TaskListModel.getInstance();

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(":todoist");
            ab.setSubtitle("tasks");
        }

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);

        lvTasks = (ListView) findViewById(R.id.listView1);

        myList = new ArrayList<HashMap<String, Object>>();
        actualTasks = new ArrayList<Task>();

        PingCheck pc = new PingCheck();
        pc.execute();

    }

    public void onResume() {
        super.onResume();
        if (model.getTaskList() != null) {
            actualTasks = model.getTaskList();
            if (actualTasks.size() > 0) {
                showList();
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (model.getTaskList() != null) {
            actualTasks = model.getTaskList();
            try {
                FktLib.saveTasks(actualTasks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_task_actions, menu);
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
                //sendTask();
                return true;
            case R.id.action_new:
                TaskDialog dialog = new TaskDialog(false);
                dialog.show(getFragmentManager(), "test");
                return true;
            case R.id.action_exportxml:
                XMLBuilder xmlb = new XMLBuilder(actualTasks, this);
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
                case REQUEST_PICK_FILE:
                    if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
                        // Get the file path
                        File f = new File(
                                data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
                        if (f.getPath().contains(".json")) {
                            try {
                                String jsonString = FktLib.readFile(f);
                                JSONObject jsnobject = new JSONObject(jsonString);
                                JSONArray jsonArray = jsnobject
                                        .getJSONArray("tasks");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject explrObject = jsonArray
                                            .getJSONObject(i);

                                    String date = explrObject
                                            .getString("created_at");
                                    String title = explrObject.getString("title");
                                    String id = explrObject.getString("id");
                                    Boolean done = !explrObject
                                            .isNull("completed_at");

                                    addTaskToTasksArray(new Task(id, title,
                                            "Beschreibung 1", date, done, 0), true);


                                }
                                showList();

                            } catch (Exception e) {
                                // Fehler beim Auslesen der JSON-Datei
                            }
                        }
                    }
            }
        }
    }


    private void showList() {
        myList.clear();
        if (actualTasks != null) {
            if (actualTasks.size() > 0) {
                for (Task actualTask : actualTasks) {
                    HashMap<String, Object> map = new HashMap<String, Object>();

                    map.put("task_title", actualTask.getTitle());
                    map.put("task_enddate", actualTask.getEnddate());

                    if (actualTask.isDone()) {
                        map.put("task_done", true);
                    } else {
                        map.put("task_done", false);
                    }

                    myList.add(map);
                }

                ExtendedSimpleAdapter aa = new ExtendedSimpleAdapter(this,
                        myList, R.layout.row_task, new String[]{"task_title",
                        "task_enddate", "task_done"}, new int[]{
                        R.id.tvtaskName,
                        R.id.tvEnddate,
                        R.id.imageViewTaskRowDone});
                lvTasks.setAdapter(aa);
                lvTasks.setItemsCanFocus(true);
                lvTasks.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        TaskDialog dialog = new TaskDialog(true);
                        dialog.show(getFragmentManager(), "test");
                    }


                });

                aa.notifyDataSetChanged();


            }
        }

    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Log.v("delete: ", "yes");
                    ServerLib.deleteTask(actualTasks.get(position).getId(), mPreferences, queue);
                    actualTasks.remove(position);
                    model.setTaskList(actualTasks);
                    showList();
                    lvTasks.invalidateViews();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


    public void addTaskToTasksArray(Task t, boolean sync) {

        boolean isTaskInArray = false;
        for (Task actualTask : actualTasks) {
            if (actualTask.getId().equals(t.getId())) {
                isTaskInArray = true;
            }
        }
        if (!isTaskInArray) {
            actualTasks.add(t);
            if (sync) {
                //direkt synchronisieren
                ServerLib.sendTask(t, mPreferences, queue);
            }
        }
    }


    public void getTasks() {

        String URL = TASKS_URL + "/?user_token=" + mPreferences.getString("AuthToken", "");

        StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

                        JsonArray jArray = new JsonParser().parse(response).getAsJsonArray();
                        for (int i = 0; i < jArray.size(); i++) {
                            JsonObject jsonObject = jArray.get(i).getAsJsonObject();
                            Log.d("title", jsonObject.get("title").toString());
                            Log.d("description", jsonObject.get("description").toString());
                            Log.d("duedate", jsonObject.get("duedate").toString());
                            Log.d("done", jsonObject.get("done").toString());
                            Log.d("priority", jsonObject.get("priority").toString());
                            Log.d("id", jsonObject.get("id").toString());

                            String id = "000";
                            String title = "";
                            String description = "";
                            String duedate = "";
                            Boolean done = false;
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
                                duedate = jsonObject.get("duedate").toString().replace("\"", "");
                            }
                            if (!jsonObject.get("done").isJsonNull()) {
                                String doneStr = jsonObject.get("done").toString().replace("\"", "");
                                if (doneStr.equals("1")) {
                                    done = true;
                                }
                            }
                            if (jsonObject.get("priority").isJsonNull()) {
                                try {
                                    priority = jsonObject.get("priority").getAsInt();
                                } catch (NumberFormatException e) {
                                    //Error beim parsen
                                    priority = 0;
                                }

                            }

                            addTaskToTasksArray(new Task(id, title, description, duedate, done, priority), false);
                            //actualTasks.add(new Task(id, title,description, duedate, done, priority));
                        }
                        model.setTaskList(actualTasks);
                        showList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
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
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

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
                    actualTasks = FktLib.readTasks();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    actualTasks = null;
                }
                if (actualTasks != null) {
                    model.setTaskList(actualTasks);
                    showList();
                }
            }

        }
    }
}
