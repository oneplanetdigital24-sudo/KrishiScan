package com.krishiscan.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.krishiscan.app.data.remote.dto.SessionResponse;
import com.krishiscan.app.data.remote.dto.UserDto;
import com.krishiscan.app.repository.AuthRepository;

import java.util.Map;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SessionResponse> session = new MutableLiveData<>(null);
    private final MutableLiveData<UserDto> me = new MutableLiveData<>(null);

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }
    public LiveData<SessionResponse> session() { return session; }
    public LiveData<UserDto> me() { return me; }

    public void createSession() {
        loading.setValue(true);
        repository.createSession(result -> {
            loading.postValue(false);
            if (result.success) {
                session.postValue(result.data);
            } else {
                error.postValue(result.errorMessage);
            }
        });
    }

    public void loadMe() {
        repository.getMe(result -> {
            if (result.success) {
                me.postValue(result.data);
            } else {
                error.postValue(result.errorMessage);
            }
        });
    }

    public void updateMe(Map<String, Object> patch) {
        loading.setValue(true);
        repository.updateMe(patch, result -> {
            loading.postValue(false);
            if (result.success) {
                me.postValue(result.data);
            } else {
                error.postValue(result.errorMessage);
            }
        });
    }
}
