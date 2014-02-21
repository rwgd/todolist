package de.thm.todoist.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.devspark.appmsg.AppMsg;
import de.thm.todoist.Helper.Constants;
import de.thm.todoist.Helper.ServerLib;
import de.thm.todoist.R;

public class LoginActivity extends Activity implements OnClickListener, Constants {

    private EditText etName, etPassword;
    private SharedPreferences mPreferences;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(this.getString(R.string.app_name));
            ab.setSubtitle("login");
        }


        queue = Volley.newRequestQueue(this);

        etName = (EditText) findViewById(R.id.editTextLoginName);
        etPassword = (EditText) findViewById(R.id.editTextLoginPassword);

        Button btnLogin = (Button) findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(this);
        Button btnRegister = (Button) findViewById(R.id.buttonLoginRegister);
        btnRegister.setOnClickListener(this);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        //Wenn User bereits eingeloggt, Activity ueberspringen
        if (mPreferences.getString("AuthToken", "") != null && mPreferences.getString("UserMail", "") != null) {
            if (!mPreferences.getString("AuthToken", "").equals("")) {
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

            if (name.equals("") || password.equals("")) {
                AppMsg.makeText(this, this.getString(R.string.missing_credentials), AppMsg.STYLE_ALERT).show();
            } else {
                login(name, password);
            }

        }

        //Register Button
        if (v.getId() == R.id.buttonLoginRegister) {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        }

    }

    public void login(String username, String password) {
        ServerLib.login(username, password, queue, mPreferences, this);
    }

}
