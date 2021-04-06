package com.example.knowyourgovernment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //activity variables
    private final List<Politician> politicianList = new ArrayList<>();
    private String locationString;

    //location service stuff
    private static int MY_LOCATION_REQUEST_CODE_ID = 111;
    private LocationManager locationManager;
    private Criteria criteria;

    //recyclerview stuff
    private RecyclerView recyclerView;
    private PoliticianAdapter politicianAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: ADD SEPARATOR IMAGE TO RECYCLER VIEWS
        //TODO: SOCIAL MEDIA - IMPLIED INTENTS??
        //TODO: Get ZIP from geocode
        //TODO: ABOUT activity
        //TODO: Create runnable for API calls
        //LOOK AT STOCKWATCH FOR EXAMPLE ON DIALOGS
        if (checkInternetConnection()) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            recyclerView = findViewById(R.id.recycler);
            politicianAdapter = new PoliticianAdapter(politicianList, this);
            recyclerView.setAdapter(politicianAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            //Location stuff
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);

            //Check for user Location permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION //request string
                        }, MY_LOCATION_REQUEST_CODE_ID); //
            } else
                setLocation(0);
        }
        else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No connection found!");
            builder.setMessage("Data cannot be accessed or loaded without an internet connection. Please review your connection and restart the app.");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @SuppressLint("MissingPermission")
    private void setLocation(int typeToRun) {
        if (typeToRun == 0) {
            String bestProvider = locationManager.getBestProvider(criteria, true);

            Location currentLocation = null;
            if (bestProvider != null) { //only null if location services disabled
                currentLocation = locationManager.getLastKnownLocation(bestProvider);
            }

            if (currentLocation != null) { //only after it is set
                int zipToSend = processLatLong(currentLocation);
                civicRunnable fetchPoliticians = new civicRunnable(this, zipToSend);
                new Thread(fetchPoliticians).start();

            } else {
                ((TextView) findViewById(R.id.mainLocationBox)).setText("AAAAHHH");
            }
        }
        else {
            politicianList.clear();
            //This is run when the user manually updates their location (sent from menu options function
            //Just use option menu and parse the info w Geocoder
            try { //if it's a zip..
                int zipToSend = Integer.parseInt(locationString);
                civicRunnable fetchPoliticians = new civicRunnable(this, zipToSend);
                new Thread(fetchPoliticians).start();
            }
            catch (NumberFormatException e) { //must be a city name.. parse!
                civicRunnable fetchPoliticians = new civicRunnable(this, locationString);
                new Thread(fetchPoliticians).start();
            }
        };
    }

    private int processLatLong(Location loc) {
        int toRet = 0;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;

            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();

            addresses = geocoder.getFromLocation(latitude, longitude, 10);
            toRet = Integer.parseInt(addresses.get(0).getPostalCode());
        } catch (Exception e) { e.printStackTrace(); }
        return toRet;
    }

    public void updatePoliticianList(List<Politician> politicianReceived) {
        politicianList.addAll(politicianReceived);
        politicianAdapter.notifyDataSetChanged();
    }

    public void updateLocation(JSONObject normalizedInput) {
        //Updated from Runnable
        try {
            TextView locationDetails = (TextView) findViewById(R.id.mainLocationBox);
            locationString = normalizedInput.getString("city") + ", "
                    + normalizedInput.getString("state") + " "
                    + normalizedInput.getString("zip");

            locationDetails.setText(locationString);
        }
        catch (Exception ignored) {};
    }

    @Override
    //Called when user acts on permission request
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, //Arrays of requests + granted permissions
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_LOCATION_REQUEST_CODE_ID) { //if the request is for location request
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PERMISSION_GRANTED) {
                setLocation(0);
                return;
            }
        }
        ((TextView) findViewById(R.id.mainLocationBox)).setText("NO PERMISSION!!! INSIDE onREQPerm");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutButton:
                Intent aboutIntent = new Intent(this, aboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.searchButton:
                if (checkInternetConnection()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    final EditText et = new EditText(this);
                    et.setInputType(InputType.TYPE_CLASS_TEXT);
                    et.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(et);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationString = et.getText().toString();
                            setLocation(1);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setMessage("Enter any state, city, or zip code");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("No connection found!");
                    builder.setMessage("Data cannot be accessed or loaded without an internet connection. Please review your connection and restart the app.");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;    
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Politician toSend = politicianList.get(pos);
        //TODO: FIX API fetching Twitter and Youtube URLs
        Intent politicianPageIntent = new Intent(this, PoliticianDetailed.class);
        politicianPageIntent.putExtra("locationString", locationString);
        politicianPageIntent.putExtra("politicianPosition", toSend.getOfficePosition());
        politicianPageIntent.putExtra("politicianName", toSend.getName());
        politicianPageIntent.putExtra("politicianParty", toSend.getParty());
        politicianPageIntent.putExtra("politicianAddress", toSend.getAddress());
        politicianPageIntent.putExtra("politicianWebsite", toSend.getWebsite());
        politicianPageIntent.putExtra("politicianImageURL", toSend.getImageURL());
        politicianPageIntent.putExtra("politicianPhone", toSend.getPhone());
        politicianPageIntent.putExtra("politicianFB", toSend.getFacebookID());
        politicianPageIntent.putExtra("politicianTwitter", toSend.getTwitterID());
        politicianPageIntent.putExtra("politicianYoutube", toSend.getYoutubeID());

        startActivity(politicianPageIntent);
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
}