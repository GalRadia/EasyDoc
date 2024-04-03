package com.example.easydoc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.R;
import com.example.easydoc.databinding.HorizontalAppointmentInfoItemBinding;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private Context context;
    private HorizontalAppointmentInfoItemBinding binding;
    private List<Appointment> appointments = new ArrayList<>();

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;

    }


    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = HorizontalAppointmentInfoItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.appointTXTDate.setText(appointment.getDate());
        holder.appointTXTTime.setText(appointment.getTime());
        holder.appointTXTName.setText(appointment.getName());
        holder.appointTXTDescription.setText(appointment.getText());
        holder.appointTXTDescription.setOnClickListener(v -> {
            if (holder.appointTXTDescription.getMaxLines() == 2) {
                holder.appointTXTDescription.setMaxLines(Integer.MAX_VALUE);
            } else {
                holder.appointTXTDescription.setMaxLines(2);
            }

        });


    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged(); // This is crucial for informing the RecyclerView about the data change.
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        HorizontalAppointmentInfoItemBinding binding;
        private MaterialTextView appointTXTDate, appointTXTTime, appointTXTName, appointTXTDescription;

        public AppointmentViewHolder(HorizontalAppointmentInfoItemBinding binding) {
            super(binding.getRoot());
            appointTXTDate = binding.appointTXTDate;
            appointTXTTime = binding.appointTXTTime;
            appointTXTName = binding.appointTXTName;
            appointTXTDescription = binding.appointTXTDescription;
        }
    }

}
