package com.etayzas.android.apiresttest;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.etayzas.android.apiresttest.data.SessionManager;
import com.etayzas.android.apiresttest.data.model.LoginRequest;
import com.etayzas.android.apiresttest.data.model.LoginResponse;
import com.etayzas.android.apiresttest.data.model.beach.BeachesResponse;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    private SOService mService;
    private LoginResponse loginDetails;

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    public EditText inputEmail;
    public EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mService = ApiUtils.getSOService();

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Hello!", Toast.LENGTH_LONG).show();

            // Login button Click Event
            btnLogin.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    String email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();

                    //delete inserts
                    inputEmail.setText("");
                    inputPassword.setText("");

                    // Check for empty data in the form
                    if (!email.isEmpty() && !password.isEmpty()) {
                        // login user
                        loginDetails = Login(email, password);

                    } else {
                        // Prompt user to enter credentials
                        Toast.makeText(getApplicationContext(),
                                "Please enter the credentials!", Toast.LENGTH_LONG)
                                .show();
                    }
                }

            });

            // Link to Register Screen
            btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    startActivity(i);
                }
            });
        }
    }

    /**
     * function to verify login details in mysql db
     * */
    private LoginResponse Login(final String email, final String password) {

        LoginRequest loginReq = new LoginRequest();
        loginReq.init(email, password);

        mService.login(loginReq).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if(response.isSuccessful()) {
                    Log.d("LoginActivity", "Logged IN!");

                    LoginResponse loginRes = response.body();
                    loginDetails = loginRes;
                    Log.d("LoginActivity", loginRes.getApiKey());

                    if (loginDetails != null)
                    {
                        // user successfully logged in
                        // Create login session
                        session.createLoginSession(loginDetails.getName(), loginDetails.getEmail(), loginDetails.getApiKey());
                        Log.d("LoginActivity api", loginDetails.getApiKey());
                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Log.d("LoginActivity", "Couldn't Login");
                    }

                }else {
                    int statusCode = response.code();
                    Log.d("LoginActivity", "" + statusCode);
                    Log.d("LoginActivity", response.toString());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d("LoginActivity", "error loading from API");
                Log.d("LoginActivity", t.getMessage());
                t.printStackTrace();
            }
        });

        return loginDetails;
    }

}
