package com.assignment.printxpress.util;

import com.assignment.printxpress.R;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class ProductImageResolver {
    private ProductImageResolver() {}

    public static int forProductName(String productName) {
        String name = productName == null ? "" : productName.toLowerCase();
        if (name.contains("business card")) return R.drawable.product_business_cards;
        if (name.contains("flyer")) return R.drawable.product_flyers;
        if (name.contains("poster")) return R.drawable.product_posters;
        if (name.contains("banner")) return R.drawable.product_banners;
        if (name.contains("sticker")) return R.drawable.product_stickers;
        if (name.contains("mug")) return R.drawable.product_mug;
        if (name.contains("t-shirt") || name.contains("tshirt") || name.contains("shirt")) {
            return R.drawable.product_tshirt;
        }
        return R.drawable.ic_placeholder;
    }
}
