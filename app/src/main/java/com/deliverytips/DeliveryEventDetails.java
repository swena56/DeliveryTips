package com.deliverytips;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.deliverytips.R.string.default_city;

public class DeliveryEventDetails extends AppCompatActivity {

    public Cursor cursor;
    public EditText editTextTip;
    public EditText editTextNotes;
    public SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initalize
        TextView ticket_id = (TextView) findViewById(R.id.textViewTicketID);
        TextView price = (TextView) findViewById(R.id.textViewPrice);
        editTextTip = (EditText) findViewById(R.id.editTextTip);
        Button call_button = (Button) findViewById(R.id.buttonCall);
        Button nav_button = (Button) findViewById(R.id.buttonMaps);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        String ticket = null;

        if( bundle.isEmpty() ){

            Toast.makeText(getApplicationContext(),"No Ticket ID for Details Activity",Toast.LENGTH_SHORT).show();
            return;

        } else {
            ticket = bundle.getString("ticket_id");
            Toast.makeText(getApplicationContext(),"Loading " + ticket,Toast.LENGTH_SHORT).show();
        }

        //set Ticket Id
        ticket_id.setText(ticket);

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        //search to see if person exists
       cursor = db.query(
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
        DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "=" + i.getExtras().getString("ticket_id"), null, null, null, null);

        if (cursor.moveToFirst()) {

            //set tip
            final String tip = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIP));
            editTextTip.setText(tip);

            final String phone = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PHONE_NUMBER));
            editTextTip.setText(tip);

            final String address = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_STREET));

            //set price
            price.setText(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PRICE)));

            //set call but action and button text
            call_button.setText( "Call: " + phone );
            call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            });

            //Navigation button
            nav_button.setText( "Navigate: " + address );
            nav_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //get ZIP or CITY from shared Preferences
                    String city = sharedPref.getString("city", String.valueOf(default_city));

                    //address nav
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String query = "";
                    try {
                        query = URLEncoder.encode(address + " " + city , "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(),"Starting Navigation to " + address + " " + city, Toast.LENGTH_SHORT).show();

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+query);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);


                }
            });
        }

        MapView mapView = (MapView) findViewById(R.id.mapView2);
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Starting Maps", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saving", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //save tips and notes
                MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
                SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
                DeliveryEvent deliveryEvent = new DeliveryEvent(cursor);
                deliveryEvent._tip = Double.parseDouble(editTextTip.getText().toString());
                deliveryEvent._notes = "";
                db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                db.close();

                Intent intent = new Intent(DeliveryEventDetails.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //close connection
        cursor.close();
    }
}
