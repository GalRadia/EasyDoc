package com.example.easydoc.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.example.easydoc.Interfaces.AppointmentCallback;
import com.example.easydoc.Logic.SortedListComperator;
import com.example.easydoc.Model.Appointment;
import com.example.easydoc.R;
import com.example.easydoc.databinding.HorizontalAppointmentInfoItemBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private Context context;
    private HorizontalAppointmentInfoItemBinding binding;
    private AppointmentCallback appointmentCallback;
    private SortedList<Appointment> appointments;
    private boolean isPass;

    public AppointmentAdapter(Context context, List<Appointment> appointments, boolean isPass) {
        this.context = context;
        SortedList<Appointment> srtl = new SortedList<>(Appointment.class, new SortedListComperator());
        srtl.addAll(appointments);
        this.appointments = srtl;
        this.isPass = isPass;

    }

    public void setAppointmentCallback(AppointmentCallback appointmentCallback) {
        this.appointmentCallback = appointmentCallback;
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
        if (appointmentCallback != null) {

            holder.appointBTNDelete.setOnClickListener(v -> appointmentCallback.onRemoveAppointment(appointment.getId()));
            holder.appointBTNUpdate.setOnClickListener(v -> {
                EditText input = new EditText(context);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setTitle("Update Appointment Text");
                builder.setView(input);
                builder.setPositiveButton("Update", (dialog, which) -> {
                    appointmentCallback.onUpdateAppointmentText(appointment.getId(), input.getText().toString());
                });
                builder.show();
            });
        }


    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<Appointment> newAppointments) {
        SortedList<Appointment> srtl = new SortedList<>(Appointment.class, new SortedListComperator());
        srtl.addAll(newAppointments);
        this.appointments = srtl;
        //this.appointments = (List<Appointment>) srtl;
        notifyDataSetChanged(); // This is crucial for informing the RecyclerView about the data change.
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView appointTXTDate, appointTXTTime, appointTXTName, appointTXTDescription;
        private MaterialButton appointBTNDelete, appointBTNUpdate;

        public AppointmentViewHolder(HorizontalAppointmentInfoItemBinding binding) {
            super(binding.getRoot());
            appointTXTDate = binding.appointTXTDate;
            appointTXTTime = binding.appointTXTTime;
            appointTXTName = binding.appointTXTName;
            appointTXTDescription = binding.appointTXTDescription;
            appointBTNDelete = binding.buttonDelete;
            appointBTNUpdate = binding.buttonChange;
            if (isPass) {
                appointBTNUpdate.setVisibility(View.GONE);
                appointBTNDelete.setBackgroundColor(Color.GRAY);
            }
        }

    }
}
