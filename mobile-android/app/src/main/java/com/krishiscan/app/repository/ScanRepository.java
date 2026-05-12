package com.krishiscan.app.repository;

import androidx.annotation.NonNull;

import com.krishiscan.app.core.network.ApiResult;
import com.krishiscan.app.core.network.ApiServiceFactory;
import com.krishiscan.app.data.remote.dto.CreateScanRequest;
import com.krishiscan.app.data.remote.dto.CreateScanResponse;
import com.krishiscan.app.data.remote.dto.ScanListResponse;
import com.krishiscan.app.data.remote.dto.UploadScanImageResponse;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanRepository {
    private final ApiServiceFactory apiFactory;

    public interface ResultCallback<T> { void onResult(ApiResult<T> result); }

    public ScanRepository(ApiServiceFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    public void uploadScanImage(byte[] imageBytes, ResultCallback<UploadScanImageResponse> cb) {
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", "scan.jpg", body);

        apiFactory.scanApi().uploadScanImage(part).enqueue(new Callback<UploadScanImageResponse>() {
            @Override
            public void onResponse(@NonNull Call<UploadScanImageResponse> call, @NonNull Response<UploadScanImageResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<UploadScanImageResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }

    public void createScan(CreateScanRequest request, ResultCallback<CreateScanResponse> cb) {
        apiFactory.scanApi().createScan(request).enqueue(new Callback<CreateScanResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateScanResponse> call, @NonNull Response<CreateScanResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<CreateScanResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }

    public void listScans(int limit, String cursor, String crop, Double minConfidence, ResultCallback<ScanListResponse> cb) {
        apiFactory.scanApi().listScans(limit, cursor, crop, minConfidence).enqueue(new Callback<ScanListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScanListResponse> call, @NonNull Response<ScanListResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<ScanListResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }
}

