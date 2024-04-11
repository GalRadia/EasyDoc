package com.example.easydoc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

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
        askNotificationPermission();

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
                        // This ensures the UI is cleared and ready for a new sign-in
                        clearUIForSignOut();
                        // Optionally, you can redirect the user to the sign-in activity directly
                        createSignInIntent();
                    } else {
                        // Handle the failure of sign-out, possibly by showing an error message
                        showErrorSignOutFailed();
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

    private void showErrorSignOutFailed() {
        // Implement error handling here, e.g., show a toast or a dialog
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
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        // Handle failure in obtaining token
                        Log.e("FirebaseMessaging", "Failed to obtain token");
                        return;
                    }
                    String token = task.getResult();
                    UserAccount userAccount = new UserAccount(user.getUid(), name, email, phoneNumber, dateOfBirth, token, false);

                    mDatabase.child(user.getUid()).setValue(userAccount)
                            .addOnSuccessListener(aVoid -> {
                                // Handle successful write to database
                                Log.d("saveUserInformation", "User information saved successfully.");
                                navigateToMainActivity();
                            });
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

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // FCM SDK (and your app) can post notifications.
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
            // TODO: display an educational UI explaining to the user the features that will be enabled
            //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
            //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
            //       If the user selects "No thanks," allow the user to continue without notifications.
        } else {
            // Directly ask for the permission
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }


    }




}
