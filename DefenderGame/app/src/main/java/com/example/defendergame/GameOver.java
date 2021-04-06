package com.example.defendergame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameOver extends AppCompatActivity {
    private RecyclerView RecyclerView;
    private entryAdapter entryAdapter;
    List<entryItem> entries = new ArrayList<>();

    JSONArray resultsToDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        setupFullScreen();

        RecyclerView = findViewById(R.id.leaderboardRecycler);
        entryAdapter = new entryAdapter(entries, this);
        RecyclerView.setAdapter(entryAdapter);
        RecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent in = getIntent();

        try {resultsToDisplay = new JSONArray(in.getStringExtra("results"));}
        catch (JSONException e) { e.printStackTrace(); }

        populateLeaderboard();
        entryAdapter.notifyDataSetChanged();
    }

    private void populateLeaderboard() {
        try {
            //forEach jsonObject, add to the list (it will always be 10);
            for (int i = 0; i < resultsToDisplay.length(); i++) {
                JSONObject curr = resultsToDisplay.getJSONObject(i);

                entryItem entry = new entryItem(
                        i+1,
                        curr.getString("initials"),
                        curr.getInt("level"),
                        curr.getInt("score"),
                        curr.getString("date")
                );

                entries.add(entry);
            }

            sortScores();
            fixIndices();
            entryAdapter.notifyDataSetChanged();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sortScores() {
        entries.sort((o1, o2) -> o2.getScore() - o1.getScore());
    }

    private void fixIndices() {
        for (int i=0; i < entries.size(); i++) {
            entries.get(i).setPosition(i+1);
        }
    }

    private void setupFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}