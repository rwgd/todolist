package de.thm.todoist;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;



import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener, Constants {
	
	private EditText etName, etPassword;
	private Button btnLogin, btnRegister;
	private SharedPreferences mPreferences;
	private Context context;
	private RequestQueue queue;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		context = this;
		
		ActionBar ab = getActionBar();
	    ab.setTitle(":todoist");
	    ab.setSubtitle("login"); 
	    
	    
		queue = Volley.newRequestQueue(this);
		
		etName = (EditText) findViewById(R.id.editTextLoginName);
		etPassword = (EditText) findViewById(R.id.editTextLoginPassword);
		
		btnLogin = (Button) findViewById(R.id.buttonLogin);
		btnLogin.setOnClickListener(this);
		btnRegister = (Button) findViewById(R.id.buttonLoginRegister);
		btnRegister.setOnClickListener(this);
		
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		
		//Wenn User bereits eingeloggt, Activity ueberspringen r
		if(mPreferences.getString("AuthToken", "") != null && mPreferences.getString("UserMail", "") != null){
			if(!mPreferences.getString("AuthToken", "").equals("")){
				Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
	            startActivity(intent);
	            finish();
			}
		}
	}


	@Override
	public void onClick(View v) {
		
		//Login Button
		if (v.getId() == R.id.buttonLogin) {

			String name = etName.getText().toString();
			String password = etPassword.getText().toString();
			
			//zum testen hardcoded
			//login("rbn99166@gmail.com", "123456789");
			
			if(name.equals("") || password.equals("")){
				Toast.makeText(this, "Please complete all the fields",Toast.LENGTH_LONG).show();
			} else {
				login(name, password);
			}

		}
		
		//Register Button
		if (v.getId() == R.id.buttonLoginRegister) {
			String name = etName.getText().toString();
			String password = etPassword.getText().toString();
			
			Intent i = new Intent(this, RegisterActivity.class);
			startActivity(i);
		}
		
	}
	
	
	//Abfrage mit Volley Library -> geht! AsyncTask kann dann raus.
	public void login(final String username, final String password){
		JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        try {
			userObj.put("email", username);
	        userObj.put("password", password);
	        holder.put("user", userObj);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JsonObjectRequest req = new JsonObjectRequest(LOGIN_API_ENDPOINT_URL, holder,
			       new Response.Listener<JSONObject>() {
			           @Override
			           public void onResponse(JSONObject response) {
			               try {
			            	   
			            	   //TODO noch fertig machen
			            	   if (response.getBoolean("success")) {
			            		   Toast.makeText(context, "token:" + response.getJSONObject("data").getString("auth_token"), Toast.LENGTH_LONG).show();
			            		   SharedPreferences.Editor editor = mPreferences.edit();
				   	                editor.putString("AuthToken", response.getJSONObject("data").getString("auth_token"));
				   	                editor.putString("UserMail", username);
				   	                editor.commit();
				   	                //Toast.makeText(context, "token:" + json.getJSONObject("data").getString("auth_token"), Toast.LENGTH_LONG).show();
				   	                
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
			       });
		
		queue.add(req);	
	}
	

/*	private class LoginTask extends UrlJsonAsyncTask {
		
		public String userMail, userPassword;
		
	    public LoginTask(Context context, String userMail, String userPassword) {
	        super(context);
	        this.userMail = userMail;
	        this.userPassword = userPassword;
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
	                userObj.put("password", userPassword);
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
	                json.put("info", "Email and/or password are invalid. Retry!");
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
	                editor.putString("UserMail", userMail);
	                editor.commit();
	                Toast.makeText(context, "token:" + json.getJSONObject("data").getString("auth_token"), Toast.LENGTH_LONG).show();
	                
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
