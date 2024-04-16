package com.example.easydoc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.easydoc.Interfaces.isDoctorCallback;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity {
    private LottieAnimationView animationView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });
    private FirebaseAuth mAuth;
    private ActivityFirebaseUiactivityBinding binding;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure Firebase is initialized once across the app
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this);
//        }
        binding = ActivityFirebaseUiactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        setupUI();
        checkCurrentUser();
    }

    private void setupUI() {
        binding.signOutButton.setOnClickListener(v -> signOut());
        binding.editDate.setOnClickListener(v -> showDatePickerDialog());
        binding.saveButton.setOnClickListener(v -> attemptSaveUserInformation());

    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            createSignInIntent();
        } else {
            updateUserUI(currentUser);
            checkIsUserExist(currentUser.getUid());
        }

    }

    private void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.easydoc_logo) // Ensure you have this drawable in your resources
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                updateUserUI(user);
                checkIsUserExist(user.getUid());
            }
        } else {
            IdpResponse response = result.getIdpResponse();
            if (response == null) {
                // User pressed back button
                return;
            }
            // TODO: Handle sign-in error, perhaps show a toast or log the error
        }
    }

    private void updateUserUI(FirebaseUser user) {
        binding.editEmail.setText(user.getEmail());
        binding.editEmail.setEnabled(user.getEmail() == null || user.getEmail().isEmpty());
        binding.editName.setText(user.getDisplayName());
        binding.editPhoneNumber.setText(user.getPhoneNumber());
        binding.editPhoneNumber.setEnabled(user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty());

    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clearUIForSignOut();
                    }
                });
    }

    private void clearUIForSignOut() {
        // Clear or reset the UI elements here, e.g., clear text fields, disable buttons, etc.
        binding.editEmail.setText("");
        binding.editName.setText("");
        binding.editPhoneNumber.setText("");
        binding.editDate.setText("");
    }



    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    binding.editDate.setText(selectedDate);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private boolean validateInputs() {
        if (binding.editName.getText().toString().isEmpty()) {
            binding.editName.setError("Name is required");
            return false;
        }
        // Add other validations as needed
        return true;
    }

    private void attemptSaveUserInformation() {
        if (validateInputs()) {
            saveUserInformation();
        }
    }

    private void saveUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Handle scenario when the user is not signed in or authenticated
            Log.e("saveUserInformation", "User is not authenticated.");
            return;
        }

        // Data validation (basic example)
        String name = binding.editName.getText().toString();
        String email = binding.editEmail.getText().toString();
        String phoneNumber = binding.editPhoneNumber.getText().toString();
        String dateOfBirth = binding.editDate.getText().toString();
        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || dateOfBirth.isEmpty()) {
            // Handle missing information
            Log.e("saveUserInformation", "One or more fields are empty.");
            return;
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        checkIfDoctor(mDatabase, isDoctor -> {
            UserAccount userAccount = new UserAccount(user.getUid(), name, email, phoneNumber, dateOfBirth, isDoctor);
            mDatabase.child(user.getUid()).setValue(userAccount)
                    .addOnSuccessListener(aVoid -> {
                        // Handle successful write to database
                        Log.d("saveUserInformation", "User information saved successfully.");
                        navigateToMainActivity();
                    });
        });


    }

    public void checkIfDoctor(DatabaseReference mDatabase, isDoctorCallback callback) {

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    callback.onChecked(false);
                    // The reference is not empty, there are children
                } else {
                    // The reference is empty, no children
                    callback.onChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error checking data", databaseError.toException());
            }
        });
    }

    // Example placeholder for navigateToMainActivity method


    private void checkIsUserExist(String uid) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                navigateToMainActivity();
            } else {
                // No such user exists, handle accordingly
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    /*
       animationView.setAnimation("newAnimation.lottie");
        animationView.setAlpha(1.0f);
        animationView.setElevation(10.0f);
        animationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white)); // Set background color
        animationView.playAnimation();
     */


}
