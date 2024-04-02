package com.example.easydoc.ui.appointments;

import android.icu.util.Calendar;
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment {

    private FragmentAppointmentsBinding binding;
    TextInputEditText dateLayout;
    TextInputEditText timeLayout;
    TimePickerDialog tpd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppointmentsViewModel appointmentsViewModel =
                new ViewModelProvider(this).get(AppointmentsViewModel.class);

        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TimePickerDialog tpd = initializeTimePicker();
        Button nextButton = binding.buttonNext;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Handle the date
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        List<String> fullyBookedDays = getAllFullyBookedDays( new FullyBookedDaysCallback() {
            @Override
            public void onFullyBookedDays(List<String> fullyBookedDays) {
                // Use the result list here
                int x=1;

            }
        });


        dateLayout = binding.appointmentDate;
        timeLayout = binding.appointmentTime;
        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            dateLayout.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
        });


        appointmentsViewModel.getText().observe(getViewLifecycleOwner(), s -> {

            nextButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("appointmentDate", dateLayout.getText().toString());
                bundle.putString("appointmentTime", timeLayout.getText().toString());
                Navigation.findNavController(root).navigate(R.id.action_navigation_appointments_to_appointmentNextFragment, bundle);
            });

            dateLayout.setOnClickListener(v -> dpd.show(getParentFragmentManager(), "Datepickerdialog"));
            timeLayout.setOnClickListener(view -> {
                String date = dateLayout.getText().toString();

                getDisabledTimeFromDate(date, new TimepointCallback() {
                    @Override
                    public void onTimepointsLoaded(List<Timepoint> timepoints) {
                        // Use the result list here
                        List<Timepoint> arr;
                        arr = timepoints;
                        tpd.setDisabledTimes(arr.toArray(new Timepoint[arr.size()]));
                        tpd.show(getParentFragmentManager(), "Timepickerdialog");
                        tpd.onDestroy();
                        initializeTimePicker(); //Change


                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Handle error
                        Log.d("@@@@@@@@@@@@@@@@@@@@@@@@@@@@", "onError: ");
                    }
                });


            });


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
            timeLayout.setText(hourOfDay + ":" + min);
        });
        return tpd;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void getDisabledTimeFromDate(String day, TimepointCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("appointments").orderByChild("date").equalTo(day);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Timepoint> disabledTimes = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.exists()) {
                        String time = postSnapshot.child("time").getValue(String.class);
                        if (time != null) {
                            String[] timeParts = time.split(":");
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            Timepoint timepoint = new Timepoint(hour, minute);
                            disabledTimes.add(timepoint);
                        }
                    }
                }
                callback.onTimepointsLoaded(disabledTimes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    public List<String> getAllFullyBookedDays(final FullyBookedDaysCallback callback) {
        List<String> fullyBookedDays = new ArrayList<>();

        // Get a reference to the appointments node in the Firebase database
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");

        // Initialize a HashMap to store the count of occurrences for each date
        final Map<String, Integer> dateOccurrences = new HashMap<>();

        // Add a ValueEventListener to retrieve all appointments
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all appointments
                for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                    // Get the date of each appointment
                    String date = appointmentSnapshot.child("date").getValue(String.class);
                    // Increment the count of occurrences for this date
                    if (dateOccurrences.containsKey(date)) {
                        dateOccurrences.put(date, dateOccurrences.get(date) + 1);
                    } else {
                        dateOccurrences.put(date, 1);
                    }
                }

                // Iterate through the dates and find those with more than 20 occurrences
                for (Map.Entry<String, Integer> entry : dateOccurrences.entrySet()) {
                    String date = entry.getKey();
                    int occurrences = entry.getValue();
                    if (occurrences >= 2) {
                        fullyBookedDays.add(date);
                        // Add your logic here to handle the dates with more than 20 occurrences
                    }
                }
                callback.onFullyBookedDays(fullyBookedDays);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                Log.e("MainActivity", "Error querying database: " + databaseError.getMessage());
            }
        });
        return fullyBookedDays;
    }


}


