package com.krishiscan.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.krishiscan.app.data.remote.dto.CreateScanRequest;
import com.krishiscan.app.data.remote.dto.CreateScanResponse;
import com.krishiscan.app.data.remote.dto.ScanListResponse;
import com.krishiscan.app.repository.ScanRepository;

public class ScanViewModel extends ViewModel {
    private final ScanRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<CreateScanResponse> lastScan = new MutableLiveData<>(null);
    private final MutableLiveData<ScanListResponse> scans = new MutableLiveData<>(null);

    public ScanViewModel(ScanRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }
    public LiveData<CreateScanResponse> lastScan() { return lastScan; }
    public LiveData<ScanListResponse> scans() { return scans; }

    public void createScanWithUpload(byte[] imageBytes, String cropName, String diseaseName, double confidence) {
        loading.setValue(true);
        repository.uploadScanImage(imageBytes, uploadResult -> {
            if (!uploadResult.success || uploadResult.data == null) {
                loading.postValue(false);
                error.postValue(uploadResult.errorMessage == null ? "Image upload failed" : uploadResult.errorMessage);
                return;
            }

            CreateScanRequest req = new CreateScanRequest();
            req.cropName = cropName;
            req.diseaseName = diseaseName;
            req.confidence = confidence;
            req.imageUrl = uploadResult.data.imageUrl;
            req.imagePath = uploadResult.data.path;

            repository.createScan(req, createResult -> {
                loading.postValue(false);
                if (createResult.success) {
                    lastScan.postValue(createResult.data);
                    loadRecent();
                } else {
                    error.postValue(createResult.errorMessage);
                }
            });
        });
    }

    public void loadRecent() {
        repository.listScans(10, null, null, null, result -> {
            if (result.success) scans.postValue(result.data);
            else error.postValue(result.errorMessage);
        });
    }
}
