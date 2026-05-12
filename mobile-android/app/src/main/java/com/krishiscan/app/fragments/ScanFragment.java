package com.krishiscan.app.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.krishiscan.app.R;
import com.krishiscan.app.adapters.ScanHistoryAdapter;
import com.krishiscan.app.core.di.AppContainer;
import com.krishiscan.app.ml.DiseaseClassifier;
import com.krishiscan.app.ml.Prediction;
import com.krishiscan.app.models.Scan;
import com.krishiscan.app.viewmodels.ScanViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {
    private ScanViewModel vm;
    private ScanHistoryAdapter adapter;
    private ImageView imgPreview;
    private byte[] selectedImageBytes;

    private DiseaseClassifier classifier;
    private Prediction latestPrediction;
    private final ExecutorService mlExecutor = Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<String> galleryPicker;
    private ActivityResultLauncher<Uri> cameraFullCapture;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    private Uri pendingCameraUri;
    private File pendingCameraFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this, AppContainer.from(requireContext()).viewModelFactory()).get(ScanViewModel.class);

        try {
            classifier = new DiseaseClassifier(requireContext());
        } catch (Exception e) {
            classifier = null;
            Toast.makeText(requireContext(), "Model not found. Add assets/krishiscan_model.tflite and labels.txt", Toast.LENGTH_LONG).show();
        }

        imgPreview = view.findViewById(R.id.imgPreview);
        RecyclerView rv = view.findViewById(R.id.rvRecentScans);
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

        setupPickers();

        MaterialCardView cardCamera = view.findViewById(R.id.cardCamera);
        MaterialCardView cardGallery = view.findViewById(R.id.cardGallery);
        MaterialButton btnAnalyze = view.findViewById(R.id.btnAnalyze);

        cardGallery.setOnClickListener(v -> galleryPicker.launch("image/*"));
        cardCamera.setOnClickListener(v -> launchCameraWithPermission());

        btnAnalyze.setOnClickListener(v -> onAnalyzeClicked());

        vm.loading().observe(getViewLifecycleOwner(), loading -> btnAnalyze.setEnabled(loading == null || !loading));

        vm.lastScan().observe(getViewLifecycleOwner(), scan -> {
            if (scan != null) {
                String crop = latestPrediction != null ? latestPrediction.cropName : "Unknown Crop";
                String disease = latestPrediction != null ? latestPrediction.diseaseName : "Unknown Disease";
                double confidence = latestPrediction != null ? latestPrediction.confidence : 0.0;

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainNavHost, ResultFragment.newInstance(
                                scan.scanId,
                                scan.severity,
                                scan.treatment,
                                crop,
                                disease,
                                confidence
                        ))
                        .addToBackStack(null)
                        .commit();
            }
        });

        vm.scans().observe(getViewLifecycleOwner(), list -> {
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
                s.userId = d.userId;
                s.imageUrl = d.imageUrl;
                scans.add(s);
            }
            adapter.submit(scans);
        });

        vm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });

        vm.loadRecent();
    }

    private void onAnalyzeClicked() {
        if (selectedImageBytes == null || selectedImageBytes.length == 0) {
            Toast.makeText(requireContext(), "Please capture or select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (classifier == null) {
            Toast.makeText(requireContext(), "Model not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes.length);
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }

        mlExecutor.execute(() -> {
            try {
                Prediction p = classifier.classify(bitmap);
                requireActivity().runOnUiThread(() -> {
                    if (p.confidence < 0.40) {
                        Toast.makeText(requireContext(), "Uncertain result. Please retake a clearer photo.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    latestPrediction = p;
                    vm.createScanWithUpload(selectedImageBytes, p.cropName, p.diseaseName, p.confidence);
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Classification failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupPickers() {
        galleryPicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                if (is == null) return;
                byte[] bytes = readAllBytes(is);
                selectedImageBytes = bytes;
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgPreview.setImageBitmap(bm);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show();
            }
        });

        cameraFullCapture = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (!success || pendingCameraUri == null) return;
            try {
                byte[] bytes;
                if (pendingCameraFile != null && pendingCameraFile.exists()) {
                    bytes = readAllBytes(new FileInputStream(pendingCameraFile));
                } else {
                    InputStream is = requireContext().getContentResolver().openInputStream(pendingCameraUri);
                    if (is == null) return;
                    bytes = readAllBytes(is);
                    is.close();
                }
                selectedImageBytes = bytes;
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgPreview.setImageBitmap(bm);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Failed to read camera image", Toast.LENGTH_SHORT).show();
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchCameraInternal();
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
        });
    }

    private void launchCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraInternal();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCameraInternal() {
        try {
            File dir = new File(requireContext().getCacheDir(), "camera");
            if (!dir.exists()) dir.mkdirs();
            pendingCameraFile = File.createTempFile("scan_", ".jpg", dir);
            pendingCameraUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pendingCameraFile);
            cameraFullCapture.launch(pendingCameraUri);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (classifier != null) classifier.close();
        mlExecutor.shutdownNow();
    }
}

