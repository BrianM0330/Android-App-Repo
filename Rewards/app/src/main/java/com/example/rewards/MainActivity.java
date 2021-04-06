package com.example.rewards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    //TODO: IMPLEMENT COMPARABLES FOR ALL RECYCLERVIEWS
    //TODO: IMPLEMENT MISSING ALERTBUILDER + CONFIRMATIONS
    //TODO: ADD CHAR COUNTER TO TEXT BOXES
    //TODO: EDIT AND DELETE
    private static final String TAG = "LoginScreen";
    private static String API_KEY = "";
    private String locationString = "Placeholder Location";

    private static int MY_LOCATION_REQUEST_CODE_ID = 111;
    private static int CAMERA_REQUEST_CODE_ID = 222;

    private LocationManager locationManager;
    private Criteria criteria;

    CheckBox rememberCredentials;
    EditText usernameField;
    EditText passwordField;
    private RewardsSharedprefs pref;

    //Text View
    TextView clearKeyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearKeyView = findViewById(R.id.clearButton);
        getSupportActionBar().setTitle("Rewards");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        pref = new RewardsSharedprefs(this);
        setContentView(R.layout.activity_main);

        rememberCredentials = findViewById(R.id.checkBox2);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);

        if (!pref.getValue("savedUsername").equals("") && !pref.getValue("savedPassword").equals("")) {
            usernameField.setText(pref.getValue("savedUsername"));
            passwordField.setText(pref.getValue("savedPassword"));
            rememberCredentials.setChecked(true);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        /* <---------------- LOCATION STUFF -----------------------------------> */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION //request string
                    }, MY_LOCATION_REQUEST_CODE_ID); //
        }
        setLocation(); //Set location after getting permission (if wasn't granted)

        /* <---------------- LOCATION STUFF -----------------------------------> */

