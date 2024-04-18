package com.example.easydoc.UI.home;

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
        databaseRepository.getDoctorOfficeLiveData().observeForever(doctorOffice ->
                doctorOfficeMutableLiveData.setValue(doctorOffice));// set the value of the mutable live data to the value of the live data
        nextAppointment = databaseRepository.getNextAppointmentLiveData(); // get the next appointment from the database
        isDoctor = databaseRepository.getIsDoctorLiveData(); // check if the user is a doctor
        userAccountMutableLiveData = databaseRepository.getCurrentUserLiveData(); // get the current user from the database
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