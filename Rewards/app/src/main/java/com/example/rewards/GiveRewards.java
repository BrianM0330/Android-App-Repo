package com.example.rewards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GiveRewards extends AppCompatActivity {

    private Intent intentReceived;

    private TextView fullname;
    private ImageView profilePic;
    private TextView pointsAwarded;
    private TextView department;
    private TextView position;
    private TextView story;
    private TextView charCounter;
    
    private EditText pointsToSend;
    private EditText comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_rewards);
        intentReceived = getIntent();

        fullname = findViewById(R.id.giveFullName);
        profilePic = findViewById(R.id.giveProfilePic);
        pointsAwarded = findViewById(R.id.givePointsAwarded);
        department = findViewById(R.id.giveDepartment);
        position = findViewById(R.id.givePosition);
        story = findViewById(R.id.giveStory);
        charCounter = findViewById(R.id.giveCharCounter);

        pointsToSend = findViewById(R.id.givePointsToSend);
        comment = findViewById(R.id.giveComment);

        fullname.setText(intentReceived.getStringExtra("fullname"));
        profilePic.setImageBitmap(b64ToBit(intentReceived.getStringExtra("imageBytes")));
        pointsAwarded.setText(intentReceived.getStringExtra("pointsAwarded"));
        department.setText(intentReceived.getStringExtra("department"));
        position.setText(intentReceived.getStringExtra("position"));
        story.setText(intentReceived.getStringExtra("story"));
        setupEditText();
    }

    public void SuccessfulPOST() {
        Intent data = new Intent();
        data.putExtra("pointDelta", pointsToSend.getText().toString());
        setResult(1, data);
        finish();
    }

    public void postRewards() {
        if (!pointsToSend.getText().toString().equals("") && !comment.getText().toString().equals("")) {
            //Check how many points the user has to give. If not enough, tell user and exit.
            if (Integer.parseInt(pointsToSend.getText().toString())
                    >
                Integer.parseInt(intentReceived.getStringExtra("pointsAvailableToGive"))) {
                notEnoughPoints();
                return;
            }
            RewardsAPIRunnable runnable = new RewardsAPIRunnable(
                    this,
                    intentReceived.getStringExtra("userName"),
                    intentReceived.getStringExtra("originalUsername"),
                    intentReceived.getStringExtra("originalName"),
                    Integer.parseInt(pointsToSend.getText().toString()),
                    comment.getText().toString(),
                    intentReceived.getStringExtra("API_KEY")
            );
            new Thread(runnable).start();
        }
        else Toast.makeText(this, "You must enter both points and a comment to send rewards", Toast.LENGTH_SHORT).show();
    }

    private void notEnoughPoints() {
        Toast.makeText(
                this,
                "You do not have enough points to give. Your current balance is "
                        + Integer.parseInt(intentReceived.getStringExtra("pointsAvailableToGive")),
                Toast.LENGTH_LONG).show();
    }

    public Bitmap b64ToBit(String bytes) {
        byte[] imageBytes = Base64.decode(bytes, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void setupEditText() {
        comment.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(80)
        });

        comment.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //When it's changed, update the counter view
                        int currentLength = s.toString().length();
                        String formatted = String.format("(%s of 80)", currentLength);
                        charCounter.setText(formatted);
                    }
                }
        );
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.giverewardsmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.postRewardsButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add Rewards Points?");
                builder.setMessage(String.format("Add rewards for %s", fullname.getText().toString()));
                builder.setIcon(R.drawable.logo);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postRewards();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}