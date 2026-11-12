package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.SavedDesignAdapter;
import com.assignment.printxpress.model.SavedDesign;
import com.assignment.printxpress.repository.SavedDesignRepository;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class SavedDesignsActivity extends AppCompatActivity {
    private SavedDesignAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private SavedDesignRepository repository;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_designs);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        repository = new SavedDesignRepository(this);
        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);

        recyclerView = findViewById(R.id.designsRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        adapter = new SavedDesignAdapter(new SavedDesignAdapter.Listener() {
            @Override
            public void onUse(SavedDesign design) {
                UiUtils.toast(SavedDesignsActivity.this, "Using design: " + design.name);
                Intent intent = new Intent(SavedDesignsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            @Override
            public void onDelete(SavedDesign design) {
                confirmDelete(design);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.createDesignBottomButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        loadDesigns();
    }

    private void loadDesigns() {
        List<SavedDesign> designs = repository.getAll(userId);
        if (designs.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            adapter.submit(designs);
        }
    }

    private void confirmDelete(SavedDesign design) {
        new AlertDialog.Builder(this)
                .setTitle("Delete design")
                .setMessage("Are you sure you want to delete '" + design.name + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (repository.delete(design.id)) {
                        UiUtils.toast(this, "Design deleted");
                        loadDesigns();
                    } else {
                        UiUtils.toast(this, "Could not delete design");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
