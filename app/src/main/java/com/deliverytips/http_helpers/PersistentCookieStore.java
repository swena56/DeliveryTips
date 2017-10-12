package com.deliverytips.http_helpers;

/**
 * Created by swena56 on 10/10/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * Class that implements CookieStore interface. This class saves to SharedPreferences the session
 * cookie.
 *
 * Created by lukas.
 */
public class PersistentCookieStore implements CookieStore {

    private CookieStore mStore;
    private Context mContext;
    private Gson mGson;
    public static final String MyPREFERENCES = "Prefs" ;

    public PersistentCookieStore(Context context) {
        // prevent context leaking by getting the application context
        mContext = context.getApplicationContext();
        mGson = new Gson();

        // get the default in memory store and if there is a cookie stored in shared preferences,
        // we added it to the cookie store


        mStore = new CookieManager().getCookieStore();
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
//        String jsonSessionCookie = pref.getJsonSessionCookie(mContext);
//        if (!jsonSessionCookie.equals(Prefs.DEFAULT_STRING)) {
//            HttpCookie cookie = mGson.fromJson(jsonSessionCookie, HttpCookie.class);
//            mStore.add(URI.create(cookie.getDomain()), cookie);
//        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        if (cookie.getName().equals("sessionid")) {
            // if the cookie that the cookie store attempt to add is a session cookie,
            // we remove the older cookie and save the new one in shared preferences
            remove(URI.create(cookie.getDomain()), cookie);
            //Prefs.saveJsonSessionCookie(mContext, mGson.toJson(cookie));
        }

        mStore.add(URI.create(cookie.getDomain()), cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return mStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return mStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return mStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return mStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        return mStore.removeAll();
    }
}
