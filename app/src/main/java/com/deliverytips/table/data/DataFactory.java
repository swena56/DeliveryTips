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

        if( selectedItem.equals("No Driver Selected.") ){

            final ArrayList<HashMap<String, String>> hashMaps = deliveryEvent.makeQuery("SELECT " +
                    "count(*) AS count, " +
                    "sum( (" + DeliveryEvent.COLUMN_NAME_PRICE + " * 0.075 )) + SUM(" + DeliveryEvent.COLUMN_NAME_PRICE + " ) AS price, " +
                    "sum(" + DeliveryEvent.COLUMN_NAME_TIP + ") AS tips, " +
                    //"printf(\"%.2f\", AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ")) AS avg_tips " +
                    "AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ") AS avg_tips " +
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
                    "AVG(" + DeliveryEvent.COLUMN_NAME_TIP + ") AS avg_tips " +
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
    public static List<DeliveryEvent> createDeliveryEventsList(Context context, String selectedItem) {

        //sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        List<DeliveryEvent> deliveryEvents = new ArrayList<>();

        ArrayList<HashMap<String, String>> hashMaps = null;
        DeliveryEvent deliveryEvent = new DeliveryEvent();
        String[] args = {selectedItem};

        if( selectedItem == null || selectedItem == "" || selectedItem == "No Driver Selected."  ){

            hashMaps = deliveryEvent.makeQuery(
                    "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " FROM " + DeliveryEvent.TABLE_NAME +
                            " ORDER BY " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+ " DESC"
                    , null
            );


        } else {

            hashMaps = deliveryEvent.makeQuery(
                    "SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " FROM " + DeliveryEvent.TABLE_NAME +
                            " WHERE " + DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + " = \"Delivery\" " +
                                    " AND (" +
                                        DeliveryEvent.COLUMN_NAME_DRIVER + " = ? "
                                        + " OR " +
                                        DeliveryEvent.COLUMN_NAME_STATUS + " != \"Complete\""
                                    + " )" +
                            //" OR " + DeliveryEvent.COLUMN_NAME_DRIVER + " = \" ()\" " +
                            "ORDER BY " +DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+ " DESC"
                    , args
            );

        }

        int size = (hashMaps != null ) ? hashMaps.size() : 0;

        for (int i = 0; i < size; i++) {
            deliveryEvents.add(
                    new DeliveryEvent(Long.parseLong(hashMaps.get(i).get(DeliveryEvent.COLUMN_NAME_ORDER_NUMBER)))
            );
        }

//            String whereClause = "";
//            String[] whereArgs = {selectedItem};
//
//            if( selectedItem == "No Driver Selected." ){
//                whereClause = DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + "= ? ";
//                whereArgs = new String[]{"Carry-Out"};
//            } else {
//                whereArgs = new String[]{selectedItem,"Delivery"};
//
//                whereClause = DeliveryEvent.COLUMN_NAME_DRIVER + "= ? " +
//                        " OR " + DeliveryEvent.COLUMN_NAME_DRIVER + "= \" ()\" " +
//                        " AND " + DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + "= ? ";
//
//            }
//
//            //search to see if person exists
//            Cursor cursor = db.query(
//                    DeliveryEvent.TABLE_NAME,
//                    new String[]{
//                            DeliveryEvent.COLUMN_NAME_ID,
//                            DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
//                            DeliveryEvent.COLUMN_NAME_PHONE_NUMBER,
//                            DeliveryEvent.COLUMN_NAME_SERVICE_METHOD,
//                            DeliveryEvent.COLUMN_NAME_STREET,
//                            DeliveryEvent.COLUMN_NAME_PRICE,
//                            DeliveryEvent.COLUMN_NAME_TIMESTAMP,
//                            DeliveryEvent.COLUMN_NAME_TIP
//                    },
//                    whereClause, whereArgs, null, null,DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " DESC"
//                    );
//
//            //
//
//            while (cursor.moveToNext()) {
//                // your calculation goes here
//                DeliveryEvent event = new DeliveryEvent(cursor);
//                Double price = Double.valueOf(String.valueOf(event._price));
//                Double tax = price * 0.075;
//                Double total = price + tax;
//
//                    deliveryEvents.add(
//                            //new DeliveryEvent(event._order_number, event._phone, event._street, total, event._tip)
//                            new DeliveryEvent(event._order_number)
//                    );
//            }
//        } else {
//            Cursor cursor = db.query(
//                    DeliveryEvent.TABLE_NAME,
//                    new String[]{
//                            DeliveryEvent.COLUMN_NAME_ID,
//                            DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
//                            DeliveryEvent.COLUMN_NAME_SERVICE_METHOD,
//                            DeliveryEvent.COLUMN_NAME_PHONE_NUMBER,
//                            DeliveryEvent.COLUMN_NAME_STREET,
//                            DeliveryEvent.COLUMN_NAME_PRICE,
//                            DeliveryEvent.COLUMN_NAME_TIMESTAMP,
//                            DeliveryEvent.COLUMN_NAME_TIP
//                    },
//                    null, null, null, null, null);
//
//            //
//
//            while (cursor.moveToNext()) {
//                // your calculation goes here
//                DeliveryEvent event = new DeliveryEvent(cursor);
//
//                DateFormat f = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
//                Date d = null;
//
////                d = f.parse(event._timestamp);
////
////                DateFormat date = new SimpleDateFormat("MM/dd/yyyy");
////                DateFormat time = new SimpleDateFormat("hh:mm:ss a");
////                System.out.println("Date: " + date.format(d));
////                System.out.println("Time: " + time.format(d));
//
//                if( event._service == "Delivery" ) {
//                    deliveryEvents.add(
//                            //new DeliveryEvent(event._order_number, time.format(d).toString(), event._street, event._price)
//                            new DeliveryEvent(event._order_number, event._phone_number, event._street, event._price, event._tip  )
//                    );
//                }
//            }
//        }


        return deliveryEvents;
    }
}
