package com.assignment.printxpress.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.assignment.printxpress.ui.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Small reusable helper used by the PrintXpress application.
 */
public final class UiUtils {
    private UiUtils() {}
    public static void back(MaterialToolbar toolbar, Activity activity){ toolbar.setNavigationOnClickListener(v -> activity.finish()); }
    public static void toast(Context context,String message){ Toast.makeText(context,message,Toast.LENGTH_SHORT).show(); }
    public static long requireUser(Activity activity){
        long id=new SessionManager(activity).getUserId();
        if(id<=0){ Intent i=new Intent(activity, LoginActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);activity.startActivity(i);activity.finish(); }
        return id;
    }
}
