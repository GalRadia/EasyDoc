package com.example.easydoc.ui.dashboard;

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
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AppointmentAdapter adapter;
    private AppointmentAdapter adapterPassedAppointments;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeRecyclerViews(dashboardViewModel);

        observeLiveData(dashboardViewModel);

        return root;
    }

    private void initializeRecyclerViews(DashboardViewModel dashboardViewModel) {
        RecyclerView recyclerView = binding.recyclerView;
        RecyclerView recyclerViewPassedAppointments = binding.recyclerViewPassedAppointments;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPassedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppointmentAdapter(getContext(), new ArrayList<>());
        adapterPassedAppointments = new AppointmentAdapter(getContext(), new ArrayList<>());

        setupAdapterCallbacks(dashboardViewModel, adapter);
        setupAdapterCallbacks(dashboardViewModel, adapterPassedAppointments);

        recyclerView.setAdapter(adapter);
        recyclerViewPassedAppointments.setAdapter(adapterPassedAppointments);
    }

    private void setupAdapterCallbacks(DashboardViewModel dashboardViewModel, AppointmentAdapter adapter) {
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

    private void observeLiveData(DashboardViewModel dashboardViewModel) {
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), text -> binding.textDashboard.setText(text));
        dashboardViewModel.isDoctor().observe(getViewLifecycleOwner(), isDoctor -> {
            LiveData<List<Appointment>> appointmentsLiveData = isDoctor ? dashboardViewModel.getAppointments() : dashboardViewModel.getUserAppointments();
            LiveData<List<Appointment>> passedAppointmentsLiveData = isDoctor ? dashboardViewModel.getPassedAppointments() : dashboardViewModel.getPassedAppointmentFromUser();

            appointmentsLiveData.observe(getViewLifecycleOwner(), appointments -> adapter.setAppointments(appointments));
            passedAppointmentsLiveData.observe(getViewLifecycleOwner(), appointments -> adapterPassedAppointments.setAppointments(appointments));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
