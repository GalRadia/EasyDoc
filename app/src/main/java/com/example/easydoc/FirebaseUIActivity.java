package com.example.easydoc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easydoc.Model.Doctor;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.databinding.ActivityFirebaseUiactivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FirebaseUIActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityFirebaseUiactivityBinding binding;

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
        binding = ActivityFirebaseUiactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            createSignInIntent();
        } else {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
            binding.editEmail.setText(currentUser.getEmail());
            binding.editName.setText(currentUser.getDisplayName());
            binding.editPhoneNumber.setText(currentUser.getPhoneNumber());
            binding.editEmail.setEnabled(false);

        }
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference("doctor");
//        Doctor doctor = new Doctor("1", "Dr. John Doe", "Doctor@health.com", "1234567890", "01/01/1990", new Timepoint(8,0), new Timepoint(18,0), 30);
//        database.child("1").setValue(doctor);

        binding.signOutButton.setOnClickListener(v -> signOut());
        binding.saveButton.setOnClickListener(v -> {
            if (!validate()) {
                return;
            }
            FirebaseUser user = mAuth.getCurrentUser();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            UserAccount userAccount = new UserAccount(Objects.requireNonNull(user.getUid()), binding.editName.getText().toString(), binding.editEmail.getText().toString(), binding.editPhoneNumber.getText().toString(),
                    binding.editDate.getText().toString());
            mDatabase.child(user.getUid()).setValue(userAccount);
            Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
            startActivity(intent);
        });
        binding.editDate.setOnClickListener(v -> showDatePickerDialog());
        // Initialize Firebase Auth


    }


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

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
            FirebaseUser user = mAuth.getCurrentUser();
            binding.editEmail.setText(user.getEmail());
            binding.editName.setText(user.getDisplayName());
            binding.editPhoneNumber.setText(user.getPhoneNumber());
            binding.editEmail.setEnabled(false);

            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    recreate();
                });
        // [END auth_fui_signout]
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


}