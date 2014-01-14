package de.thm.todoist;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
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
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
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
	private boolean newtask;
	private Calendar myCalendar;
	private DatePickerDialog.OnDateSetListener date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		ctxt = this;
		
		model =  TaskListModel.getInstance();
		allTasks = model.getTaskList();
		isNewTask = false;
		myCalendar = Calendar.getInstance();
		
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
		
		newtask = extras.getBoolean("newtask");
		Log.e("newtask", String.valueOf(newtask));
		queue = Volley.newRequestQueue(this);
		
		etTitle.setText(selTask.getTitle());
		etDes.setText(selTask.getDescription());
		etDate.setText(selTask.getEnddate());
		
		etDate.setFocusable(false);
		etDate.setClickable(true);
		date = new DatePickerDialog.OnDateSetListener() {

		    @Override
		    public void onDateSet(DatePicker view, int year, int monthOfYear,
		            int dayOfMonth) {
		        // TODO Auto-generated method stub
		        myCalendar.set(Calendar.YEAR, year);
		        myCalendar.set(Calendar.MONTH, monthOfYear);
		        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		        updateLabel();
		    }

		};
		
		etDate.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            // TODO Auto-generated method stub
	            new DatePickerDialog(SingleTaskActivity.this, date, myCalendar
	                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
	                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
	        }
	    });

		

		
		if(selTask.isDone()){
			bDone.setText("Done");
		} else {
			bDone.setText("Undone");
		}
		
	}
	
	@Override
	public void onBackPressed() {
		prepareTask();
		Task t = null;
		if(newtask){
			t = ServerLib.sendTask(selTask, mPreferences, queue);
			Log.e("idsingletask", t.getId());
			
		} else {
			ServerLib.editTask(selTask, mPreferences, queue);
		}
		
		saveTask(t);
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
	
	private void updateLabel() {

	    String myFormat = "yyyy-MM-dd"; 
	    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

	    etDate.setText(sdf.format(myCalendar.getTime()));
	    }
	
	public void prepareTask(){
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
	}
	
	public void saveTask(Task task){

		if(newtask){
			selTask.setId(task.getId());
			allTasks.add(selTask);
		} else {
			for(int i = 0; i < allTasks.size(); i++){
				if(allTasks.get(i).getId().equals(selTask.getId())){
					allTasks.set(i, selTask);
				}
			}
		}

		model.setTaskList(allTasks);
		
	}
	


}
