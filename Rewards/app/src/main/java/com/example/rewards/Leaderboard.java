package com.example.rewards;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboard extends AppCompatActivity implements View.OnClickListener {
    private final List<Profile> profileList = new ArrayList<>();

    private int profilePosition;

    private String originalName;
    private String originalUsername;
    private String pointsAvailableToGive;
    private String API_KEY;
    private Intent intentReceived;
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getSupportActionBar().setTitle("Leaderboard");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        intentReceived = getIntent();

        //Need these 3 for POST methods
        originalName = intentReceived.getStringExtra("originalName");
        originalUsername = intentReceived.getStringExtra("originalUsername");
        pointsAvailableToGive = intentReceived.getStringExtra("pointsAvailableToGive");
        API_KEY = intentReceived.getStringExtra("API_KEY");

        FetchProfilesRunnable runnable = new FetchProfilesRunnable(API_KEY, this);
        new Thread(runnable).start();

        //Data fetched, now just set the RecyclerView
        recyclerView = findViewById(R.id.leaderboardRecycler);
        profileAdapter = new ProfileAdapter(profileList, this);
        recyclerView.setAdapter(profileAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void SuccessfulGET(JSONArray responseJSON) throws JSONException {
        for (int i=0; i < responseJSON.length(); i++) {
            JSONObject individualProfile = responseJSON.getJSONObject(i);
            int pointsAwarded = 0;
            JSONArray rewardsArray = individualProfile.getJSONArray("rewardRecordViews");
            for (int x=0; x < rewardsArray.length(); x++) {
                JSONObject rewardData = rewardsArray.getJSONObject(x);
                pointsAwarded = pointsAwarded + rewardData.getInt("amount");
            }
            Profile toAdd = new Profile(
                    individualProfile.getString("firstName"),
                    individualProfile.getString("lastName"),
                    individualProfile.getString("userName"),
                    individualProfile.getString("department"),
                    individualProfile.getString("story"),
                    individualProfile.getString("position"),
                    individualProfile.getString("imageBytes"),
                    Integer.toString(pointsAwarded)
            );
            profileList.add(toAdd);
        }
        sortProfiles(profileList);
        profileAdapter.notifyDataSetChanged();
    }

    private void sortProfiles(List<Profile> pList) {
        Collections.sort(pList, new Comparator<Profile>() {
            @Override
            public int compare(Profile o1, Profile o2) {
                return Integer.parseInt(o2.getPointsAwarded()) - Integer.parseInt(o1.getPointsAwarded());
            }
        });
        profileAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);

        Profile toGive = profileList.get(pos);
        Intent givingIntent = new Intent(this, GiveRewards.class);
        givingIntent.putExtra("fullname", toGive.getFullName());
        givingIntent.putExtra("userName", toGive.getUserName());
        givingIntent.putExtra("story", toGive.getBio());
        givingIntent.putExtra("pointsAwarded", toGive.getPointsAwarded());
        givingIntent.putExtra("department", toGive.getDepartment());
        givingIntent.putExtra("position", toGive.getPosition());
        givingIntent.putExtra("imageBytes", toGive.getImageBytes());

        givingIntent.putExtra("originalName", originalName);
        givingIntent.putExtra("originalUsername", originalUsername);
        givingIntent.putExtra("pointsAvailableToGive", pointsAvailableToGive);
        givingIntent.putExtra("API_KEY", API_KEY);

        profilePosition = pos;
        startActivityForResult(givingIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Intent toProfile = new Intent();
            toProfile.putExtra("pointDelta", data.getStringExtra("pointDelta"));
            setResult(1, toProfile);

            if (requestCode == 1) {
                profileList.get(profilePosition).setPointsAwarded(
                        String.valueOf(
                                Integer.parseInt(profileList.get(profilePosition).getPointsAwarded())
                                        + Integer.parseInt(data.getStringExtra("pointDelta"))
                        )
                );
                profileAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception e) {
            finish();
        }
    }
}