package de.thm.todoist.Helper;

import android.os.Environment;

public interface Constants {

    static final String LOGIN_API_ENDPOINT_URL = "";
    static final String REGISTER_API_ENDPOINT_URL = "";
    static final String LOGOUT_URL = "";
    static final String TASKS_URL = "";
    static final String NEW_TASK_URL = "";
    static final String CHECK_URL = "";
    static final String SAVE_DIR = Environment.getExternalStorageDirectory().toString() + "/tasksList.xml";
    static final String EXPORT_XML = Environment.getExternalStorageDirectory().toString() + "/exportXML.xml";

}
