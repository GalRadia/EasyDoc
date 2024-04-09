package com.example.easydoc.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easydoc.Adapters.AppointmentAdapter;
import com.example.easydoc.Interfaces.AppointmentCallback;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.databinding.FragmentDashboardBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AppointmentAdapter adapter;

    private MaterialButton removeButton;
    private MaterialButton updateTextButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView and Adapter
        //initializeRecyclerVie();
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize your adapter with an empty list
        adapter = new AppointmentAdapter(getContext(), new ArrayList<>());
        adapter.setAppointmentCallback(new AppointmentCallback() {
            @Override
            public void onRemoveAppointment(String appointmentId) {
                // Handle the appointment removal
                dashboardViewModel.removeAppointment(appointmentId);
            }

            @Override
            public void onUpdateAppointmentText(String appointmentId, String text) {
                // Handle the appointment update
                dashboardViewModel.updateAppointment(appointmentId, text);
            }
        });
        recyclerView.setAdapter(adapter);

        // Observe the LiveData for the text
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        dashboardViewModel.isDoctor().observe(getViewLifecycleOwner(), isDoctor -> {
            if (isDoctor) {
                dashboardViewModel.getAppointments().observe(getViewLifecycleOwner(), appointments -> {
                    // Check if adapter is null, which should not happen in this setup, but just in case
                    if (adapter != null) {
                        adapter.setAppointments(appointments);
                    }
                });
                // Show the remove and update buttons
//                removeButton.setVisibility(View.VISIBLE);
                updateTextButton.setVisibility(View.GONE);
            } else {
                dashboardViewModel.getUserAppointments().observe(getViewLifecycleOwner(), appointments -> {
                    // Check if adapter is null, which should not happen in this setup, but just in case
                    if (adapter != null) {
                        adapter.setAppointments(appointments);
                    }
                });
                // Hide the remove and update buttons
//                removeButton.setVisibility(View.GONE);
//                updateTextButton.setVisibility(View.GONE);
            }
        });


        return root;
    }

    private void initializeRecyclerView() {
        // Assuming the RecyclerView is correctly defined in your fragment_dashboard.xml
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize your adapter with an empty list
        adapter = new AppointmentAdapter(getContext(), new ArrayList<>());

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}