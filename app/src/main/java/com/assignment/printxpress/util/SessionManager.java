package com.assignment.printxpress.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public class SessionManager {
    private static final String PREF = "printxpress_session";
    private static final String KEY_USER_ID = "user_id";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void signIn(long userId) { prefs.edit().putLong(KEY_USER_ID, userId).apply(); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1L); }
    public boolean isSignedIn() { return getUserId() > 0; }
    public void signOut() { prefs.edit().clear().apply(); }
}
