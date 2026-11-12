package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.NotificationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and updates customer notifications and offers.
 */
public class NotificationRepository {
    private final DatabaseHelper helper;

    public NotificationRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public List<NotificationItem> getAll(long userId) {
        List<NotificationItem> notifications = new ArrayList<>();
        String sql =
                "SELECT id,title,message,is_read,created_at FROM notifications " +
                "WHERE user_id=? ORDER BY id DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                NotificationItem notification = new NotificationItem();
                notification.id = cursor.getLong(0);
                notification.title = cursor.getString(1);
                notification.message = cursor.getString(2);
                notification.read = cursor.getInt(3) == 1;
                notification.createdAt = cursor.getString(4);
                notifications.add(notification);
            }
        }
        return notifications;
    }

    public int unreadCount(long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    public void markAllRead(long userId) {
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        helper.getWritableDatabase().update(
                "notifications",
                values,
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }
}
