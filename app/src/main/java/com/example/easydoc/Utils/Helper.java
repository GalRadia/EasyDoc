package com.example.easydoc.Utils;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import androidx.annotation.NonNull;

import com.example.easydoc.Interfaces.AppointmentCallback;
import com.example.easydoc.Interfaces.AppointmentIDCallback;
import com.example.easydoc.Model.Appointment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Helper {

    public static Calendar stringToCalendar(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateString);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static Timepoint stringToTimepoint(String timepointString) {
        int hourse = Integer.parseInt(timepointString.split(":")[0]);
        int minutes = Integer.parseInt(timepointString.split(":")[1]);
        return new Timepoint(hourse, minutes);
    }

    public static String calendarToString(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }

    public static void getAllAppointmentsIDFromUser(DatabaseReference ref, String userId, AppointmentIDCallback callback) {
        Query query = ref.child("users").child(userId).child("appointmentsID");
        List<String> appointmentID = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentIDSnapshot : snapshot.getChildren()) {
                   appointmentID.add(appointmentIDSnapshot.getValue(String.class));
                }
                callback.onAppointmentIDLoaded(appointmentID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public static void getAllAppointmentsFromList(DatabaseReference ref, List<String> appointmentsIDs, AppointmentCallback callback) {
        Query query = ref.child("appointments");
        List<Appointment> appointments = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    if (appointmentsIDs.contains(appointmentSnapshot.getKey())) {
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                        appointments.add(appointment);
                    }
                }
                callback.onAppointmentLoaded(appointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