//        pref.clearPrefs();
        // <---------------- API KEY STUFF ----------------------------------->
        if (pref.getValue("API_KEY").equals("")) { //No API key. First run OR cleared via button
            requestAPIKey();
            Log.d(TAG, "NO KEY FOUND");
        }
        else {
            API_KEY = pref.getValue("API_KEY");
            Log.d(TAG, " KEY FOUND" + API_KEY);
            //Log in as usual.
        }
        System.out.println("HELLO" + pref.toString());
        // <---------------- API KEY STUFF ----------------------------------->
    }


    @SuppressLint("MissingPermission")
    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location currentLocation = null;
        if (bestProvider != null) {
            currentLocation = locationManager.getLastKnownLocation(bestProvider);
        }

        if (currentLocation != null) {
            Log.d(TAG, "CURRENT LOCATION: " + currentLocation);
            String locationString = processLocation(currentLocation);
        }
    }

    private String processLocation(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;

            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();

            addresses = geocoder.getFromLocation(latitude, longitude, 10);
            String state = addresses.get(0).getAdminArea();
            String city = addresses.get(0).getLocality();
            locationString = String.format("%s, %s", city, state);
        } catch (Exception e) { e.printStackTrace(); }

        return locationString;
    }

    private void requestAPIKey() {
        LayoutInflater inflater = LayoutInflater.from(this);

        final View view = inflater.inflate(R.layout.request_key_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need to request an API Key");
        builder.setTitle("API Key Needed");
        builder.setIcon(R.drawable.logo);

        builder.setView(view);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText firstName = view.findViewById(R.id.requestFirstname);
                EditText lastName = view.findViewById(R.id.requestLastName);
                EditText emailField = view.findViewById(R.id.requestEmail);
                EditText idfield = view.findViewById(R.id.requestID);

                //Get fields to validate
                String fname = firstName.getText().toString();
                String lname = lastName.getText().toString();
                String email = emailField.getText().toString();
                Integer id = 0;
                try { id = Integer.valueOf(idfield.getText().toString());}
                catch (NumberFormatException e) {id=0;} //If field is null

                //Make sure all the fields are valid before fetching API
                boolean validRequest = validateRequestFields(fname, lname, email, id, API_KEY);


                //IF fields are valid -> Fetch API via runnable
                if (validRequest) {
                    //Runnable -> request API key endpoint -> Create second alertDialog with the info
                    requestAPIHelper(fname, lname, email, id);
                    //Call function here -> Write API_KEY result to SharedPrefs
                    //Use sharedPrefs Key to send to SuccessfulAPIRequest
                }
                else {
                    requestAPIKey(); //Show the dialog again
                    Log.d(TAG, "BAD REQUEST");
                }

                //Request key
                Log.d(TAG, "NOW REQUESTING API KEY");
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Request key
                Log.d(TAG, "CANCELLED API REQUEST");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Use this to call the runnable to fetch the API key
    private void requestAPIHelper(String fname, String lname, String email, Integer id) {
        createAPIKeyRunnable getKey = new createAPIKeyRunnable(this, fname, lname, email, id);
        new Thread(getKey).start(); //FALLS BACK TO SuccessfulAPIRequest()
    }

    //Validates API Key Request parameters before sending GET request
    private boolean validateRequestFields(String fname, String lname, String email, Integer ID, String API_KEY) {
        return (
            (!fname.equals("") && !lname.equals("") && !email.equals("") && ID.toString().length() == 7)
            &&
            email.contains(".edu")
        );
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.d("checkInternetConnection", "Cannot access connectivitymanager");
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        else
            return false;
    }

    //<-------------- TextView OnClick Methods ------------------>

    public void createProfile(View v) {
        Intent politicianPageIntent = new Intent(this, CreateProfile.class);
        politicianPageIntent.putExtra("API_KEY", API_KEY);
        politicianPageIntent.putExtra("location", locationString);
        startActivity(politicianPageIntent);
    }

    public void login(View v) {
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        String uname = username.getText().toString();
        String pword = password.getText().toString();

        if (!uname.equals("") && !pword.equals("")) { //Non-null - Valid login request
            LoginAPIRunnable runnable = new LoginAPIRunnable(this, uname, pword, API_KEY);
            new Thread(runnable).start();
        }

        else Toast.makeText(this, "Inusername or password. Please try again", Toast.LENGTH_SHORT).show();
    }

    public void clearKey(View v) {
        pref.clearPrefs();
        finish();
        Log.d(TAG, "Cleared API key");
    }

//<----------------------------------------------- RUNNABLE CALLBACK FUNCTIONS ---------------------->
    //Creates AlertDialog after a successful API Key Request
    //Called FROM runnable AFTER successful GET
    public void SuccessfulAPIRequest(String fname, String lname, String email, Integer id, String key) {
        Log.d(TAG, "API Key successfully fetched:" + key);
        pref.save("API_KEY", key);
        MainActivity.API_KEY = key;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("API Key Received and Stored");
        builder.setMessage(String.format("Name:\t%s %s\nStudent ID:\t%s\nEmail:\t%s\nAPI Key: \t%s", fname, lname, id, email, key));
        builder.setIcon(R.drawable.logo);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void SuccessfulLoginRequest(JSONObject response) throws JSONException {
        //TODO: DO SOMETHING WITH REMEMBER CREDENTIALS BOX

        if (rememberCredentials.isChecked()) {
            pref.save("savedUsername", usernameField.getText().toString());
            pref.save("savedPassword", passwordField.getText().toString());
            Log.d(TAG, "uname: " + pref.getValue("savedUsername") + "pword: " + pref.getValue("savedPassword"));
        }
        else {
            pref.deleteKey("savedUsername");
            pref.deleteKey("savedPassword");
        }

        int pointsAwarded = 0;
        JSONArray giftsGiven = response.getJSONArray("rewardRecordViews");
        for (int i=0; i < giftsGiven.length(); i++) {
            JSONObject rewardObject = giftsGiven.getJSONObject(i);
            pointsAwarded += rewardObject.getInt("amount");
        }

        Intent profileViewIntent = new Intent(this, ViewProfile.class);
        profileViewIntent
                .putExtra("fname",
                        String.format("%s %s", response.getString("firstName"), response.getString("lastName")));
        profileViewIntent.putExtra("uname", response.getString("userName"));
        profileViewIntent.putExtra("department", response.getString("department"));
        profileViewIntent.putExtra("location", response.getString("location"));
        profileViewIntent.putExtra("password", response.getString("password"));
        profileViewIntent.putExtra("story", response.getString("story"));
        profileViewIntent.putExtra("profilePic", response.getString("imageBytes"));
        profileViewIntent.putExtra("pointsAwarded", Integer.toString(pointsAwarded));
        profileViewIntent.putExtra("position", response.getString("position"));
        profileViewIntent.putExtra("pointsToAward", response.getInt("remainingPointsToAward"));
        profileViewIntent.putExtra("rewards", response.getJSONArray("rewardRecordViews").toString());
        profileViewIntent.putExtra("API_KEY", API_KEY);
        startActivityForResult(profileViewIntent, 1);
    }

    public void catchHTMLCodes(int code) {
        if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
            Toast.makeText(this, "The given username and/or password are invalid.", Toast.LENGTH_SHORT).show();
        }
        else if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Toast.makeText(this, "Could not find a user with that username and password combination.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) { //Deleted profile resultcode
            if (!pref.getValue("savedUsername").equals("") && !pref.getValue("savedPassword").equals("")) { //if credentials are saved
                //Clear up the saved credentials since the profile is deleted
                pref.deleteKey("savedUsername");
                pref.deleteKey("savedPassword");
                Log.d(TAG, "uname: " + pref.getValue("savedUsername") + "pword: " + pref.getValue("savedPassword"));
            }
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!rememberCredentials.isChecked()) {
            usernameField.setText("");
            passwordField.setText("");
        }
    }
}