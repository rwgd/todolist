package de.thm.todoist.Helper;

import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import de.thm.todoist.Model.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerLib implements Constants {

    public static void editTask(Task task, final SharedPreferences mPreferences, RequestQueue queue) {
        JSONObject taskObj = new JSONObject();
        JSONObject holder = new JSONObject();
        String URL = TASKS_URL + "/" + task.getId() + "/edit";

        try {

            taskObj.put("title", task.getTitle());
            taskObj.put("description", task.getDescription());
            taskObj.put("duedate", task.getEnddate());
            taskObj.put("done", task.isDone());
            taskObj.put("priority", task.getPriority());


            taskObj.put("id", task.getId());
            holder.put("user_token", mPreferences.getString("AuthToken", ""));
            holder.put("task", taskObj);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("holder", holder.toString());

        JsonObjectRequest req = new JsonObjectRequest(Method.POST, URL, holder,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        Log.d("newtask", response.toString());


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


    public static Task sendTask(final Task task, final SharedPreferences mPreferences, RequestQueue queue) {
        JSONObject taskObj = new JSONObject();
        JSONObject holder = new JSONObject();

        try {
            taskObj.put("title", task.getTitle());
            taskObj.put("description", task.getDescription());
            taskObj.put("duedate", task.getEnddate());
            taskObj.put("done", task.isDone());
            taskObj.put("priority", task.getPriority());
            //holder.put("user_token", mPreferences.getString("AuthToken", ""));
            holder.put("task", taskObj);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
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
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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
        return task;

    }


    public static void deleteTask(String id, final SharedPreferences mPreferences, RequestQueue queue) {
        String URL = TASKS_URL + "/" + id + "/delete";

        StringRequest postRequest = new StringRequest(Request.Method.DELETE, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

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
                params.put("X-AUTH-TOKEN", mPreferences.getString("AuthToken", ""));

                return params;
            }
        };

        queue.add(postRequest);
    }


}
