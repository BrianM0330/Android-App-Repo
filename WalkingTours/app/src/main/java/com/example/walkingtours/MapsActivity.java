package com.example.walkingtours;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.walkingtours.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityMapsBinding binding;
    private NotificationManager notificationManager;
    private static final int LOCATION_REQUEST = 222;
    private static final int ACCURACY_REQUEST = 222;


    private List<LatLng> latLonHistory = new ArrayList<>();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Polyline llHistoryPolyline;
    private Polyline tourPolyline;

    private Marker personMarker;
    private boolean zooming = false;
    private float oldZoom;

    private FenceManager fenceManager;

    public static int screenHeight;
    public static int screenWidth;

    private Geocoder geocoder;
    private TextView currentAddress;
    private CheckBox showAddress;
    private CheckBox showGeofences;
    private CheckBox showTravelPath;
    private CheckBox showTourPath;
    private boolean travelPathVisible = true;
    private  boolean tourPathVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO - Do the buttons. polyline.setVisible(false)
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Typeface acmeFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        currentAddress = findViewById(R.id.currentAddress);
            currentAddress.setTypeface(acmeFont);

        showAddress = findViewById(R.id.addressToggle);
            showAddress.setTypeface(acmeFont);

        showGeofences = findViewById(R.id.geofenceToggle);
            showGeofences.setTypeface(acmeFont);

        showTravelPath = findViewById(R.id.travelPathToggle);
            showTravelPath.setTypeface(acmeFont);

        showTourPath = findViewById(R.id.tourPathToggle);
            showTourPath.setTypeface(acmeFont);

        geocoder = new Geocoder(this, Locale.getDefault());

        setupCheckBoxListeners();

        getScreenDimensions();

        checkLocationAccuracy();

//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this);
    }


//<--------------------------------------------- MAPS AND LOCATION STUFF ----------------------------------------->
    /**
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the
     * user will be prompted to install it inside the SupportMapFragment.
     * This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        mMap.getUiSettings().setRotateGesturesEnabled(false);

        determineLocation(); //set the initial location to the user's
        setupLocationListener(); //set listener for tracking user location
        setupZoomListener(); //set zoom listener for handling zoom+movement
        //TODO: set up fences
    }

    //called on success -> checkLocationAccuracy()
    public void initMap() {

        fenceManager = new FenceManager(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @SuppressLint("MissingPermission") //suppressing because this was checked on Splash
    private void determineLocation() {
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Add a marker at current location
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(origin).title("My Origin"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17.0f));
                }
            })
            .addOnFailureListener(
                    e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @SuppressLint("MissingPermission")
    private void setupLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocListener(this);

        if (locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            //time interval, meter threshold, locationListener to receive the updates
    }

    //we set this up just so the user can zoom in and out.
    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(() -> {
            if (zooming) {
                Log.d("MapsActivity", "onCameraIdle: DONE ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = false;
                oldZoom = mMap.getCameraPosition().zoom;
            }
        });

        //when the camera is being moved around
        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().zoom != oldZoom) {
                Log.d("MapsActivity", "onCameraMove: ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = true;
            }
        });
    }

    /*
        Store the LatLng history for each point the user has gone through

        Redraw the polyline (can't extend them, can only re-draw them)

        If it's the first LatLng object added to the history -> addMarker to origin point, move camera

        Any point after the first ->
            Redraw the polyline by going for each LatLng object and adding it to the polyLine
            Then set the polyLine properties
     */
    public void updateLocation(Location location) {


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        //uses Geocoder to set address on every new location
        parseAddress(location);


        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.7f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }

            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(12);
            llHistoryPolyline.setColor(Color.parseColor("#00713C"));

            //continue to update polyline, but hide based on checkbox
            llHistoryPolyline.setVisible(travelPathVisible);

            float r = getRadius();
            if (r > 0) {
                Bitmap icon;
                if (location.getBearing() < 180)
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                else
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);

                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);

                if (personMarker != null) //if the marker is already on the map
                    personMarker.remove();

                personMarker = mMap.addMarker(options);
            }

        }

        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

    }

    //used for drawing the map cursor appropriately
    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        return  factor * multiplier;
    }

    private void parseAddress(Location coordinates) {
        new Thread(new GeocoderRunnable(this, coordinates, geocoder)).start();
    }

    public void setAddressToText(String address) {
        currentAddress.setText(address);
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    protected void onDestroy() {
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        setupLocationListener();
                        setupZoomListener();
                    } else {
                        Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    /*
    checkLocation() and onActivity(0 are used primarily to make sure the user has high accuracy location on.
     */

    private void checkLocationAccuracy() {
        Log.d("MapsActivity", "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d("MapsActivity", "onSuccess: High Accuracy Already Present");
            initMap();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d("MapsActivity", "onActivityResult: ");
            initMap();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public void addTourPoly(String response) {
        if (response != null)
        try {
            PolylineOptions polylineOptions = new PolylineOptions();

            JSONArray pathObject = new JSONObject(response).getJSONArray("path");
            for (int i=0; i < pathObject.length(); i++) {
                String pathVertexFlipped = pathObject.getString(i);

                //Fix the flipped vertex coordinates
                String fixedLatitude = pathVertexFlipped.substring(pathVertexFlipped.indexOf(" ")).trim();
                String fixedLongitude = pathVertexFlipped.substring(0, pathVertexFlipped.indexOf(",")).trim();

                LatLng coords = new LatLng(Double.parseDouble(fixedLatitude), Double.parseDouble(fixedLongitude));

                //add the fixed coordinates to the polyline, no need to create additional list
                polylineOptions.add(coords);
            }

            //Finished adding coordinates, now set up the polyLine
            tourPolyline = mMap.addPolyline(polylineOptions);
            tourPolyline.setEndCap(new RoundCap());
            tourPolyline.setWidth(12);
            tourPolyline.setColor(Color.YELLOW);
        }

        catch (JSONException e) {
            e.printStackTrace();
        }
    }


//<--------------------------------------------- MAPS AND LOCATION STUFF ----------------------------------------->


//<-----------------------------------UI stuff------------------------------------------->
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void setupCheckBoxListeners() {

        showAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (showAddress.isChecked())
                    currentAddress.setVisibility(View.VISIBLE);
                else
                    currentAddress.setVisibility(View.INVISIBLE); //TODO -> Experiment with View.GONE
            }
        });

        showGeofences.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (showGeofences.isChecked())
                    fenceManager.drawFences();
                else
                    fenceManager.eraseFences();
            }
        });

        showTravelPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (llHistoryPolyline != null) {
                    travelPathVisible = showTravelPath.isChecked();
                    llHistoryPolyline.setVisible(showTravelPath.isChecked());
                }
            }
        });

        showTourPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (tourPolyline != null) {
                    tourPathVisible = showTourPath.isChecked();
                    tourPolyline.setVisible(showTourPath.isChecked());
                }
            }
        });
    }
}