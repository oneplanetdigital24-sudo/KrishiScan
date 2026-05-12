package com.krishiscan.app.repository;

import androidx.annotation.NonNull;

import com.krishiscan.app.core.network.ApiResult;
import com.krishiscan.app.core.network.ApiServiceFactory;
import com.krishiscan.app.data.remote.dto.ChatListResponse;
import com.krishiscan.app.data.remote.dto.SendChatMessageRequest;
import com.krishiscan.app.data.remote.dto.SendChatMessageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ApiServiceFactory apiFactory;

    public interface ResultCallback<T> { void onResult(ApiResult<T> result); }

    public ChatRepository(ApiServiceFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    public void sendMessage(String text, ResultCallback<SendChatMessageResponse> cb) {
        apiFactory.chatApi().sendMessage(new SendChatMessageRequest(text)).enqueue(new Callback<SendChatMessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<SendChatMessageResponse> call, @NonNull Response<SendChatMessageResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<SendChatMessageResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }

    public void listMessages(int limit, String cursor, ResultCallback<ChatListResponse> cb) {
        apiFactory.chatApi().listMessages(limit, cursor).enqueue(new Callback<ChatListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatListResponse> call, @NonNull Response<ChatListResponse> response) {
                cb.onResult(ApiResult.fromResponse(response));
            }

            @Override
            public void onFailure(@NonNull Call<ChatListResponse> call, @NonNull Throwable t) {
                cb.onResult(ApiResult.fail("NETWORK_ERROR", t.getMessage() == null ? "Network error" : t.getMessage()));
            }
        });
    }
}
