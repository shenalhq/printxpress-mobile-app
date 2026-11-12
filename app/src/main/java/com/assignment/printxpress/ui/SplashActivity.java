package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.assignment.printxpress.R;
import com.assignment.printxpress.util.SessionManager;

/**
 * Splash screen with centered branding and reveal animation.
 */
public class SplashActivity extends AppCompatActivity {
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View content = findViewById(R.id.splashRoot);
        if (content != null) {
            AnimationSet set = new AnimationSet(true);
            AlphaAnimation fade = new AlphaAnimation(0.0f, 1.0f);
            fade.setDuration(1000);
            TranslateAnimation move = new TranslateAnimation(0, 0, 50, 0);
            move.setDuration(1200);
            set.addAnimation(fade);
            set.addAnimation(move);
            content.startAnimation(set);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean signedIn = new SessionManager(this).isSignedIn();
            Intent intent = new Intent(this, signedIn ? MainActivity.class : LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
