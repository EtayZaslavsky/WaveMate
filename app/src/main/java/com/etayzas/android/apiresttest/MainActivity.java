package com.etayzas.android.apiresttest;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etayzas.android.apiresttest.data.SessionManager;
import com.etayzas.android.apiresttest.data.model.UploadRequest;
import com.etayzas.android.apiresttest.data.model.UploadResponse;
import com.etayzas.android.apiresttest.data.model.beach.AddBeachRequest;
import com.etayzas.android.apiresttest.data.model.beach.AddBeachResponse;
import com.etayzas.android.apiresttest.data.model.beach.Beach;
import com.etayzas.android.apiresttest.data.model.beach.BeachesResponse;
import com.etayzas.android.apiresttest.data.model.geocoding.GeocodingResults;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;
import com.etayzas.android.apiresttest.data.remoteGeocoding.GeoCodeService;
import com.etayzas.android.apiresttest.data.remoteGeocoding.GeoCodeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private SOService mService;

    private Button logout;
    private Button show;
    private EditText geocodeAddress;
    private FloatingActionButton fabUpload;
    private FloatingActionButton fabBeach;
    private SearchView search;
    private ViewGroup transitionsContainer;
    private Spinner beach_spinner;

    private ListView mListView;
    private List<Beach> beachesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if is logged in. if not -> Turn him out!
        if(!session.isLoggedIn())
        {
            Intent intent = new Intent(MainActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {

            mService = ApiUtils.getSOService();

            logout = (Button) findViewById(R.id.logout_button);
            show = (Button) findViewById(R.id.show_button);
            geocodeAddress = (EditText) findViewById(R.id.geocode_editText);
            fabUpload = (FloatingActionButton) findViewById(R.id.upload_fab);
            fabBeach = (FloatingActionButton) findViewById(R.id.beach_fab);
            search = (SearchView) findViewById(R.id.beach_search);
            beach_spinner = (Spinner) findViewById(R.id.beach_spinner);

            transitionsContainer = (ViewGroup) findViewById(R.id.main_layout);

            mListView = (ListView) findViewById(R.id.beaches_list);



/*

            geocodeAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // When focus is lost check that the text field
                    //* has valid values.

                    if (!hasFocus) {
                        // Finished Typing -> Send Geocode Request
                        Toast.makeText(getApplicationContext(), "Geocode Done Well111", Toast.LENGTH_SHORT).show();
                        String address = geocodeAddress.getText().toString().trim();
                        Geocode(address);
                    }
                }
            });
*/

            geocodeAddress.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    String input;
                    if(actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        input = v.getText().toString();
                        Geocode(input);
                        return true; // consume.
                    }
                    return false; // pass on to other listeners.
                }
            });
            geocodeAddress.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    String input;
                    EditText editText;

                    if(!hasFocus)
                    {
                        editText= (EditText) v;
                        input= editText.getText().toString();
                        Geocode(input);
                    }
                }
            });


            //hello = (TextView) findViewById(R.id.hello_textview);
            //hello.setText("Hello " + session.getUserDetails().get("name") + "!");

            // Log User Details
            Log.d("MainActivity", session.getUserDetails().get("name"));
            Log.d("MainActivity", session.getUserDetails().get("email"));
            Log.d("MainActivity", session.getUserDetails().get("key"));

            //Hello Toast
            Toast.makeText(getApplicationContext(), "Hello " + session.getUserDetails().get("name") + "!", Toast.LENGTH_SHORT).show();

            // Initializing a String Array
            String[] beaches = new String[]{
                    "Beach Name",
                    "Marina Herzlia",
                    "Zvulun",
                    "Dromi",
                    "HaSharon"
            };

            final List<String> crowedList = new ArrayList<>(Arrays.asList(beaches));

            // Initializing an ArrayAdapter
            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    this, R.layout.support_simple_spinner_dropdown_item, crowedList){
                @Override
                public boolean isEnabled(int position){
                    if(position == 0)
                    {
                        // Disable the first item from Spinner
                        // First item will be use for hint
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if(position == 0){
                        // Set the hint text color gray
                        tv.setTextColor(Color.GRAY);
                    }
                    else {
                        tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };
            spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            beach_spinner.setAdapter(spinnerArrayAdapter);

            beach_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItemText = (String) parent.getItemAtPosition(position);
                    // If user change the default selection
                    // First item is disable and it is used for hint
                    if(position > 0){
                        // Notify the selected item text
                        Toast.makeText
                                (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //noGPS();
                }
            });


            // RegisterResponse Button Click event
            logout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Log.d("MainActivity", "LOGOUT");

                    session.logoutUser();

                    Intent i = new Intent(getApplicationContext(),
                            LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                }
            });

            // Link to Show
            show.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    /*
                    Log.d("MainActivity", "ScreenSlidePagerActivity SHOW");

                    Intent i = new Intent(getApplicationContext(),
                            ShowActivity.class);
                    startActivity(i);
                    */

                    beachesList = session.getUserBeaches();
                    if (beachesList == null){
                        beachesList = new ArrayList<>();
                    }

                    Log.d("MainActivity", "beaches count -> " + beachesList.size());
                    Log.d("MainActivity", "beaches foreach");

                    List<String> beachesNameList = new ArrayList<>();
                    for (Beach beach : beachesList) {
                        beachesNameList.add(beach.getName());
                        Log.d("MainActivity", "beach name - " + beach.getName());
                    }

                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, beachesNameList);
                    mListView.setAdapter(adapter);
                }
            });

            fabUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Upload Goovy", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    Log.d("MainActivity", "UPLOAD");

                    Intent i = new Intent(getApplicationContext(),
                            UploadActivity.class);
                    startActivity(i);
                }
            });


            fabBeach.setOnClickListener(new View.OnClickListener() {
                boolean visible;

                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Add Beach to Favorites", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    Log.d("MainActivity", "ADD BEACH");

                    //TransitionManager.beginDelayedTransition(transitionsContainer);
                    visible = !visible;
                    search.setVisibility(visible ? View.VISIBLE : View.GONE);
                    beach_spinner.setVisibility(!visible ? View.VISIBLE : View.GONE);

                }
            });
        }
    }

    public void Geocode(String address) {
        // Initialize Params (headers)
        String key = session.getUserKey();

        Toast.makeText(getApplicationContext(), "Hello " + session.getUserDetails().get("name") + "!", Toast.LENGTH_SHORT).show();

        Log.d("Geocode", "Key " + key);
        Log.d("Geocode", "address " + address);

        AddBeachRequest addBeachReq = new AddBeachRequest();
        addBeachReq.setAddress(address);

        mService.addBeach(key, addBeachReq).enqueue(new Callback<AddBeachResponse>() {
            @Override
            public void onResponse(Call<AddBeachResponse> call, Response<AddBeachResponse> response) {
                if(response.isSuccessful()) {
                    Log.d("Geocode", "UPLOADED!");
                    Toast.makeText(getApplicationContext(), "Geocode Done Well", Toast.LENGTH_SHORT).show();
                    AddBeachResponse BeachesRes = response.body();
                    if(BeachesRes != null){
                        // Beach really created or asociated to user
                        if(response.code() == 205 || response.code() == 201){
                            Beach beach = new Beach();
                            beach.init(BeachesRes.getBeachId(),BeachesRes.getBeachName());
                            session.addBeach(beach);
                        }
                    }
                }
                else {
                    int statusCode = response.code();
                    Log.d("Geocode", "" + statusCode);
                    Log.d("Geocode", response.toString());
                    Log.d("Geocode", response.message());
                    Toast.makeText(getApplicationContext(), "Couldn't Geocode", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddBeachResponse> call, Throwable t) {
                Log.d("Geocode", "error loading from API");
                Log.d("Geocode", t.getMessage());
                Toast.makeText(getApplicationContext(), "Couldn't Geocode - Failure", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });


    }
}
