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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deliverytips.table.data.DeliveryEvent;
import com.google.android.gms.maps.MapView;

public class DeliveryEventDetails extends AppCompatActivity {

    public Cursor cursor;
    public String ticket;
    public TextView ticket_id;
    public EditText editTextTip;
    public EditText editTextTipTotal;
    public EditText editTextNotes;
    public TextView textViewDescription;
    public TextView textViewTimestamp;
    public TextView textViewAddress;
    public TextView price;

    public SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initalize
        ticket_id = (TextView) findViewById(R.id.textViewTicketID);
        price = (TextView) findViewById(R.id.textViewPrice);
        textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewTimestamp = (TextView) findViewById(R.id.textViewTimestamp);
        editTextTip = (EditText) findViewById(R.id.editTextTip);
        editTextTipTotal = (EditText) findViewById(R.id.editTextTotal);
        editTextNotes = (EditText) findViewById(R.id.editTextNotes);

        Button call_button = (Button) findViewById(R.id.buttonCall);
        Button nav_button = (Button) findViewById(R.id.buttonMaps);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        ticket = bundle.getString("ticket_id");

        if( bundle.isEmpty() ){

            Toast.makeText(getApplicationContext(),"No Ticket ID for Details Activity",Toast.LENGTH_SHORT).show();
            return;

        } else {
           // ticket = bundle.getString("ticket_id");
            //Toast.makeText(getApplicationContext(),"Loading " + ticket,Toast.LENGTH_SHORT).show();
        }

        //set Ticket Id
        ticket_id.setText( ticket );

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        //search to see if person exists
        final DeliveryEvent deliveryEvent = new DeliveryEvent(Long.parseLong(ticket));
        ticket_id.setText( ticket + " ("+deliveryEvent._service+")");
        textViewDescription.setText(deliveryEvent._description);
        editTextNotes.setText(deliveryEvent._notes);
        editTextTip.setText(deliveryEvent._tip.toString());
        textViewAddress.setText(deliveryEvent._street);
        textViewTimestamp.setText(deliveryEvent._timestamp);

        Double tot = (deliveryEvent._price + deliveryEvent._tax);
        tot = Math.round(tot * 100.0) / 100.0;
        price.setText( tot.toString() );

        price.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                Log.d("BEFORE", s.toString() + " " + start + " " + after + " " + count );
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                Log.d("total price auto calc", s.toString() + " " + start + " " + before + " " + count );
                //textViewResult.setText(addNumbers());
            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                Log.d("A total price auto calc", s.toString() + s.toString() );
            }
        });
        editTextTip.setText(deliveryEvent._tip.toString());

        //set total
        Double total = tot + deliveryEvent._tip;
        editTextTipTotal.setText(total.toString());

        //clear the total tip field when clicked
        editTextTipTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( editTextTipTotal.getText() != null ){
                    String total_plus_tip = editTextTipTotal.getText().toString();
                    Log.d("totaltip",editTextTipTotal.getText().toString());
                    String actual_price = price.getText().toString();
                    if( actual_price != null && total_plus_tip != null ){
                        Double a_price = Double.parseDouble(actual_price);
                        Double total = Double.parseDouble(total_plus_tip);
                        Double calc_tip = total - a_price;

                        Double rounded_tip = Math.round(calc_tip * 100.0) / 100.0;
                        //set tip
                        editTextTip.setText(rounded_tip.toString());
                    }
                }


                //editTextTipTotal.getText().clear();
            }
        });

            //set call but action and button text
            String phone = deliveryEvent._phone_number;
            String phone_str = (phone != null && phone.length() >= 9) ? phone.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3") : "N/A";
            call_button.setText("PHONE: " + phone_str);
            if (phone != null) {

                call_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_DIAL);

                        Log.d("Calling", deliveryEvent._phone_number);
                        intent.setData(Uri.parse("tel:" + deliveryEvent._phone_number));
                        startActivity(intent);
                    }
                });

            } else {

                call_button.setText("PHONE: N/A");
                //call_button.setVisibility(View.INVISIBLE);
                call_button.setEnabled(false);
            }

            //Navigation button, hide it when dealing with no address
            String address = deliveryEvent._street;
            String complete_address = address + " New Ulm, MN"; //+ sharedPref.getString("address", String.valueOf(R.string.default_city).toString());

            nav_button.setText("Navigate: " + complete_address);
            if (address != null) {
                nav_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //get ZIP or CITY from shared Preferences
                        //String city_state = sharedPref.getString("address", String.valueOf(R.string.default_city).toString());
                        String complete_address = getIntent().getExtras().getString("address");// + "," + city_state;
                        //String complete_address = getIntent().getExtras().getString("address");
                        openMap(getApplicationContext(), complete_address);
                        Toast.makeText(getApplicationContext(), "Starting Navigation to " + complete_address, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {

                nav_button.setText("Navigate: N/A");
                nav_button.setEnabled(false);
            }

            MapView mapView = (MapView) findViewById(R.id.mapView2);
            mapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Starting Maps", Toast.LENGTH_SHORT).show();
                }
            });

            //Delcare Save Button
            Button saveButton = (Button) findViewById(R.id.buttonSaveEvent);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //save tips and notes
                    DeliveryEvent deliveryEvent = new DeliveryEvent(Long.parseLong(ticket));
                    deliveryEvent._tip = Double.parseDouble(editTextTip.getText().toString());
                    deliveryEvent._notes = editTextNotes.getText().toString();
                    deliveryEvent.printToLogs();
                    deliveryEvent.save();

                    //Toast.makeText(getApplicationContext(), "Delivery Event ( " + deliveryEvent._order_number + " ) Updated", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DeliveryEventDetails.this, MainActivity.class);
                    startActivity(intent);
                }
            });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saving", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //save tips and notes
                DeliveryEvent deliveryEvent = new DeliveryEvent(Long.parseLong(ticket));
                deliveryEvent._tip = Double.parseDouble(editTextTip.getText().toString());
                deliveryEvent._notes = editTextNotes.getText().toString();
                deliveryEvent.printToLogs("SAVING");
                deliveryEvent.save();

                //Toast.makeText(getApplicationContext(),"Delivery Event ( "+deliveryEvent._order_number+" ) Updated", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),"Delivery Event ( "+deliveryEvent._order_number+" ) Updated", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(DeliveryEventDetails.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public static boolean openMap(Context context, String address) {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("geo")
                .path("0,0")
                .appendQueryParameter("q", address);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //close connection
        if( cursor != null ){
            cursor.close();
        }


    }
}
