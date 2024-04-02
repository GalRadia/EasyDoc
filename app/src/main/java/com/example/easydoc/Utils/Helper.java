package com.example.easydoc.Utils;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;

import com.example.easydoc.Logic.SortedListComperator;
import com.example.easydoc.Model.Appointment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

    public static SortedList<Appointment> getAllAppointmentsFromUser(DatabaseReference ref, String userId) {
        List<String> appointmentsIDs = new ArrayList<>();
        ref.child("users").child(userId).child("appointments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    appointmentsIDs.add(appointmentSnapshot.getValue().toString());
                }
                //callback.onAppointmentsIDLoaded(getAllAppointmentsFromList(ref, appointmentsIDs,callback));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return (SortedList<Appointment>) getAllAppointmentsFromList(ref, appointmentsIDs);
    }
    public static List<Appointment> getAllAppointmentsFromList(DatabaseReference ref, List<String> appointmentsIDs) {
        SortedList<Appointment> appointmentsList = new SortedList<>(Appointment.class, new SortedListComperator());
        for (String appointmentID : appointmentsIDs) {
            ref.child("appointments").child(appointmentID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    appointmentsList.add(appointment);
                   // callback.onAppointmentsLoaded(appointmentsList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return (List<Appointment>) appointmentsList;
    }
}
