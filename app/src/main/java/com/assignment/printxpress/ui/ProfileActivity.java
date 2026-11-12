package com.assignment.printxpress.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.repository.AddressRepository;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.repository.OrderRepository;
import com.assignment.printxpress.repository.SavedDesignRepository;
import com.assignment.printxpress.util.BottomDockHelper;
import com.assignment.printxpress.util.SessionManager;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Displays the customer profile and links to profile-related features.
 */
public class ProfileActivity extends AppCompatActivity {
    private long userId;
    private AuthRepository repository;
    private TextView nameText;
    private TextView emailText;
    private TextView addressText;
    private TextView avatarText;
    private ImageView profileImage;
    private TextView statOrdersText;
    private TextView statDesignsText;
    private TextView statAddressesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        repository = new AuthRepository(this);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        addressText = findViewById(R.id.addressText);
        avatarText = findViewById(R.id.avatarText);
        profileImage = findViewById(R.id.profileImage);
        statOrdersText = findViewById(R.id.statOrdersText);
        statDesignsText = findViewById(R.id.statDesignsText);
        statAddressesText = findViewById(R.id.statAddressesText);

        findViewById(R.id.editProfileButton).setOnClickListener(
                v -> startActivity(new Intent(this, EditProfileActivity.class))
        );
        findViewById(R.id.addressesButton).setOnClickListener(v -> startActivity(new Intent(this, AddressesActivity.class)));
        findViewById(R.id.designsButton).setOnClickListener(v -> startActivity(new Intent(this, SavedDesignsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.resourcesButton).setOnClickListener(v -> startActivity(new Intent(this, DesignResourcesActivity.class)));
        findViewById(R.id.supportButton).setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        findViewById(R.id.signOutButton).setOnClickListener(v -> signOut());

        BottomDockHelper.bind(this, BottomDockHelper.PROFILE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        User user = repository.getUser(userId);
        if (user == null) return;

        nameText.setText(user.name);
        emailText.setText(user.email + " • " + user.phone);
        addressText.setText(user.address == null || user.address.trim().isEmpty() ? "No default address added" : user.address);
        avatarText.setText(initials(user.name));

        if (user.profileUri != null && !user.profileUri.trim().isEmpty()) {
            try {
                profileImage.setImageURI(Uri.parse(user.profileUri));
                profileImage.setVisibility(View.VISIBLE);
                avatarText.setVisibility(View.GONE);
            } catch (Exception ignored) {
                showInitials();
            }
        } else {
            showInitials();
        }

        statOrdersText.setText(String.valueOf(new OrderRepository(this).getOrders(userId).size()));
        statDesignsText.setText(String.valueOf(new SavedDesignRepository(this).getAll(userId).size()));
        statAddressesText.setText(String.valueOf(new AddressRepository(this).getAll(userId).size()));
    }

    private void showInitials() {
        profileImage.setVisibility(View.GONE);
        avatarText.setVisibility(View.VISIBLE);
    }

    private void signOut() {
        new SessionManager(this).signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
}
