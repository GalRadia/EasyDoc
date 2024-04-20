package com.example.easydoc.UI.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.easydoc.FirebaseUIActivity;
import com.example.easydoc.R;
import com.example.easydoc.databinding.FragmentHomeBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private MaterialTextView doctorName;
    private MaterialButton callButton;
    private MaterialButton signOutB;
    private MaterialButton navigateToLocation;
    private HomeViewModel homeViewModel;
    private ShapeableImageView medicalCenterImage;
    private ShapeableImageView interiorImage;
    private MaterialTextView userName;
    private MaterialTextView nextAppointment;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        initUI();
        initMap(savedInstanceState);

        return root;
    }

    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            LatLng specificLocation = new LatLng(32.11504612996519, 34.81780814048655);
            onMapReady(googleMap, specificLocation);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(specificLocation, 17));

            // Replace with your desired coordinates

        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    public void onMapReady(GoogleMap map, LatLng latLng) {
        map.addMarker(new MarkerOptions()
                .position(latLng).title("EasyDoc Location"));

    }


    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void NavigateToLocation(LatLng specificLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                        specificLocation.latitude + "," + specificLocation.longitude));
        startActivity(intent);

    }

    public void setupUI() {
        mapView = binding.mapView;
        doctorName = binding.doctorName;
        callButton = binding.callButton;
        signOutB = binding.signOutButton;
        navigateToLocation = binding.navigateButton;
        medicalCenterImage = binding.medicalCenterIV;
        interiorImage = binding.interiorIV;
        userName = binding.userName;
        nextAppointment = binding.nextAppointment;

    }

    private void initUI() {
        uploadImage(R.drawable.medical_center, medicalCenterImage);
        uploadImage(R.drawable.interior, interiorImage);
        homeViewModel.getNextAppointment().observe(getViewLifecycleOwner(), appointment -> {
            if(appointment==null)
                nextAppointment.setText("No upcoming appointments");
            else {
                nextAppointment.setText("Next appointment: " + appointment.getDate() + " at " + appointment.getTime());
            }
        });
        homeViewModel.getIsDoctorLiveData().observe(getViewLifecycleOwner(), isDoctor -> {
            if (!isDoctor) {
                homeViewModel.getUserAccountLiveData().observe(getViewLifecycleOwner(), userAccount -> {
                    userName.setText("Hello " + userAccount.getName());
                });

            }
            else {
                userName.setText("Hello Doctor");
            }
        });

        homeViewModel.getDoctorOfficeLiveData().observe(getViewLifecycleOwner(), doctorOffice -> {
            doctorName.setText("EasyDoc\n"+doctorOffice.getDoctorName());
        });
        callButton.setOnClickListener(v -> {
            String phone;
            phone=homeViewModel.getDoctorOfficeLiveData().getValue().getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);

        });
        navigateToLocation.setOnClickListener(v -> {
            LatLng specificLocation = new LatLng(32.11504612996519, 34.81780814048655);
            NavigateToLocation(specificLocation);
        });
        signOutB.setOnClickListener(v -> {
            AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(requireContext(), FirebaseUIActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    });
        });
    }

    private void uploadImage(int image, ShapeableImageView imageView) {
        Glide
                .with(this)
                .load(image)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imageView);
    }

}