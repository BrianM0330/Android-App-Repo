package com.example.rewards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewProfile extends AppCompatActivity {

    Intent intentReceived;
    private String API_KEY;
    int totalPoints = 0;

    ActionBar bar;
    private List<Reward> rewardList = new ArrayList<>();
    private JSONArray rewardsData;
    private RecyclerView recyclerView;
    private rewardAdapter rewardAdapter;

    TextView fullName;
    TextView location;
    TextView username;
    ImageView profilePic;
    TextView pointsAwarded;
    TextView department;
    TextView position;
    TextView pointsToAward;
    TextView rewardHistory;
    TextView story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        intentReceived = getIntent();

        API_KEY = intentReceived.getStringExtra("API_KEY");

        //Rewards is null on profile creation. But needs to be checked because ViewProfile is called
        //by MainActivity and Leaderboard where the "rewards" intent is populated.
        if (intentReceived.getStringExtra("rewards") != null) {
            try {rewardsData = new JSONArray(intentReceived.getStringExtra("rewards")); }
            catch (Exception e) { e.printStackTrace();}

            if (rewardsData.length() >= 1)
                try { parseRewardJSON(rewardsData); }
                catch (JSONException e) { e.printStackTrace(); }
        }

        recyclerView = findViewById(R.id.profileRecyclerView);
        rewardAdapter = new rewardAdapter(rewardList, this);
        recyclerView.setAdapter(rewardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fullName = findViewById(R.id.viewFullName);
        location = findViewById(R.id.viewLocation);
        username = findViewById(R.id.viewUsername);
        profilePic = findViewById(R.id.viewProfilePic);
        pointsAwarded = findViewById(R.id.viewPointsAwarded);
        department = findViewById(R.id.viewDepartment);
        position = findViewById(R.id.viewPosition);
        pointsToAward = findViewById(R.id.viewPointsToAward);
        rewardHistory = findViewById(R.id.viewRewardHistory);
        story = findViewById(R.id.viewStory);

        Intent intentReceived = getIntent();

        //SET DATA FROM JSON RESPONSE
        b64ToImage(intentReceived.getStringExtra("profilePic"));
        fullName.setText(intentReceived.getStringExtra("fname"));
        location.setText(intentReceived.getStringExtra("location"));
        username.setText(intentReceived.getStringExtra("uname"));
        pointsAwarded.setText(intentReceived.getStringExtra("pointsAwarded"));
        department.setText(intentReceived.getStringExtra("department"));
        position.setText(intentReceived.getStringExtra("position"));
        pointsToAward.setText(Integer.toString(intentReceived.getIntExtra("pointsToAward", 0)));
        rewardHistory.setText(String.format("Reward History (%s):", rewardList.size()));
        story.setText(intentReceived.getStringExtra("story"));

        getSupportActionBar().setTitle("Your profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    private void b64ToImage(String bytes) {
        byte[] imageBytes = Base64.decode(bytes, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        profilePic.setImageBitmap(bitmap);
    }

    private void parseRewardJSON(JSONArray toparse) throws JSONException {
        for (int i=0; i < toparse.length(); i++) {
            JSONObject individualReward = toparse.getJSONObject(i);
            Reward toAdd = new Reward(
                    individualReward.getString("awardDate"),
                    individualReward.getString("note"),
                    individualReward.getString("giverName"),
                    individualReward.getString("amount")
            );
            totalPoints += individualReward.getInt("amount");
            rewardList.add(toAdd);
        }
        sortProfiles(rewardList);
    }

    private void sortProfiles(List<Reward> rList) {
        Collections.sort(rList, new Comparator<Reward>() {
            @Override
            public int compare(Reward o1, Reward o2) {
                return Integer.parseInt(o2.getPoints()) - Integer.parseInt(o1.getPoints());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profileoptions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.leaderboardButton) {
            Intent leaderboardIntent = new Intent(this, Leaderboard.class);
            leaderboardIntent.putExtra("originalName", intentReceived.getStringExtra("fname")); //Send to leaderboard for the "gifter" query param
            leaderboardIntent.putExtra("originalUsername", intentReceived.getStringExtra("uname"));
            leaderboardIntent.putExtra("pointsAvailableToGive", Integer.toString(intentReceived.getIntExtra("pointsToAward", 0)));
            leaderboardIntent.putExtra("API_KEY", API_KEY);
            startActivityForResult(leaderboardIntent, 1);
            return true;
        }

        else if (item.getItemId() == R.id.editButton) {
            String[] nameSplitter = fullName.getText().toString().split(" ");
            Intent editIntent = new Intent(this, EditProfile.class);
            editIntent.putExtra("firstName", nameSplitter[0]);
            editIntent.putExtra("lastName", nameSplitter[1]);
            editIntent.putExtra("userName", intentReceived.getStringExtra("uname"));
            editIntent.putExtra("department", intentReceived.getStringExtra("department"));
            editIntent.putExtra("story", intentReceived.getStringExtra("story"));
            editIntent.putExtra("position", intentReceived.getStringExtra("position"));
            editIntent.putExtra("password", intentReceived.getStringExtra("password"));
            editIntent.putExtra("location", intentReceived.getStringExtra("location"));
            editIntent.putExtra("imageBytes", intentReceived.getStringExtra("profilePic"));
            editIntent.putExtra("API_KEY", API_KEY);
            startActivityForResult(editIntent, 2);
        }

        else if (item.getItemId() == R.id.deleteButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Profile?");
            builder.setIcon(R.drawable.logo);
            builder.setMessage(String.format("Delete profile for %s?\n (The rewards app will be closed upon deletion)", username.getText().toString()));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteRunnableHelper();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteRunnableHelper() {
        DeleteProfileAPIRunnable runnable = new DeleteProfileAPIRunnable(this, username.getText().toString(), API_KEY);
        new Thread(runnable).start(); //Calls SuccessfulDelete()
    }

    //CALLBACK FOR DELETE BUTTON
    public void SuccessfulDelete(int response) {
        if (response == 0) {
            Toast.makeText(this, "There was an issue deleting the profile. Please try again", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Successfully deleted profile! App is now closing.", Toast.LENGTH_SHORT).show();
            setResult(1, new Intent());
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1) {
                pointsToAward.setText(
                        String.valueOf(
                                Integer.parseInt(pointsToAward.getText().toString())
                                        - Integer.parseInt(data.getStringExtra("pointDelta"))
                        )
                );
            }

            if (resultCode == 2) {

                b64ToImage(data.getStringExtra("imageBytes"));
                fullName.setText(String.format("%s, %s",
                        data.getStringExtra("firstName"),
                        data.getStringExtra("lastName")
                ));
                username.setText(data.getStringExtra("userName"));
                department.setText(data.getStringExtra("department"));
                story.setText(data.getStringExtra("story"));
                position.setText(data.getStringExtra("position"));
                pointsToAward.setText(data.getStringExtra("remainingPointsToAward"));

                location.setText(data.getStringExtra("location"));

                if (data.getStringExtra("rewardRecordViews").length() > 4) {
                    JSONArray j = new JSONArray(data.getStringExtra("rewardRecordViews"));
                    parseRewardJSON(j);
                    rewardAdapter.notifyDataSetChanged();

                    pointsAwarded.setText(totalPoints);

                    rewardHistory.setText(String.format("Reward History (%s):", rewardList.size()));
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
}