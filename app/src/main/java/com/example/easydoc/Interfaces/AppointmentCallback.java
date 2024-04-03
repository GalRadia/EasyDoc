package com.example.easydoc.Interfaces;

import com.example.easydoc.Model.Appointment;

import java.util.List;

public interface AppointmentCallback {
    void onAppointmentLoaded(List<Appointment> appointments);
}
