package com.example.easydoc.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easydoc.Model.Appointment;
import com.example.easydoc.Model.User;
import com.example.easydoc.Model.UserAccount;
import com.example.easydoc.Utils.DatabaseRepository;
import com.example.easydoc.Utils.Helper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final DatabaseRepository repository;


    private final MutableLiveData<String> mText;


    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
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

    public LiveData<String> getText() {
        return mText;
    }
}