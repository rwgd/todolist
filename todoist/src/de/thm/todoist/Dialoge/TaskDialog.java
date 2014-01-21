package de.thm.todoist.Dialoge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;

import java.util.GregorianCalendar;

/**
 * Created by Benedikt on 17.01.14.
 */
public class TaskDialog extends DialogFragment {

    private Task editableTask = null;

    public TaskDialog(Task task) {
        editableTask = task;
    }

    public TaskDialog() {
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(String id, String name, GregorianCalendar enddate, String description);

        public void onDialogNeutralClick(String id);

        public void onDialogNegativeClick();
    }

    private NoticeDialogListener mListener;
    private LinearLayout mMainView;
    private CheckBox mActivateEnddate;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private TextView mTaskName;
    private TextView mTaskDesc;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mMainView = (LinearLayout) inflater.inflate(R.layout.dialog_task, null);
        assert mMainView != null;
        mActivateEnddate = (CheckBox) mMainView.findViewById(R.id.cbActivateDate);
        mDatePicker = (DatePicker) mMainView.findViewById(R.id.taskEndDate);
        mTimePicker = (TimePicker) mMainView.findViewById(R.id.taskEndTime);
        mTimePicker.setIs24HourView(true);
        mTaskName = (TextView) mMainView.findViewById(R.id.taskname);
        mTaskDesc = (TextView) mMainView.findViewById(R.id.taskDescription);
        mActivateEnddate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTimePicker.setEnabled(isChecked);
                mDatePicker.setEnabled(isChecked);
            }
        });
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(mMainView)
                // Set the action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GregorianCalendar enddate = null;
                        String taskID = "";
                        if (mActivateEnddate.isChecked()) {
                            enddate = new GregorianCalendar(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                        }
                        if (editableTask != null) taskID = editableTask.getId();
                        mListener.onDialogPositiveClick(taskID, mTaskName.getText().toString(), enddate, mTaskDesc.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick();
                    }
                });

        if (editableTask != null) {
            mTaskName.setText(editableTask.getTitle());
            mTaskDesc.setText(editableTask.getDescription());
            if (editableTask.getEnddate() == null) {
                mTimePicker.setEnabled(false);
                mDatePicker.setEnabled(false);
            }
            builder.setTitle(R.string.editTask);
            builder.setNeutralButton(R.string.deleteTask, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogNeutralClick(editableTask.getId());
                }
            });
        } else {
            builder.setTitle(R.string.createTask);
        }

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

}