package com.example.easydoc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAccount extends User{

    List<String> appointmentsList;
    boolean isDoctor;

    public UserAccount(String uid, String name, String email, String phoneNumber, String dateOfBirth,String deviceToken, boolean isDoctor) {
        super(uid, name, email, phoneNumber, dateOfBirth,deviceToken);
        appointmentsList = new ArrayList<>();
        this.isDoctor = isDoctor;
    }
    public UserAccount(){
        super();

    }

    public boolean isDoctor() {
        return isDoctor;
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