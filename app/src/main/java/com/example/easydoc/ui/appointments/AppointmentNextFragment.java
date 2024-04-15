package com.example.easydoc.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.Due;
import com.example.easydoc.Model.Repeat;
import com.example.easydoc.R;
import com.example.easydoc.Utils.Helper;
import com.example.easydoc.databinding.FragmentAppointmentNextBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;


public class AppointmentNextFragment extends Fragment {
    private FragmentAppointmentNextBinding binding;
    private FirebaseAuth mAuth;
    private AppointmentsViewModel appointmentsViewModel;
    private MaterialButton button;
    private Slider slider;
    private TextInputEditText message;

    public AppointmentNextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appointmentsViewModel = new ViewModelProvider(requireActivity()).get(AppointmentsViewModel.class);
        binding = FragmentAppointmentNextBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SetupUI();
        InitUI();
        return root;

    }

    private void InitUI() {
        Bundle saveInstance = getArguments();
        String t = saveInstance.getString("appointmentTime");
        String d = saveInstance.getString("appointmentDate");
        int repreat = saveInstance.getInt("recurrent");
        int duration = saveInstance.getInt("duration");
        Repeat repeat = Repeat.values()[repreat];
        Due due = Due.values()[duration];
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        button.setOnClickListener(view -> {
            Appointment appointment = new Appointment(d, t, message.getText().toString(), user.getDisplayName());
            try {
                appointmentsViewModel.addAppointment(appointment, repeat, due);
                Toast.makeText(getContext(), getString(R.string.appointment_added), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), getString(R.string.appointment_failed), Toast.LENGTH_LONG).show();
            }
            addToCalanderIntent(appointment);
            Navigation.findNavController(requireView()).navigate(R.id.action_appointmentNextFragment_to_navigation_appointments);
        });
    }

    private void SetupUI() {
        button = binding.finishB;
        message = binding.editMessage;
    }


    private void addToCalanderIntent(Appointment appointment) {
        DoctorOffice doctorOffice = appointmentsViewModel.getDoctorOffice().getValue();
        Calendar c = Helper.stringToCalendar(appointment.getDate());
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(appointment.getTime().split(":")[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(appointment.getTime().split(":")[1]));
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, c.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Appointment with ")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, doctorOffice.getAddress())
                .putExtra(CalendarContract.Events.DESCRIPTION, appointment.getText())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(CalendarContract.Events.DURATION, doctorOffice.getAppointmentDuration())
                .putExtra(CalendarContract.Events.HAS_ALARM, 1)
                .putExtra(CalendarContract.Events.ALLOWED_REMINDERS, 1)
                .putExtra(CalendarContract.Reminders.MINUTES, 15)
                .putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)

                .putExtra(Intent.EXTRA_EMAIL, mAuth.getCurrentUser().getEmail());
        startActivity(intent);
    }

}