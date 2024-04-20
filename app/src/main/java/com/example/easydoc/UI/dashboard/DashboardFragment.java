package com.example.easydoc.UI.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easydoc.Adapters.AppointmentAdapter;
import com.example.easydoc.Interfaces.AppointmentCallback;
import com.example.easydoc.Model.Appointment;
import com.example.easydoc.databinding.FragmentDashboardBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AppointmentAdapter adapter;
    private AppointmentAdapter adapterPassedAppointments;
    private MaterialButton showWaitListButton;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewPassedAppointments;
    private DashboardViewModel dashboardViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SetupUI();
        initializeRecyclerViews();
        observeLiveData();

        return root;
    }

    private void SetupUI() {
        showWaitListButton = binding.showWaitList;
        showWaitListButton.setOnClickListener(v -> {
            List<String> dates = dashboardViewModel.getWaitlistDates();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Waitlist Dates");
            if (dates.isEmpty()) {
                builder.setMessage("No waitlist dates available");
                builder.show();
                return;
            }
            builder.setItems(dates.toArray(new String[0]), (dialog, which) -> {
            });
            builder.show();

        });
    }

    private void initializeRecyclerViews() {
        recyclerView = binding.recyclerView;
        recyclerViewPassedAppointments = binding.recyclerViewPassedAppointments;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPassedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppointmentAdapter(getContext(), new ArrayList<>(), false); // false for not passed appointments
        adapterPassedAppointments = new AppointmentAdapter(getContext(), new ArrayList<>(), true); // true for passed appointments

        setupAdapterCallbacks(adapter);
        setupAdapterCallbacks(adapterPassedAppointments);

        recyclerView.setAdapter(adapter);
        recyclerViewPassedAppointments.setAdapter(adapterPassedAppointments);


    }

    private void setupAdapterCallbacks(AppointmentAdapter adapter) {
        adapter.setAppointmentCallback(new AppointmentCallback() {
            @Override
            public void onRemoveAppointment(String appointmentId) {
                dashboardViewModel.removeAppointment(appointmentId);
            }

            @Override
            public void onUpdateAppointmentText(String appointmentId, String text) {
                dashboardViewModel.updateAppointment(appointmentId, text);
            }
        });
    }

    private void observeLiveData() {
        // Update the list when the data changes
        // This is a LiveData object, so it will automatically update the UI when the data changes
        // the doctor can see all appointments, the user can see only his appointments
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), text -> binding.textDashboard.setText(text));
        dashboardViewModel.isDoctor().observe(getViewLifecycleOwner(), isDoctor -> {
            LiveData<List<Appointment>> appointmentsLiveData = isDoctor ? dashboardViewModel.getAppointments() : dashboardViewModel.getUserAppointments();
            LiveData<List<Appointment>> passedAppointmentsLiveData = isDoctor ? dashboardViewModel.getPassedAppointments() : dashboardViewModel.getPassedAppointmentFromUser();
            appointmentsLiveData.observe(getViewLifecycleOwner(), appointments -> {
                if (appointments.isEmpty()) {
                    binding.noAppointments.setVisibility(View.VISIBLE);
                } else {
                    binding.noAppointments.setVisibility(View.GONE);
                }
                adapter.setAppointments(appointments);
            });
            passedAppointmentsLiveData.observe(getViewLifecycleOwner(), appointments -> {
                if (appointments.isEmpty()) {
                    binding.noPassedAppointments.setVisibility(View.VISIBLE);
                } else {
                    binding.noPassedAppointments.setVisibility(View.GONE);
                }
                adapterPassedAppointments.setAppointments(appointments);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
