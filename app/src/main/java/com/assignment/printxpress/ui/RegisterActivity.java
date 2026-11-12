package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.util.SessionManager;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText addressInput;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout passwordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        addressInput = findViewById(R.id.addressInput);
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        findViewById(R.id.createButton).setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String name = text(nameInput);
        String email = text(emailInput);
        String phone = text(phoneInput);
        String password = text(passwordInput);
        String address = text(addressInput);

        clearErrors();

        if (!ValidationUtils.hasText(name)) {
            nameLayout.setError("Full name is required");
            return;
        }
        if (!ValidationUtils.validEmail(email)) {
            emailLayout.setError("Enter a valid email");
            return;
        }
        if (!ValidationUtils.validPhone(phone)) {
            phoneLayout.setError("Use a valid Sri Lankan mobile number");
            return;
        }
        if (!ValidationUtils.strongEnoughPassword(password)) {
            passwordLayout.setError("Use at least 6 characters");
            return;
        }

        long userId = new AuthRepository(this)
                .register(name, email, phone, password, address);

        if (userId < 1) {
            UiUtils.toast(this, "Email or phone is already registered");
            return;
        }

        new SessionManager(this).signIn(userId);
        UiUtils.toast(this, "Account created successfully");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
