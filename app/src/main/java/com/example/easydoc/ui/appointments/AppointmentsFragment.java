package com.example.easydoc.ui.appointments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.easydoc.Interfaces.BusyDaysCallback;
import com.example.easydoc.Interfaces.FullyBookedDaysCallback;
import com.example.easydoc.Interfaces.TimepointCallback;
import com.example.easydoc.R;
import com.example.easydoc.databinding.FragmentAppointmentsBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment {

    private FragmentAppointmentsBinding binding;
    private TextInputEditText appointmentDate;
    private TextInputEditText appointmentTime;
    private TimePickerDialog tpd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppointmentsViewModel appointmentsViewModel =
                new ViewModelProvider(this).get(AppointmentsViewModel.class);

        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button nextButton = binding.buttonNext;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Handle the date
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
//        getAllFullyBookedDays(new FullyBookedDaysCallback() {
//            @Override
//            public void onFullyBookedDays(List<String> fullyBookedDays) {
//                // Use the result list here
//                int x = 1;
//
//            }
//        });

        appointmentDate = binding.appointmentDate;
        appointmentTime = binding.appointmentTime;
        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            appointmentDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
        });


        nextButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("appointmentDate", appointmentDate.getText().toString());
            bundle.putString("appointmentTime", appointmentTime.getText().toString());
            Navigation.findNavController(root).navigate(R.id.action_navigation_appointments_to_appointmentNextFragment, bundle);
        });

        appointmentDate.setOnClickListener(v -> {
                    this.tpd= new TimePickerDialog();
                    this.tpd=initializeTimePicker();

                    appointmentsViewModel.getBusyDates(new BusyDaysCallback<List<Calendar>>() {
                        @Override
                        public void onSuccess(List<Calendar> result) {
                            Calendar[] busyDates = result.toArray(new Calendar[0]);
                            dpd.setDisabledDays(busyDates);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                    dpd.show(getParentFragmentManager(), "Datepickerdialog");
                }
                );
        appointmentTime.setOnClickListener(view -> {
            String date = appointmentDate.getText().toString();
            Timepoint[] disabledTimes = appointmentsViewModel.getDisabledTimepointsFromDate(date);
            tpd.setDisabledTimes(disabledTimes);
            tpd.show(getParentFragmentManager(), "Timepickerdialog");
            this.tpd=initializeTimePicker();



        });
        return root;
    }

    private TimePickerDialog initializeTimePicker() {
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                (view, hourOfDay, minute, second) -> {
                    // Handle the time
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );
        tpd.setTimeInterval(1, 30);
        tpd.setMinTime(8, 0, 0);
        tpd.setMaxTime(20, 0, 0);
        tpd.setOnTimeSetListener((view, hourOfDay, minute, second) -> {
            String min = minute > 9 ? "" + minute : "0" + minute;
            appointmentTime.setText(hourOfDay + ":" + min);
        });
        return tpd;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




//    public void getAllFullyBookedDays(final FullyBookedDaysCallback callback) {
//        List<String> fullyBookedDays = new ArrayList<>();
//
//        // Get a reference to the appointments node in the Firebase database
//        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");
//
//        // Initialize a HashMap to store the count of occurrences for each date
//        final Map<String, Integer> dateOccurrences = new HashMap<>();
//
//        // Add a ValueEventListener to retrieve all appointments
//        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                // Iterate through all appointments
//                for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
//                    // Get the date of each appointment
//                    String date = appointmentSnapshot.child("date").getValue(String.class);
//                    // Increment the count of occurrences for this date
//                    if (dateOccurrences.containsKey(date)) {
//                        dateOccurrences.put(date, dateOccurrences.get(date) + 1);
//                    } else {
//                        dateOccurrences.put(date, 1);
//                    }
//                }
//
//                // Iterate through the dates and find those with more than 20 occurrences
//                for (Map.Entry<String, Integer> entry : dateOccurrences.entrySet()) {
//                    String date = entry.getKey();
//                    int occurrences = entry.getValue();
//                    if (occurrences >= 2) {
//                        fullyBookedDays.add(date);
//                        // Add your logic here to handle the dates with more than 20 occurrences
//                    }
//                }
//                callback.onFullyBookedDays(fullyBookedDays);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle potential errors
//                Log.e("MainActivity", "Error querying database: " + databaseError.getMessage());
//            }
//        });
//    }


}


