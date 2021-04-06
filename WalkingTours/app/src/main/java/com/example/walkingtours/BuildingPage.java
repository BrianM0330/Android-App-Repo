package com.example.walkingtours;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class BuildingPage extends AppCompatActivity {

    Intent fromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_page);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fromNotification = getIntent();

        Typeface acmeFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        TextView name = findViewById(R.id.buildingName);
        ImageView image = findViewById(R.id.buildingImage);
        TextView address = findViewById(R.id.buildingAddress);
        TextView description = findViewById(R.id.buildingDescription);

        name.setText(fromNotification.getStringExtra("name"));
        name.setTypeface(acmeFont);

        address.setText(fromNotification.getStringExtra("address"));
        address.setTypeface(acmeFont);

        description.setText(fromNotification.getStringExtra("description"));
        description.setTypeface(acmeFont);
        description.setMovementMethod(new ScrollingMovementMethod());

        String imageURL = fromNotification.getStringExtra("imageURL");

        Picasso.get()
            .load(imageURL)
            .into(image);
    }
}