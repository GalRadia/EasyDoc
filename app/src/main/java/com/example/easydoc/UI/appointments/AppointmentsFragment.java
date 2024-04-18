package com.example.easydoc.UI.appointments;

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
import com.example.easydoc.Model.Due;
import com.example.easydoc.Model.Repeat;
import com.example.easydoc.R;
import com.example.easydoc.Utils.Helper;
import com.example.easydoc.databinding.FragmentAppointmentsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextInputLayout appointmentTimeLayout;
    private TextInputLayout appointmentDateLayout;
    private TimePickerDialog tpd;
    private DatePickerDialog dpd;
    private MaterialButton onceAweek;
    private Button nextButton;
    private Button waitListButton;
    private MaterialButton once2Weeks;
    private MaterialButton noRepeat;
    private MaterialButton month;
    private MaterialButton twoMonths;
    private MaterialButtonToggleGroup durationToggleGroup;
    private MaterialButtonToggleGroup repeatToggleGroup;
    private LiveData<DoctorOffice> doctorOfficeLiveData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AppointmentsViewModel appointmentsViewModel =
                new ViewModelProvider(this).get(AppointmentsViewModel.class);


        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        doctorOfficeLiveData = appointmentsViewModel.getDoctorOffice();
        SetupUI();
        InitUI(appointmentsViewModel, root);

        return root;
    }

    private void InitUI(AppointmentsViewModel appointmentsViewModel, View root) {
        waitListButton.setOnClickListener(v -> addAppointmentToWaitList(appointmentsViewModel));
        repeatToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> { // Show the duration toggle group if the user selects a recurrent appointment
            if (isChecked) {
                if (checkedId == R.id.OnceWeek || checkedId == R.id.Once2Weeks) {
                    durationToggleGroup.setVisibility(View.VISIBLE);
                    month.setChecked(true);
                } else {
                    durationToggleGroup.clearChecked();
                    durationToggleGroup.setVisibility(View.GONE);
                }
            }
        });

        appointmentTime.setEnabled(false); // Disable the appointment time, the dialog will enable it

        nextButton.setOnClickListener(v -> {
            if (!validateText())
                return;
            Bundle bundle = new Bundle();
            bundle.putString("appointmentDate", appointmentDate.getText().toString());
            bundle.putString("appointmentTime", appointmentTime.getText().toString());
            bundle.putInt("recurrent", getRepeat().ordinal());
            bundle.putInt("duration", getDuration().ordinal());
            Navigation.findNavController(root).navigate(R.id.action_navigation_appointments_to_appointmentNextFragment, bundle);
        }); // Navigate to the next fragment with the appointment details

        appointmentDate.setOnClickListener(v -> {
            /*
             * Initialize the DatePickerDialog with the disabled times
             * if the date is today, the minimum time is the current time
             * if the date is not today, the minimum time is the start time of the doctor's office
             * I used 3rd party library to handle the time and date pickers picker dialog
             * because the default android time and date pickers are not user-friendly
             */
                    this.dpd = new DatePickerDialog();
                    this.dpd = initializeDatePicker();
                    this.tpd = new TimePickerDialog();
                    this.tpd = initializeTimePicker();
                    appointmentTime.setEnabled(true);
                    appointmentDateLayout.setError(null);

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
            /*
                * Initialize the TimePickerDialog with the disabled times
                * if the date is today, the minimum time is the current time
                * if the date is not today, the minimum time is the start time of the doctor's office
                * I used 3rd party library to handle the time and date pickers picker dialog
                * because the default android time and date pickers are not user-friendly
                * but the library is not maintained anymore and there are some issues with it
                * 1. you cant un disable the times so i initialized the time picker dialog every time the user clicks on the time
                * 2. you can pick the minimum and maximum time twice
             */
            this.dpd = new DatePickerDialog();
            this.dpd = initializeDatePicker();
            this.tpd = new TimePickerDialog();
            this.tpd = initializeTimePicker();
            appointmentTimeLayout.setError(null);
            String date = appointmentDate.getText().toString();
            Timepoint[] disabledTimes = appointmentsViewModel.getDisabledTimepointsFromDate(date);
            tpd.setDisabledTimes(disabledTimes);
            tpd.show(getParentFragmentManager(), "Timepickerdialog");


        });
    }

    private void SetupUI() {
        appointmentTimeLayout = binding.appointmentTimeLayout;
        appointmentDateLayout = binding.appointmentDateLayout;
        nextButton = binding.buttonNext;
        waitListButton = binding.waitList;
        repeatToggleGroup = binding.repeatToggleGroup;
        onceAweek = binding.OnceWeek;
        once2Weeks = binding.Once2Weeks;
        durationToggleGroup = binding.DueToggleGroup;
        noRepeat = binding.noRepeat;
        appointmentDate = binding.appointmentDate;
        appointmentTime = binding.appointmentTime;
        month = binding.month;
        twoMonths = binding.twoMonths;
    }

    private Due getDuration() {
        if (month.isChecked())
            return Due.ONE_MONTH;
        if (twoMonths.isChecked())
            return Due.TWO_MONTHS;
        return Due.NONE;
    }

    private Repeat getRepeat() {
        if (onceAweek.isChecked())
            return Repeat.ONCE_A_WEEK;
        if (once2Weeks.isChecked())
            return Repeat.ONCE_TWO_WEEKS;
        return Repeat.NO_REPEAT;
    }


    private boolean validateText() {
        if (appointmentDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            appointmentDateLayout.setError("Please select a time");
            return false;
        }
        if (appointmentTime.getText().toString().isEmpty()) {
            appointmentTimeLayout.setError("Please select a time");
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
        if (currentMinuteTime > duration) // If the current time is greater than the duration, increment the current hour time
            currentHourTime++;


        tpd.setTimeInterval(1, duration); // Set the time interval to the appointment duration
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DATE) == Helper.stringToCalendar(appointmentDate.getText().toString()).get(Calendar.DATE)) {
            tpd.setMinTime(currentHourTime, 0, 0);
            if (currentHourTime > eHour) {
                tpd.setMinTime(sHour, sMinute, 0);
                tpd.setMaxTime(sHour, sMinute, 0);
                Toast.makeText(getContext(), "End of a day, no time available", Toast.LENGTH_SHORT).show();
                return tpd; // If the current time is greater than the end time, return the time picker dialog
            }
        } else {
            tpd.setMinTime(sHour, sMinute, 0);

        }
        tpd.setMaxTime(eHour, eMinute, 0);

        tpd.setOnTimeSetListener((view, hourOfDay, minute, second) -> {
            String min = minute > 9 ? "" + minute : "0" + minute;
            if ((hourOfDay > eHour) || (hourOfDay == eHour && minute > eMinute) ||
                    (hourOfDay < sHour) || (hourOfDay == sHour && minute < sMinute)) {
                // Clear the appointment time and date TextViews
                appointmentTime.setText("");
                appointmentDate.setText("");
                // Display a toast message indicating the time is not available
                Toast.makeText(getContext(), "Time is not available", Toast.LENGTH_SHORT).show();
                return;
            }
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
        dpd.setFirstDayOfWeek(Calendar.SUNDAY);// Set the first day of the week to Sunday
        DoctorOffice doc = this.doctorOfficeLiveData.getValue();
        dpd.setMinDate(Calendar.getInstance());// Set the minimum date to today
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, Integer.parseInt(doc.getMonthsInAdvance())); // Set the maximum date to the months in advance
        dpd.setMaxDate(maxDate);
        Calendar calendar = Calendar.getInstance();
        List<Calendar> disabledDays = new ArrayList<>();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Start from tomorrow
        int days = Integer.parseInt(doc.getMonthsInAdvance()) * 31;
        for (int i = 0; i < days; i++) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                disabledDays.add((Calendar) calendar.clone()); // Add the weekend days to the disabled days
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        dpd.setDisabledDays(disabledDays.toArray(new Calendar[0]));
        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
            String month = monthOfYear < 9 ? "0" + (monthOfYear + 1) : "" + (monthOfYear + 1);
            String day = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
            appointmentDate.setText(day + "/" + (month) + "/" + year);
            appointmentTime.setText(""); // Clear the appointment time

        });


        return dpd;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void addAppointmentToWaitList(AppointmentsViewModel appointmentsViewModel) {
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            date= Helper.getValidDate(date);
            if (appointmentsViewModel.isDateAvailable(date))
                Toast.makeText(getContext(), "Date is available", Toast.LENGTH_SHORT).show();
            else {
                appointmentsViewModel
                        .addToWaitingList(FirebaseAuth.getInstance().getCurrentUser().getUid(), date);
                Toast.makeText(getContext(), "Added to waiting list", Toast.LENGTH_SHORT).show();
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Select a date to add to waiting list");
        datePickerDialog.show();


    }


}


