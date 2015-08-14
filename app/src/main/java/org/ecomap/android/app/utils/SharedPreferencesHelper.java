package org.ecomap.android.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.ecomap.android.app.PersistentCookieStore;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;

import java.net.HttpCookie;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesHelper {

    private static final String LOG_TAG = SharedPreferencesHelper.class.getSimpleName();

    private static SharedPreferences sPref = null;

    public static void addLikedProblem(Context mContext, int problem_id) {

        sPref = mContext.getSharedPreferences(mContext.getString(R.string.fileNamePreferences), Context.MODE_PRIVATE);

        //get an existing set
        Set<String> set = sPref.getStringSet(mContext.getString(R.string.prefLikedProblems), null);

        if (set == null) {
            set = new HashSet<String>();
        }

        set.add(String.valueOf(problem_id));

        SharedPreferences.Editor editor = sPref.edit();
        editor.putStringSet(mContext.getString(R.string.prefLikedProblems),set);
        editor.commit();

        Log.d(LOG_TAG,"Problem was added to set LikedProblem into SharedPreferences");

    }

    //check if set in SharedPreferences contains such problem_id
    public static boolean isLikedProblem(Context mContext, int problem_id){

        sPref = mContext.getSharedPreferences(mContext.getString(R.string.fileNamePreferences), Context.MODE_PRIVATE);

        //get an existing set
        Set<String> set = sPref.getStringSet(mContext.getString(R.string.prefLikedProblems), null);

        if (set != null){
            boolean isLiked = set.contains(String.valueOf(problem_id));

            if (isLiked)
                Log.d(LOG_TAG, "Problem was liked before");
            else
                Log.d(LOG_TAG, "Problem was not liked before");

            return isLiked;
        }

        return false;
    }

    public static String getStringPref(Context mContext, String fileNamePreferences, String namePref, String defValue){

        sPref = mContext.getSharedPreferences(fileNamePreferences, Context.MODE_PRIVATE);
        return sPref.getString(namePref, defValue);

    }

    public static Set<String> getStringSetPref(Context mContext, String fileNamePreferences, String namePref, Set<String> defValue){

        sPref = mContext.getSharedPreferences(fileNamePreferences, Context.MODE_PRIVATE);
        return sPref.getStringSet(namePref, defValue);

    }

    public static int getIntegerPref(Context mContext, String fileNamePreferences, String namePref, int defValue){

        sPref = mContext.getSharedPreferences(fileNamePreferences, Context.MODE_PRIVATE);
        return sPref.getInt(namePref, defValue);

    }

    public static void onLogOutClearPref(Context mContext) {

        sPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.fileNamePreferences), Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = sPref.edit();

        edit.remove(MainActivity.LAST_NAME_KEY);
        edit.remove(MainActivity.FIRST_NAME_KEY);
        edit.remove(MainActivity.EMAIL_KEY);
        edit.remove(MainActivity.PASSWORD_KEY);

        edit.apply();

        Log.d(LOG_TAG, "Log in preferences was removed");

    }

    public static void onLogInSavePref(Context mContext,String first_name,String last_name,String email, String pass, String role, String user_id, Set<String> set) {

        sPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.fileNamePreferences), Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = sPref.edit();

        edit.putString(MainActivity.FIRST_NAME_KEY, first_name);
        edit.putString(MainActivity.LAST_NAME_KEY, last_name);
        edit.putString(MainActivity.EMAIL_KEY, email);
        edit.putString(MainActivity.PASSWORD_KEY, pass);
        edit.putString(MainActivity.ROLE_KEY, role);
        edit.putString(MainActivity.USER_ID_KEY, user_id);
        edit.putStringSet(MainActivity.USER_PERMISSION_SET_KEY, set);

        edit.apply();

        Log.d(LOG_TAG, "Log in preferences was added");

    }

    public static void addCookie(Context mContext, String cookieNames, String nameStores, String encodeCookie){

        sPref = mContext.getSharedPreferences(PersistentCookieStore.COOKIE_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsWriter = sPref.edit();

        prefsWriter.putString(PersistentCookieStore.COOKIE_NAME_PREFIX + cookieNames, encodeCookie);
        prefsWriter.putString(PersistentCookieStore.COOKIE_NAME_STORE, nameStores);

        prefsWriter.apply();

        Log.d(LOG_TAG, "Cookie preferences was added");

    }

    public static void removeCookie(Context mContext, String cookieName, String nameStore){

        sPref = mContext.getSharedPreferences(PersistentCookieStore.COOKIE_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsWriter = sPref.edit();

        prefsWriter.remove(PersistentCookieStore.COOKIE_NAME_PREFIX + cookieName);
        prefsWriter.putString(PersistentCookieStore.COOKIE_NAME_STORE, nameStore);

        prefsWriter.apply();

        Log.d(LOG_TAG, "Cookie preferences was removes");

    }

    public static void removeAllCookie(Context mContext, List<HttpCookie> cookies){

        sPref = mContext.getSharedPreferences(PersistentCookieStore.COOKIE_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsWriter = sPref.edit();

        for (HttpCookie cookie : cookies) {
            String name = cookie.getName() + cookie.getDomain();
            prefsWriter.remove(PersistentCookieStore.COOKIE_NAME_PREFIX + name);
        }

        prefsWriter.remove(PersistentCookieStore.COOKIE_NAME_STORE);
        prefsWriter.apply();

        Log.d(LOG_TAG, "All cookies preferences was removes");

    }

    public static void updateNumRevision(Context mContext,int numNewRevision){

        sPref = mContext.getSharedPreferences(mContext.getString(R.string.fileNamePreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(mContext.getString(R.string.prefNumRevision), numNewRevision);
        ed.commit();

    }

}
