package com.deliverytips.table.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.deliverytips.MainActivity;
import com.deliverytips.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data object representing a car.
 *
 * @author ISchwarz
 */
public class DeliveryEvent implements Chargable {

    public long ticket_id;
    //private final DeliveryEventsProducer producer;  // for adding a image in the column

    public static String TABLE_NAME = "DeliveryEvents";
    static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //column names and class variables
    public static String COLUMN_NAME_ID = "id";
    public long _id;

    public static String COLUMN_NAME_ORDER_NUMBER = "order_number";
    public long _order_number;

    public static String COLUMN_NAME_DATE = "date";
    public String _date;

    public static String COLUMN_NAME_TIMESTAMP = "timestamp";
    public String _timestamp = "0000-00-00 00:00:00";

    public static String COLUMN_NAME_PHONE_NUMBER = "phone_number";
    public String _phone_number = "";

    public static String COLUMN_NAME_FULL_NAME = "full_name";
    public String _full_name = "";

    public static String COLUMN_NAME_STREET = "street";
    public String _street = "";

    public static String COLUMN_NAME_PRICE = "price";
    public Double _price = 0.0;
    public Double _tax = 0.0;

    public static String COLUMN_NAME_STATUS = "status";
    public String _status = "new";

    public static String COLUMN_NAME_SOURCE = "source";
    public String _source = "";

    public static String COLUMN_NAME_SERVICE_METHOD = "service_method";
    public String _service = "";

    public static String COLUMN_NAME_CSR = "csr";
    public String _csr = "";

    public static String COLUMN_NAME_DRIVER = "driver";
    public String _driver = "";

    public static String COLUMN_NAME_DESCRIPTION = "description";
    public String _description = "";

    public static String COLUMN_NAME_NOTES = "notes";
    public String _notes = "";

    public static String COLUMN_NAME_TYPE = "type";
    public String _type = "CASH";

    public static String COLUMN_NAME_TIP = "tip";
    public Double _tip = 0.00;

    public static String COLUMN_NAME_CUSTOMER_ID = "customer_id";

    public DeliveryEvent(){}
    public DeliveryEvent( Long ticket_id ){

        //note properly labeled
        this.ticket_id = ticket_id;

        Log.d("Init Delivery Event", "ticket_id: " + Long.toString(ticket_id));

        //create query string and ARGS
        String selectQuery = "SELECT * FROM " + DeliveryEvent.TABLE_NAME +
                " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " = ?";

        String[] whereArgs = {Long.toString(ticket_id)};

        ArrayList<HashMap<String, String>> results = makeQuery(selectQuery, whereArgs);

        if( results.size() > 0 ){

            this._order_number = this.ticket_id;
            this._phone_number = results.get(0).get(DeliveryEvent.COLUMN_NAME_PHONE_NUMBER);
            this._date = results.get(0).get(DeliveryEvent.COLUMN_NAME_DATE);
            this._street = results.get(0).get(DeliveryEvent.COLUMN_NAME_STREET);
            this._driver = results.get(0).get(DeliveryEvent.COLUMN_NAME_DRIVER);
            this._timestamp = results.get(0).get(DeliveryEvent.COLUMN_NAME_TIMESTAMP);
            this._notes = results.get(0).get(DeliveryEvent.COLUMN_NAME_NOTES);
            this._description = results.get(0).get(DeliveryEvent.COLUMN_NAME_DESCRIPTION);
            this._service = results.get(0).get(DeliveryEvent.COLUMN_NAME_SERVICE_METHOD);
            this._status = results.get(0).get(DeliveryEvent.COLUMN_NAME_STATUS);
            this._tip = Double.parseDouble(results.get(0).get(DeliveryEvent.COLUMN_NAME_TIP));
            this._price = Double.parseDouble(results.get(0).get(DeliveryEvent.COLUMN_NAME_PRICE));
            this._type = results.get(0).get(DeliveryEvent.COLUMN_NAME_TYPE);

            //calc tax
            this._tax = this._price * 0.075;
        }

    }

