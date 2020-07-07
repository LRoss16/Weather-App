package com.example.lewis.weather;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.animations.IViewTranslation;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.view.View;

public class WelcomeSlider extends MaterialIntroActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new FirstTimeSlider());

        addSlide(new WelcomeHomeSlider());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        Intent x = new Intent(WelcomeSlider.this, MainActivity.class);
        startActivity(x);
    }

}

