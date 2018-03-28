package com.deliverytips.table.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.deliverytips.MainActivity;
import com.deliverytips.MyDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A factory that provides data for demonstration porpuses.
 *
 * @author ISchwarz
 */
public final class DataFactory {

    SharedPreferences sharedPref;

    public static Map<String,String> GetDriverStats(Context context, String selectedItem) {

        Log.d("Driver Stats","SelectedItem: " + selectedItem);

        String[] whereArgs = {selectedItem};

        DeliveryEvent deliveryEvent = new DeliveryEvent();
        Map<String,String> map=new HashMap<String,String>();
        map.put("size", "0");
        map.put("total_price", "0");
        map.put("total_tip", "0");
        map.put("avg_tip", "0");

        if( selectedItem.equals("No Driver Selected.") ){

            final ArrayList<HashMap<String, String>> hashMaps = deliveryEvent.makeQuery("SELECT " +
                    "count(*) AS count, " +
                    "sum( (" + DeliveryEvent.COLUMN_NAME_PRICE + " * 0.075 )) + SUM(" + DeliveryEvent.COLUMN_NAME_PRICE + " ) AS price, " +
                    "sum(" + DeliveryEvent.COLUMN_NAME_TIP + ") AS tips, " +
                    //"printf(\"%.2f\", AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ")) AS avg_tips " +
                    "printf(\"%.2f\", AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ")) AS avg_tips " +
                    "FROM " + DeliveryEvent.TABLE_NAME
                    //" WHERE " + DeliveryEvent.COLUMN_NAME_DATE + " = date('now', 'start of day','-2 days')"
                    //" WHERE " + DeliveryEvent.
                    , null);

