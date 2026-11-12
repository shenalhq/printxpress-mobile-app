package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.util.SessionManager;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class LoginActivity extends AppCompatActivity {
    private TextInputEditText identityInput;
    private TextInputEditText passwordInput;
    private TextInputLayout identityLayout;
    private TextInputLayout passwordLayout;
    private AuthRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new AuthRepository(this);
        identityInput = findViewById(R.id.identityInput);
        passwordInput = findViewById(R.id.passwordInput);
        identityLayout = findViewById(R.id.identityLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        findViewById(R.id.loginButton).setOnClickListener(v -> login());
        findViewById(R.id.registerLink).setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void login() {
        String identity = text(identityInput);
        String password = text(passwordInput);
        identityLayout.setError(null);
        passwordLayout.setError(null);

        if (!ValidationUtils.hasText(identity)) {
            identityLayout.setError("Enter your email or phone");
            return;
        }
        if (!ValidationUtils.hasText(password)) {
            passwordLayout.setError("Enter your password");
            return;
        }

        User user = repository.login(identity, password);
        if (user == null) {
            UiUtils.toast(this, "Invalid email/phone or password");
            return;
        }

        new SessionManager(this).signIn(user.id);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
