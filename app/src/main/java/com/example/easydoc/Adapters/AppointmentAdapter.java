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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>{
        private Context context;
        private HorizontalAppointmentInfoItemBinding binding;
        SortedList<Appointment> appointments;

        public AppointmentAdapter(Context context, SortedList<Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.horizontal_appointment_info_item, parent, false);
            return new AppointmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppointmentAdapter.AppointmentViewHolder holder, int position) {
            Appointment appointment = appointments.get(position);
            holder.appointTXTDate.setText(appointment.getDate());
            holder.appointTXTTime.setText(appointment.getTime());
            holder.appointTXTName.setText(appointment.getName());
            holder.appointTXTDescription.setText(appointment.getText());
            holder.appointTXTDescription.setOnClickListener(v->{
                if (holder.appointTXTDescription.getMaxLines() == 2){
                    holder.appointTXTDescription.setMaxLines(Integer.MAX_VALUE);
                }else {
                    holder.appointTXTDescription.setMaxLines(2);
                }

            });


        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }
        public class AppointmentViewHolder extends RecyclerView.ViewHolder {
            private MaterialTextView appointTXTDate, appointTXTTime, appointTXTName, appointTXTDescription;
            public AppointmentViewHolder(@NonNull View itemView) {
                super(itemView);
                appointTXTDate= binding.appointTXTDate;
                appointTXTTime= binding.appointTXTTime;
                appointTXTName= binding.appointTXTName;
                appointTXTDescription= binding.appointTXTDescription;
            }
        }

}
