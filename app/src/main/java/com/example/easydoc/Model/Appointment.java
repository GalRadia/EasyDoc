package com.example.easydoc.Model;


import java.util.Comparator;
import java.util.UUID;

public class Appointment implements Comparable<Appointment> , Comparator<Appointment> {
    private String id;

    private String date;
    private String time;
    private String text;
    private  String name;

    public Appointment( String date, String time, String text, String name) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.time = time;
        this.text = text;
        this.name = name;

    }
    public Appointment(){

    }
    public Appointment(Appointment appointment) {
        this.id = UUID.randomUUID().toString();
        this.date = appointment.getDate();
        this.time = appointment.getTime();
        this.text = appointment.getText();
        this.name = appointment.getName();
    }

    public String getDate() {
        return date;
    }

    public Appointment setDate(String date) {
        this.date = date;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Appointment setTime(String time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Appointment setText(String text) {
        this.text = text;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Appointment other) {
        String[] thisDateParts = this.date.split("/");
        String[] otherDateParts = other.getDate().split("/");
        int thisMonth = Integer.parseInt(thisDateParts[1]);
        int thisDay = Integer.parseInt(thisDateParts[0]);
        int otherMonth = Integer.parseInt(otherDateParts[1]);
        int otherDay = Integer.parseInt(otherDateParts[0]);
        int thisHour = Integer.parseInt(this.time.split(":")[0]);
        int thisMinute = Integer.parseInt(this.time.split(":")[1]);
        int otherHour = Integer.parseInt(other.getTime().split(":")[0]);
        int otherMinute = Integer.parseInt(other.getTime().split(":")[1]);
        if(thisMonth==otherMonth){
            if(thisDay==otherDay){
                if(thisHour==otherHour){
                    return thisMinute-otherMinute;
                }
                return thisHour-otherHour;
            }
            return thisDay-otherDay;
        }
        // First compare month
        else {
            return thisMonth - otherMonth;
        }

    }

    @Override
    public int compare(Appointment appointment, Appointment t1) {
        return appointment.compareTo(t1);
    }
}
