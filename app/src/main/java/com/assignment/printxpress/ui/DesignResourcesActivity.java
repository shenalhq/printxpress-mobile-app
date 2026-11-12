package com.assignment.printxpress.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class DesignResourcesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
    }
}
