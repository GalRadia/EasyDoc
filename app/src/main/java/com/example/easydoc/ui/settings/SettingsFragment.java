package com.example.easydoc.ui.settings;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.easydoc.databinding.FragmentSettingsBinding;
import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private FragmentSettingsBinding binding;
    private AutoCompleteTextView selectStartTime;
    private AutoCompleteTextView selectEndTime;
    private TextInputEditText textInputEditText;
    private MaterialButton submitButton;
    private MaterialTextView startTime;
    private MaterialTextView endTime;
    private MaterialTextView monthInAdvance;
    private MaterialTextView phoneNumber;
    private Slider slider;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel= new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupUI();
        initUI();
        return root;

    }

    private void initUI() {
        mViewModel.getDoctorOfficeLiveData().observe(getViewLifecycleOwner(), doctorOffice -> {
            startTime.setText(doctorOffice.getStartTime());
            endTime.setText(doctorOffice.getEndTime());
            monthInAdvance.setText(doctorOffice.getMonthsInAdvance());
            phoneNumber.setText(doctorOffice.getPhone());
            slider.setValue(Float.parseFloat(doctorOffice.getMonthsInAdvance()));
        });
        submitButton.setOnClickListener(v -> {
            mViewModel.setStartTime(selectStartTime.getText().toString());
            mViewModel.setEndTime(selectEndTime.getText().toString());
            mViewModel.setMonthInAdvance((int)slider.getValue() + "");
            mViewModel.setPhone(textInputEditText.getText().toString());
            Toast.makeText(getContext(), "Settings Updated", Toast.LENGTH_SHORT).show();
        });
    }

    public void setupUI(){
        selectStartTime = binding.startTime;
        selectEndTime = binding.endTime;
        textInputEditText = binding.phoneNumber;
        submitButton = binding.buttonSubmit;
        startTime = binding.officeStartTime;
        endTime = binding.officeEndTime;
        monthInAdvance = binding.officeMonthInAdvance;
        phoneNumber = binding.officePhoneNumber;
        slider=binding.sliderMonthInAdvance;
    }



}