package com.example.easydoc.UI.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.easydoc.Utils.Helper;
import com.example.easydoc.databinding.FragmentOfficeSettingsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private FragmentOfficeSettingsBinding binding;
    private AutoCompleteTextView selectStartTime;
    private AutoCompleteTextView selectEndTime;
    private TextInputEditText phoneEditText;
    private MaterialButton submitButton;
    private MaterialTextView startTime;
    private MaterialTextView endTime;
    private MaterialTextView monthInAdvance;
    private MaterialTextView phoneNumber;
    private Slider slider;
    private TextInputLayout phoneInputLayout;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel= new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentOfficeSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupUI();
        initUI();
        return root;

    }

    public void setupUI(){
        selectStartTime = binding.startTime;
        selectEndTime = binding.endTime;
        phoneEditText = binding.phoneNumber;
        submitButton = binding.buttonSubmit;
        startTime = binding.officeStartTime;
        endTime = binding.officeEndTime;
        monthInAdvance = binding.officeMonthInAdvance;
        phoneNumber = binding.officePhoneNumber;
        slider=binding.sliderMonthInAdvance;
        phoneInputLayout=binding.menuUpdatePhoneNumber;
    }

    private void initUI() {
        mViewModel.getDoctorOfficeLiveData().observe(getViewLifecycleOwner(), doctorOffice -> {
            startTime.setText(doctorOffice.getStartTime());
            endTime.setText(doctorOffice.getEndTime());
            monthInAdvance.setText(doctorOffice.getMonthsInAdvance());
            phoneNumber.setText(doctorOffice.getPhone());
            slider.setValue(Float.parseFloat(doctorOffice.getMonthsInAdvance()));
        });
        phoneInputLayout.setOnClickListener(v -> phoneInputLayout.setError(null));
        submitButton.setOnClickListener(v -> {
           if(!phoneEditText.getText().toString().isEmpty()&&!Helper.checkPhoneNumber(phoneEditText.getText().toString())){
               phoneInputLayout.setError("Invalid Phone Number");
               return;
           }
            mViewModel.setStartTime(selectStartTime.getText().toString());
            mViewModel.setEndTime(selectEndTime.getText().toString());
            mViewModel.setMonthInAdvance((int)slider.getValue() + "");
            mViewModel.setPhone(phoneEditText.getText().toString());
            Toast.makeText(getContext(), "Settings Updated", Toast.LENGTH_SHORT).show();
        });
    }



}