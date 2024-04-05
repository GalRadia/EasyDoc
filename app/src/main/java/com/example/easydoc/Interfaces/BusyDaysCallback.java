package com.example.easydoc.Interfaces;

public interface BusyDaysCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

