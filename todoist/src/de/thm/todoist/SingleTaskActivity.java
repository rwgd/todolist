package de.thm.todoist;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SingleTaskActivity extends Activity implements OnClickListener   {
	
	public EditText etTitle, etDes, etDate;
	public Button bDone, bSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_single_task);
		
		bDone = (Button) findViewById(R.id.buttonSingleTaskDone);
		bDone.setOnClickListener(this);
		bSave = (Button) findViewById(R.id.buttonSingleTaskSaveN);
		bSave.setOnClickListener(this);
		
		
		etTitle = (EditText) findViewById(R.id.editTextSingleTaskTitle);
		etDes = (EditText) findViewById(R.id.editTextSingleTaskDes);
		etDate = (EditText) findViewById(R.id.editTextSingleTaskDate);
		
		
		Bundle extras = getIntent().getExtras();
		Task selTask = (Task) extras.getSerializable("task");
		
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
	public void onClick(View v) {
		
		if (v.getId() == R.id.buttonSingleTaskDone) {
			if(bDone.getText().equals("Done")){
				bDone.setText("Undone");
			} else {
				bDone.setText("Done");
			}
		}
		
		if (v.getId() == R.id.buttonSingleTaskSaveN) {
			
		}
		
	}

}