            if( hashMaps.size() > 0 ){
                map.put("size", hashMaps.get(0).get("count"));
                map.put("total_price", hashMaps.get(0).get("price"));
                map.put("total_tip", hashMaps.get(0).get("tips"));
                map.put("avg_tip", hashMaps.get(0).get("avg_tips"));
            }

        } else {


            final ArrayList<HashMap<String, String>> hashMaps = deliveryEvent.makeQuery("SELECT " +
                    "count(*) AS count, " +
                    "printf(\"%.2f\", sum( (" + DeliveryEvent.COLUMN_NAME_PRICE + " * 0.075 )) + SUM(" + DeliveryEvent.COLUMN_NAME_PRICE + " ),'N2') AS price, " +
                    "sum(" + DeliveryEvent.COLUMN_NAME_TIP + ") AS tips, " +
                    //"printf(\"%.2f\", AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ")) AS avg_tips " +
                    "printf(\"%.2f\", AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ")) AS avg_tips " +
                    "FROM " + DeliveryEvent.TABLE_NAME
                    + " WHERE " + DeliveryEvent.COLUMN_NAME_DRIVER + " = ? " +
                    " OR " + DeliveryEvent.COLUMN_NAME_STATUS + " = 'Out the Door'", whereArgs);

            if (hashMaps.size() > 0) {
                map.put("size", hashMaps.get(0).get("count"));
                map.put("total_price", hashMaps.get(0).get("price"));
                map.put("total_tip", hashMaps.get(0).get("tips"));
                map.put("avg_tip", hashMaps.get(0).get("avg_tips"));
            }

        }
        return map;
    }

    /**
     * Creates a list of cars.
     *
     * @return The created list of cars.
     */
    public static List<DeliveryEvent> createDeliveryEventsList(Context context, String selectedItem, String search, Boolean isFiltered) {

        //sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        List<DeliveryEvent> deliveryEvents = new ArrayList<>();

        ArrayList<HashMap<String, String>> hashMaps = null;
        DeliveryEvent deliveryEvent = new DeliveryEvent();
        String[] args = {selectedItem};


        //TODO create a generated sql for search.
        if( search != null ){

            hashMaps = deliveryEvent.makeQuery(
                    "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +DeliveryEvent.COLUMN_NAME_STATUS + " FROM " + DeliveryEvent.TABLE_NAME +
                            " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " LIKE '"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " LIKE '%"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " LIKE '"+search+"%' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " LIKE '%"+search+"%' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_STREET + " LIKE '"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_STREET + " LIKE '"+search+"%' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_STREET + " LIKE '%"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_STREET + " LIKE '%"+search+"%' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_PHONE_NUMBER + " LIKE '"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_PHONE_NUMBER + " LIKE '"+search+"%' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_PHONE_NUMBER + " LIKE '%"+search+"' " +
                            " OR " + DeliveryEvent.COLUMN_NAME_PHONE_NUMBER + " LIKE '%"+search+"%' " +
                            "ORDER BY " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+ " DESC"
                    ,null
            );

        } else {
            if( selectedItem == null || selectedItem == "" || selectedItem == "No Driver Selected."  ){

                hashMaps = deliveryEvent.makeQuery(
                        "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +DeliveryEvent.COLUMN_NAME_STATUS + " FROM " + DeliveryEvent.TABLE_NAME +
                                " ORDER BY CASE WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Routing Station' THEN '1' " +
                                " WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Out the Door' THEN '2' " +
                                " WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Being Taken' THEN '3' " +
                                " ELSE '4' "+
                                " END, " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " DESC"

                        , null
                );
            } else {

                hashMaps = deliveryEvent.makeQuery(
                        "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +DeliveryEvent.COLUMN_NAME_STATUS + " FROM " + DeliveryEvent.TABLE_NAME +
                                " WHERE " + DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + " = \"Delivery\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Abandoned\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Bad\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Void\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Being Taken\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Gift Card Purchase\" " +
                                //" AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Future\" " +
                                //" AND " + DeliveryEvent.COLUMN_NAME_STATUS + " == \"Routing Station\""
                                " AND (" +
                                DeliveryEvent.COLUMN_NAME_DRIVER + " = ? OR " + DeliveryEvent.COLUMN_NAME_DRIVER + " = \" ()\" "
                                + " )" +
                                //" OR " + DeliveryEvent.COLUMN_NAME_DRIVER + " = ? " +
                                "ORDER BY CASE WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Routing Station' THEN '1' " +
                                " WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Out the Door' THEN '2' " +
                                " WHEN "+DeliveryEvent.COLUMN_NAME_STATUS+" = 'Being Taken' THEN '3' " +
                                " ELSE '4' "+
                                    " END, " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " DESC"
                        , args
                );
            }
        }

        int size = (hashMaps != null ) ? hashMaps.size() : 0;

        Log.d("Size", "" + size);

        for (int i = 0; i < size; i++) {

            Boolean include = true;
            if( isFiltered &&
                    (
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Complete") ||
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Abandoned") ||
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Bad")) ||
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Canceled") ||
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Void") ||
                            hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Gift Card Purchase")
                    ){
                include = false;
            }

            if( include ) {
                deliveryEvents.add(
                        new DeliveryEvent(Long.parseLong(hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_ORDER_NUMBER)))
                );
            }
        }

        return deliveryEvents;
    }

    public static String getDeliveryDataFromServer(String store_id, final String username, final String password){

        String url = "https://pwr-deliveries.ddns.net/delivery/"+store_id;

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(MainActivity.get().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        // return data
        List<DeliveryEvent> deliveryEvents = new ArrayList<>();
        ArrayList<HashMap<String, String>> hashMaps = null;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        //Log.d("http",response.toString());

                        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MainActivity.get());
                        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

                        //load json
                        String in = response.toString();
                            try {
                                JSONObject reader = new JSONObject(in);
                                JSONArray contacts = reader.getJSONArray("results");

                                //reader.get
                                int le = contacts.length();
                                Log.d("http",le + "");

                                for (int i = 0; i < contacts.length(); i++) {
                                    JSONObject c = contacts.getJSONObject(i);
                                    Log.d("http", c.toString());
                                    DeliveryEvent deliveryEvent = new DeliveryEvent();
                                    deliveryEvent.setOrderNumber(c.getString("order_id"));
                                    deliveryEvent.setPrice(c.getString("price"));
                                    deliveryEvent._status = c.getString("status");
                                    deliveryEvent._service = c.getString("service");
                                    deliveryEvent._timestamp = c.getString("timestamp");
                                    deliveryEvent._driver = c.getString("driver");
                                    deliveryEvent._phone_number = c.getString("phone");
                                    deliveryEvent._csr = c.getString("source");
                                    deliveryEvent._description = c.getString("description");
                                    deliveryEvent._street = c.getString("address");

                                    String[] whereArgs = {c.getString("order_id")};
                                    Cursor cursor = db.rawQuery("SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +
                                            DeliveryEvent.COLUMN_NAME_TIP + "," +
                                            DeliveryEvent.COLUMN_NAME_NOTES +
                                            " FROM " + DeliveryEvent.TABLE_NAME
                                            + " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " = ?", whereArgs);

                                    int recordCount = cursor.getCount();
                                    cursor.close();

                                    if (recordCount <= 0) {
                                        db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                                        //deliveryEvent.
//                                        deliveryEvents.add(
//                                                deliveryEvent
//                                        );
                                    } else {


                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            db.close();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.d("http",error.toString());
                    }
                }){

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Content-Type", "application/json; charset=UTF-8");
                //params.put("token", ACCESS_TOKEN);
                params.put("php-auth-user", username);
                params.put("php-auth-pw", password);
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("php-auth-user", "user");
                params.put("php-auth-pw", "password");
                return params;
            }
        };


        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);

        return "processed";
    }
}

