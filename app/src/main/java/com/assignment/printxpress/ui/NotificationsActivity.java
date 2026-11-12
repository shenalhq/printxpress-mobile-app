package com.assignment.printxpress.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.NotificationAdapter;
import com.assignment.printxpress.repository.NotificationRepository;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class NotificationsActivity extends AppCompatActivity {
    private long userId;
    private NotificationRepository repository;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Notifications & offers");
        UiUtils.back(toolbar, this);

        TextView helperText = findViewById(R.id.helperText);
        helperText.setText("Order updates, print tips and seasonal promotions.");

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        list.setAdapter(adapter);

        repository = new NotificationRepository(this);

        MaterialButton actionButton = findViewById(R.id.actionButton);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText("Mark all as read");
        actionButton.setOnClickListener(v -> {
            repository.markAllRead(userId);
            refresh();
        });

        refresh();
    }

    private void refresh() {
        adapter.submit(repository.getAll(userId));
    }
}
