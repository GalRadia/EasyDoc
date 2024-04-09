package com.example.easydoc.ui.appointments;

import android.content.Intent;
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

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.R;
import com.example.easydoc.Utils.DatabaseRepository;
import com.example.easydoc.databinding.FragmentAppointmentNextBinding;
import com.example.easydoc.databinding.FragmentAppointmentsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AppointmentNextFragment extends Fragment {
    private FragmentAppointmentNextBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseRepository repository;
    private AppointmentsViewModel appointmentsViewModel;

    public AppointmentNextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appointmentsViewModel = new ViewModelProvider(requireActivity()).get(AppointmentsViewModel.class);
        repository = DatabaseRepository.getInstance();
        Calendar c = Calendar.getInstance();
        binding = FragmentAppointmentNextBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MaterialButton button = binding.finishB;
        Slider slider = binding.seekBar;
        TextInputEditText message = binding.editMessage;
//        TextInputEditText date= appointmentsBinding.appointmentDate;
//        TextInputEditText time= appointmentsBinding.appointmentTime;
        Bundle saveInstance = getArguments();
        String t = saveInstance.getString("appointmentTime");
        String d = saveInstance.getString("appointmentDate");


        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference userRef = mDatabase.child("users").child(user.getUid());
        button.setOnClickListener(view -> {
            Appointment appointment = new Appointment( d, t, message.getText().toString(),user.getDisplayName());
//            userRef.child("appointmentsID").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    List<String> appointmentsID = new ArrayList<>();
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        if (postSnapshot.exists())
//                            appointmentsID.add(postSnapshot.getValue().toString());
//                    }
//                    appointmentsID.add(appointment.getId());
//                    userRef.child("appointmentsID").setValue(appointmentsID);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // Getting Post failed, log a message
//                    Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
//                }
//            });
            repository.insertAppointment(appointment,getContext());
//            Calendar cal = Calendar.getInstance();
//            Intent intent = new Intent(Intent.ACTION_EDIT);
//            intent.setType("vnd.android.cursor.item/event");
//            intent.putExtra("beginTime", cal.getTimeInMillis());
//            intent.putExtra("allDay", true);
//            intent.putExtra("rrule", "FREQ=YEARLY");
//            intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
//            intent.putExtra("title", "A Test Event from android app");
//            startActivity(intent);

            //mDatabase.child("appointments").child(appointment.getId()).setValue(appointment);
            Navigation.findNavController(root).navigate(R.id.action_appointmentNextFragment_to_navigation_appointments);
        });


        // Inflate the layout for this fragment

        return root;

    }

}