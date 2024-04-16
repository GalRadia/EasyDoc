package com.example.easydoc.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.Utils.DatabaseRepository;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<DoctorOffice> doctorOfficeMutableLiveData = new MutableLiveData<>();
    private LiveData<UserAccount> userAccountMutableLiveData = new MutableLiveData<>();

    private final DatabaseRepository databaseRepository;
    private LiveData<Appointment> nextAppointment = new MutableLiveData<>();
    private LiveData<Boolean> isDoctor= new MutableLiveData<>();


    public HomeViewModel() {
        databaseRepository = DatabaseRepository.getInstance();
        databaseRepository.getDoctorOfficeLiveData().observeForever(doctorOffice -> {
            doctorOfficeMutableLiveData.setValue(doctorOffice);
        });
        nextAppointment = databaseRepository.getNextAppointmentLiveData();
        isDoctor = databaseRepository.getIsDoctorLiveData();
        userAccountMutableLiveData = databaseRepository.getCurrentUserLiveData();
    }

    public LiveData<DoctorOffice> getDoctorOfficeLiveData() {
        return doctorOfficeMutableLiveData;
    }
    public LiveData<UserAccount> getUserAccountLiveData() {
        return userAccountMutableLiveData;
    }
    public LiveData<Boolean> getIsDoctorLiveData() {
        return isDoctor;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        DatabaseRepository.destroyInstance();
    }


    public LiveData<Appointment> getNextAppointment() {
        return nextAppointment;
    }

}