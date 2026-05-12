package com.krishiscan.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.krishiscan.app.R;
import com.krishiscan.app.core.di.AppContainer;
import com.krishiscan.app.viewmodels.AppViewModelFactory;
import com.krishiscan.app.viewmodels.AuthViewModel;

public class AuthActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSubmit;
    private MaterialButton btnGoogle;
    private TextView tvForgotPassword;
    private TextView tvAuthTitle;
    private TextView tvAuthSubtitle;
    private TextView tvSwitchPrompt;
    private TextView tvSwitchAction;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private AuthViewModel vm;
    private boolean signInMode = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvSwitchPrompt = findViewById(R.id.tvSwitchPrompt);
        tvSwitchAction = findViewById(R.id.tvSwitchAction);

        setupGoogleSignIn();

        AppViewModelFactory factory = AppContainer.from(this).viewModelFactory();
        vm = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        vm.loading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            btnSubmit.setEnabled(enabled);
            btnGoogle.setEnabled(enabled);
            tvSwitchAction.setEnabled(enabled);
            tvForgotPassword.setEnabled(enabled);
            updateModeUi(enabled);
        });

        vm.error().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        vm.session().observe(this, s -> {
            if (s != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (signInMode) {
                loginThenSession();
            } else {
                registerThenSession();
            }
        });
        btnGoogle.setOnClickListener(v -> googleLauncher.launch(googleSignInClient.getSignInIntent()));
        tvForgotPassword.setOnClickListener(v -> sendResetEmail());
        tvSwitchAction.setOnClickListener(v -> {
            signInMode = !signInMode;
            updateModeUi(true);
        });

        updateModeUi(true);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                        .getResult(ApiException.class);
                if (account == null || account.getIdToken() == null) {
                    Toast.makeText(this, R.string.auth_google_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance()
                        .signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                        .addOnSuccessListener(res -> vm.createSession())
                        .addOnFailureListener(e -> {
                            setAuthEnabled(true);
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                setAuthEnabled(true);
                Toast.makeText(this, R.string.auth_google_cancelled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginThenSession() {
        String email = text(etEmail);
        String password = text(etPassword);
        if (!validateEmailAndPassword(email, password)) return;
        setAuthEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> vm.createSession())
                .addOnFailureListener(e -> {
                    setAuthEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void registerThenSession() {
        String email = text(etEmail);
        String password = text(etPassword);
        if (!validateEmailAndPassword(email, password)) return;
        setAuthEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> vm.createSession())
                .addOnFailureListener(e -> {
                    setAuthEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendResetEmail() {
        String email = text(etEmail);
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.auth_reset_enter_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.auth_error_email_invalid));
            etEmail.requestFocus();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> Toast.makeText(this, R.string.auth_reset_sent, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateEmailAndPassword(String email, String password) {
        etEmail.setError(null);
        etPassword.setError(null);

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.auth_error_email_required));
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.auth_error_email_invalid));
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.auth_error_password_required));
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError(getString(R.string.auth_error_password_length));
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setAuthEnabled(boolean enabled) {
        btnSubmit.setEnabled(enabled);
        btnGoogle.setEnabled(enabled);
        tvSwitchAction.setEnabled(enabled);
        tvForgotPassword.setEnabled(enabled);
        updateModeUi(enabled);
    }

    private void updateModeUi(boolean enabled) {
        tvAuthTitle.setText(signInMode ? R.string.auth_title_login : R.string.auth_title_register);
        tvAuthSubtitle.setText(signInMode ? R.string.auth_subtitle_login : R.string.auth_subtitle_register);
        btnSubmit.setText(signInMode
                ? (enabled ? R.string.auth_sign_in_action : R.string.auth_loading_sign_in)
                : (enabled ? R.string.auth_register_action : R.string.auth_loading_register));
        btnGoogle.setText(signInMode ? R.string.auth_google : R.string.auth_google_register);
        tvSwitchPrompt.setText(signInMode ? R.string.auth_footer_login_prompt : R.string.auth_footer_register_prompt);
        tvSwitchAction.setText(signInMode ? R.string.auth_footer_login_action : R.string.auth_footer_register_action);
        tvForgotPassword.setVisibility(signInMode ? View.VISIBLE : View.GONE);
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
