package com.example.easydoc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityFirebaseUiactivityBinding binding;
    private TextInputEditText editName;
    private TextInputEditText editEmail;
    private TextInputEditText editPhoneNumber;
    private TextInputEditText editDate;
    private LottieAnimationView animationView;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_uiactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
      //  setupAnimationView();
        binding = ActivityFirebaseUiactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.animationView = binding.animationView;
        this.editDate = binding.editDate;
        this.editEmail = binding.editEmail;
        this.editName = binding.editName;
        this.editPhoneNumber = binding.editPhoneNumber;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            createSignInIntent();
        } else {
            checkIsUserExist(currentUser.getUid());
        }

//        DatabaseReference database = FirebaseDatabase.getInstance().getReference("doctor office");
//        DoctorOffice doctorOffice = new DoctorOffice("HaAlonim Blvd 5, Be'er Ya'akov","Dr. Rick Sanchez","1234567890","HealthOffice@mail.com","8:00","18:00","30");
//        database.setValue(doctorOffice);


        // Initialize Firebase Auth
    }


    // [END auth_fui_create_launcher]
    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.easydoc_logo)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_create_intent]
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            continueSignIn();

            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private void setupAnimationView() {

        final Dialog fullscreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullscreenDialog.setContentView(R.layout.dialog_fullscreen_animation);

        LottieAnimationView animationView = fullscreenDialog.findViewById(R.id.animationView);
        animationView.setAnimation("lottieAnimation.lottie");
        animationView.setBackgroundColor(Color.WHITE);
        animationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fullscreenDialog.dismiss();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    createSignInIntent();
                } else {
                    checkIsUserExist(currentUser.getUid().toString());
                }
            }

        });
        animationView.playAnimation();

        fullscreenDialog.show();
    }

    private void continueSignIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Check if the current user is not null to avoid NullPointerException
        if (currentUser != null) {
            checkIsUserExist(currentUser.getUid());
            // Set the email and name from the currentUser
            editEmail.setText(currentUser.getEmail());
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                editEmail.setEnabled(false); // Disable email field if email exists
            }
            editName.setText(currentUser.getDisplayName());

            // Only set the phone number if it's not empty to avoid overwriting user input
            String phoneNumber = currentUser.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                editPhoneNumber.setText(phoneNumber);
                // Consider whether you really need to disable the phone number input. If users should be able to edit their phone number, you might not disable it here.
                editPhoneNumber.setEnabled(false); // Disable phone number field if phone number exists
            } else {
                // If there is no phone number, ensure the field is enabled
                editPhoneNumber.setEnabled(true);
            }
        }

        binding.signOutButton.setOnClickListener(v -> signOut());
        binding.editDate.setOnClickListener(v -> showDatePickerDialog());
        binding.saveButton.setOnClickListener(v -> {
            if (validate()) {
                saveUserInformation(); // Refactor saving logic into its own method for clarity
            }
        });
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    recreate();
                });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    // Do something with the selected date
                    String selectedDate = dayOfMonth1 + "/" + (month1 + 1) + "/" + year1;
                    binding.editDate.setText(selectedDate);
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    public boolean validate() {
        if (binding.editName.getText().toString().isEmpty()) {
            binding.editName.setError("Name is required");
            return false;
        }
        if (binding.editEmail.getText().toString().isEmpty()) {
            binding.editEmail.setError("Email is required");
            return false;
        }
        if (binding.editPhoneNumber.getText().toString().isEmpty()) {
            binding.editPhoneNumber.setError("Phone number is required");
            return false;
        }
        if (binding.editDate.getText().toString().isEmpty()) {
            binding.editDate.setError("Date of birth is required");
            return false;
        }
        return true;
    }

    private void saveUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        UserAccount userAccount = new UserAccount(
                                user.getUid(),
                                binding.editName.getText().toString(),
                                binding.editEmail.getText().toString(),
                                binding.editPhoneNumber.getText().toString(),
                                binding.editDate.getText().toString(),
                                token
                                ,false
                        );
                        mDatabase.child(user.getUid()).setValue(userAccount).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); // Finish this activity to prevent returning here after saving
                            } else {
                                // Handle failure, perhaps show a message to the user
                            }
                        });
                    });

        }
    }


    public void checkIsUserExist(String uid) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    continueSignIn();
                }
            }
        });
    }


}