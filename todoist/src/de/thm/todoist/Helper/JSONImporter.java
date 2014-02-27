package de.thm.todoist.Helper;

import android.app.Activity;
import android.util.Log;
import com.devspark.appmsg.AppMsg;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Benedikt on 24.02.14.
 */
public class JSONImporter {

    public static ArrayList<Task> parseFile(File f, Activity act) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        try {
            String jsonString = FktLib.readFile(f);
            JSONObject jsnobject = new JSONObject(jsonString);
            if (f.getPath().contains(".json")) {
                JSONArray jsonArray = jsnobject.getJSONArray("tasks");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject explrObject = jsonArray.getJSONObject(i);

                    String title = explrObject.getString("title");

                    String id = explrObject.getString("id");
                    Boolean done = !explrObject
                            .isNull("completed_at");
                    tasks.add(new Task(id, title, "", new GregorianCalendar(), done, false));
                }
                AppMsg.makeText(act, R.string.wunderlist_import_fail, AppMsg.STYLE_ALERT);
            } else if (f.getPath().contains(".abk")) {
                JSONArray taskArray = jsnobject.getJSONArray("tasks");
                for (int i = 0; i < taskArray.length(); i++) {
                    JSONObject curTask = taskArray.getJSONObject(i);
                    String title = curTask.getString("title");
                    String id = curTask.getString("globalTaskId");
                    boolean hasEndDate = false;
                    GregorianCalendar calDueDate = new GregorianCalendar();
                    if (curTask.has("dueDate")) {
                        hasEndDate = true;
                        long dueDate = curTask.getLong("dueDate");
                        calDueDate.setTimeInMillis(dueDate);
                    }
                    boolean done = false;
                    if (curTask.getString("status").equals("CHECKED")) done = true;
                    tasks.add(new Task(id, title, "", calDueDate, done, hasEndDate));
                }
            }

        } catch (JSONException e) {
            AppMsg.makeText(act, R.string.external_import_fail, AppMsg.STYLE_ALERT);
            Log.e("Error Importing", "external Data", e);
        } catch (IOException e) {
            AppMsg.makeText(act, R.string.external_import_fail, AppMsg.STYLE_ALERT);
            Log.e("Error Importing", "external Data", e);
        }
        return tasks;
    }

}

