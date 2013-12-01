package de.thm.todoist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity implements OnClickListener,
		Constants {

	public ListView lvTasks;
	public Button bReload, bLogout, bNew, bImport;
	public User actualUser;
	public ArrayList<Task> actualTasks;
	public ArrayList<HashMap<String, Object>> myList;
	public Context ctxt;
	public SharedPreferences mPreferences;
	public TextView tvUserName;
	private static final int REQUEST_PICK_FILE = 1;
	final int FILE_CHOOSER = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_task);

		ctxt = this;
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

		lvTasks = (ListView) findViewById(R.id.listView1);

		bReload = (Button) findViewById(R.id.buttonTaskReload);
		bReload.setOnClickListener(this);
		bLogout = (Button) findViewById(R.id.buttonTaskLogout);
		bLogout.setOnClickListener(this);
		bNew = (Button) findViewById(R.id.buttonTaskNew);
		bNew.setOnClickListener(this);
		bImport = (Button) findViewById(R.id.buttonTaskImport);
		bImport.setOnClickListener(this);

		tvUserName = (TextView) findViewById(R.id.textViewTaskLoggedInValue);
		tvUserName.setText(mPreferences.getString("UserMail", ""));

		myList = new ArrayList<HashMap<String, Object>>();
		actualTasks = new ArrayList<Task>();

		populateTasks();

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.buttonTaskImport) {

			Intent intent = new Intent(this, FilePickerActivity.class);
			intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);

			// Only make .json files visible
			ArrayList<String> extensions = new ArrayList<String>();
			extensions.add(".json");
			intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS,
					extensions);

			startActivityForResult(intent, REQUEST_PICK_FILE);

		}

		if (v.getId() == R.id.buttonTaskReload) {

		}

		if (v.getId() == R.id.buttonTaskNew) {
			Task selectedTask = new Task("0", "", "", "", false, 0);
			Intent intent = new Intent(ctxt, SingleTaskActivity.class);
			intent.putExtra("task", selectedTask);
			startActivity(intent);
		}

		if (v.getId() == R.id.buttonTaskLogout) {
			LogoutTask logoutTask = new LogoutTask(this);
			logoutTask.setMessageLoading("Loggin out...");
			logoutTask.execute(LOGOUT_URL);
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

	public void populateTasks() {

		actualTasks.add(new Task("1", "Aufgabe1", "Beschreibung 1",
				"12.12.1999", false, 0));
		actualTasks.add(new Task("2", "Aufgabe2", "Beschreibung 2",
				"12.12.1998", false, 0));
		actualTasks.add(new Task("3", "Aufgabe3", "Beschreibung 3",
				"12.12.1997", true, 0));

		showList();
	}

	private class GetUserTask extends AsyncTask<String, Void, User> {

		@Override
		protected User doInBackground(String... params) {
			User usr = null;
			return usr;
		}

		@Override
		protected void onPostExecute(User result) {

			if (result != null) {

			}
		}
	}

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

}
