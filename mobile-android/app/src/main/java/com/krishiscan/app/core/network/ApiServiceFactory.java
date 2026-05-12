package com.krishiscan.app.core.network;

import com.krishiscan.app.BuildConfig;
import com.krishiscan.app.data.remote.api.AuthApi;
import com.krishiscan.app.data.remote.api.ChatApi;
import com.krishiscan.app.data.remote.api.ScanApi;
import com.krishiscan.app.data.remote.api.UserApi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiServiceFactory {
    private final Retrofit retrofit;

    public ApiServiceFactory(FirebaseTokenProvider tokenProvider) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenProvider))
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public AuthApi authApi() { return retrofit.create(AuthApi.class); }
    public UserApi userApi() { return retrofit.create(UserApi.class); }
    public ScanApi scanApi() { return retrofit.create(ScanApi.class); }
    public ChatApi chatApi() { return retrofit.create(ChatApi.class); }
}
