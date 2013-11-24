package de.thm.todoist;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
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

public class RegisterActivity extends Activity implements OnClickListener, Constants {
	
	public EditText etName, etEmail, etPassword1, etPassword2;
	public Button btnRegister, btnLogin;
	public SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);
		
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
			
			if(name.equals("") || email.equals("") || password1.equals("") || password2.equals("")){
				Toast.makeText(this, "Please complete all the fields",Toast.LENGTH_LONG).show();
			} else {
				if(password1.equals(password2)){
					
					if(password1.length()< 8){
						Toast.makeText(this, "Password must be min. 8 Characters long",Toast.LENGTH_LONG).show();
					} else {
						 RegisterTask registerTask = new RegisterTask(this, name, email, password1, password2);
				         registerTask.setMessageLoading("Registering new account...");
				         registerTask.execute(REGISTER_API_ENDPOINT_URL);
					}
  
				} else {
					Toast.makeText(this, "Your password doesn't match confirmation, check again",Toast.LENGTH_LONG).show();
				}
			}

		}
		
		//Login instead
		if (v.getId() == R.id.buttonRegisterLogin) {
			Intent i = new Intent(this, LoginActivity.class);
			startActivity(i);
		}
		
	}

	
	private class RegisterTask extends UrlJsonAsyncTask {
		
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
	}

}
