package com.example.easydoc.Model;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.Calendar;
import java.util.UUID;

public class Appointment {
    private UUID id;
    private Calendar date;
    private Timepoint time;
    private String text;

    public Appointment( Calendar date, Timepoint time, String text) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.time = time;
        this.text = text;
    }

    public Calendar getDate() {
        return date;
    }

    public Appointment setDate(Calendar date) {
        this.date = date;
        return this;
    }

    public Timepoint getTime() {
        return time;
    }

    public Appointment setTime(Timepoint time) {
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
