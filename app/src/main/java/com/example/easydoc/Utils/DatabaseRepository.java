package com.example.easydoc.Utils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easydoc.Interfaces.DoctorCheckCallback;
import com.example.easydoc.MainActivity;
import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseRepository {
    private static DatabaseRepository instance;
    private final MutableLiveData<List<Appointment>> passedAppointmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> appointmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> userAppointmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> userPassedAppointmentsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Map<String, String>> appointmentsWaitListLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<UserAccount>> usersLiveData = new MutableLiveData<>();
    private final DatabaseReference doctorOfficeReference;
    private final DatabaseReference appointmentsReference;
    private final DatabaseReference usersReference;
    private final DatabaseReference appointmentsWaitListReference;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;
    private final MutableLiveData<FirebaseUser> currentUserLiveData = new MutableLiveData<>();
    private final DatabaseReference userAppointmentsReference;
    private final MutableLiveData<DoctorOffice> doctorOfficeMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDoctorLiveData = new MutableLiveData<>();


    private DatabaseRepository() {
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        appointmentsReference = database.getReference("appointments");
        usersReference = database.getReference("users");
        doctorOfficeReference = database.getReference("doctor office");
        appointmentsWaitListReference = database.getReference("appointmentsWaitList");

        // fetchCurrentUser();
        userAppointmentsReference = database.getReference("users").child(currentUser.getUid().toString()).child("appointmentsID");
        fetchAppointments();
        fetchUsers();
        fetchAppointmentsForUser();
        fetchCurrentUserDoctor();
        fetchDoctorOffice();
        fetchAppointmentsWaitList();
    }

    private void fetchAppointmentsWaitList() {
        appointmentsWaitListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> appointmentsWaitList = new HashMap<>();
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    String date = appointmentSnapshot.getValue(String.class);
                    if (date != null) {
                        appointmentsWaitList.put(appointmentSnapshot.getKey(), date);
                    }
                }
                appointmentsWaitListLiveData.postValue(appointmentsWaitList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error
            }
        });
    }

    private void fetchDoctorOffice() {
        doctorOfficeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DoctorOffice doctorOffice = snapshot.getValue(DoctorOffice.class);
                doctorOfficeMutableLiveData.postValue(doctorOffice);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error
            }
        });
    }

    private void fetchCurrentUser() {
        mAuth.addAuthStateListener(firebaseAuth -> currentUserLiveData.postValue(firebaseAuth.getCurrentUser()));


    }

    public static void destroyInstance() {
        instance = null;
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
        //removePassedAppointments();
        appointmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Appointment> appointments = new ArrayList<>();
                List<Appointment> passedAppointments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (Helper.isAppointmentPassed(appointment.getDate(), appointment.getTime())) {
                        passedAppointments.add(appointment);
                    } else appointments.add(appointment);
                }
                appointmentsLiveData.postValue(appointments);
                passedAppointmentsLiveData.postValue(passedAppointments);
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

    public LiveData<List<Appointment>> getPassedAppointmentsLiveData() {
        return passedAppointmentsLiveData;
    }

    public LiveData<List<Appointment>> getUserPassedAppointmentsLiveData() {
        return userPassedAppointmentsLiveData;
    }

    public LiveData<DoctorOffice> getDoctorOfficeLiveData() {
        return doctorOfficeMutableLiveData;
    }

    public LiveData<Boolean> getIsDoctorLiveData() {
        return isDoctorLiveData;
    }

    public void removeAppointment(String appointmentID) {
        String date=getDateFromAppointmentID(appointmentID);
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot appointmentsIDSnapshot = userSnapshot.child("appointmentsID");
                    for (DataSnapshot appointmentIdSnapshot : appointmentsIDSnapshot.getChildren()) {
                        if (appointmentIdSnapshot.getValue(String.class).equals(appointmentID)) {
                            appointmentIdSnapshot.getRef().removeValue();
                            break; // Break if you assume an appointment can only be under one user
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error updating users: " + databaseError.getMessage());
            }
        });

        AbstractMap.SimpleEntry<String, String> appointmentEntry;
        appointmentEntry = getAppointmentFromWaitingList(date);
        if (appointmentEntry != null) {
            String userID = appointmentEntry.getKey();
            String name = getNameFromUserID(userID);
            usersReference.child(userID).child("appointmentsID").push().setValue(appointmentID);
            appointmentsReference.child(appointmentID).child("name").setValue(name);
            appointmentsReference.child(appointmentID).child("text").setValue("Appointment from waiting list");
            deleteFromWaitingList(appointmentEntry.getKey());
        } else appointmentsReference.child(appointmentID).removeValue();


    }
    public boolean dateAvailable(String date){
        int count=0;
        for (Appointment appointment : appointmentsLiveData.getValue()) {
            if (appointment.getDate().equals(date)) {
                count++;
            }
        }
        return count<appointmentsInDay();
    }
    public int appointmentsInDay(){
        int duration= Integer.parseInt(doctorOfficeMutableLiveData.getValue().getAppointmentDuration());
        int startHour=Integer.parseInt(doctorOfficeMutableLiveData.getValue().getStartTime().split(":")[0]);
        int endHour=Integer.parseInt(doctorOfficeMutableLiveData.getValue().getEndTime().split(":")[0]);
         return (endHour-startHour)*60/duration;
    }

    private String getNameFromUserID(String userID) {
        String name = "";
        for (UserAccount user : usersLiveData.getValue()) {
            if (user.getUid().equals(userID)) {
                name = user.getName();
                break;
            }
        }
        return name;
    }

    private String getDateFromAppointmentID(String appointmentID) {
        String date = "";
        for (Appointment appointment : appointmentsLiveData.getValue()) {
            if (appointment.getId().equals(appointmentID)) {
                date = appointment.getDate();
                break;
            }
        }
        return date;
    }

    public void addToWaitingList(String userID, String date) {
        appointmentsWaitListReference.child(userID).setValue(date);
    }
    private void deleteFromWaitingList(String userID) {
        appointmentsWaitListReference.child(userID).removeValue();
    }



    public AbstractMap.SimpleEntry<String, String> getAppointmentFromWaitingList(String date) {
        for (Map.Entry<String, String> entry : appointmentsWaitListLiveData.getValue().entrySet()) {
            if (entry.getValue().equals(date)) {
                return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    public void updateText(String appointmentID, String text) {
        appointmentsReference.child(appointmentID).child("text").setValue(text);
    }


    public void insertAppointment(Appointment appointment, Context context) {
        // Generate a unique ID for each appointment

        // Insert the appointment into the database
        appointmentsReference.child(appointment.getId()).setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, R.string.appointment_added, Toast.LENGTH_SHORT).show();
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, R.string.appointment_failed, Toast.LENGTH_SHORT).show();
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
                List<Appointment> passedAppointments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (appointmentIds.contains(snapshot.getValue(Appointment.class).getId())) {
                        Appointment appointment = snapshot.getValue(Appointment.class);
                        if (Helper.isAppointmentPassed(appointment.getDate(), appointment.getTime())) {
                            passedAppointments.add(appointment);
                        } else appointments.add(appointment);
                    }
                }
                // Update the LiveData with the filtered list of appointments.
                userAppointmentsLiveData.postValue(appointments);
                userPassedAppointmentsLiveData.postValue(passedAppointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors.
            }
        });
    }

    public void addUserToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        usersReference.child(currentUser.getUid()).child("token").setValue(token);

                    }
                });
    }

    public void fetchCurrentUserDoctor() {
        usersReference.child(currentUser.getUid()).child("doctor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isDoctor = snapshot.getValue(Boolean.class);
                isDoctorLiveData.postValue(isDoctor != null && isDoctor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error or set LiveData value to handle the error state
                isDoctorLiveData.postValue(false); // Optionally, handle this differently to distinguish between false and error.
            }
        });
    }


    private void removePassedAppointments() {
        appointmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        String date = appointment.getDate();
                        String time = appointment.getTime();
                        if (Helper.isAppointmentPassed(date, time)) {
                            removeAppointment(appointment.getId());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });

    }

    public void setCurrentUser() {
        currentUser = mAuth.getCurrentUser();

    }


//    public Timepoint[] getDisabledTimepointsFromDate(String date) {
//        List<Timepoint> disabledTimes = new ArrayList<>();
//        appointmentsReference.orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String time = snapshot.child("time").getValue(String.class);
//                    if (time != null) {
//                        String[] timeParts = time.split(":");
//                        int hour = Integer.parseInt(timeParts[0]);
//                        int minute = Integer.parseInt(timeParts[1]);
//                        Timepoint timepoint = new Timepoint(hour, minute);
//                        disabledTimes.add(timepoint);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle potential errors
//            }
//        });
//        return disabledTimes.toArray(new Timepoint[0]);
//    }
}