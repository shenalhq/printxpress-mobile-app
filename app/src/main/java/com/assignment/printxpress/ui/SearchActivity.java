package com.assignment.printxpress.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.ProductAdapter;
import com.assignment.printxpress.model.Product;
import com.assignment.printxpress.repository.ProductRepository;
import com.assignment.printxpress.util.BottomDockHelper;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Searches and filters the available printing products.
 */
public class SearchActivity extends AppCompatActivity {
    private ProductRepository repository;
    private ProductAdapter adapter;
    private TextInputEditText searchInput;
    private TextView resultCountText;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);

        repository = new ProductRepository(this);
        String requestedCategory = getIntent().getStringExtra("category");
        if (requestedCategory != null && !requestedCategory.trim().isEmpty()) {
            selectedCategory = requestedCategory.trim();
        }
        searchInput = findViewById(R.id.searchInput);
        resultCountText = findViewById(R.id.resultCount);

        bindProductList();
        bindCategoryChips();
        bindSearch();
        BottomDockHelper.bind(this, BottomDockHelper.SEARCH);
        refresh();
    }

    private void bindProductList() {
        RecyclerView list = findViewById(R.id.productList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.id);
            startActivity(intent);
        });
        list.setAdapter(adapter);
    }

    private void bindCategoryChips() {
        ChipGroup group = findViewById(R.id.categoryGroup);
        String[] categories = {
                "All", "Business Cards", "Flyers", "Posters",
                "Banners", "Stickers", "Merchandise"
        };

        int bg = ContextCompat.getColor(this, R.color.px_surface);
        int checked = ContextCompat.getColor(this, R.color.px_primary);
        int text = ContextCompat.getColor(this, R.color.px_text);
        int checkedText = ContextCompat.getColor(this, android.R.color.white);

        for (int i = 0; i < categories.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(categories[i]);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setChipCornerRadius(42f);
            chip.setChipBackgroundColor(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{checked, bg}
            ));
            chip.setTextColor(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{checkedText, text}
            ));
            group.addView(chip);

            if (categories[i].equals(selectedCategory)) chip.setChecked(true);
            chip.setOnClickListener(v -> {
                selectedCategory = ((Chip) v).getText().toString();
                refresh();
            });
        }
    }

    private void bindSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh();
            }
        });
    }

    private void refresh() {
        String term = searchInput.getText() == null ? "" : searchInput.getText().toString();
        List<Product> products = repository.search(term, selectedCategory);
        adapter.submit(products);
        resultCountText.setText(
                products.size() + " result" + (products.size() == 1 ? "" : "s") +
                        " in " + selectedCategory
        );
    }
}
