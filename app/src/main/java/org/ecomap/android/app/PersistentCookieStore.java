package org.ecomap.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.ecomap.android.app.data.model.SerializableUriCookiePair;
import org.ecomap.android.app.sync.EcoMapAPIContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Created by y.ridkous@gmail.com on 14.07.2015.
 * based on
 * https://github.com/loopj/android-async-http/blob/master/library/src/main/java/com/loopj/android/http/PersistentCookieStore.java#L44
 */
public class PersistentCookieStore implements CookieStore {
    private CookieStore store;

    private static final String LOG_TAG = PersistentCookieStore.class.getSimpleName();
    private static final String COOKIE_PREFS = EcoMapAPIContract.APP_PACKAGE_NAME + ".CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private boolean omitNonPersistentCookies = false;
    private final SharedPreferences cookiePrefs;

    public PersistentCookieStore(Context context) {

        // get the default in memory cookie store
        store = new CookieManager().getCookieStore();
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);

        // Load any previously stored cookies into the store
        String storedCookieNames = cookiePrefs.getString(COOKIE_NAME_STORE, null);
        if (storedCookieNames != null) {
            String[] cookieNames = TextUtils.split(storedCookieNames, ",");
            for (String name : cookieNames) {
                String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                if (encodedCookie != null) {
                    Pair<URI, HttpCookie> pairUriCookie = decodeCookie(encodedCookie);
                    if (pairUriCookie != null) {
                        store.add(pairUriCookie.first, pairUriCookie.second);
                    }
                }
            }
        }

    }

    /**
     * Add a cookie to persistent store
     * @param uri - cookie URI
     * @param cookie
     */
    public void	add(URI uri, HttpCookie cookie) {
        store.add(uri, cookie);

        String name = cookie.getName() + cookie.getDomain();

        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(COOKIE_NAME_STORE, getInlineCookiesNames());
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableUriCookiePair(uri, cookie)));
        prefsWriter.commit();

    }

    public List<HttpCookie> get(URI uri) {
        return store.get(uri);
    }

    public List<HttpCookie> getCookies() {
        return store.getCookies();
    }

    public List<URI> getURIs() {
        return store.getURIs();
    }

    public boolean remove(URI uri, HttpCookie cookie) {

        boolean b = store.remove(uri, cookie);

        String name = cookie.getName() + cookie.getDomain();
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        prefsWriter.putString(COOKIE_NAME_STORE, getInlineCookiesNames());
        prefsWriter.commit();

        return b;
    }

    public boolean removeAll()  {

        // Clear cookies from persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        List<HttpCookie> cookies = getCookies();

        for (HttpCookie cookie : cookies) {
            String name = cookie.getName() + cookie.getDomain();
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }

        prefsWriter.remove(COOKIE_NAME_STORE);
        prefsWriter.commit();

        return store.removeAll();
    }

    /**
     * Serializes Cookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    protected String encodeCookie(SerializableUriCookiePair cookie) {
        if (cookie == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in encodeCookie", e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns cookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    protected Pair<URI, HttpCookie> decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Pair<URI, HttpCookie> pairUriCookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            pairUriCookie = ((SerializableUriCookiePair) objectInputStream.readObject()).toCookie();
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e);
        }

        return pairUriCookie;
    }

    /**
     * Using some super basic byte array <-> hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte array
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Using for
     * @return comma separeted string with cookies names
     */
    private String getInlineCookiesNames(){

        List<HttpCookie> cookies = store.getCookies();

        StringBuilder sCookiesNames = new StringBuilder();

        boolean firstTime = true;
        for (HttpCookie token: cookies) {
            if (firstTime) {
                firstTime = false;
            } else {
                sCookiesNames.append(",");
            }
            sCookiesNames.append(token.getName() + token.getDomain());
        }

        return sCookiesNames.toString();
    }


}

