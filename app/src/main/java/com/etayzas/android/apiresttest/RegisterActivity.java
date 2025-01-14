package com.etayzas.android.apiresttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.etayzas.android.apiresttest.data.SessionManager;
import com.etayzas.android.apiresttest.data.model.RegisterRequest;
import com.etayzas.android.apiresttest.data.model.RegisterResponse;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterActivity extends AppCompatActivity {

    private SessionManager session;
    private SOService mService;

    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;

    @Override
    protected void onCreate (Bundle savedInstanceState)  {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.activity_register);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mService = ApiUtils.getSOService();

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
            }
        });

        // RegisterResponse Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("RegisterActivity", "CLICKED");

                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                //delete inserts
                inputFullName.setText("");
                inputEmail.setText("");
                inputPassword.setText("");

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    register(name, email, password);
                    Log.d("RegisterActivity", "Not Empty");

                } else {
                    Log.d("RegisterActivity", "Empty Details");
                }

            }
        });

    }


    public void register(String name, String email, String password) {
        Log.d("RegisterActivity name", name);
        Log.d("RegisterActivity email", email);
        Log.d("RegisterActivity pass", password);

        RegisterRequest registerReq = new RegisterRequest();
        registerReq.init(name, email, password);

        mService.register(registerReq).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {

                if(response.isSuccessful()) {
                    Log.d("RegisterActivity", "REGISTERD!");
                    // Login
                    Intent i = new Intent(getApplicationContext(),
                            LoginActivity.class);
                    startActivity(i);
                    finish();
                }else {
                    int statusCode = response.code();
                    Log.d("RegisterActivity", "" + statusCode);
                    Log.d("RegisterActivity", response.toString());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.d("RegisterActivity", "error loading from API");
                Log.d("RegisterActivity", t.getMessage());
                t.printStackTrace();
            }
        });


    }

}
