package com.assignment.printxpress.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.adapter.ProductAdapter;
import com.assignment.printxpress.model.Product;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.repository.NotificationRepository;
import com.assignment.printxpress.repository.ProductRepository;
import com.assignment.printxpress.util.BottomDockHelper;
import com.assignment.printxpress.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen that shows products, offers, notifications and main navigation.
 */
public class MainActivity extends AppCompatActivity {
    private static final long OFFER_INTERVAL_MS = 4500L;

    private final Handler offerHandler = new Handler(Looper.getMainLooper());
    private final int[] offerImages = {
            R.drawable.promo_business_pack,
            R.drawable.promo_sticker_week,
            R.drawable.promo_merch_week
    };
    private final String[] offerEyebrows = {
            "BUSINESS OFFER",
            "STICKER WEEK",
            "CUSTOM MERCH"
    };
    private final String[] offerTitles = {
            "Build your brand for less",
            "Make your logo stick",
            "Turn ideas into merch"
    };
    private final String[] offerDescriptions = {
            "Special bundle pricing on business cards and flyers for new brands.",
            "Save on colourful vinyl sticker packs for packaging, laptops and events.",
            "Create personalised mugs and T-shirts for teams, gifts and promotions."
    };
    private final String[] offerCategories = {
            "Business Cards",
            "Stickers",
            "Merchandise"
    };

    private long userId;
    private NotificationRepository notificationRepository;
    private AuthRepository authRepository;
    private TextView notificationBadge;
    private ImageView topProfileImage;
    private ImageView promoImage;
    private TextView promoEyebrow;
    private TextView promoTitle;
    private TextView promoDescription;
    private View promoDot1;
    private View promoDot2;
    private View promoDot3;
    private int currentOffer = 0;

    private final Runnable rotateOffer = new Runnable() {
        @Override
        public void run() {
            currentOffer = (currentOffer + 1) % offerImages.length;
            showOffer(currentOffer, true);
            offerHandler.postDelayed(this, OFFER_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        notificationRepository = new NotificationRepository(this);
        authRepository = new AuthRepository(this);
        notificationBadge = findViewById(R.id.notificationBadge);
        topProfileImage = findViewById(R.id.topProfileImage);
        promoImage = findViewById(R.id.promoImage);
        promoEyebrow = findViewById(R.id.promoEyebrow);
        promoTitle = findViewById(R.id.promoTitle);
        promoDescription = findViewById(R.id.promoDescription);
        promoDot1 = findViewById(R.id.promoDot1);
        promoDot2 = findViewById(R.id.promoDot2);
        promoDot3 = findViewById(R.id.promoDot3);

        bindGreeting();
        bindProducts();
        bindNavigation();
        showOffer(0, false);
        BottomDockHelper.bind(this, BottomDockHelper.HOME);
    }

    private void bindGreeting() {
        User user = authRepository.getUser(userId);
        String greeting = "Hello";
        if (user != null && user.name != null && !user.name.trim().isEmpty()) {
            greeting = "Hello, " + user.name.trim().split("\\s+")[0];
        }
        ((TextView) findViewById(R.id.greetingText)).setText(greeting);
    }

    private void bindProducts() {
        RecyclerView list = findViewById(R.id.productList);
        list.setLayoutManager(new LinearLayoutManager(this));

        ProductAdapter adapter = new ProductAdapter(product -> openProduct(product.id));
        list.setAdapter(adapter);

        List<Product> products = new ProductRepository(this).getAll();
        int homeLimit = Math.min(4, products.size());
        adapter.submit(new ArrayList<>(products.subList(0, homeLimit)));
    }

    private void bindNavigation() {
        findViewById(R.id.searchBox).setOnClickListener(v -> open(SearchActivity.class));
        findViewById(R.id.notificationContainer).setOnClickListener(v -> open(NotificationsActivity.class));
        findViewById(R.id.profileTopButton).setOnClickListener(v -> open(ProfileActivity.class));
        findViewById(R.id.seeAllProducts).setOnClickListener(v -> open(SearchActivity.class));
        findViewById(R.id.promoCard).setOnClickListener(v -> openOfferCategory());
        findViewById(R.id.promoCta).setOnClickListener(v -> openOfferCategory());
        promoDot1.setOnClickListener(v -> selectOffer(0));
        promoDot2.setOnClickListener(v -> selectOffer(1));
        promoDot3.setOnClickListener(v -> selectOffer(2));
    }

    private void selectOffer(int index) {
        currentOffer = index;
        offerHandler.removeCallbacks(rotateOffer);
        showOffer(index, true);
        offerHandler.postDelayed(rotateOffer, OFFER_INTERVAL_MS);
    }

    private void showOffer(int index, boolean animate) {
        if (animate) {
            promoImage.animate().alpha(0f).setDuration(140).withEndAction(() -> {
                applyOffer(index);
                promoImage.animate().alpha(1f).setDuration(240).start();
            }).start();
        } else {
            applyOffer(index);
        }
    }

    private void applyOffer(int index) {
        promoImage.setImageResource(offerImages[index]);
        promoEyebrow.setText(offerEyebrows[index]);
        promoTitle.setText(offerTitles[index]);
        promoDescription.setText(offerDescriptions[index]);
        setDot(promoDot1, index == 0);
        setDot(promoDot2, index == 1);
        setDot(promoDot3, index == 2);
    }

    private void setDot(View dot, boolean active) {
        android.view.ViewGroup.LayoutParams params = dot.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        params.width = Math.round((active ? 22 : 7) * density);
        params.height = Math.round((active ? 6 : 7) * density);
        dot.setLayoutParams(params);
        dot.setBackgroundResource(active
                ? R.drawable.bg_promo_dot_active
                : R.drawable.bg_promo_dot_inactive);
    }

    private void openOfferCategory() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("category", offerCategories[currentOffer]);
        startActivity(intent);
    }

    private void openProduct(long productId) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", productId);
        startActivity(intent);
    }

    private void open(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void refreshHeader() {
        int unread = notificationRepository.unreadCount(userId);
        if (unread > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(unread > 9 ? "9+" : String.valueOf(unread));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }

        User user = authRepository.getUser(userId);
        if (user != null && user.profileUri != null && !user.profileUri.trim().isEmpty()) {
            try {
                topProfileImage.setPadding(0, 0, 0, 0);
                topProfileImage.setImageURI(Uri.parse(user.profileUri));
            } catch (Exception ignored) {
                showDefaultProfileIcon();
            }
        } else {
            showDefaultProfileIcon();
        }
    }

    private void showDefaultProfileIcon() {
        int p = Math.round(8 * getResources().getDisplayMetrics().density);
        topProfileImage.setPadding(p, p, p, p);
        topProfileImage.setImageResource(R.drawable.ic_nav_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId > 0 && notificationRepository != null) {
            bindGreeting();
            refreshHeader();
            offerHandler.removeCallbacks(rotateOffer);
            offerHandler.postDelayed(rotateOffer, OFFER_INTERVAL_MS);
        }
    }

    @Override
    protected void onPause() {
        offerHandler.removeCallbacks(rotateOffer);
        super.onPause();
    }
}
