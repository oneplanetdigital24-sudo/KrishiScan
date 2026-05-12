package com.krishiscan.app.data.remote.api;

import com.krishiscan.app.data.remote.dto.CreateScanRequest;
import com.krishiscan.app.data.remote.dto.CreateScanResponse;
import com.krishiscan.app.data.remote.dto.ScanDto;
import com.krishiscan.app.data.remote.dto.ScanListResponse;
import com.krishiscan.app.data.remote.dto.UploadScanImageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ScanApi {
    @Multipart
    @POST("uploads/scan-image")
    Call<UploadScanImageResponse> uploadScanImage(@Part MultipartBody.Part file);

    @POST("scans")
    Call<CreateScanResponse> createScan(@Body CreateScanRequest request);

    @GET("scans")
    Call<ScanListResponse> listScans(
            @Query("limit") Integer limit,
            @Query("cursor") String cursor,
            @Query("crop") String crop,
            @Query("minConfidence") Double minConfidence
    );

    @GET("scans/{scanId}")
    Call<ScanDto> getScan(@Path("scanId") String scanId);

    @DELETE("scans/{scanId}")
    Call<Void> deleteScan(@Path("scanId") String scanId);
}
