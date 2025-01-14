package com.etayzas.android.apiresttest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.etayzas.android.apiresttest.data.SessionManager;
import com.etayzas.android.apiresttest.data.model.Goovy;
import com.etayzas.android.apiresttest.data.model.GooviesResponse;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;
import com.etayzas.android.apiresttest.helpers.Beach;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowActivity extends AppCompatActivity {

    // Declare variables
    private ProgressDialog pDialog;
    private VideoView videoview;
    private SOService mService;
    private SessionManager session;
    private Button next;
    private Button prev;
    private List<Goovy> goovies;
    private int goovyCounter = 0;
    private String key;

    private TextView desc;
    private TextView height;
    private TextView crowd;
    private TextView time;
    private TextView beach;
    private String currentUrl;
    private Button share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if is logged in. if not -> Turn him out!
        if(!session.isLoggedIn())
        {
            Intent intent = new Intent(ShowActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {

            next = (Button) findViewById(R.id.next_button);
            prev = (Button) findViewById(R.id.prev_button);
            videoview = (VideoView) findViewById(R.id.VideoView);

            height = (TextView) findViewById(R.id.height_textView);
            crowd = (TextView) findViewById(R.id.crowd_textView);
            desc = (TextView) findViewById(R.id.desc_textView);
            time = (TextView) findViewById(R.id.time_textView);
            beach = (TextView) findViewById(R.id.beach_textView);
            share = (Button) findViewById(R.id.share_button);

            // Log User Details
            Log.d("ShowActivity", session.getUserDetails().get("name"));
            Log.d("ShowActivity", session.getUserDetails().get("email"));
            Log.d("ShowActivity", session.getUserDetails().get("key"));

            // Share Goovy
            share.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Log.d("ShowActivity", "SHARE");

                    //sharing implementation here
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Wave Mate");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharingString());
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            // Next Goovy
            next.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Log.d("ShowActivity", "NEXT");

                    if (goovies == null) {
                        getGoovies(key);
                        Log.d("ShowActivity", "no goovies");
                    }
                    nextGoovy();
                    Log.d("ShowActivity", "show more");
                }
            });

            // Previous Goovy
            prev.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Log.d("ShowActivity", "PREV");

                    if (goovies == null) {
                        getGoovies(key);
                        Log.d("ShowActivity", "no goovies");
                    }
                    prevGoovy();
                    Log.d("ShowActivity", "show more");
                }
            });


            // PLAY
            mService = ApiUtils.getSOService();
            key = session.getUserKey();

            // successful Getting Plays first Automatically
            getGoovies(key);
        }
    }

    public void nextGoovy(){
        goovyCounter++;

        if(goovyCounter >= goovies.size()){
            goovyCounter = 0;
        }

        showCurrentGoovy();
    }

    public void prevGoovy(){
        goovyCounter -= 1;

        if(goovyCounter < 0 ){
            goovyCounter = goovies.size() - 1;
        }

        showCurrentGoovy();
    }

    public void showCurrentGoovy() {
        Goovy currentGoovy = goovies.get(goovyCounter);
        currentUrl = currentGoovy.getUrl();

        desc.setText(currentGoovy.getDescription());
        crowd.setText(currentGoovy.getCrowed());
        height.setText(currentGoovy.getHeight());
        time.setText(currentGoovy.getCreatedAt());

        double currentLat = currentGoovy.getLat();
        double currentLon = currentGoovy.getLon();

        String currentBeach = "";
        if (currentLat > 0 && currentLon > 0)
        {
            currentBeach = beachFromGPS(currentLat, currentLon);
        }

        beach.setText(currentBeach);


        // Execute StreamVideo AsyncTask
        // Create a progressbar
        pDialog = new ProgressDialog(ShowActivity.this);
        // Set progressbar title
        pDialog.setTitle("Goovy Update on the Way");
        // Set progressbar message
        pDialog.setMessage("Waiting for a Wave...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        // Show progressbar
        pDialog.show();

        try {
            Log.d("ShowActivity", "try video show");
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
                    ShowActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(currentUrl);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoview.requestFocus();
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                mp.setLooping(true);
                videoview.start();
            }
        });
    }

    public String sharingString()
    {
        return "Surfing Report From WaveMate. \n\n" + currentUrl + "\n\nDownload the App Now - http://goodvibesurfing.me";
    }

    public String beachFromGPS(double currentLat, double currentLon){
        //double accuracy = 0.001;

        // Can't be Bigger...
        double smallestDistance = 10000000000000.000;
        Beach nearestBeach = new Beach(-1, -1, "");

        List<Beach> beaches = new ArrayList();

        Beach Zvulun = new Beach(32.17501, 34.80066, "Zvulun");
        Beach Marina = new Beach(32.16531, 34.79744, "Marina");
        Beach Dromi = new Beach(32.15718, 34.79506, "Dromi");
        Beach HaSharon = new Beach(32.18118, 34.80282, "HaSharon");
        Beach nowhere = new Beach(3, 3, "nowhere");


        beaches.add(Zvulun);
        beaches.add(Marina);
        beaches.add(Dromi);
        beaches.add(HaSharon);
        beaches.add(nowhere);

        for (Beach beach : beaches) {

            double distance = Distance(beach.getLat(), beach.getLon(), currentLat, currentLon, "Kilometers");
            Log.d("Beaches", beach.getName() + " distance " + distance);

            if(distance < smallestDistance)
            {
                smallestDistance = distance;
                nearestBeach = beach;
            }
        }

        return nearestBeach.getName();
    }

    public double Positive(double num)
    {
        if(num < 0)
        {
            return num * -1;
        }
        else
        {
            return num;
        }
    }

    public double Square(double num)
    {
        return num * num;
    }

    // Initiates List<Goovy> goovies with the GOOVIES.
    public void getGoovies(String key) {
        mService.play(key).enqueue(new Callback<GooviesResponse>() {
            @Override
            public void onResponse(Call<GooviesResponse> call, Response<GooviesResponse> response) {

                if(response.isSuccessful()) {
                    Log.d("PlayActivity", "GotIt!");
                    GooviesResponse playList = response.body();
                    if (!playList.getError()) {
                        goovies = playList.getGoovies();
                        showCurrentGoovy();
                    }
                }else {
                    int statusCode = response.code();
                    Log.d("PlayActivity", "" + statusCode);
                    Log.d("PlayActivity", response.toString());
                }
            }

            @Override
            public void onFailure(Call<GooviesResponse> call, Throwable t) {
                Log.d("PlayActivity", "error loading from API");
                Log.d("PlayActivity", t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static double Distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "Kilometers") {
            dist = dist * 1.609344;
        } else if (unit == "Nautical Miles") {
            dist = dist * 0.8684;
        } else if(unit == "Meters") {
            dist = dist * 1.609344;
            dist = dist * 1000;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
