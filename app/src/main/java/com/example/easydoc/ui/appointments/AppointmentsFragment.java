package com.example.easydoc.ui.appointments;

import static com.example.easydoc.Utils.Helper.stringToCalendar;

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

import com.example.easydoc.Callbacks.TimepointCallback;
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
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private FragmentAppointmentsBinding binding;

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

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                (view, hourOfDay, minute, second) -> {
                    // Handle the time
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );
        tpd.setTimeInterval(1, 30);
        final TextInputEditText dateLayout = binding.appointmentDate;
        final TextInputEditText timeLayout = binding.appointmentTime;
        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            dateLayout.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
        });
        tpd.setOnTimeSetListener((view, hourOfDay, minute, second) -> {
            String min = minute > 9 ? "" + minute : "0" + minute;
            timeLayout.setText(hourOfDay + ":" + min);
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

}