    public DeliveryEvent(Cursor cursor){
        if( cursor != null ){
            this._id = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ID)));
            this._order_number = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ORDER_NUMBER)));
            this._service = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_SERVICE_METHOD));
            this._price = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PRICE)));
            this._tip = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIP)));
            this._timestamp = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIMESTAMP));
            this._phone_number = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PHONE_NUMBER));
            this._street = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_STREET));

            //set tax
            //add date
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DATE) != -1) {
                this._date = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DATE));
            }

            //add status
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_STATUS) != -1) {
                this._status = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_STATUS));
            }

            //add full_name
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_FULL_NAME) != -1) {
                this._full_name = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_FULL_NAME));
            }

            //add driver
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DRIVER) != -1) {
                this._driver = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DRIVER));
            }

            //add descescription
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DESCRIPTION) != -1) {
                this._description = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DESCRIPTION));
            }

            //add notes
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_NOTES) != -1) {
                this._notes = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_NOTES));
            }

            //add types
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TYPE) != -1) {
                this._type = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TYPE));
            }
        }
    }
    public ContentValues getContentValues(){

        ContentValues c = new ContentValues();
        c.put(this.COLUMN_NAME_PRICE, this._price);
        c.put(this.COLUMN_NAME_DATE, this._date);
        c.put(this.COLUMN_NAME_TIP, this._tip);
        c.put(this.COLUMN_NAME_STATUS, this._status);
        c.put(this.COLUMN_NAME_SERVICE_METHOD, this._service);
        c.put(this.COLUMN_NAME_ORDER_NUMBER, this._order_number);
        c.put(this.COLUMN_NAME_TIMESTAMP, this._timestamp);
        c.put(this.COLUMN_NAME_FULL_NAME, this._full_name);
        c.put(this.COLUMN_NAME_CSR, this._csr);
        c.put(this.COLUMN_NAME_DESCRIPTION, this._description);
        c.put(this.COLUMN_NAME_PHONE_NUMBER, this._phone_number);
        c.put(this.COLUMN_NAME_STREET, this._street);
        c.put(this.COLUMN_NAME_NOTES, this._notes);
        c.put(this.COLUMN_NAME_TYPE, this._type);
        c.put(this.COLUMN_NAME_DRIVER, this._driver);

        return c;
    }
    public ArrayList<HashMap<String, String>> makeQuery(String selectQuery, String[] whereArgs){
        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MainActivity.get());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, whereArgs);
        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i=0; i<cursor.getColumnCount();i++)
                {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }

                maplist.add(map);
            } while (cursor.moveToNext());
        }

        //close
        cursor.close();
        db.close();
        myDatabaseHelper.close();

        return maplist;
    }
    public String getTime(){
        if( this._timestamp != null && this._timestamp.contains(" ") ){
            String[] splited = this._timestamp.split("\\s+");

            if( splited.length >= 1 ){
                return splited[1];
            }
        }

        return "N/A";
    }
    public String getDate(){
        if( this._timestamp != null && this._timestamp.contains(" ") ){
            String[] splited = this._timestamp.split("\\s+");

            if( splited.length >= 1 ){
                return splited[0];
            }
        }

        return "N/A";
    }
    public DeliveryEvent(final long ticket_id, final String name, final String Address, final double price, final double tip) {
        this.ticket_id = ticket_id;
        this._phone_number = name;
        this._street = Address;
        this._price = price;
        this._tip = tip;
    }
    public void printToLogs(String... optionalLogs){
        Log.d("Delivery Dump", getContentValues().toString() + "\n" + optionalLogs.toString());
    }
    public DeliveryEventsProducer getProducer() {
        return   null;
    }
    public boolean save(){

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MainActivity.get());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

        String[] whereArgs = { Long.toString(ticket_id)};

        String updateQuery = "UPDATE " + TABLE_NAME +
                " SET " + COLUMN_NAME_TIP + " = ?," +
                " SET " + COLUMN_NAME_DRIVER + " = ?," +
                " SET " + COLUMN_NAME_NOTES + " = ?," +
                " SET " + COLUMN_NAME_TIMESTAMP + " = ? " +
                " WHERE " + COLUMN_NAME_ORDER_NUMBER + "= ?"
                ;

        //db.execSQL(updateQuery,whereArgs);
        db.update(TABLE_NAME,getContentValues(),COLUMN_NAME_ORDER_NUMBER + "= ?",whereArgs );

        myDatabaseHelper.close();
        db.close();

        return false;
    }

    public Long getTicketID() {
        return ticket_id;
    }

    public String getPhoneNumber() { return this._phone_number;   }

    public void setPhoneNumber(final String name) {
        this._phone_number = name;
    }

    public String getAddress(){ return this._street; }

    public void setAddress(final String address) {
        this._street = address;
    }
    public double getBasePrice() {

        return _price;
    }

    public double getPrice() {

        Double total = ( _price * 0.075 ) + _price;
        return _price;
    }
    public double getTip() { return _tip; }

    @Override
    public String toString() {
        return getContentValues().toString();
    }
}
