package com.deliverytips;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.DefaultHttpClient;

import java.util.HashMap;
import java.util.Map;

public class DashboardTable extends Fragment {

    static String store_id;
    static String username;
    static String password;

    String server_date = "10/3/2017:10/3/2017";  //might need to manually update this
    String session_id = "XXXX";
    String token = "XXXXX";
    String cookie = "XXXXXXXXXXXXXXXXXXX";
    String cookie_str;

    String url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;

    TextView text;

    RequestQueue queue;
    //CookieManager cookieManager;
    HttpStack httpStack;
    StringRequest stringRequest = null;

    SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dashboard_table, container, false);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        text = (TextView) rootView.findViewById(R.id.textViewDashboardText);
        text.setText("Dashboard\nCustomers\n");
        text.setText(text.getText() + "ID PHONE ADDRESS\n");

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        //search to see if person exists
        Cursor cursor = db.query(
                Person.TABLE_NAME,
                new String[]{
                        Person.COLUMN_NAME_ID,
                        Person.COLUMN_NAME_PHONE_NUMBER,
                        Person.COLUMN_NAME_ADDRESS
                },
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            // your calculation goes here
            Person person = new Person(cursor);
            text.setText(text.getText().toString() + person._id + " " + person._phone_number + " " + person._address + "\n");
        }

        // add deliveryEvents
        text.setText(text.getText() + "\nDelivery Events\n");
        text.setText(text.getText() + "ID ORDER_NUM PRICE TIMESTAMP \n");
        cursor = db.query(
                DeliveryEvent.TABLE_NAME,
                new String[]{
                        DeliveryEvent.COLUMN_NAME_ID,
                        DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
                        DeliveryEvent.COLUMN_NAME_PRICE,
                        DeliveryEvent.COLUMN_NAME_TIMESTAMP,
                        DeliveryEvent.COLUMN_NAME_CUSTOMER_ID
                },
                null, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // your calculation goes here
                DeliveryEvent deliveryEvent = new DeliveryEvent(cursor);
                text.setText(text.getText().toString() + deliveryEvent._id + " " + deliveryEvent._order_number + " " + deliveryEvent._price + " " + deliveryEvent._timestamp + " " + deliveryEvent._person_id + "\n");
            }
        }

        //cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        //CookieHandler.setDefault(cookieManager);
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpStack = new HttpClientStack(httpclient);
        queue = Volley.newRequestQueue(getActivity(), httpStack);

        Button login_button = (Button) rootView.findViewById(R.id.buttonLogin);

        store_id = sharedPref.getString("store_id", null);
        username = sharedPref.getString("username", null);
        password = sharedPref.getString("password", null);

        //enable the button if storeid, username, and password are not null
        if (store_id == "" || store_id == null || username == "" || username == null || password == null || password == "") {

            login_button.setText("Settings");
            // if null values send user to settings page to fill it in.
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getActivity().getFragmentManager();
                    fm.beginTransaction().replace(R.id.content_frame, new Settings()).commit();
                }
            });
        } else {

            login_button.setText("Login in to PWR");
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //httpclient.setCookieStore( cookieManager.getCookieStore() );
                    Toast.makeText(getActivity(), "Procesing PWR Login for store " + store_id, Toast.LENGTH_LONG).show();

                    final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    Log.d("Response", response);

                                    //parse data
                                    String delims = "id=\"__VIEWSTATE\"";
                                    String[] str_tokens = response.split(delims);
                                    text.setText(text.getText().toString() +  "\n\nsize: " + str_tokens.length );

                                    if( str_tokens.length > 0 ){
                                        //text.setText(text.getText().toString() + str_tokens[1] );
                                    }

                                    // Check if successfully logged in
                                    String[] login_name_label = response.split("LabelLoginName");
                                    if( login_name_label.length > 0){
                                        text.setText(text.getText().toString() +  "\nSuccessful login" );
                                        text.setText(text.getText().toString() + "\n\nURL: " + url + "url\n\n" + "\nCookie: " + cookie_str + "\n\n" + response);
                                    } else {
                                        text.setText(text.getText().toString() +  "\nFailed to login");
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Error.Response", error.toString());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("txtUsername", username);
                            params.put("txtPassword", password);
                            params.put("btnLogin", "Sign+In");
                            params.put(username + "_RptParams", "+RealTime|RealTime_OrderDetail|sr_" + store_id + "|0|" + server_date + "|Store " + store_id + "|");
                            params.put("__AntiXsrfToken", token);
                            params.put("ReturnUrl", "%2fPWR%2fRealTimeOrderDetail.aspx%3fFilterCode%3dsr_" + store_id);
                            params.put("FilterCode", "sr_" + store_id);
                            params.put("PwrMainGridRowSelect", "1");
                            params.put("ASP.NET_SessionId", session_id);
                            params.put("PwrAuthCookie", cookie);
                            params.put(username + "_CurrentCulture", "en-US");
                            params.put("TimeZoneOffset", "7");

                            //params.put("fileDownloadToken","?");
                            //params.put("__LASTFOCUS","");
                            //params.put("__VIEWSTATE","");
                            //params.put("__VIEWSTATEGENERATOR","");
                            //params.put("__EVENTTARGET","");
                            //params.put("__EVENTARGUMENT","");
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();

                            params.put("Host", "pwr.dominos.com");
                            params.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:55.0) Gecko/20100101 Firefox/55.0");
                            //params.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                            params.put("Accept", "*/*");
                            params.put("Accept-Language", "en-US,en;q=0.8");
                            params.put("Accept-Encoding", "gzip, deflate, br");
                            params.put("Pragma", "no-cache");
                            params.put("Upgrade-Insecure-Requests", "1");
                            params.put("Cache-Control", "no-cache");
                            params.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

                            // params.put("Content-Length","20");
                            params.put("Connection", "keep-alive");

                            return params;
                        }
                    };

                    queue.add(postRequest);
                }
            });
        }

        return rootView;
    }

}
