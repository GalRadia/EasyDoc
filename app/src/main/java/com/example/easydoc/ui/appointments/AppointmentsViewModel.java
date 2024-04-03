package com.example.easydoc.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Utils.DatabaseRepository;

import java.util.List;

public class AppointmentsViewModel extends ViewModel {
    private final DatabaseRepository repository;

    private final MutableLiveData<String> mText;

    public AppointmentsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
        repository = DatabaseRepository.getInstance();
    }
    public LiveData<List<Appointment>> getAppointments() {
        return repository.getAppointmentsLiveData();
    }

    public LiveData<String> getText() {
        return mText;
    }
}