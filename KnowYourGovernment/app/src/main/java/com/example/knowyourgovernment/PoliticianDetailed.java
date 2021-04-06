package com.example.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PoliticianDetailed extends AppCompatActivity {
    String twitterID = "";
    String fbID = "";
    String ytID = "";

    String name;
    String position;
    String location;
    String party;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.politician_page);
        TextView locationText = findViewById(R.id.politician_page_location);
        TextView positionText = findViewById(R.id.politician_page_position);
        TextView nameText = findViewById(R.id.politician_page_name);
        TextView partyText = findViewById(R.id.politician_page_party);

        final ImageButton portrait = findViewById(R.id.politician_page_portrait);
        ImageView partylogo = findViewById(R.id.politician_page_logo);

        TextView address = findViewById(R.id.politician_page_address);
        TextView website = findViewById(R.id.politician_page_website);
        TextView phone = findViewById(R.id.politician_page_phone);

        ImageButton facebookButton = findViewById(R.id.fbButton);

        ImageButton twitterButton = findViewById(R.id.twitterButton);

        ImageButton youtubeButton = findViewById(R.id.youtubeButton);

        Intent intentReceived = getIntent();

        locationText.setText(intentReceived.getStringExtra("locationString"));
        positionText.setText(intentReceived.getStringExtra("politicianPosition"));
        nameText.setText(intentReceived.getStringExtra("politicianName"));
        partyText.setText(intentReceived.getStringExtra("politicianParty"));


        address.setText(intentReceived.getStringExtra("politicianAddress"));
        website.setText(intentReceived.getStringExtra("politicianWebsite"));
        phone.setText(intentReceived.getStringExtra("politicianPhone"));

        if (intentReceived.getStringExtra("politicianAddress").length() > 0)
            Linkify.addLinks(address, Linkify.ALL);
        if (intentReceived.getStringExtra("politicianPhone").length() > 0)
            Linkify.addLinks(phone, Linkify.ALL);
        if (intentReceived.getStringExtra("politicianWebsite").length() > 0)
            Linkify.addLinks(website, Linkify.ALL);
        address.setLinkTextColor(Color.WHITE);
        phone.setLinkTextColor(Color.WHITE);
        website.setLinkTextColor(Color.WHITE);

        if (intentReceived.getStringExtra("politicianTwitter").length() == 0)
            twitterButton.setVisibility(View.INVISIBLE);
        else
            this.twitterID = intentReceived.getStringExtra("politicianTwitter");

        if (intentReceived.getStringExtra("politicianFB").length() == 0)
            facebookButton.setVisibility(View.INVISIBLE);
        else
            this.fbID = intentReceived.getStringExtra("politicianFB");

        if (intentReceived.getStringExtra("politicianYoutube").length() == 0)
            youtubeButton.setVisibility(View.INVISIBLE);
        else
            this.ytID = intentReceived.getStringExtra("politicianYoutube");

        if (intentReceived.getStringExtra("politicianParty").contains("Democrat")) {
            partylogo.setImageResource(R.drawable.dem_logo);
            findViewById(R.id.politician_page_background).setBackgroundColor(Color.parseColor("#34AAE0"));
        }
        else if (intentReceived.getStringExtra("politicianParty").contains("Republican")) {
            partylogo.setImageResource(R.drawable.rep_logo);
            findViewById(R.id.politician_page_background).setBackgroundColor(Color.parseColor("#DF0100"));
        }
        else {
            findViewById(R.id.politician_page_background).setBackgroundColor(Color.parseColor("#000000"));
            partylogo.setVisibility(View.INVISIBLE);
        }

        name = intentReceived.getStringExtra("politicianName");
        position = intentReceived.getStringExtra("politicianPosition");
        location = intentReceived.getStringExtra("locationString");
        party = intentReceived.getStringExtra("politicianParty");
        url = intentReceived.getStringExtra("politicianImageURL");


        final long start = System.currentTimeMillis();

        if (checkInternetConnection()) {
            if (intentReceived.getStringExtra("politicianImageURL").length() > 0) {
                Picasso.get().load(intentReceived.getStringExtra("politicianImageURL"))
                        .error(R.drawable.brokenimage)
                        .placeholder(R.drawable.placeholder)
                        .into(portrait,
                                new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("PortraitDownload", "onSuccess: Size: " +
                                                ((BitmapDrawable) portrait.getDrawable()).getBitmap().getByteCount());
                                        long dur = System.currentTimeMillis() - start;
                                        Log.d("PortraitDownload", "onSuccess: Time: " + dur);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.d("PortraitDownload", "onError: " + e.getMessage());
                                    }
                                });

                portrait.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        portraitClicked(v);
                    }
                });
                //TODO: Add onclick to separate page on politician. only if URL exists (So in this block)
            } else {
                portrait.setImageResource(R.drawable.missing);
            }
        }
        else {
            portrait.setImageResource(R.drawable.brokenimage);
        }
    }

    public void portraitClicked(View v) {
        Intent portraitIntent = new Intent(this, PhotoDetailActivity.class);

        portraitIntent.putExtra("name", name);
        portraitIntent.putExtra("position", position);
        portraitIntent.putExtra("location", location);
        portraitIntent.putExtra("party", party);
        portraitIntent.putExtra("url", url);

        startActivity(portraitIntent);
    }


    public void TwitterClicked(View v) {
        Intent intent = null;
        String name = this.twitterID;
        try {
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent (Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + name));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + name));
        }
        startActivity(intent);
    }

    public void FacebookClicked(View v) {
        String URLToUse = "https://www.facebook.com/%s";
        String URLForIntent = "";

        PackageManager packageManager = getPackageManager();

        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) {
                URLForIntent = String.format("fb://facewebmodal/f?href=%s", URLToUse);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            URLForIntent = String.format(URLToUse, fbID);
        }

        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(URLForIntent));
        startActivity(facebookIntent);
    }

    public void YoutubeClicked(View v) {
        String name = this.ytID;
        Intent youtubeIntent = null;

        try {
            youtubeIntent = new Intent(Intent.ACTION_VIEW);
            youtubeIntent.setPackage("com.google.android.youtube");
            youtubeIntent.setData(Uri.parse("https://www.youtube.com/" + name));
            startActivity(youtubeIntent);
        } catch (ActivityNotFoundException e) {startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://www.youtube.com/" + name)));}
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