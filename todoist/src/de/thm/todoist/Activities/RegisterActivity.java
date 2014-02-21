package de.thm.todoist.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import de.thm.todoist.Helper.Constants;
import de.thm.todoist.R;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity implements OnClickListener, Constants {

    private EditText etName, etEmail, etPassword1, etPassword2;
    private Button btnRegister, btnLogin;
    private SharedPreferences mPreferences;
    private Context context;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(this.getString(R.string.app_name));
            ab.setSubtitle("tasks");
        }


        queue = Volley.newRequestQueue(this);

        etName = (EditText) findViewById(R.id.editTextRegisterName);
        etEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        etPassword1 = (EditText) findViewById(R.id.editTextRegisterPassword1);
        etPassword2 = (EditText) findViewById(R.id.editTextRegisterPassword2);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        btnRegister.setOnClickListener(this);
        btnLogin = (Button) findViewById(R.id.buttonRegisterLogin);
        btnLogin.setOnClickListener(this);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {

        //Register Button
        if (v.getId() == R.id.buttonRegister) {

            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String password1 = etPassword1.getText().toString();
            String password2 = etPassword2.getText().toString();

            if (name.equals("") || email.equals("") || password1.equals("") || password2.equals("")) {
                Toast.makeText(this, "Please complete all the fields", Toast.LENGTH_LONG).show();
            } else {
                if (password1.equals(password2)) {

                    if (password1.length() < 8) {
                        Toast.makeText(this, "Password must be min. 8 Characters long", Toast.LENGTH_LONG).show();
                    } else {
                        register(name, email, password1, password2);
                    }

                } else {
                    Toast.makeText(this, "Your password doesn't match confirmation, check again", Toast.LENGTH_LONG).show();
                }
            }

        }

        //Login instead
        if (v.getId() == R.id.buttonRegisterLogin) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

    }

    public void register(final String userName, String userMail, String userPassword1, String userPassword2) {
        //Build the object
        JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        try {
            userObj.put("email", userMail);
            userObj.put("username", userName);
            userObj.put("password", userPassword1);
            userObj.put("password_confirmation", userPassword2);
            holder.put("user", userObj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(REGISTER_API_ENDPOINT_URL, holder,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if (response.getBoolean("success")) {
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString("AuthToken", response.getJSONObject("data").getString("auth_token"));
                                editor.putString("UserMail", userName);
                                editor.commit();

                                Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(context, "error: couldn't login.", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }
        )/*{
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { 
                    Map<String, String>  params = new HashMap<String, String>();  
                    params.put("Content-Type", "application/json");  
                    params.put("Accept", "application/json");

                    return params;  
            }
        }*/;

        queue.add(req);

    }

	
/*	private class RegisterTask extends UrlJsonAsyncTask {

		public String userName, userMail, userPassword1, userPassword2;
		
	    public RegisterTask(Context context, String userName, String userMail, String userPassword1, String userPassword2) {
	       super(context);
	       this.userName = userName;
	       this.userMail = userMail;
	       this.userPassword1 = userPassword1;
	       this.userPassword2 = userPassword2;
	    }

	    @Override
	    protected JSONObject doInBackground(String... urls) {
	        DefaultHttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(urls[0]);
	        JSONObject holder = new JSONObject();
	        JSONObject userObj = new JSONObject();
	        String response = null;
	        JSONObject json = new JSONObject();

	        try {
	            try {
	                json.put("success", false);
	                json.put("info", "Something went wrong. Retry!");

	                userObj.put("email", userMail);
	                userObj.put("username", userName);
	                userObj.put("password", userPassword1);
	                userObj.put("password_confirmation", userPassword2);
	                holder.put("user", userObj);
	                StringEntity se = new StringEntity(holder.toString());
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
	                editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
	                editor.commit();

	                Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
	                startActivity(intent);
	                finish();
	            }
	            Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
	        } catch (Exception e) {
	            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
	        } finally {
	            super.onPostExecute(json);
	        }
	    }
	}*/

}
