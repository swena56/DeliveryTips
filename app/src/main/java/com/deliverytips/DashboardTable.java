package com.deliverytips;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DashboardTable extends Fragment {

    TextView text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dashboard_table, container, false);

        text = (TextView) rootView.findViewById(R.id.textViewDashboardText);
        text.setText("Dashboard\nCustomers\n");
        text.setText(text.getText() + "ID PHONE ADDRESS\n");

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        //search to see if person exists
        Cursor cursor = db.query(
                Person.TABLE_NAME,
                new String[] {
                        Person.COLUMN_NAME_ID,
                        Person.COLUMN_NAME_PHONE_NUMBER,
                        Person.COLUMN_NAME_ADDRESS
                },
                null, null, null, null, null);

        while(cursor.moveToNext())
        {
            // your calculation goes here
            Person person = new Person(cursor);
            text.setText( text.getText().toString() + person._id + " " + person._phone_number + " " + person._address+ "\n" );
        }

        // add deliveryEvents
        text.setText(text.getText() + "\nDelivery Events\n");
        text.setText(text.getText() + "ID ORDERNUM PRICE TIMESTAMP \n");
        cursor = db.query(
                DeliveryEvent.TABLE_NAME,
                new String[] {
                        DeliveryEvent.COLUMN_NAME_ID,
                        DeliveryEvent.COLUMN_NAME_ORDER_NUMBER,
                        DeliveryEvent.COLUMN_NAME_PRICE,
                        DeliveryEvent.COLUMN_NAME_TIMESTAMP,
                        DeliveryEvent.COLUMN_NAME_CUSTOMER_ID
                },
                null, null, null, null, null);

        if( cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // your calculation goes here
                DeliveryEvent deliveryEvent = new DeliveryEvent(cursor);
                text.setText(text.getText().toString() + deliveryEvent._id + " " + deliveryEvent._order_number + " " + deliveryEvent._price + " " + deliveryEvent._timestamp + " " + deliveryEvent._person_id +  "\n");
            }
        }

        return rootView;
    }


}
