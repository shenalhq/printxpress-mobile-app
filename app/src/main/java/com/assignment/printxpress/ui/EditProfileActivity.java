package com.assignment.printxpress.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.repository.AddressRepository;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Lets the customer update profile details and choose a profile picture.
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_PROFILE_IMAGE = 701;

    private long userId;
    private AuthRepository repository;
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private ImageView profileImage;
    private TextView avatarText;
    private Uri selectedProfileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        repository = new AuthRepository(this);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        profileImage = findViewById(R.id.profileImage);
        avatarText = findViewById(R.id.avatarText);

        findViewById(R.id.photoButton).setOnClickListener(v -> choosePhoto());
        findViewById(R.id.saveProfileButton).setOnClickListener(v -> saveProfile());
        loadUser();
    }

    private void loadUser() {
        User user = repository.getUser(userId);
        if (user == null) return;

        nameInput.setText(user.name);
        phoneInput.setText(user.phone);
        addressInput.setText(user.address);
        avatarText.setText(initials(user.name));

        if (user.profileUri != null && !user.profileUri.trim().isEmpty()) {
            try {
                selectedProfileUri = Uri.parse(user.profileUri);
                profileImage.setImageURI(selectedProfileUri);
                profileImage.setVisibility(View.VISIBLE);
                avatarText.setVisibility(View.GONE);
            } catch (Exception ignored) {
                showInitials(user.name);
            }
        }
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_PROFILE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PROFILE_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        selectedProfileUri = data.getData();
        try {
            int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(selectedProfileUri, flags);
        } catch (Exception ignored) {
            // Some providers only grant temporary read permission.
        }

        profileImage.setImageURI(selectedProfileUri);
        profileImage.setVisibility(View.VISIBLE);
        avatarText.setVisibility(View.GONE);
    }

    private void saveProfile() {
        String name = text(nameInput);
        String phone = text(phoneInput);
        String address = text(addressInput);

        if (!ValidationUtils.hasText(name) || !ValidationUtils.validPhone(phone)) {
            UiUtils.toast(this, "Enter a name and valid Sri Lankan mobile number");
            return;
        }

        boolean updated = repository.updateProfile(userId, name, phone, address);
        if (!updated) {
            UiUtils.toast(this, "Phone number may already be used by another account");
            return;
        }

        if (selectedProfileUri != null) {
            repository.updateProfilePhoto(userId, selectedProfileUri.toString());
        }
        if (ValidationUtils.hasText(address)) {
            new AddressRepository(this).replaceDefault(userId, address);
        }

        UiUtils.toast(this, "Profile updated");
        finish();
    }

    private void showInitials(String name) {
        profileImage.setVisibility(View.GONE);
        avatarText.setVisibility(View.VISIBLE);
        avatarText.setText(initials(name));
    }

    private String initials(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) return "PX";
        String[] parts = clean.split("\\s+");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return clean.substring(0, Math.min(2, clean.length())).toUpperCase();
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
