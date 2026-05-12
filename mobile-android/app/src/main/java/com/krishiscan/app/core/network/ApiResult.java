package com.krishiscan.app.core.network;

import com.google.gson.Gson;
import com.krishiscan.app.data.remote.dto.ApiErrorResponse;

import retrofit2.Response;

public class ApiResult<T> {
    public final T data;
    public final String errorCode;
    public final String errorMessage;
    public final boolean success;

    private ApiResult(T data, String errorCode, String errorMessage, boolean success) {
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.success = success;
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(data, null, null, true);
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return new ApiResult<>(null, code, message, false);
    }

    public static <T> ApiResult<T> fromResponse(Response<T> response) {
        if (response.isSuccessful()) {
            return ok(response.body());
        }

        try {
            Gson gson = new Gson();
            ApiErrorResponse err = gson.fromJson(response.errorBody().charStream(), ApiErrorResponse.class);
            if (err != null && err.error != null) {
                return fail(err.error.code, err.error.message);
            }
        } catch (Exception ignored) {
        }

        return fail("HTTP_" + response.code(), "Request failed");
    }
}
