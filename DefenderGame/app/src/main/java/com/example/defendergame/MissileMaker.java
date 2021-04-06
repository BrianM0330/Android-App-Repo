package com.example.defendergame;

import android.animation.AnimatorSet;

import java.util.ArrayList;

public class MissileMaker implements Runnable{
    private final MainActivity main;
    private boolean isRunning;
    private final ArrayList<Missile> activeMissiles = new ArrayList<>();
    private final int screenW;
    private final int screenH;

    private int missileCount = 0;
    private static final int LEVEL_CHANGE_VALUE = 5;
    private int level = 1;
    private long delay = 5000;

    public MissileMaker(MainActivity main, int screenW, int screenH) {
        this.main = main;
        this.screenW = screenW;
        this.screenH = screenH;
    }

    void setRunningState(boolean b) {
        isRunning = b;
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
        for (Missile m : temp)
            m.stop();
    }


    @Override
    public void run() {
        setRunningState(true);
        while (isRunning) {
                makeMissile();
                missileCount++;

                if (missileCount > LEVEL_CHANGE_VALUE) {
                    increaseLevel();
                    missileCount = 0;
                }

                try {
                    Thread.sleep((long) getSleepTime());
                }
                catch (Exception e) {e.printStackTrace();}
        }
        setRunningState(false);
    }

    public void makeMissile() {
        int resID = R.drawable.missile;
        long screenTime = (long) ((delay * 0.5) + (Math.random() * delay));
        final Missile missile = new Missile(main, screenW, screenH, screenTime);

        main.addMissile(missile);

        SoundPlayer.start("launch_missile");

        final AnimatorSet as = missile.setData(resID);
        main.runOnUiThread(as::start);
    }

    private void increaseLevel() {
        level++;

        delay -= 500;
        if (delay <= 0) delay = 1000;

        main.runOnUiThread(() -> main.setLevel(level));
        try {
            Thread.sleep(2000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float getSleepTime() {
        double random = Math.random();
        if (random < 0.1) return 1000;
        else if (random < 0.2) return (float) (0.5 * delay);
        else return delay;
    }

    public int getLevel() {
        return level;
    }

    public void removeMissile(Missile m) {
        activeMissiles.remove(m);
    }

    public void addMissile(Missile m) {
        activeMissiles.add(m);
    }
}
