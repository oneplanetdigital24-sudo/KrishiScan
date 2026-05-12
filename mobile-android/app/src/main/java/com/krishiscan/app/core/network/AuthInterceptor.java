package com.krishiscan.app.core.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final FirebaseTokenProvider tokenProvider;

    public AuthInterceptor(FirebaseTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        try {
            String token = tokenProvider.getIdToken();
            if (token != null && !token.isEmpty()) {
                Request request = original.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(request);
            }
        } catch (Exception ignored) {
            // Allow request to continue; endpoint will return 401 if required.
        }
        return chain.proceed(original);
    }
}
