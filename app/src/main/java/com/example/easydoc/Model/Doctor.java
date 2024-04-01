package com.example.easydoc.Model;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

public class Doctor extends User {
    Timepoint startTime;
    Timepoint endTime;
    int appointmentDuration;


    public Doctor(String uid, String name, String email, String phone, String dateOfBirth, Timepoint startTime, Timepoint endTime, int appointmentDuration) {
        super(uid, name, email, phone, dateOfBirth);
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
    }

    public Timepoint getStartTime() {
        return startTime;
    }

    public Doctor setStartTime(Timepoint startTime) {
        this.startTime = startTime;
        return this;
    }

    public Timepoint getEndTime() {
        return endTime;
    }

    public Doctor setEndTime(Timepoint endTime) {
        this.endTime = endTime;
        return this;
    }

    public int getAppointmentDuration() {
        return appointmentDuration;
    }

    public Doctor setAppointmentDuration(int appointmentDuration) {
        this.appointmentDuration = appointmentDuration;
        return this;
    }

}
