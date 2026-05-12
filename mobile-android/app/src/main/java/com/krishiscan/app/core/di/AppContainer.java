package com.krishiscan.app.core.di;

import android.content.Context;

import com.krishiscan.app.core.auth.FirebaseIdTokenProvider;
import com.krishiscan.app.core.network.ApiServiceFactory;
import com.krishiscan.app.repository.AuthRepository;
import com.krishiscan.app.repository.ChatRepository;
import com.krishiscan.app.repository.ScanRepository;
import com.krishiscan.app.viewmodels.AppViewModelFactory;

public class AppContainer {
    private static AppContainer instance;
    private final AppViewModelFactory factory;

    private AppContainer(Context context) {
        ApiServiceFactory apiFactory = new ApiServiceFactory(new FirebaseIdTokenProvider());
        AuthRepository authRepository = new AuthRepository(apiFactory);
        ScanRepository scanRepository = new ScanRepository(apiFactory);
        ChatRepository chatRepository = new ChatRepository(apiFactory);
        factory = new AppViewModelFactory(authRepository, scanRepository, chatRepository);
    }

    public static synchronized AppContainer from(Context context) {
        if (instance == null) instance = new AppContainer(context.getApplicationContext());
        return instance;
    }

    public AppViewModelFactory viewModelFactory() {
        return factory;
    }
}
