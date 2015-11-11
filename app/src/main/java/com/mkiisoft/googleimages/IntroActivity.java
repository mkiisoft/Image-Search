package com.mkiisoft.googleimages;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.mkiisoft.googleimages.utils.intro.AppIntro;
import com.mkiisoft.googleimages.utils.intro.AppIntro2;
import com.mkiisoft.googleimages.utils.intro.AppIntroFragment;
import com.mkiisoft.googleimages.utils.KeySaver;

/**
 * Created by Mariano on 18/08/2015.
 */
public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle bundle) {

        getSupportActionBar().hide();

        if(KeySaver.isExist(this, "doneintro")){
            Intent intent = new Intent(IntroActivity.this, SelectActivity.class);
            startActivity(intent);
            finish();
        }

        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_1, Color.parseColor("#4bb5c5")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_2, Color.parseColor("#4bb5c5")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_3, Color.parseColor("#4bb5c5")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_4, Color.parseColor("#4bb5c5")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_5, Color.parseColor("#4bb5c5")));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.onboarding_6, Color.parseColor("#4bb5c5")));

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed() {

    }

    @Override
    public void onDonePressed() {
        KeySaver.saveShare(IntroActivity.this, "doneintro", "true");
        Intent intent = new Intent(IntroActivity.this, SelectActivity.class);
        startActivity(intent);
        finish();
    }
}
