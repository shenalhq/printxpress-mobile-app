package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;

import com.assignment.printxpress.data.DatabaseHelper;

/**
 * Keeps database operations out of the Activity classes.
 */
public class SupportRepository {
    private final DatabaseHelper helper;

    public SupportRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public boolean submit(long userId, String subject, String message) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("subject", subject.trim());
        values.put("message", message.trim());
        return helper.getWritableDatabase().insert("support_requests", null, values) > 0;
    }
}
