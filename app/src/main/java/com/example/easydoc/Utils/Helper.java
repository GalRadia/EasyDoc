package com.example.easydoc.Utils;

import android.icu.text.SimpleDateFormat;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.text.ParseException;
import java.util.Calendar;
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


    public static Timepoint stringToTimepoint(String timepointString) {
        int hourse = Integer.parseInt(timepointString.split(":")[0]);
        int minutes = Integer.parseInt(timepointString.split(":")[1]);
        return new Timepoint(hourse, minutes);
    }
//    public static float stringToHour(String timepointString) {
//        int hourse = Integer.parseInt(timepointString.split(":")[0]);
//        int minutes = Integer.parseInt(timepointString.split(":")[1]);
//        return hourse + (float) minutes /60;
//    }

    public static String calendarToString(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }


    public static boolean isAppointmentPassed(String date, String time) {
        Calendar today = Calendar.getInstance();
        Calendar apppointmentDate = stringToCalendar(date);
        apppointmentDate.set(Calendar.HOUR_OF_DAY, stringToTimepoint(time).getHour());
        apppointmentDate.set(Calendar.MINUTE, stringToTimepoint(time).getMinute());
        return today.after(apppointmentDate);
    }

    public static String getValidDate(String date) {
        String[] parts = date.split("/");
        return String.format("%02d/%02d/%s", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);

    }
}
