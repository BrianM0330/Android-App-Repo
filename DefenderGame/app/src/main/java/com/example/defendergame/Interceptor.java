package com.example.defendergame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class Interceptor {
    private final MainActivity main;
    private ImageView interceptorImage;
    public  AnimatorSet animSet = new AnimatorSet();
    private final float startX;
    private final float startY;
    private float endX;
    private float endY;
    private static int idVal = -1;
    private double travelDistance;

    Interceptor(MainActivity m, float sX, float sY, float eX, float eY) {
        main = m;
        startX = sX;
        startY = sY;
        endX = eX;
        endY = eY;
//        initialize();

        interceptorImage = new ImageView(main);
        interceptorImage.setId(idVal--);
        interceptorImage.setImageResource(R.drawable.interceptor);
        interceptorImage.setX(sX);
        interceptorImage.setY(startY);

        endX -= (int) (interceptorImage.getDrawable().getIntrinsicWidth() * 0.5);
        endY -= (int) (interceptorImage.getDrawable().getIntrinsicHeight() * 0.5);

        interceptorImage.setRotation(calculateAngle(sX, sY, eX, eY));

        main.getLayout().addView(interceptorImage);

        travelDistance = main.distanceFormula(eX, sX, eY, sY);

        animateInterceptor();
    }

    private void animateInterceptor() {
        ObjectAnimator xAnim = ObjectAnimator.ofFloat(interceptorImage, "x", startX, endX);
        xAnim.setInterpolator(new LinearInterpolator());
        xAnim.setDuration((long) (travelDistance * 2));

        ObjectAnimator yAnim = ObjectAnimator.ofFloat(interceptorImage, "y", startY, endY);
        yAnim.setInterpolator(new LinearInterpolator());
        yAnim.setDuration((long) (travelDistance * 2));

        xAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                main.getLayout().removeView(interceptorImage);
                makeBlast();
            }
        });

        animSet.playTogether(xAnim, yAnim);
    }

    private void makeBlast() {
        SoundPlayer.start("interceptor_blast");
        ImageView blastImage = new ImageView(main);
        blastImage.setImageResource(R.drawable.i_explode);

        blastImage.setX(endX);
        blastImage.setY(endY);

        blastImage.setX(blastImage.getX() - (int) (blastImage.getDrawable().getIntrinsicWidth() * 0.5) );
        blastImage.setY(blastImage.getY() - (int) (blastImage.getDrawable().getIntrinsicWidth() * 0.5) );

        main.getLayout().addView(blastImage);

        ObjectAnimator explosion = ObjectAnimator.ofFloat(blastImage, "alpha", 1.0f, 0.0f);
        explosion.setDuration(3000);

        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                main.getLayout().removeView(blastImage);
            }
        });

        explosion.start();
        main.applyInterceptorBlast(blastImage.getX(), blastImage.getY());
    }

    public static float calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (190.0f - angle);
    }
}
