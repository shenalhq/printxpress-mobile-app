package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.OrderAdapter;
import com.assignment.printxpress.model.Order;
import com.assignment.printxpress.repository.OrderRepository;
import com.assignment.printxpress.util.BottomDockHelper;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Shows the customer order history and opens order tracking.
 */
public class OrdersActivity extends AppCompatActivity {
    private long userId;
    private OrderRepository repository;
    private OrderAdapter adapter;
    private TextView orderCountText;
    private TextView activeOrderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        UiUtils.back(toolbar, this);

        TextView helperText = findViewById(R.id.helperText);
        helperText.setText(
                "Track production, request a reschedule, or cancel before printing begins."
        );
        orderCountText = findViewById(R.id.orderCountText);
        activeOrderText = findViewById(R.id.activeOrderText);
        findViewById(R.id.topCartButton).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));

        repository = new OrderRepository(this);
        adapter = new OrderAdapter(order -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", order.id);
            startActivity(intent);
        });
        list.setAdapter(adapter);
        BottomDockHelper.bind(this, BottomDockHelper.ORDERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            List<Order> orders = repository.getOrders(userId);
            adapter.submit(orders);
            orderCountText.setText(String.valueOf(orders.size()));

            int active = 0;
            for (Order order : orders) {
                if (order.status != null && !"Cancelled".equalsIgnoreCase(order.status)) {
                    active++;
                }
            }
            activeOrderText.setText(String.valueOf(active));
        }
    }
}
