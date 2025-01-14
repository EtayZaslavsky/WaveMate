package com.etayzas.android.apiresttest;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etayzas.android.apiresttest.data.SessionManager;
import com.etayzas.android.apiresttest.data.model.UploadRequest;
import com.etayzas.android.apiresttest.data.model.UploadResponse;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;
import com.etayzas.android.apiresttest.helpers.GPSTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    private SessionManager session;
    private SOService mService;
    private GPSTracker gps;

    private Button upload;

    private double lat = 0;
    private double lon = 0;

    private boolean isCrowdSpinnerTriggered = false;

    //private TextView hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if is logged in. if not -> Turn him out!
        if(!session.isLoggedIn())
        {
            Intent intent = new Intent(UploadActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            upload = (Button) findViewById(R.id.upload_button);
            gps = new GPSTracker(getApplicationContext());

            if (!gps.canGetLocation()) {
                Log.d("UploadActivity", "No Location");
                //upload.setEnabled(false);
                //int disabledBackgroundColor = ContextCompat.getColor(getApplicationContext(), R.color.button_main_disabled_bg);
                //upload.setBackgroundColor(disabledBackgroundColor);

                Toast.makeText(getApplicationContext(), "Please Enable Location to Upload", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getApplicationContext(), "lat " + gps.getLatitude(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "lon " + gps.getLongitude(), Toast.LENGTH_LONG).show();
            }

            mService = ApiUtils.getSOService();

            //hello = (TextView) findViewById(R.id.hello_textview);
            //hello.setText("Hello " + session.getUserDetails().get("name") + "!");

            // Log User Details
            Log.d("UploadActivity", session.getUserDetails().get("name"));
            Log.d("UploadActivity", session.getUserDetails().get("email"));
            Log.d("UploadActivity", session.getUserDetails().get("key"));



            // Get reference of widgets from XML layout
            final Spinner spinner = (Spinner) findViewById(R.id.crowd_spinner);

            // Initializing a String Array
            String[] crowedTypes = new String[]{
                    "How Crowded?",
                    "heaven on earth",
                    "good lone spot",
                    "possible",
                    "very crowded",
                    "just... dont."
            };

            final List<String> crowedList = new ArrayList<>(Arrays.asList(crowedTypes));

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
            spinner.setAdapter(spinnerArrayAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItemText = (String) parent.getItemAtPosition(position);
                    // If user change the default selection
                    // First item is disable and it is used for hint
                    if(position > 0){
                        // Notify the selected item text
                        isCrowdSpinnerTriggered = true;
                        Toast.makeText
                                (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    noGPS();
                }
            });


            /*
            Spinner spinner = (Spinner) findViewById(R.id.crowd_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.crowd_array, android.R.layout.simple_spinner_item);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            */

            // Upload Button Click event
            upload.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Log.d("UploadActivity", "UPLOAD");

                    if (isDetailsFull()) {
                        // Set GPS Details if Enabled
                        if (gps.canGetLocation()) {
                            Log.d("UploadActivity", "GPSTracker Location OK");
                            lat = gps.getLatitude(); // returns latitude
                            lon = gps.getLongitude(); // returns longitude

                            Log.d("UploadActivity lat", "" + gps.getLatitude());
                            Log.d("UploadActivity lon", "" + gps.getLongitude());
                            dispatchTakeVideoIntent();
                        }
                        else{
                            //upload.setEnabled(false);
                            //int disabledBackgroundColor = ContextCompat.getColor(getApplicationContext(), R.color.button_main_disabled_bg);
                            //upload.setBackgroundColor(disabledBackgroundColor);

                            Toast.makeText(getApplicationContext(), "Please Enable Location to Upload", Toast.LENGTH_LONG).show();
                        }

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Please Fill the Details", Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }
    }

    //Goovy Record
    static final int REQUEST_VIDEO_CAPTURE = 1;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Video Duration Limit: 120seconds
        takeVideoIntent.putExtra("android.intent.extra.durationLimit", 120);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();

            Log.d("UploadActivity", videoUri.toString());

            EditText heightEdit = (EditText) findViewById(R.id.height);
            EditText descEdit = (EditText) findViewById(R.id.description);

            String height = heightEdit.getText().toString().trim();
            String desc = descEdit.getText().toString().trim();

            Spinner spinner = (Spinner) findViewById(R.id.crowd_spinner);
            String crowed = spinner.getSelectedItem().toString();

            // Upload File to Server
            Upload(lat, lon, desc, height, crowed, videoUri);

            // Redirect to Main Activity
            Intent i = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(i);

            Toast.makeText(getApplicationContext(), "Sending your Goovy to another adventure", Toast.LENGTH_LONG).show();
        }

    }

    public void noGPS()
    {
        Log.d("UploadActivity", "No Location");
        upload.setEnabled(false);
        int disabledBackgroundColor = ContextCompat.getColor(getApplicationContext(), R.color.button_main_disabled_bg);
        upload.setBackgroundColor(disabledBackgroundColor);

        Toast.makeText(getApplicationContext(), "Please Enable Location to Upload", Toast.LENGTH_LONG).show();
    }

    public void Upload(double lat, double lon, String desc, String height, String crowed, Uri goovyUri) {
        // Initialize Params (headers)
        String key = session.getUserKey();

        Log.d("UploadActivity", "Key " + key);

        // Generate Base64
        String goovyPath = getRealPathFromURI(goovyUri);
        Log.d("UploadActivity", goovyPath);

        File goovyFile = new File(goovyPath);
        String base64Goovy = encodeFileToBase64Binary(goovyFile);

        UploadRequest uploadReq = new UploadRequest();
        uploadReq.init(lat, lon, desc, height, crowed, base64Goovy);

        mService.upload(key, uploadReq).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                if(response.isSuccessful()) {
                    Log.d("UploadActivity", "UPLOADED!");
                    Toast.makeText(getApplicationContext(), "Uploaded Done Well", Toast.LENGTH_SHORT).show();

                }else {
                    int statusCode = response.code();
                    Log.d("UploadActivity", "" + statusCode);
                    Log.d("UploadActivity", response.toString());
                    Toast.makeText(getApplicationContext(), "Couldn't Upload", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.d("UploadActivity", "error loading from API");
                Log.d("UploadActivity", t.getMessage());
                Toast.makeText(getApplicationContext(), "Couldn't Upload", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });


    }

    public boolean isDetailsFull() {
        EditText heightEdit = (EditText) findViewById(R.id.height);
        EditText descEdit = (EditText) findViewById(R.id.description);

        String height = heightEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();

        Spinner spinner = (Spinner) findViewById(R.id.crowd_spinner);
        String crowed = spinner.getSelectedItem().toString();

        if (!height.isEmpty() && !desc.isEmpty() && !crowed.isEmpty() && isCrowdSpinnerTriggered){
            return true;
        }
        return false;
    }

    /**
     * Method used for encode the file to base64 binary format
     * @param file
     * @return encoded file format
     */
    private String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.encodeToString(bytes, Base64.DEFAULT).toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedfile;
    }


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor  = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}

