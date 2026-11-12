package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.CartAdapter;
import com.assignment.printxpress.model.CartItem;
import com.assignment.printxpress.repository.CartRepository;
import com.assignment.printxpress.util.BottomDockHelper;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Shows the saved cart items and lets the customer update or remove them.
 */
public class CartActivity extends AppCompatActivity {
    private long userId;
    private CartRepository repository;
    private CartAdapter adapter;
    private TextView totalText;
    private TextView deliveryProgressText;
    private TextView itemCountText;
    private TextView deliveryLabelText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        repository = new CartRepository(this);
        totalText = findViewById(R.id.totalText);
        deliveryProgressText = findViewById(R.id.deliveryProgress);
        itemCountText = findViewById(R.id.itemCountText);
        deliveryLabelText = findViewById(R.id.deliveryLabelText);

        findViewById(R.id.topSearchButton).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        RecyclerView list = findViewById(R.id.cartList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(new CartAdapter.Listener() {
            @Override
            public void onQuantity(CartItem item, int newQuantity) {
                repository.updateQuantity(item.id, newQuantity);
                refresh();
            }

            @Override
            public void onRemove(CartItem item) {
                repository.remove(item.id);
                refresh();
            }
        });
        list.setAdapter(adapter);

        findViewById(R.id.checkoutButton).setOnClickListener(v -> {
            if (repository.count(userId) == 0) {
                UiUtils.toast(this, "Your cart is empty");
                return;
            }
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        BottomDockHelper.bind(this, BottomDockHelper.CART);
        refresh();
    }

    private void refresh() {
        List<CartItem> items = repository.getItems(userId);
        adapter.submit(items);
        itemCountText.setText(String.valueOf(items.size()));
        deliveryLabelText.setText(deliverySummary(items));

        double total = repository.total(userId);
        totalText.setText(FormatUtils.money(total));

        double remaining = Math.max(5000 - total, 0);
        deliveryProgressText.setText(
                remaining == 0
                        ? "✓ Free delivery unlocked for this cart"
                        : "Add " + FormatUtils.money(remaining) + " more to unlock free delivery"
        );
    }


    private String deliverySummary(List<CartItem> items) {
        if (items.isEmpty()) return "Not selected";
        String first = items.get(0).fulfillment == null ? "" : items.get(0).fulfillment;
        for (CartItem item : items) {
            String value = item.fulfillment == null ? "" : item.fulfillment;
            if (!first.equals(value)) return "Mixed options";
        }
        return first.isEmpty() ? "Not selected" : first;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) refresh();
    }
}
