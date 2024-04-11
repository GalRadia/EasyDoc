package com.example.easydoc.ui.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.easydoc.Interfaces.BusyDaysCallback;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.R;
import com.example.easydoc.Utils.Helper;
import com.example.easydoc.databinding.FragmentAppointmentsBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private FragmentAppointmentsBinding binding;
    private TextInputEditText appointmentDate;
    private TextInputEditText appointmentTime;
    private TimePickerDialog tpd;
    private DatePickerDialog dpd;
    private LiveData<DoctorOffice> doctorOfficeLiveData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AppointmentsViewModel appointmentsViewModel =
                new ViewModelProvider(this).get(AppointmentsViewModel.class);

        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        doctorOfficeLiveData = appointmentsViewModel.getDoctorOffice();
        Button nextButton = binding.buttonNext;


        appointmentDate = binding.appointmentDate;
        appointmentTime = binding.appointmentTime;
        appointmentTime.setEnabled(false);

        nextButton.setOnClickListener(v -> {
            if(!validateText())
                return;
            Bundle bundle = new Bundle();
            bundle.putString("appointmentDate", appointmentDate.getText().toString());
            bundle.putString("appointmentTime", appointmentTime.getText().toString());
            Navigation.findNavController(root).navigate(R.id.action_navigation_appointments_to_appointmentNextFragment, bundle);
        });

        appointmentDate.setOnClickListener(v -> {
                    this.dpd = new DatePickerDialog();
                    this.dpd = initializeDatePicker();
                    this.tpd = new TimePickerDialog();
                    this.tpd = initializeTimePicker();
                    appointmentTime.setEnabled(true);

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
            this.dpd = new DatePickerDialog();
            this.dpd = initializeDatePicker();
            this.tpd = new TimePickerDialog();
            this.tpd = initializeTimePicker();
            String date = appointmentDate.getText().toString();
            Timepoint[] disabledTimes = appointmentsViewModel.getDisabledTimepointsFromDate(date);
            tpd.setDisabledTimes(disabledTimes);

            tpd.show(getParentFragmentManager(), "Timepickerdialog");


        });

        return root;
    }

    private boolean validateText() {
        if (appointmentDate.getText().toString().isEmpty()) {
            appointmentDate.setError("Please select a date");
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (appointmentTime.getText().toString().isEmpty()) {
            appointmentTime.setError("Please select a time");
            Toast.makeText(getContext(), "Please select a time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        String sTime = doctorOfficeLiveData.getValue().getStartTime();
        String eTime = doctorOfficeLiveData.getValue().getEndTime();
        int sHour = Integer.parseInt(sTime.split(":")[0]);
        int sMinute = Integer.parseInt(sTime.split(":")[1]);
        int eHour = Integer.parseInt(eTime.split(":")[0]);
        int eMinute = Integer.parseInt(eTime.split(":")[1]);
        int duration = Integer.parseInt(doctorOfficeLiveData.getValue().getAppointmentDuration());
        int currentHourTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinuteTime = Calendar.getInstance().get(Calendar.MINUTE);
        if(currentMinuteTime>duration)
            currentHourTime++;


        tpd.setTimeInterval(1, duration);
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DATE) == Helper.stringToCalendar(appointmentDate.getText().toString()).get(Calendar.DATE)) {
            tpd.setMinTime(currentHourTime, 0, 0);
            if(currentHourTime>eHour)
            {
                tpd.setMinTime(sHour, sMinute, 0);
                tpd.setMaxTime(sHour, sMinute, 0);
                return tpd;
            }
        } else{
            tpd.setMinTime(sHour, sMinute, 0);

        }
        tpd.setMaxTime(eHour, eMinute, 0);

        tpd.setOnTimeSetListener((view, hourOfDay, minute, second) -> {
            String min = minute > 9 ? "" + minute : "0" + minute;
            appointmentTime.setText(hourOfDay + ":" + min);
        });
        return tpd;
    }

    private DatePickerDialog initializeDatePicker() {
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Handle the date
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            appointmentDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
        });
        dpd.setMinDate(Calendar.getInstance());
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 1);
        dpd.setMaxDate(maxDate);
        Calendar calendar = Calendar.getInstance();
        List<Calendar> disabledDays = new ArrayList<>();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Start from tomorrow

        for (int i = 0; i < 30; i++) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                disabledDays.add((Calendar) calendar.clone());
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        dpd.setDisabledDays(disabledDays.toArray(new Calendar[0]));

        return dpd;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}


