package com.example.easydoc.Model;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.Calendar;
import java.util.UUID;

public class Appointment {
    private UUID id;
    private String date;
    private String time;
    private String text;

    public Appointment( String date, String time, String text) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.time = time;
        this.text = text;
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
        return id.toString();
    }

    public String getText() {
        return text;
    }

    public Appointment setText(String text) {
        this.text = text;
        return this;
    }
}
