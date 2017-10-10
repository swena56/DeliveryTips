package com.deliverytips.table.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.deliverytips.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that provides data for demonstration porpuses.
 *
 * @author ISchwarz
 */
public final class DataFactory {

    SharedPreferences sharedPref;

    /**
     * Creates a list of cars.
     *
     * @return The created list of cars.
     */
    public static List<DeliveryEvent> createDeliveryEventsList(Context context) {

        //sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        final List<DeliveryEvent> deliveryEvents = new ArrayList<>();

//        deliveryEvents.add(
//                new DeliveryEvent(Long.parseLong("11111"), "6513318021", "Mexico City", 17.25)
//        );

        //search to see if person exists
        Cursor cursor = db.query(
                com.deliverytips.DeliveryEvent.TABLE_NAME,
                new String[]{
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_ID,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_PHONE_NUMBER,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_FULL_NAME,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_STREET,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_PRICE,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_TIMESTAMP,
                        com.deliverytips.DeliveryEvent.COLUMN_NAME_TIP
                },
                null, null, null, null, null);

        //String whereClause = com.deliverytips.DeliveryEvent.COLUMN_NAME_CUSTOMER_ID + "= ? ";

        while (cursor.moveToNext()) {
            // your calculation goes here
            com.deliverytips.DeliveryEvent event = new com.deliverytips.DeliveryEvent(cursor);
            deliveryEvents.add(
                    new DeliveryEvent(event._order_number, event._phone, event._street, event._price)
            );
        }

        return deliveryEvents;
    }
}
