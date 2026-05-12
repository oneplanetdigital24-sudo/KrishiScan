package com.krishiscan.app.repository;

import androidx.annotation.NonNull;

import com.krishiscan.app.core.network.ApiResult;
import com.krishiscan.app.core.network.ApiServiceFactory;
import com.krishiscan.app.data.remote.dto.SessionResponse;
import com.krishiscan.app.data.remote.dto.UserDto;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiServiceFactory apiFactory;

    public interface ResultCallback<T> { void onResult(ApiResult<T> result); }

    public AuthRepository(ApiServiceFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    public void createSession(ResultCallback<SessionResponse> cb) {
        apiFactory.authApi().createSession().enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }

    public void getMe(ResultCallback<UserDto> cb) {
        apiFactory.userApi().getMe().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }

    public void updateMe(Map<String, Object> patch, ResultCallback<UserDto> cb) {
        apiFactory.userApi().patchMe(patch).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }
}
