package com.deliverytips.table.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.deliverytips.MyDatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        Map<String,String> map=new HashMap<String,String>();

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        //if a driver is selected, base the statistics on that driver
        String whereClause = com.deliverytips.DeliveryEvent.COLUMN_NAME_DRIVER + "= ? ";
        String[] whereArgs ={selectedItem};

        Cursor cursor = db.rawQuery("SELECT count(*) AS count, sum("+ com.deliverytips.DeliveryEvent.COLUMN_NAME_PRICE+"), sum("+ com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP+"), printf(\"%.2f\", AVG("+ com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP+")) FROM "+ com.deliverytips.DeliveryEvent.TABLE_NAME
                +" WHERE "+ com.deliverytips.DeliveryEvent.COLUMN_NAME_DRIVER+" = ?",whereArgs);

        cursor.moveToFirst();
        map.put("size",cursor.getString(0));
        map.put("total_price",cursor.getString(1));
        map.put("total_tip",cursor.getString(2));
        map.put("avg_tip",cursor.getString(3));

        Log.d("dealerStats",map.toString());
//         cursor = db.query(
//                com.deliverytips.DeliveryEvent.TABLE_NAME,
//                new String[]{
//                        com.deliverytips.DeliveryEvent.COLUMN_NAME_ID,
//                        com.deliverytips.DeliveryEvent.COLUMN_NAME_PRICE,
//                        com.deliverytips.DeliveryEvent.COLUMN_NAME_TIMESTAMP,
//                        com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP
//                },
//                whereClause, whereArgs, null, null, null);
//
//        cursor.moveToFirst();


        cursor.close();
        db.close();

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

        final List<DeliveryEvent> deliveryEvents = new ArrayList<>();

        if( selectedItem != null && selectedItem != "" ){

            String whereClause = "";
            //com.deliverytips.DeliveryEvent.COLUMN_NAME_DRIVER + "= ? " +
            //" AND " + com.deliverytips.DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + "= ? ";;

            String[] whereArgs = {selectedItem};

            if( selectedItem == "No Driver Selected." ){
                whereClause = com.deliverytips.DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + "= ? ";
                whereArgs = new String[]{"Carry-Out"};
            } else {
                whereArgs = new String[]{selectedItem,"Delivery"};


                whereClause = com.deliverytips.DeliveryEvent.COLUMN_NAME_DRIVER + "= ? " +
                        " OR " + com.deliverytips.DeliveryEvent.COLUMN_NAME_DRIVER + "= \" ()\" " +
                        " AND " + com.deliverytips.DeliveryEvent.COLUMN_NAME_SERVICE_METHOD + "= ? ";

            }

            //search to see if person exists
            Cursor cursor = db.query(
                    com.deliverytips.DeliveryEvent.TABLE_NAME,
                    new String[]{
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_ID,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_PHONE_NUMBER,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_SERVICE_METHOD,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_STREET,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_PRICE,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_TIMESTAMP,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP
                    },
                    whereClause, whereArgs, null, null,com.deliverytips.DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " DESC"
                    );

            //

            while (cursor.moveToNext()) {
                // your calculation goes here
                com.deliverytips.DeliveryEvent event = new com.deliverytips.DeliveryEvent(cursor);
                Double price = Double.valueOf(String.valueOf(event._price));
                Double tax = price * 0.075;
                Double total = price + tax;

                    deliveryEvents.add(

                            new DeliveryEvent(event._order_number, event._phone, event._street, total, event._tip)
                    );
            }
        } else {
            Cursor cursor = db.query(
                    com.deliverytips.DeliveryEvent.TABLE_NAME,
                    new String[]{
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_ID,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_SERVICE_METHOD,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_PHONE_NUMBER,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_STREET,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_PRICE,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_TIMESTAMP,
                            com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP
                    },
                    null, null, null, null, null);

            //

            while (cursor.moveToNext()) {
                // your calculation goes here
                com.deliverytips.DeliveryEvent event = new com.deliverytips.DeliveryEvent(cursor);

                DateFormat f = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                Date d = null;

//                d = f.parse(event._timestamp);
//
//                DateFormat date = new SimpleDateFormat("MM/dd/yyyy");
//                DateFormat time = new SimpleDateFormat("hh:mm:ss a");
//                System.out.println("Date: " + date.format(d));
//                System.out.println("Time: " + time.format(d));

                if( event._service == "Delivery" ) {
                    deliveryEvents.add(
                            //new DeliveryEvent(event._order_number, time.format(d).toString(), event._street, event._price)
                            new DeliveryEvent(event._order_number, event._phone, event._street, event._price, event._tip  )
                    );
                }
            }
        }


        return deliveryEvents;
    }
}
