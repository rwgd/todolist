package de.thm.todoist;

import android.os.Environment;

public interface Constants {
	
	static final String LOGIN_API_ENDPOINT_URL = "http://kadrei.caelum.uberspace.de/api/sessions.json";
	static final String REGISTER_API_ENDPOINT_URL = "http://kadrei.caelum.uberspace.de/api/registrations";
	static final String LOGOUT_URL = "http://kadrei.caelum.uberspace.de/api/sessions/logout";
	static final String TASKS_URL = "http://kadrei.caelum.uberspace.de/api/tasks";
	static final String NEW_TASK_URL = "http://kadrei.caelum.uberspace.de/api/tasks.json";
	static final String CHECK_URL = "http://kadrei.caelum.uberspace.de/";
	static final String SAVE_DIR = Environment.getExternalStorageDirectory().toString() +  "/tasksList.xml";

}
