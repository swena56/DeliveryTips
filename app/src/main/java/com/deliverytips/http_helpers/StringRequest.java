package com.deliverytips.http_helpers;

import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.deliverytips.MainActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by swena56 on 10/10/2017.
 */

public class StringRequest extends com.android.volley.toolbox.StringRequest {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";

    //private static MyApp _instance;
    private RequestQueue _requestQueue;
    private SharedPreferences _preferences;


    private final Map<String, String> _params;

    /**
     * @param method
     * @param url
     */
    public StringRequest(int method, String url, Map<String, String> params) {
        super(method, url,

            new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                }

            },

            new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
            }

        );

        _params = params;
    }

    public StringRequest(int method, String url, Map<String, String> params, Response.Listener<String> listener) {
        super(method, url, listener,

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }

        );

        _params = params;
    }


    @Override
    protected Map<String, String> getParams() {
        return _params;
    }

    /* (non-Javadoc)
     * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        // since we don't know which of the two underlying network vehicles
        // will Volley use, we have to handle and store session cookies manually
        MainActivity.get().checkSessionCookie(response.headers);

        return super.parseNetworkResponse(response);
    }

    /* (non-Javadoc)
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }

        MainActivity.get().addSessionCookie(headers);

        return headers;
    }
}
