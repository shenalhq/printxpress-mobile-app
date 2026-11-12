package com.assignment.printxpress.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import com.assignment.printxpress.R;
import com.assignment.printxpress.ui.CartActivity;
import com.assignment.printxpress.ui.MainActivity;
import com.assignment.printxpress.ui.OrdersActivity;
import com.assignment.printxpress.ui.ProfileActivity;
import com.assignment.printxpress.ui.SearchActivity;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class BottomDockHelper {
    public static final String HOME = "home";
    public static final String SEARCH = "search";
    public static final String ORDERS = "orders";
    public static final String CART = "cart";
    public static final String PROFILE = "profile";

    private BottomDockHelper() {}

    public static void bind(Activity activity, String current) {
        ImageButton home = activity.findViewById(R.id.dockHome);
        ImageButton search = activity.findViewById(R.id.dockSearch);
        ImageButton orders = activity.findViewById(R.id.dockOrders);
        ImageButton cart = activity.findViewById(R.id.dockCart);
        ImageButton profile = activity.findViewById(R.id.dockProfile);

        if (home == null) return;

        style(activity, home, HOME.equals(current));
        style(activity, search, SEARCH.equals(current));
        style(activity, orders, ORDERS.equals(current));
        style(activity, cart, CART.equals(current));
        style(activity, profile, PROFILE.equals(current));

        home.setOnClickListener(v -> openIfNeeded(activity, current, HOME, MainActivity.class));
        search.setOnClickListener(v -> openIfNeeded(activity, current, SEARCH, SearchActivity.class));
        orders.setOnClickListener(v -> openIfNeeded(activity, current, ORDERS, OrdersActivity.class));
        cart.setOnClickListener(v -> openIfNeeded(activity, current, CART, CartActivity.class));
        profile.setOnClickListener(v -> openIfNeeded(activity, current, PROFILE, ProfileActivity.class));
    }

    private static void openIfNeeded(Activity activity, String current, String target, Class<?> cls) {
        if (target.equals(current)) return;
        activity.startActivity(new Intent(activity, cls));
    }

    private static void style(Activity activity, ImageButton button, boolean active) {
        int text = ContextCompat.getColor(activity, R.color.px_text);
        int primary = ContextCompat.getColor(activity, R.color.px_primary);
        button.setBackgroundResource(active ? R.drawable.bg_dock_item_active : android.R.color.transparent);
        button.setColorFilter(active ? primary : text, PorterDuff.Mode.SRC_IN);
        button.setAlpha(active ? 1f : 0.88f);
    }
}
