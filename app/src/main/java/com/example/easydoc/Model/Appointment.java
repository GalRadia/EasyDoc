package com.example.easydoc.Model;

import java.util.UUID;

public class Appointment implements Comparable<Appointment> {
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
    public int compareTo(Appointment appointment) {
        return this.getDate().compareTo(appointment.getDate());
    }
}
