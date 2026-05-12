package com.krishiscan.app.data.remote.api;

import com.krishiscan.app.data.remote.dto.SessionResponse;

import retrofit2.Call;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/session")
    Call<SessionResponse> createSession();
}
