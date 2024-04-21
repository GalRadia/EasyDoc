package com.example.easydoc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.Utils.Helper;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.data.model.PhoneNumber;
import com.google.android.material.button.MaterialButton;
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
import java.util.Objects;

public class FirebaseUIActivity extends AppCompatActivity {
    private ActivityFirebaseUiactivityBinding binding;
    private TextInputEditText phoneEdit;
    private TextInputLayout phoneEditLayout;
    private TextInputEditText emailEdit;
    private TextInputLayout emailEditLayout;
    private TextInputEditText nameEdit;
    private TextInputLayout nameEditLayout;

    private FirebaseAuth mAuth;
    private TextInputEditText editDate;
    private MaterialButton saveButton;
    private MaterialButton signOutButton;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirebaseUiactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addDoctorOffice(); // Uncomment this line to add a doctor office to the database
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        setupUI(); // Initialize UI components
        initUI(); // Initialize UI state
        checkCurrentUser(); // Check if user is already signed in
    }

    private static void addDoctorOffice() {
        DoctorOffice doctorOffice = new DoctorOffice("EasyDoc, Tel-Aviv,70300", "EasyDoc", "Dr Rick Sanchez",
                "0547773686", "HealthOffice@mail.com", "08:00", "20:00", "30", "1");
        DatabaseReference officeReference = FirebaseDatabase.getInstance().getReference("DoctorOffice");
        officeReference.setValue(doctorOffice);
    }

    //Binding all the components
    private void setupUI() {
        emailEdit = binding.editEmail;
        emailEditLayout = binding.editEmailLayout;
        phoneEdit = binding.editPhoneNumber;
        phoneEditLayout = binding.editPhoneNumberLayout;
        nameEdit = binding.editName;
        nameEditLayout = binding.editNameLayout;
        signOutButton = binding.signOutButton;
        editDate = binding.editDate;
        saveButton = binding.saveButton;

    }

    private void initUI() {
        signOutButton.setOnClickListener(v -> signOut());
        editDate.setOnClickListener(v -> {
            editDate.setError(null); // Clear any previous error
            showDatePickerDialog();
        });
        saveButton.setOnClickListener(v -> attemptSaveUserInformation());
        phoneEdit.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneEdit.setOnClickListener(v -> phoneEditLayout.setError(null));
    }


    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            promptForSignIn(); //No user is loged in, Initiate sign in
        } else {
            updateUIWithUserDetails(currentUser); //User is already signed in, update UI with user details
            checkUserExistence(currentUser.getUid()); //Check if user exists in the database
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
        }
    }

    //Update the UI with the user details
    private void updateUIWithUserDetails(FirebaseUser user) {
        emailEdit.setText(user.getEmail());
        emailEdit.setEnabled(user.getEmail() == null || user.getEmail().isEmpty()); // Disable email field if email is already set
        nameEdit.setText(user.getDisplayName());
        phoneEdit.setText(user.getPhoneNumber());
        phoneEdit.setEnabled(user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty());
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resetUIPostSignOut();
                recreate();
            }
        });
    }

    //Reset the UI after sign out
    private void resetUIPostSignOut() {
        emailEdit.setText("");
        nameEdit.setText("");
        phoneEdit.setText("");
        editDate.setText("");
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, this::setDate, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setDate(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
        editDate.setText(selectedDate);
    }

    //Validate the inputs
    private boolean validateInputs() {
        if (Objects.requireNonNull(nameEdit.getText()).toString().isEmpty()) {
            nameEditLayout.setError("Name is required");
            return false;
        }
        String email = Objects.requireNonNull(binding.editEmail.getText()).toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditLayout.setError("Please make sure the email is valid");
            return false;
        }
        if (Objects.requireNonNull(phoneEdit.getText()).toString().isEmpty() || !Helper.checkPhoneNumber(phoneEdit.getText().toString())) {
            phoneEditLayout.setError("Phone is required");
            return false;
        }
        if (Objects.requireNonNull(editDate.getText()).toString().isEmpty()) {
            editDate.setError("Date is required");
            return false;
        }
        return true;
    }

    //Save the user information if the inputs are valid
    private void attemptSaveUserInformation() {
        if (validateInputs()) {
            saveUserInformation();
        }
    }

    //Save the user data to the database
    private void saveUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.orderByChild("doctor").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isDoctor = !snapshot.exists(); // If no doctor exists, this user will be the doctor.

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
