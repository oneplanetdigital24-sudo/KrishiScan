package com.krishiscan.app.data.remote.api;

import com.krishiscan.app.data.remote.dto.SaveFcmTokenRequest;
import com.krishiscan.app.data.remote.dto.UserDto;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface UserApi {
    @GET("users/me")
    Call<UserDto> getMe();

    @PATCH("users/me")
    Call<UserDto> patchMe(@Body Map<String, Object> body);

    @POST("notifications/token")
    Call<Void> saveFcmToken(@Body SaveFcmTokenRequest request);
}
