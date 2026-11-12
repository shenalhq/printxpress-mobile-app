package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.CartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and updates configured products in the customer cart.
 */
public class CartRepository {
    private final DatabaseHelper helper;

    public CartRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public long add(
            long userId,
            long productId,
            int quantity,
            String size,
            String material,
            String finish,
            String instructions,
            String artworkUri,
            String fulfillment
    ) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        values.put("quantity", Math.max(1, quantity));
        values.put("size", size);
        values.put("material", material);
        values.put("finish", finish);
        values.put("instructions", instructions == null ? "" : instructions.trim());
        values.put("artwork_uri", artworkUri == null ? "" : artworkUri);
        values.put("fulfillment", fulfillment);
        return helper.getWritableDatabase().insert("cart_items", null, values);
    }

    public List<CartItem> getItems(long userId) {
        List<CartItem> items = new ArrayList<>();
        String sql =
                "SELECT c.id,c.product_id,p.name,c.quantity,p.base_price," +
                "c.size,c.material,c.finish,c.instructions,c.artwork_uri,c.fulfillment " +
                "FROM cart_items c " +
                "JOIN products p ON p.id=c.product_id " +
                "WHERE c.user_id=? ORDER BY c.id DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                CartItem item = new CartItem();
                item.id = cursor.getLong(0);
                item.productId = cursor.getLong(1);
                item.productName = cursor.getString(2);
                item.quantity = cursor.getInt(3);
                item.unitPrice = cursor.getDouble(4);
                item.size = cursor.getString(5);
                item.material = cursor.getString(6);
                item.finish = cursor.getString(7);
                item.instructions = cursor.getString(8);
                item.artworkUri = cursor.getString(9);
                item.fulfillment = cursor.getString(10);
                items.add(item);
            }
        }
        return items;
    }

    public int count(long userId) {
        String sql = "SELECT COALESCE(SUM(quantity),0) FROM cart_items WHERE user_id=?";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    public double total(long userId) {
        String sql =
                "SELECT COALESCE(SUM(c.quantity*p.base_price),0) " +
                "FROM cart_items c JOIN products p ON p.id=c.product_id " +
                "WHERE c.user_id=?";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            cursor.moveToFirst();
            return cursor.getDouble(0);
        }
    }

    public void updateQuantity(long itemId, int quantity) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (quantity <= 0) {
            db.delete("cart_items", "id=?", new String[]{String.valueOf(itemId)});
            return;
        }

        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        db.update("cart_items", values, "id=?", new String[]{String.valueOf(itemId)});
    }

    public void remove(long itemId) {
        helper.getWritableDatabase().delete(
                "cart_items",
                "id=?",
                new String[]{String.valueOf(itemId)}
        );
    }

    public void clear(long userId) {
        helper.getWritableDatabase().delete(
                "cart_items",
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }
}
