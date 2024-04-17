package com.example.easydoc.Model;

import java.util.ArrayList;
import java.util.List;

public class UserAccount extends User{

   private List<String> appointmentsList;

    private boolean doctor;

    public UserAccount(String uid, String name, String email, String phoneNumber, String dateOfBirth , boolean doctor) {
        super(uid, name, email, phoneNumber, dateOfBirth);
        appointmentsList = new ArrayList<>();
        this.doctor = doctor;
    }
    public UserAccount(){

    }

    public boolean isDoctor() {
        return doctor;
    }

    public List<String> getAppointmentsList() {
        return appointmentsList;
    }
    public void addAppointment(String appointment){
        appointmentsList.add(appointment);
    }

    public UserAccount setAppointmentsList(List<String> appointmentsList) {
        this.appointmentsList = appointmentsList;
        return this;
    }
}