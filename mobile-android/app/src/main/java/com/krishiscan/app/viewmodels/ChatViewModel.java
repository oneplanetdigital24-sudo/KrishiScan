package com.krishiscan.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.krishiscan.app.data.remote.dto.ChatListResponse;
import com.krishiscan.app.data.remote.dto.SendChatMessageResponse;
import com.krishiscan.app.repository.ChatRepository;

public class ChatViewModel extends ViewModel {
    private static final String AI_FALLBACK_REPLY =
            "I could not reach the AI service right now. Please share the crop name, "
                    + "leaf color changes, spots, pests, and watering condition so I can help step by step.";

    private final ChatRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SendChatMessageResponse> reply = new MutableLiveData<>(null);
    private final MutableLiveData<ChatListResponse> history = new MutableLiveData<>(null);

    public ChatViewModel(ChatRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }
    public LiveData<SendChatMessageResponse> reply() { return reply; }
    public LiveData<ChatListResponse> history() { return history; }

    public void sendMessage(String text) {
        loading.setValue(true);
        repository.sendMessage(text, result -> {
            loading.postValue(false);
            if (result.success && result.data != null && result.data.reply != null && !result.data.reply.trim().isEmpty()) {
                reply.postValue(result.data);
                return;
            }

            if (shouldUseLocalFallback(result.errorMessage)) {
                SendChatMessageResponse fallback = new SendChatMessageResponse();
                fallback.reply = AI_FALLBACK_REPLY;
                reply.postValue(fallback);
                return;
            }

            if (result.success) {
                SendChatMessageResponse fallback = new SendChatMessageResponse();
                fallback.reply = AI_FALLBACK_REPLY;
                reply.postValue(fallback);
            } else {
                error.postValue(result.errorMessage);
            }
        });
    }

    public void loadHistory() {
        repository.listMessages(50, null, result -> {
            if (result.success) history.postValue(result.data);
            else error.postValue(result.errorMessage);
        });
    }

    private boolean shouldUseLocalFallback(String errorMessage) {
        if (errorMessage == null) return false;
        String normalized = errorMessage.trim().toLowerCase();
        return normalized.contains("ai service")
                || normalized.contains("service unavailable")
                || normalized.contains("service not working")
                || normalized.contains("external_service_error");
    }
}
