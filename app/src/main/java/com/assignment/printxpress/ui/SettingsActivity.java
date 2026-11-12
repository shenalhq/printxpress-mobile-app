package com.assignment.printxpress.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "px_settings";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);

        setupSwitch(R.id.switchPush, "notifications_enabled", true);
        setupSwitch(R.id.switchSms, "sms_updates_enabled", true);
        setupSwitch(R.id.switchUsage, "usage_sharing_enabled", false);
    }

    private void setupSwitch(int id, String key, boolean defaultValue) {
        SwitchMaterial sw = findViewById(id);
        sw.setChecked(prefs.getBoolean(key, defaultValue));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
        });
    }
}
