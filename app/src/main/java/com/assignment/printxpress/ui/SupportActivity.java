package com.assignment.printxpress.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.repository.SupportRepository;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class SupportActivity extends AppCompatActivity {
    private long userId;
    private TextInputEditText subjectInput;
    private TextInputEditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        subjectInput = findViewById(R.id.subjectInput);
        messageInput = findViewById(R.id.messageInput);
        findViewById(R.id.sendButton).setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String subject = text(subjectInput);
        String message = text(messageInput);

        if (!ValidationUtils.hasText(subject) || !ValidationUtils.hasText(message)) {
            UiUtils.toast(this, "Enter a subject and message");
            return;
        }

        if (new SupportRepository(this).submit(userId, subject, message)) {
            UiUtils.toast(this, "Support request submitted");
            subjectInput.setText("");
            messageInput.setText("");
        }
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
