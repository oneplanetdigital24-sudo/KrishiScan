package com.krishiscan.app.core.auth;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.krishiscan.app.core.network.FirebaseTokenProvider;

public class FirebaseIdTokenProvider implements FirebaseTokenProvider {
    @Override
    public String getIdToken() throws Exception {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return "";
        GetTokenResult result = Tasks.await(user.getIdToken(false));
        return result.getToken() == null ? "" : result.getToken();
    }
}
