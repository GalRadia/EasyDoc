package com.example.easydoc.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.Utils.DatabaseRepository;

import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final DatabaseRepository repository;


    private final MutableLiveData<String> mText;


    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Passed Appointments");
        repository = DatabaseRepository.getInstance();
    }

    public LiveData<List<Appointment>> getAppointments() {
        return repository.getAppointmentsLiveData();
    }
    public LiveData<List<Appointment>> getUserAppointments() {
        return repository.getAppointmentsFromUser();
    }

    public LiveData<List<UserAccount>> getUsers() {
        return repository.getUsersLiveData();
    }
    public LiveData<List<Appointment>> getPassedAppointments() {
        return repository.getPassedAppointmentsLiveData();
    }
    public LiveData<List<Appointment>> getPassedAppointmentFromUser() {
        return repository.getUserPassedAppointmentsLiveData();
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void removeAppointment(String appointmentID) {
        repository.removeAppointment(appointmentID);
    }

    public void updateAppointment(String appointmentID,String text) {
        repository.updateText(appointmentID,text);
    }

  public LiveData<Boolean> isDoctor(){
        return repository.getIsDoctorLiveData();
  }
  public List<String> getWaitlistDates(){
        return repository.getWailistDatesFromCurrentUser();
  }
    @Override
    protected void onCleared() {
        super.onCleared();
        DatabaseRepository.destroyInstance();
    }
}