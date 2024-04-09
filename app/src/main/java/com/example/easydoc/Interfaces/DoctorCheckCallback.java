package com.example.easydoc.Interfaces;

public interface DoctorCheckCallback {
    void onChecked(boolean isDoctor);
    void onError(String error);
}
