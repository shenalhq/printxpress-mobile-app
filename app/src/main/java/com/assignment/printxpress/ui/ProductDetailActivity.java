package com.assignment.printxpress.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.Product;
import com.assignment.printxpress.repository.CartRepository;
import com.assignment.printxpress.repository.ProductRepository;
import com.assignment.printxpress.repository.SavedDesignRepository;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.ProductImageResolver;
import com.assignment.printxpress.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Lets the customer configure a print product and add it to the cart.
 */
public class ProductDetailActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST = 501;

    private long userId;
    private Product product;
    private Uri artworkUri;
    private TextView artworkNameText;
    private Spinner sizeSpinner;
    private Spinner materialSpinner;
    private Spinner finishSpinner;
    private TextInputEditText quantityInput;
    private TextInputEditText instructionsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        userId = UiUtils.requireUser(this);
        if (userId < 1) return;

        UiUtils.back((MaterialToolbar) findViewById(R.id.toolbar), this);

        long productId = getIntent().getLongExtra("product_id", -1L);
        product = new ProductRepository(this).find(productId);
        if (product == null) {
            UiUtils.toast(this, "Product not found");
            finish();
            return;
        }

        bindProduct();
        bindInputs();
        bindActions();
    }

    private void bindProduct() {
        ((ImageView) findViewById(R.id.productImage)).setImageResource(ProductImageResolver.forProductName(product.name));
        ((TextView) findViewById(R.id.productName)).setText(product.name);
        ((TextView) findViewById(R.id.productDescription)).setText(product.description);
        ((TextView) findViewById(R.id.productPrice))
                .setText("From " + FormatUtils.money(product.basePrice));
    }

    private void bindInputs() {
        sizeSpinner = findViewById(R.id.sizeSpinner);
        materialSpinner = findViewById(R.id.materialSpinner);
        finishSpinner = findViewById(R.id.finishSpinner);
        quantityInput = findViewById(R.id.quantityInput);
        instructionsInput = findViewById(R.id.instructionsInput);
        artworkNameText = findViewById(R.id.artworkName);

        bindSpinner(sizeSpinner, sizeOptionsForProduct());
        bindSpinner(
                materialSpinner,
                new String[]{"Standard stock", "Matte", "Gloss", "Premium / heavy stock", "Vinyl / fabric"}
        );
        bindSpinner(
                finishSpinner,
                new String[]{"Standard finish", "Matte laminate", "Gloss laminate", "No laminate"}
        );
    }

    private void bindActions() {
        findViewById(R.id.artworkButton).setOnClickListener(v -> pickArtwork());
        findViewById(R.id.saveDesignButton).setOnClickListener(v -> saveDesign());
        findViewById(R.id.addCartButton).setOnClickListener(v -> addToCart());
    }

    private void bindSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void pickArtwork() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{"application/pdf", "image/png", "image/jpeg"}
        );
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PICK_FILE_REQUEST
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        artworkUri = data.getData();
        try {
            int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(artworkUri, flags);
        } catch (Exception ignored) {
            // Some document providers do not offer persistable permissions.
        }
        artworkNameText.setText(displayName(artworkUri));
    }

    private String displayName(Uri uri) {
        String name = uri.getLastPathSegment();
        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
        } catch (Exception ignored) {
            // The URI can still be saved even if a display name is unavailable.
        }
        return name == null ? "Selected artwork" : name;
    }

    private void saveDesign() {
        if (artworkUri == null) {
            UiUtils.toast(this, "Choose artwork first");
            return;
        }

        boolean saved = new SavedDesignRepository(this).save(
                userId,
                product.name + " design",
                artworkUri.toString(),
                text(instructionsInput)
        );
        UiUtils.toast(this, saved ? "Design saved" : "Could not save design");
    }

    private void addToCart() {
        int quantity;
        try {
            quantity = Integer.parseInt(text(quantityInput));
        } catch (NumberFormatException e) {
            quantity = 0;
        }

        if (quantity < 1 || quantity > 10000) {
            quantityInput.setError("Enter a quantity from 1 to 10000");
            return;
        }

        RadioGroup group = findViewById(R.id.fulfillmentGroup);
        RadioButton selected = findViewById(group.getCheckedRadioButtonId());
        String fulfillment = selected == null ? "Store pickup" : selected.getText().toString();

        long cartItemId = new CartRepository(this).add(
                userId,
                product.id,
                quantity,
                sizeSpinner.getSelectedItem().toString(),
                materialSpinner.getSelectedItem().toString(),
                finishSpinner.getSelectedItem().toString(),
                text(instructionsInput),
                artworkUri == null ? "" : artworkUri.toString(),
                fulfillment
        );

        if (cartItemId > 0) {
            UiUtils.toast(this, "Configured item added to cart");
            Intent home = new Intent(this, MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        } else {
            UiUtils.toast(this, "Could not add item");
        }
    }

    private String[] sizeOptionsForProduct() {
        if (product == null || product.category == null) {
            return new String[]{"Standard (10 × 15 cm)", "Custom size (cm)"};
        }

        switch (product.category) {
            case "Business Cards":
                return new String[]{
                        "Standard (9 × 5 cm)",
                        "Square (6 × 6 cm)",
                        "Large (9 × 5.5 cm)",
                        "Custom size (cm)"
                };
            case "Flyers":
                return new String[]{
                        "A6 (10.5 × 14.8 cm)",
                        "A5 (14.8 × 21 cm)",
                        "A4 (21 × 29.7 cm)",
                        "Custom size (cm)"
                };
            case "Posters":
                return new String[]{
                        "A3 (29.7 × 42 cm)",
                        "A2 (42 × 59.4 cm)",
                        "A1 (59.4 × 84.1 cm)",
                        "Custom size (cm)"
                };
            case "Banners":
                return new String[]{
                        "Small (60 × 180 cm)",
                        "Medium (90 × 240 cm)",
                        "Large (120 × 300 cm)",
                        "Custom size (cm)"
                };
            case "Stickers":
                return new String[]{
                        "Small (5 × 5 cm)",
                        "Medium (7.5 × 7.5 cm)",
                        "Large (10 × 10 cm)",
                        "Custom size (cm)"
                };
            case "Merchandise":
                return new String[]{
                        "Small print area (15 × 20 cm)",
                        "Medium print area (20 × 25 cm)",
                        "Large print area (25 × 30 cm)",
                        "Custom size (cm)"
                };
            default:
                return new String[]{"Standard (10 × 15 cm)", "Custom size (cm)"};
        }
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
