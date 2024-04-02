package com.example.easydoc.Interfaces;

import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.List;

public interface TimepointCallback {
    void onTimepointsLoaded(List<Timepoint> timepoints);

    void onError(String errorMessage);
}
