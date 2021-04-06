package com.example.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PhotoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        TextView name = findViewById(R.id.photoPageName);
        TextView position = findViewById(R.id.photoPagePosition);
        TextView location = findViewById(R.id.photoPageLocation);

        ImageView portrait = findViewById(R.id.photoPagePortrait);
        ImageView logo = findViewById(R.id.photoPageLogo);

        Intent intentReceived = getIntent();
        name.setText(intentReceived.getStringExtra("name"));
        position.setText(intentReceived.getStringExtra("position"));
        location.setText(intentReceived.getStringExtra("location"));

        if (intentReceived.getStringExtra("party").contains("Democrat")) {
            findViewById(R.id.background).setBackgroundColor(Color.parseColor("#34AAE0"));
            logo.setImageResource(R.drawable.dem_logo);
        }
        else if (intentReceived.getStringExtra("party").contains("Republican")) {
            findViewById(R.id.background).setBackgroundColor(Color.parseColor("#DF0100"));
            logo.setImageResource(R.drawable.rep_logo);
        }
        else {
            findViewById(R.id.background).setBackgroundColor(Color.parseColor("#000000"));
            logo.setVisibility(View.INVISIBLE);
        }

        Picasso.get().load(intentReceived.getStringExtra("url"))
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.placeholder)
                .into(portrait);
    }
}