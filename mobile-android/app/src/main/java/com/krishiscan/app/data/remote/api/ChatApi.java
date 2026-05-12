package com.krishiscan.app.data.remote.api;

import com.krishiscan.app.data.remote.dto.ChatListResponse;
import com.krishiscan.app.data.remote.dto.SendChatMessageRequest;
import com.krishiscan.app.data.remote.dto.SendChatMessageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {
    @POST("chat/messages")
    Call<SendChatMessageResponse> sendMessage(@Body SendChatMessageRequest request);

    @GET("chat/messages")
    Call<ChatListResponse> listMessages(
            @Query("limit") Integer limit,
            @Query("cursor") String cursor
    );
}
