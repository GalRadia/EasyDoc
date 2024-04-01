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
import java.util.Date;
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
//        Calendar[] days = new Calendar[3];
//        for (int i = 0; i < 3; i++) {
//            days[i] = Calendar.getInstance();
//            days[i].add(Calendar.DAY_OF_MONTH, i);
//        }
//        dpd.setDisabledDays(days);
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                (view, hourOfDay, minute, second) -> {
                    // Handle the time
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );
        tpd.setTimeInterval(1, 30);
        Timepoint[] timepoint = new Timepoint[3];
        timepoint[0] = new Timepoint(19);
        timepoint[1] = new Timepoint(19, 30);
        timepoint[2] = new Timepoint(21);
        tpd.setDisabledTimes(timepoint);
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
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(1712696400000L));
                List<Timepoint> disabledTimes = getDisabledTimeFromDate(c);
                tpd.setDisabledTimes(disabledTimes.toArray(new Timepoint[disabledTimes.size()]));
                tpd.show(getParentFragmentManager(), "Timepickerdialog");
            });


        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public List<Timepoint> getDisabledTimeFromDate(Calendar day){
        List <Timepoint> disabledTimes = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("appointments").orderByChild("date").equalTo(day.getTimeInMillis());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(postSnapshot.exists()){
                        String time = postSnapshot.child("time").getValue().toString();
                        int hour=Integer.parseInt(time.split(":")[0]);
                        int minute= Integer.parseInt(time.split(":")[1]);
                        Timepoint timepoint = new Timepoint(hour,minute);
                        disabledTimes.add(timepoint);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });
        return disabledTimes;
    }
}