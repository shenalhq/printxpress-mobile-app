package com.assignment.printxpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.repository.AuthRepository;
import com.assignment.printxpress.repository.CartRepository;
import com.assignment.printxpress.repository.OrderRepository;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class CheckoutActivity extends AppCompatActivity {
    private long userId;
    private CartRepository cartRepository;
    private TextInputEditText addressInput;
    private TextInputEditText cardNameInput;
    private TextInputEditText cardNumberInput;
    private TextInputEditText expiryInput;
    private TextInputEditText cvvInput;
    private RadioGroup fulfillmentGroup;
    private RadioGroup paymentGroup;
    private LinearLayout cardFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        cartRepository = new CartRepository(this);

        if (cartRepository.count(userId) == 0) {
            UiUtils.toast(this, "Your cart is empty");
            finish();
            return;
        }

        bindViews();
        prefillAddress();
        bindSummary();

        paymentGroup.setOnCheckedChangeListener((group, checkedId) ->
                cardFields.setVisibility(checkedId == R.id.cardRadio ? View.VISIBLE : View.GONE)
        );
        findViewById(R.id.placeOrderButton).setOnClickListener(v -> placeOrder());
    }

    private void bindViews() {
        addressInput = findViewById(R.id.addressInput);
        cardNameInput = findViewById(R.id.cardNameInput);
        cardNumberInput = findViewById(R.id.cardNumberInput);
        expiryInput = findViewById(R.id.expiryInput);
        cvvInput = findViewById(R.id.cvvInput);
        fulfillmentGroup = findViewById(R.id.fulfillmentGroup);
        paymentGroup = findViewById(R.id.paymentGroup);
        cardFields = findViewById(R.id.cardFields);
    }

    private void prefillAddress() {
        User user = new AuthRepository(this).getUser(userId);
        if (user != null) addressInput.setText(user.address);
    }

    private void bindSummary() {
        ((TextView) findViewById(R.id.itemCountText))
                .setText(cartRepository.count(userId) + " print item(s)");
        ((TextView) findViewById(R.id.totalText))
                .setText(FormatUtils.money(cartRepository.total(userId)));
    }

    private void placeOrder() {
        int fulfillmentId = fulfillmentGroup.getCheckedRadioButtonId();
        int paymentId = paymentGroup.getCheckedRadioButtonId();
        boolean homeDelivery = fulfillmentId == R.id.deliveryRadio;
        boolean cardPayment = paymentId == R.id.cardRadio;
        String address = text(addressInput);

        if (homeDelivery && !ValidationUtils.hasText(address)) {
            addressInput.setError("Delivery address is required");
            return;
        }

        if (cardPayment && !validCard()) {
            UiUtils.toast(this, "Enter valid demo card details");
            return;
        }

        String fulfillment = homeDelivery ? "Home delivery" : "Store pickup";
        String deliveryAddress = homeDelivery ? address : "PrintXpress pickup counter";
        String paymentMethod = cardPayment ? "Card (demo)" : "Pay on pickup / delivery";

        String orderCode = new OrderRepository(this).placeOrder(
                userId,
                fulfillment,
                deliveryAddress,
                paymentMethod
        );

        if (orderCode == null) {
            UiUtils.toast(this, "Order could not be created");
            return;
        }

        UiUtils.toast(this, "Order " + orderCode + " placed successfully");
        Intent intent = new Intent(this, OrdersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean validCard() {
        String number = text(cardNumberInput).replace(" ", "");
        return ValidationUtils.hasText(text(cardNameInput))
                && number.matches("\\d{13,19}")
                && text(expiryInput).matches("(0[1-9]|1[0-2])/\\d{2}")
                && text(cvvInput).matches("\\d{3,4}");
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
