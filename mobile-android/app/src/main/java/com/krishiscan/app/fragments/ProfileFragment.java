package com.krishiscan.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.krishiscan.app.R;
import com.krishiscan.app.activities.AuthActivity;
import com.krishiscan.app.adapters.ScanHistoryAdapter;
import com.krishiscan.app.core.di.AppContainer;
import com.krishiscan.app.models.Scan;
import com.krishiscan.app.viewmodels.AuthViewModel;
import com.krishiscan.app.viewmodels.ScanViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private AuthViewModel authVm;
    private ScanViewModel scanVm;
    private ScanHistoryAdapter adapter;
    private boolean profileSavePending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authVm = new ViewModelProvider(this, AppContainer.from(requireContext()).viewModelFactory()).get(AuthViewModel.class);
        scanVm = new ViewModelProvider(this, AppContainer.from(requireContext()).viewModelFactory()).get(ScanViewModel.class);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvMeta = view.findViewById(R.id.tvMeta);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etState = view.findViewById(R.id.etState);
        MaterialButton btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        RecyclerView rv = view.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ScanHistoryAdapter(scan -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainNavHost, ResultFragment.newInstance(
                        scan.scanId,
                        scan.severity,
                        scan.treatment,
                        scan.cropName,
                        scan.diseaseName,
                        scan.confidence
                ))
                .addToBackStack(null)
                .commit());
        rv.setAdapter(adapter);

        authVm.me().observe(getViewLifecycleOwner(), me -> {
            if (me == null) return;
            tvName.setText((me.name == null || me.name.trim().isEmpty()) ? "Farmer" : me.name);
            String role = me.role == null ? "farmer" : me.role;
            String state = me.state == null ? "Unknown" : me.state;
            tvMeta.setText(state + " • " + role);
            etName.setText(me.name == null ? "" : me.name);
            etPhone.setText(me.phone == null ? "" : me.phone);
            etState.setText(me.state == null ? "" : me.state);
            if (profileSavePending) {
                profileSavePending = false;
                Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show();
            }
        });

        authVm.loading().observe(getViewLifecycleOwner(), loading ->
                btnSaveProfile.setEnabled(loading == null || !loading)
        );

        scanVm.scans().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.items == null) return;
            List<Scan> scans = new ArrayList<>();
            for (com.krishiscan.app.data.remote.dto.ScanDto d : list.items) {
                Scan s = new Scan();
                s.scanId = d.scanId;
                s.cropName = d.cropName;
                s.diseaseName = d.diseaseName;
                s.confidence = d.confidence;
                s.createdAt = d.createdAt;
                s.severity = d.severity;
                s.treatment = d.treatment;
                scans.add(s);
            }
            adapter.submit(scans);
        });

        authVm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                profileSavePending = false;
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
        scanVm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile(etName, etPhone, etState));

        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), AuthActivity.class));
            requireActivity().finish();
        });

        authVm.loadMe();
        scanVm.loadRecent();
    }

    private void saveProfile(TextInputEditText etName, TextInputEditText etPhone, TextInputEditText etState) {
        String name = text(etName);
        String phone = text(etPhone);
        String state = text(etState);

        etName.setError(null);
        etPhone.setError(null);
        etState.setError(null);

        if (name.length() < 3) {
            etName.setError("Name must be at least 3 characters");
            etName.requestFocus();
            return;
        }
        if (!phone.isEmpty() && !phone.matches("^[6-9][0-9]{9}$")) {
            etPhone.setError("Enter a valid 10 digit Indian phone number");
            etPhone.requestFocus();
            return;
        }
        if (!state.isEmpty() && state.length() < 2) {
            etState.setError("State must be at least 2 characters");
            etState.requestFocus();
            return;
        }

        Map<String, Object> patch = new HashMap<>();
        patch.put("name", name);
        patch.put("phone", phone.isEmpty() ? null : phone);
        if (!state.isEmpty()) patch.put("state", state);
        profileSavePending = true;
        authVm.updateMe(patch);
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}

