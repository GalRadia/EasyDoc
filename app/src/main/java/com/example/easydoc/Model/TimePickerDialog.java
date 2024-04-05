package com.example.easydoc.Model;

import androidx.fragment.app.Fragment;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

public class TimePickerDialog extends com.wdullaer.materialdatetimepicker.time.TimePickerDialog {
    public TimePickerDialog(OnTimeSetListener callback, int hourOfDay, int minute, boolean is24HourMode) {
        super();

    }

    @Override
    public void setDisabledTimes(Timepoint[] disabledTimes) {
        super.setDisabledTimes(disabledTimes);
    }
}
