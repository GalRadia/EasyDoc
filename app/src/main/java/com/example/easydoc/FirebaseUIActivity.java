package com.example.easydoc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity {
    private ActivityFirebaseUiactivityBinding binding;
    private TextInputEditText phoneEdit;
    private TextInputLayout phoneEditLayout;
    private TextInputEditText emailEdit;
    private TextInputLayout emailEditLayout;
    private TextInputEditText nameEdit;
    private TextInputLayout nameEditLayout;

    private FirebaseAuth mAuth;

    // Launchers for various activity results
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::handlePermissionResult);
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirebaseUiactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        DoctorOffice doctorOffice=new DoctorOffice("HaAlonim Blvd 5, Be'er Ya'akov","EasyDoc","Dr Rick Sanchez",
//                "0547773686","HealthOffice@mail.com","8:30","9:30","30","1");
//        DatabaseReference officeReference = FirebaseDatabase.getInstance().getReference("DoctorOffice");
//        officeReference.setValue(doctorOffice);
        mAuth = FirebaseAuth.getInstance();
        initializeUI();
        checkCurrentUser();
    }

    private void initializeUI() {
        binding.signOutButton.setOnClickListener(v -> signOut());
        binding.editDate.setOnClickListener(v -> showDatePickerDialog());
        binding.saveButton.setOnClickListener(v -> attemptSaveUserInformation());
        emailEdit = binding.editEmail;
        emailEditLayout = binding.editEmailLayout;
        phoneEdit = binding.editPhoneNumber;
        phoneEditLayout = binding.editPhoneNumberLayout;
        nameEdit = binding.editName;
        nameEditLayout = binding.editNameLayout;
    }

    private void handlePermissionResult(boolean isGranted) {
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Cannot post notifications.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            promptForSignIn();
        } else {
            updateUIWithUserDetails(currentUser);
            checkUserExistence(currentUser.getUid());
        }
    }

    private void promptForSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.easydoc_logo)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK && mAuth.getCurrentUser() != null) {
            updateUIWithUserDetails(mAuth.getCurrentUser());
            checkUserExistence(mAuth.getCurrentUser().getUid());
        } else {
            handleSignInFailure(result.getIdpResponse());
        }
    }

    private void handleSignInFailure(IdpResponse response) {
        if (response == null) {
            Toast.makeText(this, "Sign-in cancelled by user.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("SignInError", "Error during sign-in: " + response.getError().getMessage());
        }
    }

    private void updateUIWithUserDetails(FirebaseUser user) {
        emailEdit.setText(user.getEmail());
        emailEdit.setEnabled(user.getEmail() == null || user.getEmail().isEmpty());
        nameEdit.setText(user.getDisplayName());
        phoneEdit.setText(user.getPhoneNumber());
        phoneEdit.setEnabled(user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty());
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resetUIPostSignOut();
            }
        });
    }

    private void resetUIPostSignOut() {
        emailEdit.setText("");
        nameEdit.setText("");
        phoneEdit.setText("");
        binding.editDate.setText("");
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, this::setDate, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setDate(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
        binding.editDate.setText(selectedDate);
    }

    private boolean validateInputs() {
        if (nameEdit.getText().toString().isEmpty()) {
            nameEditLayout.setError("Name is required");
            return false;
        }
        String email = binding.editEmail.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditLayout.setError("Please make sure the email is valid");
            return false;
        }
        if (phoneEdit.getText().toString().isEmpty()) {
            phoneEditLayout.setError("Phone is required");
            return false;
        }
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
            return;
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.orderByChild("doctor").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isDoctor = !snapshot.exists(); // If no doctor exists, this user can be the doctor.

                UserAccount newUser = new UserAccount(
                        user.getUid(),
                        nameEdit.getText().toString(),
                        emailEdit.getText().toString(),
                        phoneEdit.getText().toString(),
                        binding.editDate.getText().toString(),
                        isDoctor
                );

                mDatabase.child(user.getUid()).setValue(newUser)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success, navigate to main activity
                            navigateToMainActivity();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure, e.g., show a Toast or log
                            Toast.makeText(FirebaseUIActivity.this, "Failed to save user information: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        mDatabase.child(user.getUid()).setValue(new UserAccount(user.getUid(),
//                nameEdit.getText().toString(),
//                emailEdit.getText().toString(),
//                phoneEdit.getText().toString(),
//                binding.editDate.getText().toString(), false))
//        ;
//        navigateToMainActivity();
    }

    private void checkUserExistence(String uid) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uid);
        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                navigateToMainActivity();
            } else {
                Toast.makeText(this, "No such user exists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
