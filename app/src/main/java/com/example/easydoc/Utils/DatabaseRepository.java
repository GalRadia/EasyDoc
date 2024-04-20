package com.example.easydoc.Utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.Due;
import com.example.easydoc.Model.Repeat;
import com.example.easydoc.Model.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.sql.Time;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DatabaseRepository {
    private static DatabaseRepository instance;
    private final MutableLiveData<UserAccount> userAccountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Appointment> nextAppointmentLiveData = new MutableLiveData<>();
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
    private final DatabaseReference userAppointmentsReference;
    private final MutableLiveData<DoctorOffice> doctorOfficeMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDoctorLiveData = new MutableLiveData<>();

    // Singleton pattern
    private DatabaseRepository() {
        // Initialize Firebase references
        // Fetch data from Firebase and set it in LiveData
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        appointmentsReference = database.getReference("appointments");
        usersReference = database.getReference("users");
        doctorOfficeReference = database.getReference("DoctorOffice");
        appointmentsWaitListReference = database.getReference("appointmentsWaitList");
        userAppointmentsReference = database.getReference("users").child(currentUser.getUid().toString()).child("appointmentsID");
        fetchAppointments();
        fetchUsers();
        fetchAppointmentsForUser();
        fetchCurrentUserDoctor();
        fetchDoctorOffice();
        fetchAppointmentsWaitList();
        fetchNextAppointment();
        fetchCurrentUser();
    }

    public static synchronized DatabaseRepository getInstance() {
        // Singleton pattern
        if (instance == null) {
            instance = new DatabaseRepository();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private void fetchCurrentUser() {
        usersLiveData.observeForever(users -> {
            for (UserAccount user : users) {
                if (user.getUid().equals(currentUser.getUid())) {
                    userAccountLiveData.postValue(user);
                    break;
                }
            }
        });

    }

    public void fetchNextAppointment() {
        // Observe the LiveData for changes in the isDoctor value
        // if the user is a doctor, get the next appointment from the appointments list
        // if the user is a patient, get the next appointment from the user's appointments list
        isDoctorLiveData.observeForever(isDoctor -> {
            if (isDoctor) {
                appointmentsLiveData.observeForever(appointments -> {
                    Appointment nextAppointment = findNextAppointment();
                    if (nextAppointment != null && appointments != null && !appointments.isEmpty()) {
                        nextAppointmentLiveData.postValue(nextAppointment);
                    } else {
                        nextAppointmentLiveData.postValue(null);
                    }
                });
            } else {
                userAppointmentsLiveData.observeForever(appointments -> {
                    Appointment nextAppointment = findNextAppointment();
                    if (nextAppointment != null && appointments != null && !appointments.isEmpty()) {
                        nextAppointmentLiveData.postValue(nextAppointment);
                    } else {
                        nextAppointmentLiveData.postValue(null);
                    }
                });
            }
        });
    }

    private void fetchAppointmentsWaitList() {
        // Fetch the appointments wait list from Firebase and set it in LiveData
        // Only fetch appointments that are not passed
        appointmentsWaitListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> appointmentsWaitList = new HashMap<>();
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    String date = appointmentSnapshot.getValue(String.class);
                    if (date != null && !Helper.isAppointmentPassed(date, "00:00")) {
                        appointmentsWaitList.put(appointmentSnapshot.getKey(), date);
                    }
                }
                appointmentsWaitListLiveData.postValue(appointmentsWaitList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDoctorOffice() {
        // Fetch the doctor's office details from Firebase and set it in LiveData
        // This is used to display the doctor's office details in the UI
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
        // Fetch all appointments from Firebase and set them in LiveData
        // Separate the appointments into two lists: passed and upcoming
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
        // Fetch all users from Firebase and set them in LiveData
        // Also, fetch the appointments list for each user
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

    private void fetchAppointmentsByIds(List<String> appointmentIds) {
        // Fetch the appointments details using the IDs and set them in LiveData
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


    public void fetchCurrentUserDoctor() {
        // Fetch the user's doctor status from Firebase and set it in LiveData
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

    public LiveData<UserAccount> getCurrentUserLiveData() {
        return userAccountLiveData;
    }

    public LiveData<List<Appointment>> getAppointmentsLiveData() {
        return appointmentsLiveData;
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

    public LiveData<Appointment> getNextAppointmentLiveData() {
        return nextAppointmentLiveData;
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

    public List<String> getWailistDatesList() {
        List<String> dates = new ArrayList<>();
        for (Map.Entry<String, String> entry : appointmentsWaitListLiveData.getValue().entrySet()) {
            isDoctorLiveData.observeForever(isDoctor -> {
                if (!isDoctor) {
                    if (entry.getKey().equals(currentUser.getUid())) {
                        dates.add(entry.getValue());
                    }
                } else dates.add(entry.getValue());
            });
        }
        return dates;
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

    public AbstractMap.SimpleEntry<String, String> getAppointmentFromWaitingList(String date) {
        for (Map.Entry<String, String> entry : appointmentsWaitListLiveData.getValue().entrySet()) {
            if (entry.getValue().equals(date)) {
                return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }


    public void setOfficeEndTime(String string) {
        doctorOfficeReference.child("endTime").setValue(string);
    }

    public void setOfficeMonthsInAdvance(String string) {
        doctorOfficeReference.child("monthsInAdvance").setValue(string);
    }

    public void setOfficePhone(String string) {
        doctorOfficeReference.child("phone").setValue(string);
    }

    public void setOfficeStartTime(String string) {
        doctorOfficeReference.child("startTime").setValue(string);
    }

    public Appointment findNextAppointment() {
        List<Appointment> appointmentList;
        if (isDoctorLiveData.getValue())
            appointmentList = appointmentsLiveData.getValue();
        else appointmentList = userAppointmentsLiveData.getValue();
        if (appointmentList == null || appointmentList.isEmpty()) {
            return null;
        }
        Collections.sort(appointmentList);
        return appointmentList.get(0);
    }

    public void removeAppointment(String appointmentID) {
        String date = getDateFromAppointmentID(appointmentID);
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot appointmentsIDSnapshot = userSnapshot.child("appointmentsID");
                    for (DataSnapshot appointmentIdSnapshot : appointmentsIDSnapshot.getChildren()) {
                        if (appointmentIdSnapshot.getValue(String.class).equals(appointmentID)) {
                            appointmentIdSnapshot.getRef().removeValue();
                            break; // Break because an appointment can only be under one user
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

    public boolean dateAvailable(String date) {
        // Check if the date has available appointments
        // If the date has less appointments than the maximum allowed, return true
        int count = 0;
        for (Appointment appointment : Objects.requireNonNull(appointmentsLiveData.getValue())) {
            if (appointment.getDate().equals(date)) {
                count++;
            }
        }
        return count < appointmentsInDay();
    }

    public int appointmentsInDay() {
        // Calculate the number of appointments that can be scheduled in a day
        String startTime = doctorOfficeMutableLiveData.getValue().getStartTime();
        String endTime = doctorOfficeMutableLiveData.getValue().getEndTime();
        int duration = Integer.parseInt(doctorOfficeMutableLiveData.getValue().getAppointmentDuration());
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);
        int endMinute = Integer.parseInt(endTime.split(":")[1]);

        // Convert start and end times to minutes since midnight
        int startMinutes = startHour * 60 + startMinute;
        int endMinutes = endHour * 60 + endMinute;

        // Calculate total available minutes for appointments
        int availableMinutes = endMinutes - startMinutes;

        // Return the number of appointments that can be fit into the available time
        return availableMinutes / duration;
    }


    public void addToWaitingList(String userID, String date) {
        appointmentsWaitListReference.child(userID).setValue(date);
    }

    private void deleteFromWaitingList(String userID) {
        appointmentsWaitListReference.child(userID).removeValue();
    }


    public void updateText(String appointmentID, String text) {
        appointmentsReference.child(appointmentID).child("text").setValue(text);
    }


    public void insertAppointment(Appointment appointment, Repeat repeat, Due due) throws RuntimeException {
        // Calculate the initial due date based on the specified Due enum
        Calendar dueCalendar;
        dueCalendar = Helper.stringToCalendar(appointment.getDate());
        dueCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(appointment.getTime().split(":")[0]));
        dueCalendar.set(Calendar.MINUTE, Integer.parseInt(appointment.getTime().split(":")[1]));
        // Adjust the due date based on the selection
        switch (due) {
            case ONE_MONTH:
                dueCalendar.add(Calendar.MONTH, 1);
                break;
            case TWO_MONTHS:
                dueCalendar.add(Calendar.MONTH, 2);
                break;
        }

        Date dueDate = dueCalendar.getTime();

        // Initialize repeat interval
        int repeatIntervalDays = 0;
        switch (repeat) {
            case ONCE_A_WEEK:
                repeatIntervalDays = 7;
                break;
            case ONCE_TWO_WEEKS:
                repeatIntervalDays = 14;
                break;
            case NO_REPEAT:
                // No additional appointments needed, just insert the initial one
                try {
                    saveAppointment(appointment, appointment.getDate());
                } catch (RuntimeException e) {
                    // Handle error
                    throw new RuntimeException("Failed to save appointment");
                }
                return;
        }

        // Repeat logic
        Calendar repeatCalendar = Helper.stringToCalendar(appointment.getDate());
        String text= appointment.getText();
        int i=1;
        while (repeatCalendar.getTime().before(dueDate)) {
            String newDate = Helper.calendarToString(repeatCalendar);
            if (isDateAndTimeAvailable(newDate, appointment.getTime())) {
                try {
                    String newText="No" + i + ". " + text; // Add the appointment number to the text
                    appointment.setText(newText);
                    i++;
                    saveAppointment(appointment, newDate);
                } catch (RuntimeException e) {
                    // Handle error
                    throw new RuntimeException("Failed to save appointment");
                }
            } else addToWaitingList(currentUser.getUid(), newDate);
            repeatCalendar.add(Calendar.DAY_OF_YEAR, repeatIntervalDays);
        }
    }

    private boolean isDateAndTimeAvailable(String date, String time) {
        for (Appointment appointment : appointmentsLiveData.getValue()) {
            if (appointment.getDate().equals(date) && appointment.getTime().equals(time)) {
                return false;
            }
        }
        return true;
    }

    private void saveAppointment(Appointment appointment, String appointmentDate) throws RuntimeException {
        Appointment newAppointment = new Appointment(appointment); // Copy the appointment
        newAppointment.setDate(appointmentDate); // Set the new date

        appointmentsReference.child(newAppointment.getId()).setValue(newAppointment).addOnFailureListener(e -> {
            // Handle error
            throw new RuntimeException("Failed to save appointment");
        });


        assert currentUser != null;
        usersReference.child(currentUser.getUid()).
                child("appointmentsID").push().setValue(newAppointment.getId())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to save appointment");
                });
    }


}