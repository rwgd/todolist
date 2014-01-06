package de.thm.todoist;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SingleTaskActivity extends Activity implements OnClickListener, Constants    {
	
	private EditText etTitle, etDes, etDate;
	private Button bDone;
	private ArrayList<Task> allTasks;
	private Task selTask;
	private TaskListModel model;
	private SharedPreferences mPreferences;
	private RequestQueue queue;
	private Boolean isNewTask;
	private Context ctxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		ctxt = this;
		
		model =  TaskListModel.getInstance();
		allTasks = model.getTaskList();
		isNewTask = false;
		
		ActionBar ab = getActionBar();
	    ab.setTitle(":todoist");
	    ab.setSubtitle("edit/new task"); 
		
		bDone = (Button) findViewById(R.id.buttonSingleTaskDone);
		bDone.setOnClickListener(this);
		
		
		etTitle = (EditText) findViewById(R.id.editTextSingleTaskTitle);
		etDes = (EditText) findViewById(R.id.editTextSingleTaskDes);
		etDate = (EditText) findViewById(R.id.editTextSingleTaskDate);
		
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		Bundle extras = getIntent().getExtras();
		selTask = (Task) extras.getSerializable("task");
		queue = Volley.newRequestQueue(this);
		
		etTitle.setText(selTask.getTitle());
		etDes.setText(selTask.getDescription());
		etDate.setText(selTask.getEnddate());
		
		if(selTask.isDone()){
			bDone.setText("Done");
		} else {
			bDone.setText("Undone");
		}
		
	}
	
	@Override
	public void onBackPressed() {
		saveTask();
		
		if(isNewTask){
			//wenn neuer Task, dann auf den Server uebertragen
			ServerLib.sendTask(selTask, mPreferences, queue);
		} else {
			//sonst editieren
			ServerLib.editTask(selTask, mPreferences, queue);
		}
		
		finish();
	}


	@Override
	public void onClick(View v) {
		
		if (v.getId() == R.id.buttonSingleTaskDone) {
			
			Toast.makeText(ctxt, selTask.getId(), Toast.LENGTH_LONG).show();
			
			if(bDone.getText().equals("Done")){
				bDone.setText("Undone");
			} else {
				bDone.setText("Done");
			}
		}

		
	}
	
	public void saveTask(){
		String title = etTitle.getText().toString();
		String description = etDes.getText().toString();
		String date = etDate.getText().toString();
		Boolean done = false;
		if(bDone.getText().toString().equals("Done")){
			done = true;
		}
		
		selTask.setTitle(title);
		selTask.setDescription(description);
		selTask.setEnddate(date);
		selTask.setDone(done);
		selTask.setPriority(2);
		
		if(!selTask.getId().equals("0")){
			//Vorhander Task wird bearbeitet
			for(int i = 0; i < allTasks.size(); i++){
				if(allTasks.get(i).getId().equals(selTask.getId())){
					allTasks.set(i, selTask);
				}
			}
		} else {
			//neuer Task
			SecureRandom random = new SecureRandom();
			String id = new BigInteger(130, random).toString(32);
			selTask.setId(id);
			
			allTasks.add(selTask);
			isNewTask = true;
		}
		
		model.setTaskList(allTasks);
		
	}
	


}
