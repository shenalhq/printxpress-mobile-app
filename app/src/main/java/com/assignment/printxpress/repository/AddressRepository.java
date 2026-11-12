package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.assignment.printxpress.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps database operations out of the Activity classes.
 */
public class AddressRepository {
    private final DatabaseHelper helper;

    public AddressRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public List<String> getAll(long userId) {
        List<String> addresses = new ArrayList<>();
        String sql =
                "SELECT label,address,is_default FROM addresses " +
                "WHERE user_id=? ORDER BY is_default DESC,id DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                String marker = cursor.getInt(2) == 1 ? "★ " : "";
                addresses.add(marker + cursor.getString(0) + "\n" + cursor.getString(1));
            }
        }
        return addresses;
    }

    public boolean add(long userId, String label, String address) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("label", label.trim());
        values.put("address", address.trim());
        return helper.getWritableDatabase().insert("addresses", null, values) > 0;
    }

    public void replaceDefault(long userId, String address) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long defaultId = -1L;
            try (Cursor cursor = db.rawQuery(
                    "SELECT id FROM addresses WHERE user_id=? AND is_default=1 LIMIT 1",
                    new String[]{String.valueOf(userId)}
            )) {
                if (cursor.moveToFirst()) defaultId = cursor.getLong(0);
            }

            ContentValues values = new ContentValues();
            values.put("label", "Default");
            values.put("address", address.trim());
            values.put("is_default", 1);

            if (defaultId > 0) {
                db.update(
                        "addresses",
                        values,
                        "id=? AND user_id=?",
                        new String[]{String.valueOf(defaultId), String.valueOf(userId)}
                );
            } else {
                values.put("user_id", userId);
                db.insertOrThrow("addresses", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
