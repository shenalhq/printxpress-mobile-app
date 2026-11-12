package com.assignment.printxpress.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.repository.AddressRepository;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class AddressesActivity extends AppCompatActivity {
    private long userId;
    private AddressRepository repository;
    private TextView addressListText;
    private TextInputEditText labelInput;
    private TextInputEditText addressInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        repository = new AddressRepository(this);
        addressListText = findViewById(R.id.addressListText);
        labelInput = findViewById(R.id.labelInput);
        addressInput = findViewById(R.id.addressInput);

        findViewById(R.id.addButton).setOnClickListener(v -> addAddress());
        refresh();
    }

    private void addAddress() {
        String label = text(labelInput);
        String address = text(addressInput);

        if (!ValidationUtils.hasText(label) || !ValidationUtils.hasText(address)) {
            UiUtils.toast(this, "Enter a label and address");
            return;
        }

        if (repository.add(userId, label, address)) {
            labelInput.setText("");
            addressInput.setText("");
            refresh();
            UiUtils.toast(this, "Address added");
        }
    }

    private void refresh() {
        List<String> addresses = repository.getAll(userId);
        addressListText.setText(
                addresses.isEmpty()
                        ? "No saved addresses yet."
                        : TextUtils.join("\n\n", addresses)
        );
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
