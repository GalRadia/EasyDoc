package com.example.easydoc.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.DoctorOffice;
import com.example.easydoc.Utils.DatabaseRepository;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<DoctorOffice> doctorOfficeMutableLiveData = new MutableLiveData<>();

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
    }

    public LiveData<DoctorOffice> getDoctorOfficeLiveData() {
        return doctorOfficeMutableLiveData;
    }
    public LiveData<Boolean> getIsDoctorLiveData() {
        return isDoctor;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        DatabaseRepository.destroyInstance();
    }

    public String getUserName() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return "User";
        }
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public LiveData<Appointment> getNextAppointment() {
        return nextAppointment;
    }

}