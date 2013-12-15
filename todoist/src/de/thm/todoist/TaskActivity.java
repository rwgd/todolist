package de.thm.todoist;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity implements Constants {

	private ListView lvTasks;
	private User actualUser;
	private ArrayList<Task> actualTasks;
	private ArrayList<HashMap<String, Object>> myList;
	private Context ctxt;
	private SharedPreferences mPreferences;
	private TextView tvUserName;
	private static final int REQUEST_PICK_FILE = 1;
	private final int FILE_CHOOSER = 1;
	private RequestQueue queue;
	private TaskListModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_task);
		ctxt = this;
		model = TaskListModel.getInstance();
		
		ActionBar ab = getActionBar();
	    ab.setTitle(":todoist");
	    ab.setSubtitle("tasks"); 
	    
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		queue = Volley.newRequestQueue(this);
		
		lvTasks = (ListView) findViewById(R.id.listView1);
		tvUserName = (TextView) findViewById(R.id.textViewTaskLoggedInValue);
		tvUserName.setText(mPreferences.getString("UserMail", ""));

		myList = new ArrayList<HashMap<String, Object>>();
		actualTasks = new ArrayList<Task>();

		getTasks();
	}
	
	 public void onResume(){
	 super.onResume();
	 if(model.getTaskList()!=null){
		 actualTasks = model.getTaskList();
		 if(actualTasks.size() > 0){
			 showList();
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
        	model.setTaskList(actualTasks);
        	Task selectedTask = new Task("0", "", "", "", false, 0);
			Intent intentS = new Intent(ctxt, SingleTaskActivity.class);
			intentS.putExtra("task", selectedTask);
			startActivity(intentS);
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

								actualTasks.add(new Task(id, title,
										"Beschreibung 1", date, done, 0));
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

				Bitmap greenIcon = BitmapFactory.decodeResource(getResources(),
						R.drawable.green);
				Bitmap redIcon = BitmapFactory.decodeResource(getResources(),
						R.drawable.red);

				for (int i = 0; i < actualTasks.size(); i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();

					map.put("task_title", actualTasks.get(i).getTitle());
					map.put("task_enddate", actualTasks.get(i).getEnddate());

					if (actualTasks.get(i).isDone()) {
						map.put("task_done", greenIcon);
					} else {
						map.put("task_done", redIcon);
					}

					myList.add(map);
				}

				ExtendedSimpleAdapter aa = new ExtendedSimpleAdapter(this,
						myList, R.layout.row_task, new String[] { "task_title",
								"task_enddate", "task_done" }, new int[] {
								R.id.textViewTaskRowTitle,
								R.id.textViewTaskRowTillValue,
								R.id.imageViewTaskRowDone });
				lvTasks.setAdapter(aa);

				lvTasks.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						model.setTaskList(actualTasks);
						Task selectedTask = actualTasks.get(position);

						Intent intent = new Intent(ctxt,
								SingleTaskActivity.class);
						intent.putExtra("task", selectedTask);
						startActivity(intent);
					}
				});

			}
		}

	}



	public void sendTask(){
		JSONObject taskObj = new JSONObject();
		JSONObject holder = new JSONObject();

		try {
			
			taskObj.put("title", "blub");
			holder.put("user_token", mPreferences.getString("AuthToken", ""));
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
				                
			               
			           }
			       }, new Response.ErrorListener() {
			           @Override
			           public void onErrorResponse(VolleyError error) {
			               VolleyLog.e("Error: ", error.getMessage());
			           }
			       }){     
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { 
                    Map<String, String>  params = new HashMap<String, String>();  
                    params.put("Content-Type", "application/json");  
                    params.put("Accept", "application/json");

                    return params;  
            }
        };
		
		queue.add(req);	

	}
	
	public void getTasks(){

        String URL = TASKS_URL + "/?user_token=" + mPreferences.getString("AuthToken", "");

        StringRequest postRequest = new StringRequest(Request.Method.GET, URL, 
            new Response.Listener<String>() 
            {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.d("Response", response);

                    JsonArray jArray = new JsonParser().parse(response).getAsJsonArray();
                    for (int i=0;i<jArray.size();i++) {
                        JsonObject jsonObject = jArray.get(i).getAsJsonObject();
                        Log.d("title", jsonObject.get("title").toString());
                        Log.d("description", jsonObject.get("description").toString());
                        Log.d("duedate", jsonObject.get("duedate").toString());
                        Log.d("done", jsonObject.get("done").toString());
                        Log.d("priority", jsonObject.get("priority").toString());
                        
                        String id = "000";
                        String title = "";
                        String description = "";
                        String duedate = "";
                        Boolean done = false;
                        int priority = 0;
                        
                        //ID
                        SecureRandom random = new SecureRandom();
                        id = new BigInteger(130, random).toString(32);

                        if(!jsonObject.get("title").isJsonNull()){
                        	title = jsonObject.get("title").toString().replace("\"", "");
                        }
                        if(!jsonObject.get("description").isJsonNull()){
                        	description = jsonObject.get("description").toString().replace("\"", "");
                        }
                        if(!jsonObject.get("duedate").isJsonNull()){
                        	duedate =jsonObject.get("duedate").toString().replace("\"", "");
                        }
                        if(!jsonObject.get("done").isJsonNull()){
                        	done = jsonObject.get("done").getAsBoolean();
                        }
                        if(jsonObject.get("priority").isJsonNull()){
                        	try{
                        		priority = jsonObject.get("priority").getAsInt(); 
                        	} catch (NumberFormatException e){
                        		//Error beim parsen
                        		priority = 0;
                        	}
                        	
                        }
                        
                        
                        actualTasks.add(new Task(id, title,description, duedate, done, priority));
                    }
                    model.setTaskList(actualTasks);
                    showList();
                }
            }, 
            new Response.ErrorListener() 
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    Log.d("ERROR","error => "+error.toString());
                }
            }
        ) {     
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { 
                    Map<String, String>  params = new HashMap<String, String>();  
                    params.put("Content-Type", "application/json");  
                    params.put("Accept", "application/json");

                    return params;  
            }
        };

		queue.add(postRequest);	

	}
	
	
	public void logout(){

		JSONObject authObj = new JSONObject();
		try {
			authObj.put("user_token", mPreferences.getString("AuthToken", ""));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JsonObjectRequest req = new JsonObjectRequest(Method.POST, LOGOUT_URL, authObj, 
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
				                
			               
			           }}
			       }, new Response.ErrorListener() {
			           @Override
			           public void onErrorResponse(VolleyError error) {
			               VolleyLog.e("Error: ", error.getMessage());
			           }
			       }){     
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { 
                    Map<String, String>  params = new HashMap<String, String>();  
                    params.put("Content-Type", "application/json");  
                    params.put("Accept", "application/json");

                    return params;  
            }
        };
		
		queue.add(req);	
		
	}
	
	
/*
	private class LogoutTask extends UrlJsonAsyncTask {
		public LogoutTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			String response = null;
			JSONObject json = new JSONObject();
			JSONObject authObj = new JSONObject();

			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Retry!");
					authObj.put("user_token",
							mPreferences.getString("AuthToken", ""));

					StringEntity se = new StringEntity(authObj.toString());
					post.setEntity(se);

					post.setHeader("Accept", "application/json");
					post.setHeader("Content-Type", "application/json");

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					response = client.execute(post, responseHandler);
					json = new JSONObject(response);

				} catch (HttpResponseException e) {
					e.printStackTrace();
					Log.e("ClientProtocol", "" + e);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("IO", "" + e);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("JSON", "" + e);
			}

			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					SharedPreferences.Editor editor = mPreferences.edit();
					editor.remove("AuthToken");
					editor.remove("UserMail");
					editor.commit();

					Intent intent = new Intent(TaskActivity.this,
							LoginActivity.class);
					startActivityForResult(intent, 0);
				}
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
			}
		}
	}
*/
}
