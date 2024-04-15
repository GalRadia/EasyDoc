package com.example.easydoc;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.easydoc.databinding.ActivitySplashScreenBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashScreenActivity extends AppCompatActivity {
    private LottieAnimationView lottieAnimationView;
    private ActivitySplashScreenBinding binding;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupLottieAnimation();
        performBackgroundTask();
    }


    private void setupLottieAnimation() {
        lottieAnimationView = binding.animationView;
        lottieAnimationView.setAnimation("newAnimation.lottie");
        lottieAnimationView.setAlpha(1.0f);
        lottieAnimationView.setElevation(10.0f);
        lottieAnimationView.setTranslationZ(10.0f);
        lottieAnimationView.setTranslationY(10.0f);
        lottieAnimationView.setTranslationX(10.0f);
        lottieAnimationView.setSpeed(1.0f);
        lottieAnimationView.setRepeatCount(0);
        lottieAnimationView.setRepeatMode(LottieDrawable.RESTART);
        lottieAnimationView.resumeAnimation();
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                performBackgroundTask();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void performBackgroundTask() {
        executorService.execute(() -> {

            // Background task logic here

            // Run on UI thread if needed
            runOnUiThread(() -> {
                // UI update logic here
                Intent intent = new Intent(SplashScreenActivity.this, FirebaseUIActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
