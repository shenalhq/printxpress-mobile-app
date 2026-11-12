package com.assignment.printxpress.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.User;
import com.assignment.printxpress.util.PasswordUtils;

/**
 * Handles registration, login and profile database operations.
 */
public class AuthRepository {
    private final DatabaseHelper helper;
    public AuthRepository(Context context) { helper = DatabaseHelper.getInstance(context); }

    public long register(String name, String email, String phone, String password, String address) {
        String salt = PasswordUtils.newSalt();
        ContentValues cv = new ContentValues();
        cv.put("name", name.trim()); cv.put("email", email.trim().toLowerCase()); cv.put("phone", phone.trim());
        cv.put("salt", salt); cv.put("password_hash", PasswordUtils.hash(password, salt)); cv.put("address", address.trim());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long userId = db.insertOrThrow("users", null, cv);
            if (!address.trim().isEmpty()) {
                ContentValues a = new ContentValues();
                a.put("user_id", userId); a.put("label", "Home"); a.put("address", address.trim()); a.put("is_default", 1);
                db.insertOrThrow("addresses", null, a);
            }
            ContentValues n = new ContentValues();
            n.put("user_id", userId); n.put("title", "Welcome to PrintXpress");
            n.put("message", "Your account was created successfully. Start by choosing a print product.");
            db.insertOrThrow("notifications", null, n);
            db.setTransactionSuccessful();
            return userId;
        } catch (SQLiteConstraintException ex) {
            return -1L;
        } finally { db.endTransaction(); }
    }

    public User login(String identity, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,email,phone,address,profile_uri,password_hash,salt FROM users WHERE lower(email)=lower(?) OR phone=? LIMIT 1", new String[]{identity.trim(), identity.trim()})) {
            if (!c.moveToFirst()) return null;
            String hash = c.getString(c.getColumnIndexOrThrow("password_hash"));
            String salt = c.getString(c.getColumnIndexOrThrow("salt"));
            if (!PasswordUtils.matches(password, salt, hash)) return null;
            return new User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
        }
    }

    public User getUser(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,email,phone,address,profile_uri FROM users WHERE id=?", new String[]{String.valueOf(userId)})) {
            if (!c.moveToFirst()) return null;
            return new User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
        }
    }

    public boolean updateProfilePhoto(long userId, String profileUri) {
        ContentValues cv = new ContentValues();
        cv.put("profile_uri", profileUri == null ? "" : profileUri);
        return helper.getWritableDatabase().update(
                "users", cv, "id=?", new String[]{String.valueOf(userId)}
        ) == 1;
    }

    public boolean updateProfile(long userId, String name, String phone, String address) {
        ContentValues cv = new ContentValues();
        cv.put("name", name.trim()); cv.put("phone", phone.trim()); cv.put("address", address.trim());
        try { return helper.getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(userId)}) == 1; }
        catch (SQLiteConstraintException ex) { return false; }
    }
}
