package com.krishiscan.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.krishiscan.app.viewmodels.AuthViewModel;
import com.krishiscan.app.viewmodels.AppViewModelFactory;

public class AuthActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private MaterialButton btnGoogle;
    private TextView tvForgotPassword;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        setupGoogleSignIn();

        AppViewModelFactory factory = AppContainer.from(this).viewModelFactory();
        AuthViewModel vm = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        vm.loading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            btnLogin.setEnabled(enabled);
            btnRegister.setEnabled(enabled);
            btnGoogle.setEnabled(enabled);
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

        btnLogin.setOnClickListener(v -> loginThenSession(vm));
        btnRegister.setOnClickListener(v -> registerThenSession(vm));
        btnGoogle.setOnClickListener(v -> googleLauncher.launch(googleSignInClient.getSignInIntent()));
        tvForgotPassword.setOnClickListener(v -> sendResetEmail());
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
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance()
                        .signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                        .addOnSuccessListener(res -> {
                            AppViewModelFactory factory = AppContainer.from(this).viewModelFactory();
                            AuthViewModel vm = new ViewModelProvider(this, factory).get(AuthViewModel.class);
                            vm.createSession();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginThenSession(AuthViewModel vm) {
        String email = text(etEmail);
        String password = text(etPassword);
        if (!validateEmailAndPassword(email, password, false)) return;
        setAuthButtonsEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> vm.createSession())
                .addOnFailureListener(e -> {
                    setAuthButtonsEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void registerThenSession(AuthViewModel vm) {
        String email = text(etEmail);
        String password = text(etPassword);
        if (!validateEmailAndPassword(email, password, true)) return;
        setAuthButtonsEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> vm.createSession())
                .addOnFailureListener(e -> {
                    setAuthButtonsEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendResetEmail() {
        String email = text(etEmail);
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateEmailAndPassword(String email, String password, boolean registering) {
        etEmail.setError(null);
        etPassword.setError(null);

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError(registering ? "Use at least 6 characters" : "Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setAuthButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
        btnGoogle.setEnabled(enabled);
    }

    private String text(TextInputEditText et) { return et.getText() == null ? "" : et.getText().toString().trim(); }
}
