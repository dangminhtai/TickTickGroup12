package hcmute.edu.vn.tick_tick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.tick_tick.util.ThemeUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        ThemeUtil.applyNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FrameLayout logoContainer = findViewById(R.id.logo_container);
        ImageView logoView = findViewById(R.id.splash_logo);
        TextView titleView = findViewById(R.id.splash_title);
        TextView taglineView = findViewById(R.id.splash_tagline);
        View progressBar = findViewById(R.id.progress_loading);

        // Initial states
        logoContainer.setScaleX(0.5f);
        logoContainer.setScaleY(0.5f);
        logoContainer.setAlpha(0f);
        
        titleView.setTranslationY(30f);
        taglineView.setTranslationY(20f);
        progressBar.setAlpha(0f);

        // Logo animation with bounce
        logoContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        // Logo rotation for visual interest
        logoView.animate()
                .rotation(360f)
                .setDuration(1000)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Title animation
        titleView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Tagline animation
        taglineView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(550)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Progress bar fade in
        progressBar.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(700)
                .start();

        // Navigate to main activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
