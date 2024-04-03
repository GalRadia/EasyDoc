package com.example.easydoc.Utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.User;
import com.example.easydoc.Model.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseRepository {
    private static DatabaseRepository instance;
    private final MutableLiveData<List<Appointment>> appointmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> userAppointmentsLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<UserAccount>> usersLiveData = new MutableLiveData<>();
    private final DatabaseReference appointmentsReference;
    private final DatabaseReference usersReference;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();
    private final DatabaseReference userAppointmentsReference;

    private DatabaseRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        appointmentsReference = database.getReference("appointments");
        usersReference = database.getReference("users");
        userAppointmentsReference = database.getReference("users").child(currentUser.getUid()).child("appointmentsID");
        fetchAppointments();
        fetchUsers();
        fetchAppointmentsForUser();
    }

    public static synchronized DatabaseRepository getInstance() {
        if (instance == null) {
            instance = new DatabaseRepository();
        }
        return instance;
    }

    public void fetchAppointmentsForUser() {

        // First, get all appointment IDs for the user
        userAppointmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> appointmentIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String appointmentId = snapshot.getValue(String.class);
                    if (appointmentId != null) {
                        appointmentIds.add(appointmentId);
                    }
                }

                // Then, fetch each appointment detail using the IDs and set them in LiveData
                fetchAppointmentsByIds(appointmentIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

    }

    private void fetchAppointments() {
        appointmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Appointment> appointments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    appointments.add(appointment);
                }
                appointmentsLiveData.postValue(appointments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log error
            }
        });
    }

    private void fetchUsers() {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserAccount> users = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserAccount user = snapshot.getValue(UserAccount.class);
                    if (user != null) {
                        DataSnapshot appointmentsIDSnapshot = snapshot.child("appointmentsID");
                        if (appointmentsIDSnapshot.exists()) {
                            ArrayList<String> appointmentsList = new ArrayList<>();
                            for (DataSnapshot appointmentSnapshot : appointmentsIDSnapshot.getChildren()) {
                                String appointmentID = appointmentSnapshot.getValue(String.class);
                                if (appointmentID != null) {
                                    appointmentsList.add(appointmentID);
                                }
                            }
                            user.setAppointmentsList(appointmentsList);
                        }
                        users.add(user);
                    }
                }
                usersLiveData.postValue(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log error
            }
        });
    }

    public LiveData<List<Appointment>> getAppointmentsLiveData() {
        return appointmentsLiveData;
    }

    public LiveData<List<UserAccount>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<List<Appointment>> getAppointmentsFromUser() {
        return userAppointmentsLiveData;
    }

    public void insertAppointment(Appointment appointment) {
        // Generate a unique ID for each appointment

        // Insert the appointment into the database
        appointmentsReference.child(appointment.getId()).setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
        assert currentUser != null;
        usersReference.child(currentUser.getUid()).child("appointmentsID").push().setValue(appointment.getId())
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void fetchAppointmentsByIds(List<String> appointmentIds) {
        appointmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Appointment> appointments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (appointmentIds.contains(snapshot.getValue(Appointment.class).getId())) {
                        Appointment appointment = snapshot.getValue(Appointment.class);
                        appointments.add(appointment);
                    }
                }
                // Update the LiveData with the filtered list of appointments.
                userAppointmentsLiveData.postValue(appointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors.
            }
        });
    }
}