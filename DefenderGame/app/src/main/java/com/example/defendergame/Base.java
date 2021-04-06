package com.example.defendergame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.widget.ImageView;

public class Base {
    private MainActivity main;
    private ImageView baseReference;

    Base(MainActivity m, ImageView b) {
        main = m;
        baseReference = b;
    }

    public void destroy() {
        SoundPlayer.start("base_blast");

        main.getLayout().removeView(baseReference);

        ImageView explosion = new ImageView(main);
        explosion.setImageResource(R.drawable.explode);

        explosion.setX(baseReference.getX());
        explosion.setY(baseReference.getY());
        main.getLayout().addView(explosion);

        ObjectAnimator kablooie = ObjectAnimator.ofFloat(explosion, "alpha", 1.0f, 0.0f);
        kablooie.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                main.getLayout().removeView(explosion);
            }
        });

        kablooie.start();
    }

    public double getX() {
        return baseReference.getX() + (0.5 * baseReference.getWidth());
    }

    public double getY() {
        return baseReference.getY() + (0.5 * baseReference.getHeight());
    }
}
