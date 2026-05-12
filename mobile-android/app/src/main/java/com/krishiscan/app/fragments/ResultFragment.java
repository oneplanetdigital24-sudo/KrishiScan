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

import com.krishiscan.app.R;

import java.util.Locale;

public class ResultFragment extends Fragment {
    private static final String ARG_ID = "id";
    private static final String ARG_SEVERITY = "severity";
    private static final String ARG_TREATMENT = "treatment";
    private static final String ARG_CROP = "crop";
    private static final String ARG_DISEASE = "disease";
    private static final String ARG_CONFIDENCE = "confidence";
    private String shareText = "";

    public static ResultFragment newInstance(
            String id,
            String severity,
            String treatment,
            String crop,
            String disease,
            double confidence
    ) {
        ResultFragment f = new ResultFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ID, id);
        b.putString(ARG_SEVERITY, severity);
        b.putString(ARG_TREATMENT, treatment);
        b.putString(ARG_CROP, crop);
        b.putString(ARG_DISEASE, disease);
        b.putDouble(ARG_CONFIDENCE, confidence);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvDisease = view.findViewById(R.id.tvDisease);
        TextView tvConfidence = view.findViewById(R.id.tvConfidence);
        TextView tvSeverity = view.findViewById(R.id.tvSeverity);
        TextView tvTreatment = view.findViewById(R.id.tvTreatment);

        Bundle args = getArguments();
        if (args != null) {
            String crop = args.getString(ARG_CROP, "Unknown Crop");
            String disease = args.getString(ARG_DISEASE, "Unknown Disease");
            double confidence = args.getDouble(ARG_CONFIDENCE, 0.0);
            String severity = args.getString(ARG_SEVERITY, "Unknown");
            String treatment = args.getString(ARG_TREATMENT, "No treatment available yet.");

            tvDisease.setText(crop + " " + disease);
            tvConfidence.setText(String.format(Locale.US, "Confidence: %.0f%%", confidence * 100.0));
            tvSeverity.setText("Severity: " + severity);
            tvTreatment.setText(treatment == null || treatment.trim().isEmpty()
                    ? "AI service unavailable right now. Scan result is saved."
                    : treatment);
            shareText = String.format(
                    Locale.US,
                    "KrishiScan result: %s %s\nConfidence: %.0f%%\nSeverity: %s\nTreatment:\n%s",
                    crop,
                    disease,
                    confidence * 100.0,
                    severity,
                    tvTreatment.getText().toString()
            );
        }

        view.findViewById(R.id.btnSave).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Scan result is already saved in history", Toast.LENGTH_SHORT).show()
        );
        view.findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(intent, "Share scan result"));
        });
    }
}
