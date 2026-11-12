package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Creates orders from cart items and manages order status operations.
 */
public class OrderRepository {
    private final DatabaseHelper helper;

    public OrderRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public String placeOrder(
            long userId,
            String fulfillment,
            String address,
            String paymentMethod
    ) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            double total = cartTotal(db, userId);
            if (total <= 0) return null;

            String code = "PX-" + new SimpleDateFormat(
                    "yyMMdd-HHmmssSSS",
                    Locale.US
            ).format(new Date());

            ContentValues orderValues = new ContentValues();
            orderValues.put("order_code", code);
            orderValues.put("user_id", userId);
            orderValues.put("status", "Processing");
            orderValues.put("total", total);
            orderValues.put("fulfillment", fulfillment);
            orderValues.put("address", address == null ? "" : address.trim());
            orderValues.put("payment_method", paymentMethod);
            long orderId = db.insertOrThrow("orders", null, orderValues);

            copyCartItemsToOrder(db, userId, orderId);
            db.delete("cart_items", "user_id=?", new String[]{String.valueOf(userId)});
            addNotification(
                    db,
                    userId,
                    "Order confirmed",
                    code + " is confirmed and waiting for production."
            );

            db.setTransactionSuccessful();
            return code;
        } finally {
            db.endTransaction();
        }
    }

    private double cartTotal(SQLiteDatabase db, long userId) {
        String sql =
                "SELECT COALESCE(SUM(c.quantity*p.base_price),0) " +
                "FROM cart_items c JOIN products p ON p.id=c.product_id " +
                "WHERE c.user_id=?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            cursor.moveToFirst();
            return cursor.getDouble(0);
        }
    }

    private void copyCartItemsToOrder(SQLiteDatabase db, long userId, long orderId) {
        String sql =
                "SELECT c.product_id,p.name,c.quantity,p.base_price,c.size,c.material," +
                "c.finish,c.instructions,c.artwork_uri " +
                "FROM cart_items c JOIN products p ON p.id=c.product_id " +
                "WHERE c.user_id=?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                ContentValues item = new ContentValues();
                item.put("order_id", orderId);
                item.put("product_id", cursor.getLong(0));
                item.put("product_name", cursor.getString(1));
                item.put("quantity", cursor.getInt(2));
                item.put("unit_price", cursor.getDouble(3));
                item.put("size", cursor.getString(4));
                item.put("material", cursor.getString(5));
                item.put("finish", cursor.getString(6));
                item.put("instructions", cursor.getString(7));
                item.put("artwork_uri", cursor.getString(8));
                db.insertOrThrow("order_items", null, item);
            }
        }
    }

    public List<Order> getOrders(long userId) {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT id,order_code,status,total,fulfillment,address,payment_method,created_at " +
                "FROM orders WHERE user_id=? ORDER BY id DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                orders.add(readOrder(cursor));
            }
        }
        return orders;
    }

    public Order find(long orderId, long userId) {
        String sql =
                "SELECT id,order_code,status,total,fulfillment,address,payment_method,created_at " +
                "FROM orders WHERE id=? AND user_id=?";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(orderId), String.valueOf(userId)}
        )) {
            return cursor.moveToFirst() ? readOrder(cursor) : null;
        }
    }

    public boolean cancel(long orderId, long userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("status", "Cancelled");
            int changed = db.update(
                    "orders",
                    values,
                    "id=? AND user_id=? AND status='Processing'",
                    new String[]{String.valueOf(orderId), String.valueOf(userId)}
            );

            if (changed == 1) {
                addNotification(
                        db,
                        userId,
                        "Order cancelled",
                        "Your processing order was cancelled successfully."
                );
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean reschedule(long orderId, long userId, String note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql =
                "SELECT order_code FROM orders " +
                "WHERE id=? AND user_id=? AND status='Processing'";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(orderId), String.valueOf(userId)}
        )) {
            if (!cursor.moveToFirst()) return false;
            addNotification(
                    db,
                    userId,
                    "Reschedule requested",
                    "Reschedule request for " + cursor.getString(0) + ": " + note
            );
            return true;
        }
    }

    private Order readOrder(Cursor cursor) {
        Order order = new Order();
        order.id = cursor.getLong(0);
        order.code = cursor.getString(1);
        order.status = cursor.getString(2);
        order.total = cursor.getDouble(3);
        order.fulfillment = cursor.getString(4);
        order.address = cursor.getString(5);
        order.paymentMethod = cursor.getString(6);
        order.createdAt = cursor.getString(7);
        return order;
    }

    private void addNotification(
            SQLiteDatabase db,
            long userId,
            String title,
            String message
    ) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("message", message);
        values.put("type", "order");
        db.insertOrThrow("notifications", null, values);
    }
}
