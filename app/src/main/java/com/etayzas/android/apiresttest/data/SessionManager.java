package com.etayzas.android.apiresttest.data;

/**
 * Created by Ewise on 06/01/2018.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.etayzas.android.apiresttest.LoginActivity;
import com.etayzas.android.apiresttest.MainActivity;
import com.etayzas.android.apiresttest.data.model.beach.Beach;
import com.etayzas.android.apiresttest.data.model.beach.BeachesResponse;
import com.etayzas.android.apiresttest.data.remote.ApiUtils;
import com.etayzas.android.apiresttest.data.remote.SOService;
import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    ObjectMapper jsonMapper;

    // Context
    Context _context;

    private SOService mService;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static List<Beach> beachesList;

    // Sharedpref file name
    private static final String PREF_NAME = "GoodVibePref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // Api Key (make variable private to disallow access from outside)
    private static final String KEY_API = "key";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    private static String USER_BEACHES_JSON = "[bla]";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        mService = ApiUtils.getSOService();
        jsonMapper = new ObjectMapper();
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String email, String key){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing key in pref
        editor.putString(KEY_API, key);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing beachesJson in pref
        editor.putString(USER_BEACHES_JSON, "[none]");

        Log.d("SessionManager", "beachesList | creator -> " + userBeaches(key).size());


        /*
        if(beachesList != null){
            Log.d("MainActivity", "beachesRes != null");
            setUserBeaches(beachesList);
        }else{
            Log.d("MainActivity", "beachesRes else");
            List<Beach> beaches = new ArrayList<>();
            setUserBeaches(beaches);
        }
        */

        /*
        List<Beach> beaches = new ArrayList<>();
        String beachesJson;
        try{
            beachesJson = jsonMapper.writeValueAsString(beaches);
        }catch(JsonProcessingException e){
            beachesJson = "";
        }

        // Storing beachesJson in pref
        editor.putString(USER_BEACHES_JSON, beachesJson);
        */

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    /**
     * Get stored KEY_API
     * */
    public String getUserKey(){
        // return key
        return pref.getString(KEY_API, null);
    }

    /**
     * Get stored USER_BEACHES_JSON
     * */
    public List<Beach> getUserBeaches(){
        List<Beach> beaches;

        try{
            TypeReference<List<Beach>> mapType = new TypeReference<List<Beach>>() {};
            String beachesJson = pref.getString(USER_BEACHES_JSON, null);
            Log.d("SessionManager", "beachesJson | get -> " + beachesJson);
            beaches = jsonMapper.readValue(beachesJson, mapType);
        }catch (IOException e){
            Log.d("SessionManager", "beaches null");
            beaches = null;
        }

        return beaches;
    }

    /**
     * Set stored USER_BEACHES_JSON
     * */
    public boolean setUserBeaches(List<Beach> beaches){
        String beachesJson;
        try{
            beachesJson = jsonMapper.writeValueAsString(beaches);
            editor.putString(USER_BEACHES_JSON, beachesJson);
            Log.d("SessionManager", "beachesJson | set -> " + beachesJson);
            String getBeachesJSON = pref.getString(USER_BEACHES_JSON, null);
            Log.d("SessionManager", "getBeachesJSON | set get -> " + getBeachesJSON);
            return true;
        }catch(JsonProcessingException e){
            beachesJson = "[]";
            editor.putString(USER_BEACHES_JSON, beachesJson);
            Log.d("SessionManager", "beachesJson | set failed -> " + beachesJson);
            return false;
        }
    }

    /**
     * Add beach to stored USER_BEACHES_JSON
     * */
    public boolean addBeach(Beach beach){
        List<Beach> beaches = getUserBeaches();
        if (beaches != null){
            beaches.add(beach);
            setUserBeaches(beaches);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user key
        user.put(KEY_API, pref.getString(KEY_API, null));

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     * **/
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    
    
    private List<Beach> userBeaches(String key) {

        mService.getUserBeaches(key).enqueue(new Callback<BeachesResponse>() {
            @Override
            public void onResponse(Call<BeachesResponse> call, Response<BeachesResponse> response) {

                if(response.isSuccessful()) {
                    Log.d("SessionManager", "got beaches for user");
                    // store beaches

                    beachesList = response.body().getBeaches();

                    Log.d("SessionManager", "how many beaches? -> " + beachesList.size());

                    if(beachesList.size() > 0){
                        // Got Beaches
                        for (Beach beach: beachesList) {
                            Log.d("SessionManager", beach.getName());
                        }

                        Log.d("SessionManager", "beachesRes != null");
                        Log.d("SessionManager", "Set Beaches -> " + setUserBeaches(beachesList));

                    }else{
                        // No Beaches
                        Log.d("SessionManager", "no beaches");

                        Log.d("SessionManager", "beachesRes else");
                        List<Beach> beaches = new ArrayList<>();
                        Log.d("SessionManager", "Set Beaches -> " + setUserBeaches(beaches));
                    }

                }else {
                    int statusCode = response.code();
                    Log.d("SessionManager", "" + statusCode);
                    Log.d("SessionManager", response.toString());
                    //beachesList = null;
                }
            }

            @Override
            public void onFailure(Call<BeachesResponse> call, Throwable t) {
                //beachesList = null;
                Log.d("SessionManager", "error loading from API");
                Log.d("SessionManager", t.getMessage());
                t.printStackTrace();
            }
        });

        return beachesList;
    }
}