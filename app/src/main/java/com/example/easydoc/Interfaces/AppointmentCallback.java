package com.example.easydoc.Interfaces;

import com.example.easydoc.Model.Appointment;

import java.util.List;

public interface AppointmentCallback {
    void onRemoveAppointment(String appointmentId);
    void onUpdateAppointmentText(String appointmentId, String text);
}
