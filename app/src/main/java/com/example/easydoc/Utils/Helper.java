package com.example.easydoc.Utils;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.text.ParseException;
import java.util.Date;

public class Helper {
    public static Calendar stringToCalendar(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateString);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }
    public static Timepoint stringToTimepoint(String timepointString){
        int hourse = Integer.parseInt(timepointString.split(":")[0]);
        int minutes = Integer.parseInt(timepointString.split(":")[1]);
        return new Timepoint(hourse, minutes);
    }
    public static String calendarToString(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }
}
