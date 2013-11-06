package com.sundowner.util;

import android.content.Context;
import android.content.SharedPreferences;

// TODO need to use a more secure storage mechanism than plaintext in SharedPreferences
public class LocalNativeAccountData {

    private static final String SHARED_PREFS_NAME = "LOCAL_NATIVE_ACCOUNT";
    private static final String SHARED_PREFS_USER_NAME_KEY = "LOCAL_NATIVE_ACCOUNT_USER_NAME";
    private static final String SHARED_PREFS_USER_ID_KEY = "LOCAL_NATIVE_ACCOUNT_USER_ID";

    // NOTE I couldn't find a way to access the SharedPreferences from inside this class, without
    // first passing it in

    public static LocalNativeAccountData load(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userName = prefs.getString(SHARED_PREFS_USER_NAME_KEY, null);
        String userId = prefs.getString(SHARED_PREFS_USER_ID_KEY, null);
        if (userName == null || userId == null) {
            return null;
        }
        return new LocalNativeAccountData(userName, userId);
    }

    public static void clear(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SHARED_PREFS_USER_NAME_KEY);
        editor.remove(SHARED_PREFS_USER_ID_KEY);
        editor.commit();
    }

    public String userName;
    public String userId;

    public LocalNativeAccountData(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public void save(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFS_USER_NAME_KEY, userName);
        editor.putString(SHARED_PREFS_USER_ID_KEY, userId);
        editor.commit();
    }
}
