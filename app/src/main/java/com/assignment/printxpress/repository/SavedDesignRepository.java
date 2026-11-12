package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.SavedDesign;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps database operations out of the Activity classes.
 */
public class SavedDesignRepository {
    private final DatabaseHelper helper;

    public SavedDesignRepository(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    public boolean save(long userId, String name, String uri, String notes) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name.trim());
        values.put("uri", uri);
        values.put("notes", notes == null ? "" : notes.trim());
        return helper.getWritableDatabase().insert("saved_designs", null, values) > 0;
    }

    public List<SavedDesign> getAll(long userId) {
        List<SavedDesign> designs = new ArrayList<>();
        String sql =
                "SELECT id, name, uri, notes, created_at FROM saved_designs " +
                "WHERE user_id=? ORDER BY id DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                designs.add(new SavedDesign(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            }
        }
        return designs;
    }

    public boolean delete(long designId) {
        return helper.getWritableDatabase().delete(
                "saved_designs",
                "id=?",
                new String[]{String.valueOf(designId)}
        ) > 0;
    }
}
