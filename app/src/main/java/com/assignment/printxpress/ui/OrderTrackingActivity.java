package com.assignment.printxpress.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.Order;
import com.assignment.printxpress.repository.OrderRepository;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.UiUtils;
import com.assignment.printxpress.util.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for one of the main PrintXpress user interface screens.
 */
public class OrderTrackingActivity extends AppCompatActivity {
    private long userId;
    private long orderId;
    private OrderRepository repository;
    private TextInputEditText rescheduleInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);
        orderId = getIntent().getLongExtra("order_id", -1L);
        repository = new OrderRepository(this);
        rescheduleInput = findViewById(R.id.rescheduleInput);

        findViewById(R.id.rescheduleButton).setOnClickListener(v -> requestReschedule());
        findViewById(R.id.cancelButton).setOnClickListener(v -> confirmCancellation());
        refresh();
    }

    private void refresh() {
        Order order = repository.find(orderId, userId);
        if (order == null) {
            UiUtils.toast(this, "Order not found");
            finish();
            return;
        }

        ((TextView) findViewById(R.id.codeText)).setText(order.code);
        ((TextView) findViewById(R.id.statusText)).setText(order.status);
        ((TextView) findViewById(R.id.detailsText)).setText(
                FormatUtils.money(order.total) + "\n" +
                        order.fulfillment + "\n" +
                        order.address + "\nPayment: " + order.paymentMethod +
                        "\nCreated: " + order.createdAt
        );

        String timeline;
        if ("Cancelled".equals(order.status)) {
            timeline = "✓ Order confirmed\n○ Production stopped\n○ Cancelled by customer";
        } else if ("Ready for pickup".equals(order.status) || "Ready for delivery".equals(order.status)) {
            timeline = "✓ Order confirmed\n✓ Processing\n✓ Printing\n✓ " + order.status;
        } else if ("Printing".equals(order.status)) {
            timeline = "✓ Order confirmed\n✓ Processing\n✓ Printing\n○ Ready for pickup / delivery";
        } else {
            timeline = "✓ Order confirmed\n✓ Processing\n○ Printing\n○ Ready for pickup / delivery";
        }
        ((TextView) findViewById(R.id.timelineText)).setText(timeline);

        boolean editable = "Processing".equals(order.status);
        findViewById(R.id.rescheduleButton).setVisibility(editable ? View.VISIBLE : View.GONE);
        findViewById(R.id.cancelButton).setVisibility(editable ? View.VISIBLE : View.GONE);
        rescheduleInput.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void requestReschedule() {
        String note = rescheduleInput.getText() == null
                ? ""
                : rescheduleInput.getText().toString().trim();

        if (!ValidationUtils.hasText(note)) {
            rescheduleInput.setError("Enter a preferred date or note");
            return;
        }

        boolean saved = repository.reschedule(orderId, userId, note);
        UiUtils.toast(
                this,
                saved
                        ? "Reschedule request saved"
                        : "This order can no longer be rescheduled"
        );
    }

    private void confirmCancellation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel order?")
                .setMessage("Cancellation is allowed only while the order is still processing.")
                .setNegativeButton("Keep order", null)
                .setPositiveButton("Cancel order", (dialog, which) -> {
                    boolean cancelled = repository.cancel(orderId, userId);
                    UiUtils.toast(
                            this,
                            cancelled ? "Order cancelled" : "Order can no longer be cancelled"
                    );
                    refresh();
                })
                .show();
    }
}
