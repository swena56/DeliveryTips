package com.deliverytips.table.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.deliverytips.MyDatabaseHelper;

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
                    + " WHERE " + DeliveryEvent.COLUMN_NAME_DRIVER + " = ?", whereArgs);

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
                                " ORDER BY " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+ " DESC"
                        , null
                );


            } else {

                hashMaps = deliveryEvent.makeQuery(
                        "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +DeliveryEvent.COLUMN_NAME_STATUS + " FROM " + DeliveryEvent.TABLE_NAME +
                                " WHERE " + DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + " = \"Delivery\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Abandoned\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Bad\" " +
                                " AND " + DeliveryEvent.COLUMN_NAME_STATUS + " != \"Future\" " +
                                " AND (" +
                                DeliveryEvent.COLUMN_NAME_DRIVER + " = ?  OR " + DeliveryEvent.COLUMN_NAME_DRIVER + " = \" ()\" "
                                + " OR " +
                                DeliveryEvent.COLUMN_NAME_STATUS + " == \"Routing Station\""
                                + " )" +
                                //" OR " + DeliveryEvent.COLUMN_NAME_DRIVER + " = ? " +
                                "ORDER BY " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+ " DESC"
                        , args
                );
            }
        }

        int size = (hashMaps != null ) ? hashMaps.size() : 0;

        for (int i = 0; i < size; i++) {

            Boolean include = true;
            if( isFiltered && hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_STATUS).equals("Complete") ){
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
}
