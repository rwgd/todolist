package de.thm.todoist.Helper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.devspark.appmsg.AppMsg;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.thm.todoist.Activities.LoginActivity;
import de.thm.todoist.Activities.TaskActivity;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ServerLib implements Constants {

    public static void editTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct) {
        editTask(task, mPreferences, queue, callingAct, false);
    }

    public static void editTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct, final boolean silently) {
        JSONObject taskObj = new JSONObject();
        JSONObject holder = new JSONObject();
        String URL = TASKS_URL + "/" + task.getId() + "/edit";
        try {
            holder.put("editDate", task.getLastUpdatedString());
            taskObj.put("title", task.getTitle());
            taskObj.put("description", task.getDescription());
            taskObj.put("duedate", task.getDateString());
            taskObj.put("enabledDueDate", task.hasEndDate());
            taskObj.put("done", task.isDone());
            taskObj.put("priority", task.getPriority());
            taskObj.put("id", task.getId());
            holder.put("user_token", mPreferences.getString("AuthToken", ""));
            holder.put("task", taskObj);
        } catch (JSONException e) {
            Log.e("JSOn exc", "test");
            e.printStackTrace();
        }
        Log.d("holder", holder.toString());
        JsonObjectRequest req = new JsonObjectRequest(Method.POST, URL, holder,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("edittask", response.toString());
                        if (!silently)
                            AppMsg.makeText(callingAct, callingAct.getString(R.string.editTask_success), AppMsg.STYLE_INFO).show();
                        task.setSynced();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
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

    public static void sendTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct) {
        sendTask(task, mPreferences, queue, callingAct, false);
    }

    public static Task sendTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct, final boolean silently) {
        JSONObject taskObj = new JSONObject();
        JSONObject holder = new JSONObject();
        try {
            holder.put("editDate", task.getLastUpdatedString());
            taskObj.put("title", task.getTitle());
            taskObj.put("description", task.getDescription());
            taskObj.put("duedate", task.getDateString());
            taskObj.put("enabledDueDate", task.hasEndDate());
            taskObj.put("done", task.isDone());
            taskObj.put("priority", task.getPriority());
            holder.put("task", taskObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("holder", holder.toString());

        JsonObjectRequest req = new JsonObjectRequest(Method.POST, NEW_TASK_URL, holder,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("newtask", response.toString());
                        try {
                            String generatedId = response.getJSONObject("data").getJSONObject("task").getString("id");
                            Log.d("id", response.getJSONObject("data").getJSONObject("task").getString("id"));
                            task.setId(generatedId);
                            task.setSynced();
                            if (!silently)
                                AppMsg.makeText(callingAct, callingAct.getString(R.string.createTask_success), AppMsg.STYLE_INFO).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
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
        return task;

    }

    public static void getAllTasks(final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct) {
        getAllTasks(mPreferences, queue, callingAct, false);
    }

    public static void getAllTasks(final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct, final boolean silently) {
        String URL = TASKS_URL + "/?user_token=" + mPreferences.getString("AuthToken", "");
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

                    Task newTask = new Task(id, title, description, duedate, done, priority, hasDueDate);
                    newTask.setSynced();
                    callingAct.addTaskToTasksArray(newTask, false);
                }
                callingAct.stopRefreshView();
                callingAct.refreshList();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!silently)
                            AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
                        callingAct.stopRefreshView();
                        callingAct.refreshList();
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

    public static void deleteTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct) {
        deleteTask(task, mPreferences, queue, callingAct, false);
    }

    public static void deleteTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue, final TaskActivity callingAct, final boolean silently) {
        String URL = TASKS_URL + "/" + task.getId() + "/delete";
        StringRequest postRequest = new StringRequest(Request.Method.DELETE, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!silently)
                            AppMsg.makeText(callingAct, callingAct.getString(R.string.deleteTask_success), AppMsg.STYLE_INFO).show();
                        Log.d("Response", response);
                        task.setSynced();
                        callingAct.deleteTask(task);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
                        Log.d("ERROR", "error => " + error.toString());
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
        queue.add(postRequest);
    }

    public static void login(final String username, final String password, RequestQueue queue, final SharedPreferences mPreferences, final Activity callingAct) {
        JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        try {
            userObj.put("email", username);
            userObj.put("password", password);
            holder.put("user", userObj);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(LOGIN_API_ENDPOINT_URL, holder,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if (response.getBoolean("success")) {
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString("AuthToken", response.getJSONObject("data").getString("auth_token"));
                                editor.putString("UserMail", username);
                                editor.commit();

                                Intent intent = new Intent(callingAct.getApplicationContext(), TaskActivity.class);
                                callingAct.startActivity(intent);
                                callingAct.finish();
                            } else {
                                AppMsg.makeText(callingAct, callingAct.getText(R.string.login_fail), AppMsg.STYLE_ALERT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
            }
        }
        );

        queue.add(req);
    }

    public static void logout(final SharedPreferences mPreferences, RequestQueue queue, final Activity callingAct) {
        JSONObject authObj = new JSONObject();
        try {
            authObj.put("user_token", mPreferences.getString("AuthToken", ""));
        } catch (JSONException e) {
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
                                Intent intent = new Intent(callingAct,
                                        LoginActivity.class);
                                callingAct.startActivityForResult(intent, 0);
                            }
                            AppMsg.makeText(callingAct, response.getString("info"), AppMsg.STYLE_INFO);
                        } catch (Exception e) {
                            AppMsg.makeText(callingAct, e.getMessage(), AppMsg.STYLE_ALERT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                AppMsg.makeText(callingAct, VolleyErrorHelper.getMessage(error, callingAct.getBaseContext()), AppMsg.STYLE_ALERT).show();
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


}
