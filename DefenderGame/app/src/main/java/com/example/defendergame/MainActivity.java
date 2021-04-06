package com.example.defendergame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout layout;
    private TextView scoreCounter;
    private TextView levelCounter;
    private ImageView gameOverImage;
    private JSONArray resultsToDisplay;

    private final String levelFormatString = "Level %d";
    private static final int GAME_OVER_TIME_OUT = 3000;


    public static int screenHeight;
    public static int screenWidth;
    private int currentScore = 0;
    String initials = "THC";
    int indexToReplace = 0;
    private List<Base> bases = new ArrayList<>();
    public static List<Missile> activeMissiles = new ArrayList<>();

    private MissileMaker missileMaker;

    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO -> applyInterceptorBlast inside Interceptor class
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameOverImage = findViewById(R.id.gameOverImage);
        gameOverImage.setAlpha(0.0f);
        gameOverImage.setZ(10);

        bases.add(new Base(this, findViewById(R.id.base1)));
        bases.add(new Base(this, findViewById(R.id.base2)));
        bases.add(new Base(this, findViewById(R.id.base3)));

        setupFullScreen();
        getScreenDimensions();

        layout = findViewById(R.id.layout);

        layout.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                handleTouch(motionEvent.getX(), motionEvent.getY());
            }
            return false;
        });

        scoreCounter = findViewById(R.id.score);
        levelCounter = findViewById(R.id.levelCounter);
        levelCounter.setText(String.format(levelFormatString, 0));

        SoundPlayer.startLoop("background");
        new ScrollingBackground(this, layout, R.drawable.clouds, 15000);

        SoundPlayer.setupSound(this, "interceptor_blast", R.raw.interceptor_blast);
        SoundPlayer.setupSound(this, "launch_interceptor", R.raw.launch_interceptor);
        SoundPlayer.setupSound(this, "interceptor_hit_missile", R.raw.interceptor_hit_missile);
        SoundPlayer.setupSound(this, "base_blast", R.raw.base_blast);
        SoundPlayer.setupSound(this, "missile_miss", R.raw.missile_miss);
        SoundPlayer.setupSound(this, "launch_missile", R.raw.launch_missile);

        if (missileMaker == null) {
            missileMaker = new MissileMaker(this, screenWidth, screenHeight);
            new Thread(missileMaker).start();
        }
    }

    private void handleTouch(float x, float y) {
        if (bases.size() > 0) {
            Base closestBase = null;
            double closest = 0;

            //find the closest base
            for (Base b : bases) { //x2 and y2 are base coords, x1,y1 touch coords
                double newDistance = distanceFormula(b.getX(), x, b.getY(), y);
                if (closest == 0) {
                    closest = newDistance;
                    closestBase = b;
                }
                if (newDistance < closest) {
                    closest = newDistance;
                    closestBase = b;
                }
            }

            launchInterceptor(closestBase, x, y);
        }

        else gameOver();
    }

    public void applyMissileBlast(float blastX, float blastY) {
        for (int i=0; i < bases.size(); i++) {
            Base b = bases.get(i);
            double blastDistance = distanceFormula(b.getX(), blastX, b.getY(), blastY);

            if (blastDistance < 250) {
                bases.remove(b);
                b.destroy();

                if (bases.size() == 0) gameOver();
            }
        }
    }
    public void applyInterceptorBlast(float blastX, float blastY) {
        for (int i=0; i < activeMissiles.size(); i++) {
            Missile m = activeMissiles.get(i);
            double distanceFromInterceptor = distanceFormula(m.missile.getX(), blastX, m.missile.getY(), blastY);
            if (distanceFromInterceptor <= 120) {
                currentScore++;
                scoreCounter.setText(String.valueOf(currentScore));
                SoundPlayer.start("interceptor_hit_missile");
                m.interceptorBlast();
//                  todo -> check if needed (Missile handles this already) uncomment activeMissiles.remove(m);
            //      todo -> removeView(m.missile);
            }
        }

        for (int i=0; i < bases.size(); i++) {
            Base b = bases.get(i);
            double distanceFromBase = distanceFormula(b.getX(), blastX, b.getY(), blastY);
            if (distanceFromBase < 120) {b.destroy(); bases.remove(b);}
            if (bases.size() == 0) gameOver();
        }
    }

    public double distanceFormula (double x2, double x1, double y2, double y1) {
        //sqrt{ (x2 - y1)^2 + (y2 - y1)^2 }
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }


    public void addMissile(Missile m) {
        activeMissiles.add(m);
    }

    public void removeMissile(Missile m) {
//        Log.d("Missile", "removing missile: " + m);
        activeMissiles.remove(m);

        getLayout().removeView(m.missile);
    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
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

    @Override
    protected void onPause() {
        super.onPause();
        SoundPlayer.stop();
        missileMaker.setRunningState(false);
    }

    public void setLevel(int l) {
        levelCounter.setText(String.format(levelFormatString, l));
    }

    private void promptUser(int i) {
        final EditText initialsInput = new EditText(this);
        InputFilter[] filter = new InputFilter[1];
        filter[0] = new InputFilter.LengthFilter(3);
        initialsInput.setFilters(filter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You are a top player!");
        builder.setMessage("Please enter your initials (up to 3 characters");
        builder.setView(initialsInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            initials = initialsInput.getText().toString();
            try {
                //create object to insert into the leaderboard
                JSONObject toInsert = new JSONObject(); //initialize object to put in
                toInsert.put("date", sdf.format(System.currentTimeMillis()));
                toInsert.put("initials", initials);
                toInsert.put("score", currentScore);
                toInsert.put("level", missileMaker.getLevel());
                resultsToDisplay.put(i, toInsert);

                //once inserted via, insert via sql
                //todo -> sql insert statement (runnable)
                startLeaderboard();
            } catch (Exception e) {e.printStackTrace();}
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void gameOver() {
        missileMaker.setRunningState(false);
        SoundPlayer.stop();

        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(gameOverImage, "alpha", 0, 1);
        fadeIn.setDuration(2000);
        fadeIn.start();

        LeaderboardFetchRunnable lR = new LeaderboardFetchRunnable(this, initials, currentScore, missileMaker.getLevel());
        new Thread(lR).start();
    }

    public void processResults(JSONArray results) {
        resultsToDisplay = results;
        boolean highScore = true;
        try {
            //checkif the user needs to be placed on the leaderboard
            for (int i = 0; i < resultsToDisplay.length(); i++) {
                JSONObject curr = resultsToDisplay.getJSONObject(i);
                if (currentScore > curr.getInt("score")) {
                    highScore = false;
                    promptUser(i);
//                    startLeaderboard();
                    break; //just replace the first highest value
                }
            }

            if (highScore) {
                Toast.makeText(this, "You suck at this game!", Toast.LENGTH_LONG).show();
                startLeaderboard();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //base to launch interceptor, (x,y) = touchCoordinates (where to send interceptor to)
    private void launchInterceptor(Base b, float x, float y) {
        SoundPlayer.start("launch_interceptor");
        Interceptor i = new Interceptor(this, (float) b.getX(), (float) b.getY(), x, y);
        i.animSet.start();
    }

    private void startLeaderboard() {
        Intent i = new Intent(this, GameOver.class);
        i.putExtra("results", resultsToDisplay.toString());

        startActivity(i);
        finish();
    }
}