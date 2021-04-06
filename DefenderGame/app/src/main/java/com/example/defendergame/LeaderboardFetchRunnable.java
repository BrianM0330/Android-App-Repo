package com.example.defendergame;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LeaderboardFetchRunnable implements Runnable{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private MainActivity main;
    private static final String dbURL = "jdbc:mysql://christopherhield.com:3306/chri5558_missile_defense";
    private Connection conn;
    private static final String SCORE_TABLE = "AppScores";
    private static final String password = "ABC.123";
    private static final String username = "chri5558_student";

    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    //from activity
    private final String initials;

    //from intent
    private final double score;
    private final int level;

    String QUERY = "SELECT * FROM AppScores ORDER BY Score DESC LIMIT 10";
    String INSERT = "INSERT INTO AppScores VALUES (%d, '%s', %f, %d)";
    boolean updated = false;

    LeaderboardFetchRunnable(MainActivity m, String i, int s, int l) {
        main = m;
        initials = i;
        score = s;
        level = l;
    }

    @Override
    public void run() {
        try {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbURL, username, password);
            JSONArray resultArray = new JSONArray();

            Statement insert_stmt = conn.createStatement();
            INSERT = String.format(Locale.getDefault(), INSERT, System.currentTimeMillis(), initials, score, level);

            StringBuilder sb = new StringBuilder();

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(QUERY);
            while (rs.next()) {
                JSONObject results = new JSONObject();
                results.put("date", sdf.format(new Date(rs.getLong(1))));
                results.put("initials", rs.getString(2));

                if (score > rs.getInt(3) && !updated) { //dont want to overwrite all the scores
                    updated = true;
                    insert_stmt.executeUpdate(INSERT);
                }

                results.put("score", rs.getInt(3));
                results.put("level", rs.getInt(4));

                Log.d("LeaderboardFetchRunnable", "added entity -> " + results.toString());
                resultArray.put(results);
            }
            rs.close();
            stmt.close();
            insert_stmt.close();

            conn.close();

            main.runOnUiThread(() -> main.processResults(resultArray));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
