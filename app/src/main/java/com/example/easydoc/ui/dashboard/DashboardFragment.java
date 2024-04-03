package com.example.easydoc.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easydoc.Adapters.AppointmentAdapter;
import com.example.easydoc.databinding.FragmentDashboardBinding;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView and Adapter
        initializeRecyclerView();

        // Observe the LiveData for the text
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Observe the LiveData for appointments
        dashboardViewModel.getUserAppointments().observe(getViewLifecycleOwner(), appointments -> {
            // Check if adapter is null, which should not happen in this setup, but just in case
            if (adapter != null) {
                adapter.setAppointments(appointments);
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