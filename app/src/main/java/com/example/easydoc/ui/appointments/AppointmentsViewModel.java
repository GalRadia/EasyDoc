package com.example.easydoc.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Interfaces.BusyDaysCallback;
import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.Due;
import com.example.easydoc.Model.Repeat;
import com.example.easydoc.Utils.DatabaseRepository;
import com.example.easydoc.Utils.Helper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsViewModel extends ViewModel {
    private final DatabaseRepository repository;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
    private final MutableLiveData<String> mText;

    public AppointmentsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
        repository = DatabaseRepository.getInstance();
    }

    public LiveData<List<Appointment>> getAppointments() {
        return repository.getAppointmentsLiveData();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public boolean isDateAvailable(String date) {
        return repository.dateAvailable(date);
    }

    public void getBusyDates(BusyDaysCallback<List<Calendar>> callback) {
        Query query = databaseReference.orderByChild("date");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Integer> dateCounts = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (appointment != null && appointment.getDate() != null) {
                        String date = appointment.getDate();
                        dateCounts.put(date, dateCounts.getOrDefault(date, 0) + 1);
                    }
                }

                List<Calendar> busyDates = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : dateCounts.entrySet()) {
                    if (entry.getValue() > 4) {
                        Calendar date = Helper.stringToCalendar(entry.getKey());
                        if (date != null) {
                            busyDates.add(date);
                        }
                    }
                }

                callback.onSuccess(busyDates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void addToWaitingList(String userID, String date) {
        repository.addToWaitingList(userID, date);
    }

    public LiveData<DoctorOffice> getDoctorOffice() {
        return repository.getDoctorOfficeLiveData();
    }


    public Timepoint[] getDisabledTimepointsFromDate(String date) {
        List<Appointment> appointments = repository.getAppointmentsLiveData().getValue();
        if (appointments == null) {
            return new Timepoint[0]; // Return an empty array if there are no appointments.
        }

        List<Timepoint> disabledTimepoints = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getDate() != null && appointment.getDate().equals(date)) {
                // Assuming your Appointment model's time is stored as a String in "HH:mm" format
                String[] parts = appointment.getTime().split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                disabledTimepoints.add(new Timepoint(hours, minutes));


            }
        }

        return disabledTimepoints.toArray(new Timepoint[0]);
    }
    public void addAppointment(Appointment appointment, Repeat repeat, Due due) {
        repository.insertAppointment(appointment, repeat, due);
    }


}