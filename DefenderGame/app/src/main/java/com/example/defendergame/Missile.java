package com.example.defendergame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

public class Missile {

    private final MainActivity main;
    private final String TAG = "Missile";
    public ImageView missile;
    private final AnimatorSet aSet = new AnimatorSet();
    private final int screenWidth;
    private final int screenHeight;
    private long duration;
    private int startX;
    private final int endX;
    private int startY;
    private final int endY;

    private static int idVal = -1; //used to reference the missile in initialize()
    private final boolean hit = false;

    public Missile(MainActivity m, int w, int h, long d) {
        this.main = m;
        this.duration = d;
        screenWidth = w;
        screenHeight = h;
        startX = (int) (Math.random() * w);
        endX = (int) (Math.random() * w);
        startY = -100;
        endY = screenHeight;
        initialize();
    }

    private void initialize() {
        missile = new ImageView(main);
        missile.setId(idVal-=1);
        missile.setImageResource(R.drawable.missile);

        startX -= missile.getDrawable().getIntrinsicWidth() * 0.5;
        startY -= missile.getDrawable().getIntrinsicWidth() * 0.5;

        missile.setRotation(calculateAngle(startX, startY, endX, endY));
        missile.setX(startX);
        missile.setY(startY);
//        missile.setZ(-10); wont work because clouds dont work......

        main.runOnUiThread(() -> main.getLayout().addView(missile));
    }

    AnimatorSet setData(final int drawID) {
        Log.d("Missile", "Missile created");
        main.runOnUiThread(() -> missile.setImageResource(drawID));

        //Probably need to change to y
        ObjectAnimator xAnim = ObjectAnimator.ofFloat(missile, "x", startX, endX);
        xAnim.setInterpolator(new LinearInterpolator());
        xAnim.setDuration(duration);

        ObjectAnimator yAnim = ObjectAnimator.ofFloat(missile, "y", startY, screenHeight);
        yAnim.setInterpolator(new LinearInterpolator());
        yAnim.setDuration(duration);

        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int y = (int) missile.getY();
                if (y > screenHeight * 0.85) {
//                    Log.d("Missile", "Missile exploded at x: " + missile.getX() +" y: " + missile.getY());
                    xAnim.removeAllUpdateListeners();
                    xAnim.cancel();
                    yAnim.cancel();

                    makeGroundBlast(missile.getX(), missile.getY());
                    main.removeMissile(Missile.this);
                }
            }
        });

        aSet.playTogether(xAnim, yAnim);
        return aSet; //aSet starts in MissileMaker, just setting it up in here
    }

    private void makeGroundBlast(float x, float y) {
        Log.d("Missile", "Creating blast at x: " + x +" , y: "+ y);

        ImageView gBlast = new ImageView(main);
        gBlast.setImageResource(R.drawable.explode);
        gBlast.setX(x); gBlast.setY(y);

        float imageWidth = gBlast.getDrawable().getIntrinsicWidth();
        float imageHeight = gBlast.getDrawable().getIntrinsicHeight();

        gBlast.setX(gBlast.getX() - (int) (imageWidth * 0.5));
        gBlast.setY(gBlast.getY() - (int) (imageHeight * 0.5));
        main.getLayout().addView(gBlast);

        Log.d("Missile", "Updated blast to x: " + gBlast.getX() +" , y: "+ gBlast.getY());

        ObjectAnimator explosion = ObjectAnimator.ofFloat(gBlast, "alpha", 1.0f, 0.0f);
        explosion.setDuration(3000);

        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                main.getLayout().removeView(gBlast);
            }
        });

        explosion.start();

        main.applyMissileBlast(gBlast.getX(), gBlast.getY());

        //todo -> applyMissileBlast
    }

    public void interceptorBlast() {
        Log.d("Missile", "Calling remove on main for: " + Missile.this);
        main.removeMissile(Missile.this); //removes view and active missile from list

        //WARNING -> MIGHT EXPLODE EVEN MORE
        ImageView destroyedMissile = new ImageView(main);
        destroyedMissile.setImageResource(R.drawable.explode);

        destroyedMissile.setX(missile.getX());
        destroyedMissile.setY(missile.getY());

        Missile.this.aSet.cancel(); //cancel all the animators for this object

        main.getLayout().addView(destroyedMissile);

        ObjectAnimator explosionAnim = ObjectAnimator.ofFloat(destroyedMissile, "alpha", 1.0f, 0.0f);
        explosionAnim.setDuration(3000);

        explosionAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                main.getLayout().removeView(destroyedMissile);
            }
        });

        explosionAnim.start();
    }

    void stop() {
        aSet.cancel();
    }

    public static float calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (190.0f - angle);
    }

}
