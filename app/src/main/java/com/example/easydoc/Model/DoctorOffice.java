package com.example.easydoc.Model;

public class DoctorOffice {
    String startTime;
    String endTime;
    String appointmentDuration;
    String address;
    String phone;
    String email;
    String name;


    public DoctorOffice(String address, String name, String phone,String email, String startTime, String endTime, String appointmentDuration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.name = name;


    }

    public DoctorOffice() {

    }

    public String getStartTime() {
        return startTime;
    }

    public DoctorOffice setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public DoctorOffice setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getAppointmentDuration() {
        return appointmentDuration;
    }

    public DoctorOffice setAppointmentDuration(String appointmentDuration) {
        this.appointmentDuration = appointmentDuration;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public DoctorOffice setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public DoctorOffice setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public DoctorOffice setEmail(String email) {
        this.email = email;
        return this;
    }
}
