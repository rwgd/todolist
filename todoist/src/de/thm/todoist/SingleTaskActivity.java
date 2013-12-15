package de.thm.todoist;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SingleTaskActivity extends Activity implements OnClickListener   {
	
	private EditText etTitle, etDes, etDate;
	private Button bDone;
	private ArrayList<Task> allTasks;
	private Task selTask;
	private TaskListModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		
		model =  TaskListModel.getInstance();
		allTasks = model.getTaskList();
		
		ActionBar ab = getActionBar();
	    ab.setTitle(":todoist");
	    ab.setSubtitle("edit/new task"); 
		
		bDone = (Button) findViewById(R.id.buttonSingleTaskDone);
		bDone.setOnClickListener(this);
		
		
		etTitle = (EditText) findViewById(R.id.editTextSingleTaskTitle);
		etDes = (EditText) findViewById(R.id.editTextSingleTaskDes);
		etDate = (EditText) findViewById(R.id.editTextSingleTaskDate);
		
		
		Bundle extras = getIntent().getExtras();
		selTask = (Task) extras.getSerializable("task");
		
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
		finish();
	}


	@Override
	public void onClick(View v) {
		
		if (v.getId() == R.id.buttonSingleTaskDone) {
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
		
		if(!selTask.getId().equals("0")){
			//Vorhander Task wird bearbeitet
			for(int i = 0; i < allTasks.size(); i++){
				if(allTasks.get(i).getId().equals(selTask.getId())){
					allTasks.set(i, selTask);
				}
			}
		} else {
			//neuer Task
			allTasks.add(selTask);
		}
		
		model.setTaskList(allTasks);
		
	}

}
