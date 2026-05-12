package com.krishiscan.app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.krishiscan.app.repository.AuthRepository;
import com.krishiscan.app.repository.ChatRepository;
import com.krishiscan.app.repository.ScanRepository;

public class AppViewModelFactory implements ViewModelProvider.Factory {
    private final AuthRepository authRepository;
    private final ScanRepository scanRepository;
    private final ChatRepository chatRepository;

    public AppViewModelFactory(AuthRepository authRepository, ScanRepository scanRepository, ChatRepository chatRepository) {
        this.authRepository = authRepository;
        this.scanRepository = scanRepository;
        this.chatRepository = chatRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) return (T) new AuthViewModel(authRepository);
        if (modelClass.isAssignableFrom(ScanViewModel.class)) return (T) new ScanViewModel(scanRepository);
        if (modelClass.isAssignableFrom(ChatViewModel.class)) return (T) new ChatViewModel(chatRepository);
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
