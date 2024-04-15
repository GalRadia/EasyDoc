package com.example.easydoc.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Utils.DatabaseRepository;

public class SettingsViewModel extends ViewModel {
    private DatabaseRepository databaseRepository;
    private MutableLiveData<DoctorOffice> doctorOfficeMutableLiveData = new MutableLiveData<>();

    public SettingsViewModel() {
        databaseRepository = DatabaseRepository.getInstance();
        databaseRepository.getDoctorOfficeLiveData().observeForever(doctorOffice -> {
            doctorOfficeMutableLiveData.setValue(doctorOffice);
        });

    }

    public LiveData<DoctorOffice> getDoctorOfficeLiveData() {
        return doctorOfficeMutableLiveData;
    }


    public void setStartTime(String string) {
        if (!string.isEmpty()) {
            databaseRepository.setOfficeStartTime(string);
        }
    }

    public void setEndTime(String string) {
        if (!string.isEmpty()) {
            databaseRepository.setOfficeEndTime(string);
        }
    }

    public void setMonthInAdvance(String string) {
        if (!string.isEmpty()) {
            databaseRepository.setOfficeMonthsInAdvance(string);
        }
    }

    public void setPhone(String string) {
        if (!string.isEmpty()) {
            databaseRepository.setOfficePhone(string);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        DatabaseRepository.destroyInstance();
    }
}